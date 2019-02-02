import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';
import { UserService } from '../../shared_service/user.service';
import { User } from '../../user';
import { Version } from '../../version';
import { Folder } from 'src/app/folder';
import { ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { Model } from 'src/app/model';
import { HttpClient, HttpResponse, HttpEventType } from '@angular/common/http';
import { Search } from 'src/app/search';
import { Clipboard } from 'ts-clipboard';

@Component({
  selector: 'app-public-folder',
  templateUrl: './public-folder.component.html',
  styleUrls: ['./public-folder.component.css']
})
export class PublicFolderComponent implements OnInit {
  isLoggedIn: boolean;
  previousFolder: String;
  fName: String;
  mName: String;
  id: string;
  private user: User;
  existingFolder: String;
  existingModel: String;
  existingModel2: String;
  existingVersion: String;
  emptyPath: String;
  pathFolder = false;
  alertFolder: boolean;
  alertModel: boolean;
  alertModelName: boolean;
  alertModelName2: boolean;
  alertFolderName: boolean;
  alertVersionName: boolean;
  alertSearchedModelName: boolean;
  alertNameFolder: boolean;
  currentPath: String;
  newPath: String;
  selectedFolderIndex;
  indice = 0;
  private selectedFolder: String;
  private renameType: String = '';
  private newPathMoveModel: String = '';
  private selectedModel: string;
  private selectedModelName: string;
  private splittedName: String[];
  private realFolderName: String;
  private userFolders: Folder[] = [];
  private modelVersions: Model[] = [];
  private modelList: Model[] = [];
  private userFolderTree: Folder[] = [];
  private publicModels: Model[] = [];
  private fullmodelVersions: Model[] = [];
  private modelsChecked: boolean[];
  private searchResult: Search;
  fileName: String = 'No file selected';
  selectedFiles: FileList;
  currentFileUpload: File;
  progress: { percentage: number } = { percentage: 0 };
  private timer;
  private uploadCompleted = false;
  alertSearch: boolean;
  private searchedElement = "";
  searched: boolean = false;
  existingSearch: String;
  folderNotFoundMessage: String = "";
  modelNotFoundMessage: String = "";
  alertShareURLError: boolean;
  alertShareURL: boolean;
  shareMessage: String;
  private shareURL: string;
  shareModelPath: String;
  private getShareString: String = "";
  splittedNumber: String = "";
  invalidFile = false;
  fileNameSplitted: String[];

  constructor(private cd: ChangeDetectorRef, private route: ActivatedRoute,
    private _userService: UserService, private router: Router, private authService: AuthService) { }
  ngOnInit() {
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.isLoggedIn = true;
    } else {
      this.isLoggedIn = false;
    }
    this.id = localStorage.getItem('token');
    this.route.params.subscribe(params => {
      const folderName = this.route.snapshot.params['fName'];
      this.previousFolder = this.fName;
      this.fName = folderName;
      this.splittedName = this.fName.split('&');
      this.realFolderName = this.splittedName[this.splittedName.length - 1];
      this._userService.checkPublicFolderPath(this.fName).subscribe((rightPath) => {
        if (!rightPath) {
          this.router.navigate(['']);
        }
      });
      this._userService.showPublicInnerFolders(this.fName).subscribe((folders) => {
        this.userFolders = folders;
      });
      this._userService.showPublicModels(this.fName).subscribe((models) => {
        this.publicModels = models;
      });
      this._userService.showPublicFolders().subscribe((folders) => {
        this.userFolderTree = folders;
      });
    });
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.user.username = this.id;
    this._userService.checkPublicFolderPath(this.fName).subscribe((rightPath) => {
      if (!rightPath) {
        this.router.navigate(['']);
      }
    });
    this._userService.showPublicInnerFolders(this.fName).subscribe((folders) => {
      this.userFolders = folders;
    });
    this._userService.showPublicModels(this.fName).subscribe((models) => {
      this.publicModels = models;
    });
    this._userService.showPublicFolders().subscribe((folders) => {
      this.userFolderTree = folders;
    });
  }

  openFolder(folderName: string) {
    this.router.navigate(['/pubf', this.fName.concat('&'.concat(folderName))]);
  }

  openSearchedFolder(folderName: string, index) {
    if (this.searchResult.folderPaths[index] != 'root') {
      folderName = this.searchResult.folderPaths[index].concat("&" + folderName);
    }
    folderName = folderName.replace(/\//g, '&');
    this.router.navigate(['/pubf', folderName]);
    this.searched = false;
  }

  createPublicInnerFolder(folderName) {
    this.existingFolder = '';
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    if (folderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertFolder = true;
      return;
    }
    this._userService.createPublicInnerFolder(this.fName, folderName, this.user.username).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name already exists!';
        this.alertFolder = true;
      } else {
        this.alertFolder = false;
        window.location.reload();
      }
    });
  }

  getModel(modelName) {
    this.selectedModel = modelName;
    this.selectedModelName = modelName;
  }

  getModelVersion(modelName) {
    this.selectedModel = modelName;
    var suppString: string[] = this.selectedModel.split("-");
    if (suppString.length > 1 && suppString[0] == "version") {
      this.selectedModel = this.splittedNumber.concat(this.selectedModel);
    }
  }

  getSearchedFolder(folderName, index) {
    this.selectedFolder = folderName;
    this.renameType = 'search';
    this.selectedFolderIndex = index;
  }

  getSearchedModel(modelName, index) {
    this.selectedModelName = modelName;
    this.selectedModel = modelName;
    this.renameType = 'search';
    this.selectedFolderIndex = index;
  }

  getFolder(folderName, type) {
    this.selectedFolder = folderName;
    if (type == '0') {
      this.renameType = 'root';
    } else if (type == '1') {
      this.renameType = 'child';
    }
  }

  moveModel() {
    this.currentPath = this.fName;
    if (this.renameType == 'search') {
      this.currentPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    }
    this.newPathMoveModel = this.newPathMoveModel.replace(/\//g, '&');
    this._userService.movePublicModel(this.newPathMoveModel, this.selectedModel, this.currentPath).subscribe((success) => {
      if (success) {
        window.location.reload();
      } else {
        this.existingModel = 'A model with the same name already exists, please rename the model or select another destination!';
        this.alertModelName = true;
      }
    });
  }

  renameFolder(currentFolderName, newFolderName) {

    this.alertFolder = false;
    this.alertFolderName = false;
    if (this.renameType == 'search') {
      this.fName = this.searchResult.folderPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    if (newFolderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertFolderName = true;
      return;
    }
    this._userService.renamePublicFolder(currentFolderName, newFolderName, this.fName, this.renameType).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name already exists!';
        this.alertFolderName = true;
      } else {
        if (this.renameType === 'child') {
          window.location.reload();
        }
        this.alertFolderName = false;
      }
    });
  }

  renameModel(currentModelName, newModelName) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel = '';
    if (this.renameType == 'search') {
      this.fName = this.searchResult.folderPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    if (newModelName === '') {
      this.existingModel = 'The model name can not be empty!';
      this.alertModelName = true;
      return;
    }

    this._userService.renamePublicModel(currentModelName, newModelName, this.fName).subscribe((success) => {
      if (!success && !this.searched) {
        this.existingModel = 'The model name already exists!';
        this.alertModelName = true;
      } else if (success && !this.searched) {
        this.alertModelName = false;
        window.location.reload();
      } else if (!success && this.searched) {
        this.existingFolder = 'The folder name already exists!';
        this.alertSearchedModelName = true;
      } else if (success && this.searched) {
        this.alertNameFolder = false;
        window.location.reload();
      }
    });
  }

  addVersion(selectedModel, versionDescription) {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    this._userService.addPublicVersionService(selectedModel, versionDescription, this.fName, this.user.username).subscribe((success) => {
      if (!success) {
        this.existingVersion = 'The name is empty or already exists!';
        this.alertVersionName = true;
      } else {
        this.alertModelName = false;
        window.location.reload();
      }
    });
  }

  showVersions(selectedModel) {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.showPublicVersionsService(selectedModel, this.fName).subscribe((versions) => {
      this.fullmodelVersions = versions;
      this.modelVersions = versions;
      var suppString: string[];
      var finalName: string = "";
      for (var i = 0; i < this.modelVersions.length; i++) {
        suppString = this.modelVersions[i].mName.split("-");
        if (suppString.length > 1) {
          this.splittedNumber = suppString[0].concat("-");
          for (var j = 1; j < suppString.length; j++) {
            if (j == suppString.length - 1) {
              finalName = finalName.concat(suppString[j]);
            } else {
              finalName = finalName.concat(suppString[j] + "-");
            }
          }
          this.modelVersions[i].mName = finalName;
          finalName = "";
        }
      }
    });
  }

  folderTreeTrigger(modelName) {
    if (this.newPathMoveModel === '') {
      this.newPathMoveModel = this.newPathMoveModel.concat(modelName);
    } else {
      this.newPathMoveModel = this.newPathMoveModel.concat('&' + modelName);
    }
    this.pathFolder = true;
    this.newPath = this.newPathMoveModel.replace(/\//g, '&');
    this._userService.showPublicInnerFolders(this.newPath).subscribe((folderTree) => {
      this.userFolderTree = folderTree;
    });
  }

  folderTreeBack(path) {
    this.indice = path.lastIndexOf('/');
    if (this.indice > 0) {
      path = path.slice(0, this.indice);
      this.newPathMoveModel = path;
      this.newPath = path.replace('/', '&');
      this._userService.showPublicInnerFolders(this.newPath).subscribe((folderTree) => {
        this.userFolderTree = folderTree;
      });
    } else {
      this.newPathMoveModel = '';
      this._userService.showPublicFolders().subscribe((folders) => {
        this.userFolderTree = folders;
      });
      this.pathFolder = false;
    }
  }

  deleteModel() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    this._userService.deletePublicModelService(this.selectedModel, this.fName).subscribe((success) => {
      if (success) {
        window.location.reload();
      }
    });
  }

  deleteFolder() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.folderPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    this._userService.deletePublicFolderService(this.selectedFolder, this.fName, this.renameType).subscribe((success) => {
      if (success) {
        window.location.reload();
      }
    });
  }

  clear() {
    this.newPathMoveModel = '';
    this._userService.showPublicFolders().subscribe((folders) => {
      this.userFolderTree = folders;
    });
    this.pathFolder = false;
  }

  selectFile(event) {
    this.selectedFiles = event.target.files;
    this.fileName = event.target.files[0].name;
    this.fileNameSplitted = this.fileName.split(".");
    if (this.fileNameSplitted[this.fileNameSplitted.length - 1] != "bpmn") {
      this.invalidFile = true;
      this.existingModel2 = "The file you selected is not a BPMN model!"
      this.alertModelName2 = true;
    } else {
      this.invalidFile = false;
      this.alertModelName2 = false;
    }
  }

  createModel(createModelName) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel = '';
    if (createModelName === '') {
      this.existingModel = 'The model name can not be empty!';
      this.alertModel = true;
      return;
    }
    this._userService.createPublicModel(this.user.username, this.fName, createModelName).subscribe((success) => {
      if (!success) {
        this.existingModel = 'The model name already exists!';
        this.alertModel = true;
      } else {
        this.alertModel = false;
        window.location.reload();
      }
    });
  }

  upload() {
    this.progress.percentage = 0;
    this.currentFileUpload = this.selectedFiles.item(0);
    this._userService.pushPublicFileToStorage(this.currentFileUpload, this.fName, this.user).subscribe(event => {
      if (event.type === HttpEventType.UploadProgress) {
        this.progress.percentage = Math.round(100 * event.loaded / event.total);
      } else if (event instanceof HttpResponse) {
        this.timer = setTimeout(() => (this.uploadCompleted = true), 1000);
        this.timer = setTimeout(() => window.location.reload(), 3000);
      }
    });
    this.selectedFiles = undefined;
    this._userService.uploadPublicFileService(this.fName, this.fileName, this.user.username).subscribe((success) => {
      this.alertModelName2 = false;
      if (!success) {
        this.existingModel2 = 'A model with the same name already exists';
        this.alertModelName2 = true;
      }
    });
  }

  exportPublicModel() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.exportPublicModelService(this.selectedModel, this.fName).subscribe((response) => {
      var prova = response;
      window.location.href = "http://localhost:8080/api/exportPublicModelService/" + this.selectedModel + "+" + this.fName;
    });
  }

  openModel(modelName) {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    var suppString: string[] = modelName.split("-");
    if (suppString.length > 1 && suppString[0] == "version") {
      modelName = this.splittedNumber.concat(modelName);
    }
    this.router.navigate(['/bpmnpub', this.fName.concat("&" + modelName)]);
  }

  keyDownFunction(event, searchBarElement, select) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingSearch = '';
    if (event.keyCode == 13) {
      if (searchBarElement === '') {
        this.existingSearch = 'The folder name can not be empty!';
        this.alertSearch = true;
        return;
      }
      if (select == 3 && !searchBarElement.match("(0[1-9]|1[0-9]|2[0-9]|3[01])-(0[1-9]|1[012])-[0-9]{4}")) {
        this.existingSearch = 'The date must respect the DD-MM-YYYY pattern!';
        this.alertSearch = true;
        return;
      }
      this.searched = true;
      this.searchedElement = searchBarElement;
      this._userService.publicSearchElements(this.searchedElement, select).subscribe((search) => {
        this.searchResult = search;
        if (this.searchResult.folders.length == 0) {
          this.folderNotFoundMessage = "No folders found";
        } else {
          this.folderNotFoundMessage = "";
        }
        if (this.searchResult.models.length == 0) {
          this.modelNotFoundMessage = "No models found";
        } else {
          this.modelNotFoundMessage = "";
        }
      });
    }
  }

  shareModel() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.getPublicShareCode(this.selectedModel, this.fName).subscribe((shareString) => {
      this.getShareString = shareString.shareCode;
      this._userService.sharePublicModelService(this.selectedModel, this.fName).subscribe((success) => {
        this.shareURL = "http://localhost:8080/api/downloadPublicSharedModel/" + this.getShareString;
        this.selectedModel + "+" + this.shareModelPath;
      });
    });
  }

  showCollection() {
    this._userService.showPublicModels(this.fName).subscribe((modelList) => {
      this.modelList = modelList;
      let arr = new Array<boolean>(modelList.length);
      this.modelsChecked = arr;
      for (var i = 0; i < this.modelsChecked.length; i++) {
        this.modelsChecked[i] = false;
      }
    });
  }

  exportCollection() {
    var modelsToExport = new Array<String>();
    for (var i = 0; i < this.modelsChecked.length; i++) {
      if (this.modelsChecked[i] == true) {
        modelsToExport.push(this.modelList[i].mName);
      }
    }
    this._userService.exportPublicModelCollection(this.fName, modelsToExport).subscribe((success) => {
      if (success) {
        this._userService.exportPublicModelCollectionService().subscribe((success) => {
          window.location.href = "http://localhost:8080/api/exportPublicModelCollectionService";
          this.timer = setTimeout(() => (
            this._userService.deletePublicCollection().subscribe((success) => {

            })), 3000);
        });
      }
    });
  }

  changed(modelName, i) {
    if (this.modelsChecked[i] == true) {
      this.modelsChecked[i] = false;
    } else {
      this.modelsChecked[i] = true;
    }
  }

  cancelSearch() {
    this.searched = false;
    this.searchedElement = "";
  }

  copyMessage() {
    Clipboard.copy(this.shareURL);
    this.alertShareURL = true;
    this.shareMessage = "Copied to clipboard!";
    this.timer = setTimeout(() => this.alertShareURL = false, 3000);
  }
}