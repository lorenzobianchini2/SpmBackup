import { Component, OnInit, OnChanges, SimpleChange, Input, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';
import { UserService } from '../../shared_service/user.service';
import { User } from '../../user';
import { Folder } from 'src/app/folder';
import { ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { Model } from 'src/app/model';
import { Version } from '../../version';
import { HttpClient, HttpResponse, HttpEventType } from '@angular/common/http';
import { Search } from 'src/app/search';
import { Clipboard } from 'ts-clipboard';
import { ShareCode } from 'src/app/share-code';

@Component({
  selector: 'app-user-folder',
  templateUrl: './user-folder.component.html',
  styleUrls: ['./user-folder.component.css']
})
export class UserFolderComponent implements OnInit {
  fName: String;
  mName: String;
  id: string;
  index;
  private user: User;
  private searchResult: Search;
  existingFolder: String;
  existingFolder2: String;
  existingModel: String;
  existingModel2: String;
  existingVersion: String;
  existingSearch: String;
  renameType: String = '';
  pathFolder = false;
  isLoggedIn: boolean;
  alertFolder: boolean;
  alertModel: boolean;
  alertModelName: boolean;
  alertModelName2: boolean;
  alertFolderName: boolean;
  alertVersionName: boolean;
  alertShareURLError: boolean;
  shareMessage: String;
  alertSearch: boolean;
  searched: boolean = false;
  currentPath: String;
  newPath: String;
  indice = 0;
  folderNotFoundMessage: String = "";
  modelNotFoundMessage: String = "";
  private previousFolder: String;
  private newPathMoveModel: String = '';
  private selectedModel: string;
  private selectedModelName: string;
  private selectedFolder: string;
  private splittedName: string[];
  private realFolderName: String;
  private userFolders: Folder[] = [];
  private userFolderTree: Folder[] = [];
  private userModels: Model[] = [];
  private modelList: Model[] = [];
  private modelsChecked: boolean[];
  private modelVersions: Model[] = [];
  private timer;
  private uploadCompleted = false;
  private searchedElement = "";
  private folderPath: String[];
  selectedFiles: FileList;
  currentFileUpload: File;
  progress: { percentage: number } = { percentage: 0 };
  fileName: String = 'No file selected';
  fileNameSplitted: String[];
  private getShareString: String = "";
  alertShareURL: boolean;
  private shareCode: ShareCode;
  private shareURL: string;
  private selectedModelList: string[];
  private fullmodelVersions: Model[] = [];
  private modelVersionsFiltered: Model[] = [];
  splittedNumber: String = "";
  selectedFolderIndex;
  invalidFile = false;

  constructor(private cd: ChangeDetectorRef, private route: ActivatedRoute,
    private _userService: UserService, private router: Router, private authService: AuthService) { }
  ngOnInit() {
    this.newPathMoveModel = '';
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.isLoggedIn = true;
    } else {
      this.isLoggedIn = false;
    }
    this.route.params.subscribe(params => {
      const folderName = this.route.snapshot.params['fName'];
      this.fName = folderName;
      this.splittedName = this.fName.split('&');
      this.realFolderName = this.splittedName[this.splittedName.length - 1];
      this._userService.checkFolderPath(this.user, this.fName).subscribe((rightPath) => {
        if (!rightPath) {
          this.router.navigate(['/hp']);
        }
      });
      this._userService.showInnerFolders(this.user, this.fName).subscribe((folders) => {
        this.userFolders = folders;
      });
      this._userService.showModels(this.user, this.fName).subscribe((models) => {
        this.userModels = models;
      });
      this._userService.showFolders(this.user).subscribe((folders) => {
        this.userFolderTree = folders;
      });
    });
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.user.username = this.id;
    this._userService.checkFolderPath(this.user, this.fName).subscribe((rightPath) => {
      if (!rightPath) {
        this.router.navigate(['/hp']);
      }
    });
    this._userService.showInnerFolders(this.user, this.fName).subscribe((folders) => {
      this.userFolders = folders;
    });
    this._userService.showModels(this.user, this.fName).subscribe((models) => {
      this.userModels = models;
    });
    this._userService.showFolders(this.user).subscribe((folders) => {
      this.userFolderTree = folders;
    });
  }

  openFolder(folderName: string) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.router.navigate(['/dir', this.fName.concat('&'.concat(folderName))]);
  }

  openSearchedFolder(folderName: string) {
    for (var i = 0; i < this.searchResult.folders.length; i++) {
      if (this.searchResult.folders[i].fName == folderName) {
        if (this.searchResult.folderPaths[i] != 'root') {
          folderName = this.searchResult.folderPaths[i].concat("&" + folderName);
        }
      }
    }
    folderName = folderName.replace(/\//g, '&');
    this.router.navigate(['/dir', folderName]);
    this.searched = false;
  }

  createInnerFolder(folderName) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingFolder = '';
    if (folderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertFolder = true;
      return;
    }
    this._userService.createInnerFolder(this.user, this.fName, folderName).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name is empty or already exists!';
        this.alertFolder = true;
      } else {
        this.alertFolder = false;
        window.location.reload();
      }
    });
  }

  renameFolder(currentFolderName, newFolderName) {
    this.existingFolder = '';
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    if (this.renameType == 'search') {
      for (var i = 0; i < this.searchResult.folders.length; i++) {
        if (this.searchResult.folders[i].fName == currentFolderName) {
          this.fName = this.searchResult.folderPaths[i];
          this.fName = this.fName.replace(/\//g, '&');
          this.renameType = 'child';
        }
      }
    }
    if (newFolderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertFolderName = true;
      return;
    }
    this._userService.renameFolder(this.user, currentFolderName, newFolderName, this.fName, this.renameType).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name already exists!';
        this.alertFolderName = true;
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

  getFolder(folderName, type) {
    this.selectedFolder = folderName;
    if (type == '0') {
      this.renameType = 'root';
    } else if (type == '1') {
      this.renameType = 'child';
    } else if (type == '2') {
      this.renameType = 'search';
    }
  }

  getSearchedFolder(folderName, index) {
    this.selectedFolder = folderName;
    this.renameType = 'search';
    this.selectedFolderIndex = index;
  }

  getSearchedModel(modelName, index) {
    this.selectedModel = modelName;
    this.selectedModelName = modelName;
    this.renameType = 'search';
    this.selectedFolderIndex = index;
  }

  moveModel() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel = '';
    this.currentPath = this.fName;
    this.newPathMoveModel = this.newPathMoveModel.replace(/\//g, '&');
    if (this.renameType == 'search') {
      this.currentPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    }
    this._userService.moveModel(this.user, this.newPathMoveModel, this.selectedModel, this.currentPath).subscribe((success) => {
      if (success) {
        window.location.reload();
      } else {
        this.existingModel = 'A model with the same name already exists, please rename the model or select another destination!';
        this.alertModelName = true;
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
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    }
    if (newModelName === '') {
      this.existingModel = 'The model name can not be empty!';
      this.alertModelName = true;
      return;
    }
    this._userService.renameModel(this.user, currentModelName, newModelName, this.fName).subscribe((success) => {
      if (!success) {
        this.existingModel = 'The model name already exists!';
        this.alertModelName = true;
      } else {
        this.alertModelName = false;
        window.location.reload();
      }
    });
  }

  addVersion(versionDescription) {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.addVersionService(this.user, this.selectedModel, versionDescription, this.fName).subscribe((success) => {
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
    this._userService.showVersionsService(this.user, selectedModel, this.fName).subscribe((versions) => {
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
    if (this.newPathMoveModel == '') {
      this.newPathMoveModel = this.newPathMoveModel.concat(modelName);
    } else {
      this.newPathMoveModel = this.newPathMoveModel.concat('/' + modelName);
    }
    this.pathFolder = true;
    this.newPath = this.newPathMoveModel.replace(/\//g, '&');
    this._userService.showInnerFolders(this.user, this.newPath).subscribe((folderTree) => {
      this.userFolderTree = folderTree;
    });
  }

  folderTreeBack(path) {
    this.indice = path.lastIndexOf('/');
    if (this.indice > 0) {
      path = path.slice(0, this.indice);
      this.newPathMoveModel = path;
      this.newPath = path.replace('/', '&');
      this._userService.showInnerFolders(this.user, this.newPath).subscribe((folderTree) => {
        this.userFolderTree = folderTree;
      });
    } else {
      this.newPathMoveModel = '';
      this._userService.showFolders(this.user).subscribe((folders) => {
        this.userFolderTree = folders;
      });
      this.pathFolder = false;
    }
  }

  deleteModel() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.deleteModelService(this.user, this.selectedModel, this.fName).subscribe((success) => {
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
    this._userService.deleteFolderService(this.user, this.selectedFolder, this.fName, this.renameType).subscribe((success) => {
      if (success) {
        window.location.reload();
      }
    });
  }

  clear() {
    this.newPathMoveModel = '';
    this._userService.showFolders(this.user).subscribe((folders) => {
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

  upload() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel2 = '';
    this.progress.percentage = 0;
    this.currentFileUpload = this.selectedFiles.item(0)
    this._userService.pushFileToStorage(this.currentFileUpload, this.fName, this.user).subscribe(event => {
      if (event.type === HttpEventType.UploadProgress) {
        this.progress.percentage = Math.round(100 * event.loaded / event.total);
      } else if (event instanceof HttpResponse) {
        this.timer = setTimeout(() => (this.uploadCompleted = true), 1000);
        this.timer = setTimeout(() => window.location.reload(), 3000);
      }
    })
    this.selectedFiles = undefined;
    this._userService.uploadFileService(this.user, this.fName, this.fileName).subscribe((success) => {
      this.alertModelName2 = false;
      if (!success) {
        this.existingModel2 = "A model with the same name already exists";
        this.alertModelName2 = true;
        this.timer = setTimeout(() => this.alertModelName2 = false, 3000);
        this.timer = setTimeout(() => this.currentFileUpload = null, 3000);
      }
    });
  }

  cancelSearch() {
    this.searched = false;
    this.searchedElement = "";
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
      this._userService.searchElements(this.user, this.searchedElement, select).subscribe((search) => {
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

  exportModel() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.exportModelService(this.selectedModel, this.fName).subscribe((response) => {
      window.location.href = "http://localhost:8080/api/exportModelService/" + this.selectedModel + "+" + this.fName + "+" + this.user.username;
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
    this.router.navigate(['/bpmnpr', this.fName.concat("&" + modelName)]);
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
    this._userService.createModel(this.user, this.fName, createModelName).subscribe((success) => {
      if (!success) {
        this.existingModel = 'The model name already exists!';
        this.alertModel = true;
      } else {
        this.alertModel = false;
        window.location.reload();
      }
    });
  }

  copyMessage() {
    Clipboard.copy(this.shareURL);
    this.alertShareURL = true;
    this.shareMessage = "Copied to clipboard!";
    this.timer = setTimeout(() => this.alertShareURL = false, 3000);
  }

  shareModel() {
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.getShareCode(this.selectedModel, this.fName, this.user).subscribe((shareString) => {
      this.getShareString = shareString.shareCode;
      this._userService.shareModelService(this.selectedModel, this.fName).subscribe((success) => {
        this.shareURL = "http://localhost:8080/api/downloadSharedModel/" + this.getShareString;
        this.selectedModel + "+" + this.fName + "+" + this.user.username;
      });
    });
  }

  publishModel() {
    this.alertShareURL = false;
    this.alertShareURLError = false;
    this.shareMessage = '';
    if (this.renameType == 'search') {
      this.fName = this.searchResult.modelPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
    }
    this._userService.publishModelService(this.selectedModel, this.fName, this.user).subscribe((success) => {
      if (success == 0) {
        this.alertShareURL = true;
        this.alertShareURLError = false;
        this.shareMessage = "Model published succesfully!";
      } else if (success == 2) {
        this.alertShareURLError = true;
        this.alertShareURL = false;
        this.shareMessage = 'A model with the same name already exists in the public repository!';
      } else if (success == 1) {
        this.alertShareURLError = true;
        this.alertShareURL = false;
        this.shareMessage = 'You already published this model!';
      }
    });
  }

  showCollection() {
    this._userService.showModels(this.user, this.fName).subscribe((modelList) => {
      this.modelList = modelList;
      let arr = new Array<boolean>(modelList.length);
      this.modelsChecked = arr;
      for (var i = 0; i < this.modelsChecked.length; i++) {
        this.modelsChecked[i] = false;
      }
    });
  }

  clearAlert() {
    this.alertShareURL = false;
    this.alertShareURLError = false;
    this.alertVersionName = false;
  }

  changed(modelName, i) {
    if (this.modelsChecked[i] == true) {
      this.modelsChecked[i] = false;
    } else {
      this.modelsChecked[i] = true;
    }
  }

  exportCollection() {
    var modelsToExport = new Array<String>();
    for (var i = 0; i < this.modelsChecked.length; i++) {
      if (this.modelsChecked[i] == true) {
        modelsToExport.push(this.modelList[i].mName);
      }
    }
    this._userService.exportModelCollection(this.fName, modelsToExport, this.user.username).subscribe((success) => {
      if (success) {
        this._userService.exportModelCollectionService().subscribe((success) => {
          window.location.href = "http://localhost:8080/api/exportModelCollectionService";
          this.timer = setTimeout(() => (
            this._userService.deleteCollection().subscribe((success) => {

            })), 1500);
        });
      }
    });
  }
}