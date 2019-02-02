import { Component, OnInit, HostListener, Directive, HostBinding } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../auth.service';
import { UserService } from '../../shared_service/user.service';
import { User } from '../../user';
import { Folder } from 'src/app/folder';
import { Search } from 'src/app/search';
import { Model } from 'src/app/model';
import { Clipboard } from 'ts-clipboard';

@Component({
  selector: 'app-user-home',
  templateUrl: './user-home.component.html',
  styleUrls: ['./user-home.component.css']
})
export class UserHomeComponent implements OnInit {
  fName: String = 'root';
  id: string;
  selectedFolder: String;
  selectedModel: string;
  selectedModelName: string;
  renameType: String;
  alert: boolean;
  searched: boolean = false;
  alertNameFolder: boolean;
  alertSearch: boolean;
  alertFolder: boolean;
  alertModel: boolean;
  alertModelName: boolean;
  alertFolderName: boolean;
  existingModel: String;
  private pathIndex: BigInteger;
  private user: User;
  existingFolder: String;
  existingSearch: String;
  currentPath: String;
  private newPathMoveModel: String = '';
  private userFolders: Folder[] = [];
  private userFolderTree: Folder[] = [];
  private path: String[];
  private searchedElement = "";
  private searchResult: Search;
  folderNotFoundMessage: String = "";
  modelNotFoundMessage: String = "";
  selectedFolderIndex;
  selectedModelIndex;
  newPath: String;
  pathFolder = false;
  indice = 0;
  private getShareString: String = "";
  private shareURL: string;
  alertShareURLError: boolean;
  alertShareURL: boolean;
  shareMessage: String;
  shareModelPath: String;
  private timer;
  private fullmodelVersions: Model[] = [];
  private modelVersions: Model[] = [];
  splittedNumber: String = "";
  existingVersion: String;
  alertVersionName: boolean;
  alertSearchedModelName: boolean;

  constructor(private _userService: UserService, private router: Router, private authService: AuthService) { }
  ngOnInit() {
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.user.username = this.id;
    this._userService.showFolders(this.user).subscribe((folders) => {
      this.userFolders = folders;
      this.userFolderTree = folders;
    });
  }

  createFolder(folderName) {
    this.existingFolder = '';
    this.alert = false;
    this.alertSearch = false;
    this.alertNameFolder = false;
    if (folderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alert = true;
      return;
    }
    this._userService.createFolder(this.user, folderName).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'A folder with the same name already exists!';
        this.alert = true;
      } else {
        this.alert = false;
        window.location.reload();
      }
    });
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
    this.selectedModel = modelName;
    this.selectedModelName = modelName;
    this.renameType = 'search';
    this.selectedFolderIndex = index;
  }

  openModel(modelName) {
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    var suppString: string[] = modelName.split("-");
    if (suppString.length > 1 && suppString[0] == "version") {
      modelName = this.splittedNumber.concat(modelName);
    }
    this.router.navigate(['/bpmnpr', selectedModelPath.concat("&" + modelName)]);
  }

  deleteModel() {
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.deleteModelService(this.user, this.selectedModel, selectedModelPath).subscribe((success) => {
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

  renameFolder(currentFolderName, newFolderName) {
    this.existingFolder = '';
    this.alert = false;
    this.alertSearch = false;
    this.alertNameFolder = false;
    if (this.renameType == 'search') {
      this.fName = this.searchResult.folderPaths[this.selectedFolderIndex];
      this.fName = this.fName.replace(/\//g, '&');
      this.renameType = 'child';
    }
    if (newFolderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertNameFolder = true;
      return;
    }
    this._userService.renameFolder(this.user, currentFolderName, newFolderName, this.fName, this.renameType).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name already exists!';
        this.alertNameFolder = true;
      } else {
        this.alertNameFolder = false;
        window.location.reload();
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
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    if (newModelName === '') {
      this.existingModel = 'The model name can not be empty!';
      this.alertModelName = true;
      return;
    }
    this._userService.renameModel(this.user, currentModelName, newModelName, selectedModelPath).subscribe((success) => {
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

  exportModel() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.exportModelService(this.selectedModel, this.fName).subscribe((response) => {
      window.location.href = "http://localhost:8080/api/exportModelService/" + this.selectedModel + "+" + selectedModelPath + "+" + this.user.username;
    });
  }

  moveModel() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel = '';
    this.currentPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this.newPathMoveModel = this.newPathMoveModel.replace(/\//g, '&');
    this._userService.moveModel(this.user, this.newPathMoveModel, this.selectedModel, this.currentPath).subscribe((success) => {
      if (success) {
        window.location.reload();
      } else {
        this.existingModel = 'A model with the same name already exists, please rename the model or select another destination!';
        this.alertModelName = true;
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
    this.shareModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.getShareCode(this.selectedModel, this.shareModelPath, this.user).subscribe((shareString) => {
      this.getShareString = shareString.shareCode;

      this._userService.shareModelService(this.selectedModel, this.shareModelPath).subscribe((success) => {
        this.shareURL = "http://localhost:8080/api/downloadSharedModel/" + this.getShareString;
        this.selectedModel + "+" + this.shareModelPath + "+" + this.user.username;
      });
    });
  }

  publishModel() {
    this.alertShareURL = false;
    this.alertShareURLError = false;
    this.shareMessage = '';
    this.shareModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.publishModelService(this.selectedModel, this.shareModelPath, this.user).subscribe((success) => {
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

  showVersions(selectedModel) {
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.showVersionsService(this.user, selectedModel, selectedModelPath).subscribe((versions) => {
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

  addVersion(versionDescription) {
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.addVersionService(this.user, this.selectedModel, versionDescription, selectedModelPath).subscribe((success) => {
      if (!success) {
        this.existingVersion = 'The name is empty or already exists!';
        this.alertVersionName = true;
      } else {
        this.alertModelName = false;
        window.location.reload();
      }
    });
  }

  cancelSearch() {
    this.searched = false;
    this.searchedElement = "";
  }

  keyDownFunction(event, searchBarElement, select) {
    this.existingSearch = '';
    this.alert = false;
    this.alertSearch = false;
    this.alertNameFolder = false;
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
      this._userService.searchElements(this.user, this.searchedElement,select).subscribe((search) => {
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

  openFolder(folderName: string) {
    this.router.navigate(['/dir', folderName]);
  }

  openSearchedFolder(folderName: string, index) {
    if (this.searchResult.folderPaths[index] != 'root') {
      folderName = this.searchResult.folderPaths[index].concat("&" + folderName);
    }
    folderName = folderName.replace(/\//g, '&');
    this.router.navigate(['/dir', folderName]);
    this.searched = false;
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

  clear() {
    this.newPathMoveModel = '';
    this._userService.showFolders(this.user).subscribe((folders) => {
      this.userFolderTree = folders; this._userService.showFolders(this.user).subscribe((folders) => {
        this.userFolderTree = folders;
      });
    });
    this.pathFolder = false;
  }
}