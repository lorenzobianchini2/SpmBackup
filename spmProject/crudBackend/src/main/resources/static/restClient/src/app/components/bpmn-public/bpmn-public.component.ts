import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Modeler, OriginalPropertiesProvider, PropertiesPanelModule, InjectionNames, OriginalPaletteProvider } from './bpmn-js/bpmn-js';
import { CustomPropsProvider } from './props-provider/CustomPropsProvider';
import { CustomPaletteProvider } from './props-provider/CustomPaletteProvider';
import { UserService } from '../../shared_service/user.service';
import { ActivatedRoute } from '@angular/router';
import { User } from 'src/app/user';

const customModdle = {
  name: 'customModdle',
  uri: 'http://example.com/custom-moddle',
  prefix: 'custom',
  xml: {
    tagAlias: 'lowerCase'
  },
  associations: [],
  types: [
    {
      'name': 'ExtUserTask',
      'extends': [
        'bpmn:UserTask'
      ],
      'properties': [
        {
          'name': 'worklist',
          'isAttr': true,
          'type': 'String'
        }
      ]
    },
  ]
};
@Component({
  selector: 'app-bpmn-public',
  templateUrl: './bpmn-public.component.html',
  styleUrls: ['./bpmn-public.component.css']
})

export class BpmnPublicComponent implements OnInit {
  modeler;
  isLoggedIn: boolean;
  private mName: String;
  private splittedName: string[];
  private splittedVersion: string[];
  private modelName: String;
  private modelPath: String = '';
  private olderVersion: boolean = false;
  private saveAlert = false;
  private saveAlertMessage: String;
  private timer;
  private checkExport = false;
  private alertExport = false;
  private checkExportMessage: String;
  id: string;
  user: User;

  constructor(private route: ActivatedRoute, private _userService: UserService, private http: HttpClient) { }
  ngOnInit() {
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.user.username = this.id;
    this.mName = this.route.snapshot.params['mName'];
    this.splittedName = this.mName.split('&');
    this.splittedVersion = this.mName.split('-');
    for (var i = 0; i < this.splittedVersion.length - 1; i++) {
      if (this.splittedVersion[i] == "version") {
        this.olderVersion = true;
      }
    }
    for (var i = 0; i < this.splittedName.length - 1; i++) {
      if (i < this.splittedName.length - 2) {
        this.modelPath = this.modelPath.concat(this.splittedName[i] + "&");
      } else {
        this.modelPath = this.modelPath.concat(this.splittedName[i]);
      }
    }
    this.modelName = this.splittedName[this.splittedName.length - 1];
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.isLoggedIn = true;
    } else {
      this.isLoggedIn = false;
    }
    this.modeler = new Modeler({
      container: '#canvas',
      width: '100%',
      height: '600px',
      additionalModules: [
        PropertiesPanelModule,
        // Re-use original bpmn-properties-module, see CustomPropsProvider
        { [InjectionNames.bpmnPropertiesProvider]: ['type', OriginalPropertiesProvider.propertiesProvider[1]] },
        { [InjectionNames.propertiesProvider]: ['type', CustomPropsProvider] },
        // Re-use original palette, see CustomPaletteProvider
        { [InjectionNames.originalPaletteProvider]: ['type', OriginalPaletteProvider] },
        { [InjectionNames.paletteProvider]: ['type', CustomPaletteProvider] },
      ],
      propertiesPanel: {
        parent: '#properties'
      },
      moddleExtension: {
        custom: customModdle
      }
    });
    this._userService.openPublicModel(this.modelName, this.modelPath).subscribe((req) => {
      const url = 'http://localhost:8080/api/openPublicModel/' + this.modelName + "+" + this.modelPath;
      this.http.get(url, {
        headers: { observe: 'response' }, responseType: 'text'
      }).subscribe(
        (x: any) => {
          console.log('Fetched XML, now importing: ', x);
          this.modeler.importXML(x, this.handleError);
        },
        this.handleError
      );
    });
  }

  handleError(err: any) {
    if (err) {
      console.warn('Ups, error: ', err);
    }
  }

  exportPublicModel() {
    if (this.isLoggedIn && !this.checkExport && this.modelPath!="Shared Files") {
      this.alertExport = true;
      this.checkExport = true;
      this.saveAlert = false;
      this.checkExportMessage = "Make sure you have saved the changes to the model before exporting it!"
      return;
    }
    this._userService.exportPublicModelService(this.modelName, this.modelPath).subscribe((response) => {
      var prova = response;
      window.location.href = "http://localhost:8080/api/exportPublicModelService/" + this.modelName + "+" + this.modelPath;
      this.alertExport = false;
      this.checkExport = false;
      this.saveAlert = false;
    });
  }

  saveCurrent() {
    this.modeler.saveXML((err: any, xml: String) => this._userService.savePublicModel(this.modelPath, this.modelName, xml).subscribe((response) => { }));
    this.saveAlert = true;
    this.saveAlertMessage = "Model succesfully saved in a new version!"
    this.timer = setTimeout(() => this.saveAlert = false, 3000);
    this.alertExport = false;
  }

  saveNew(versionDescription) {
    this.modeler.saveXML((err: any, xml: String) => this._userService.savePublicModelAsNewVersion(this.user.username, this.modelPath, this.modelName, versionDescription, xml).subscribe((response) => { }));
    this.saveAlert = true;
    this.saveAlertMessage = "Model succesfully saved in a new version!"
    this.timer = setTimeout(() => this.saveAlert = false, 3000);
    this.alertExport = false;
  }
}