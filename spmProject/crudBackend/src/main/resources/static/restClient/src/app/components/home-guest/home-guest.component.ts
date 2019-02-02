import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../shared_service/user.service';
import { Folder } from 'src/app/folder';
import { Search } from 'src/app/search';
import { Clipboard } from 'ts-clipboard';
import { Model } from 'src/app/model';
import { User } from '../../user';

@Component({
  selector: 'app-home-guest',
  templateUrl: './home-guest.component.html',
  styleUrls: ['./home-guest.component.css']
})
export class HomeGuestComponent implements OnInit {
  today = new Date();
  dd = this.today.getDate();
  mm = this.today.getMonth();
  yyyy = this.today.getFullYear();
  dateMax = this.yyyy + '-' + this.mm + '-' + this.dd;
  alertFolder: boolean;
  alertNameFolder: boolean;
  fName: String = 'root';
  isLoggedIn: boolean;
  id: string;
  existingFolder: String;
  selectedFolder: String;
  selectedModel: string;
  selectedModelName: string;
  existingModel: String;
  renameType: String;
  private user: User;
  private publicFolders: Folder[] = [];
  private userFolderTree: Folder[] = [];
  existingSearch: String;
  alertModel: boolean;
  alertModelName: boolean;
  alertModelName2: boolean;
  alertFolderName: boolean;
  alertSearch: boolean;
  alertSearchedModelName: boolean;
  private searchedElement = "";
  searched: boolean = false;
  folderNotFoundMessage: String = "";
  modelNotFoundMessage: String = "";
  private searchResult: Search;
  selectedFolderIndex;
  currentPath: String;
  private newPathMoveModel: String = '';
  newPath: String;
  pathFolder = false;
  indice = 0;
  shareModelPath: String;
  private getShareString: String = "";
  private shareURL: string;
  alertShareURLError: boolean;
  alertShareURL: boolean;
  shareMessage: String;
  private timer;
  private fullmodelVersions: Model[] = [];
  private modelVersions: Model[] = [];
  splittedNumber: String = "";
  existingVersion: String;
  alertVersionName: boolean; authorSearch: boolean;
  nameSearch: boolean;
  searchBy: String = "Name";

  constructor(private _userService: UserService, private router: Router) { }
  ngOnInit() {
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.isLoggedIn = true;
    } else {
      this.isLoggedIn = false;
    }
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.user.username = this.id;

    this._userService.showPublicFolders().subscribe((folders) => {
      this.publicFolders = folders;
    });
    this._userService.showPublicFolders().subscribe((folders) => {
      this.userFolderTree = folders;
    });
  }

  createPublicFolder(folderName) {
    this.alertFolder = false;
    this.alertNameFolder = false;
    this.existingFolder = '';
    if (folderName === '') {
      this.existingFolder = 'The folder name can not be empty!';
      this.alertFolder = true;
      return;
    }
    this._userService.createPublicFolder(folderName, this.user.username).subscribe((success) => {
      if (!success) {
        this.existingFolder = 'The folder name already exists!';
        this.alertFolder = true;
      } else {
        this.alertFolder = false;
        window.location.reload();
      }
    });
  }

  openFolder(folderName: string) {
    this.router.navigate(['/pubf', folderName]);
  }

  getFolder(folderName, type) {
    this.selectedFolder = folderName;
    if (type === '0') {
      this.renameType = 'root';
    } else if (type === '1') {
      this.renameType = 'child';
    }
  }

  getModelVersion(modelName) {
    this.selectedModel = modelName;
    var suppString: string[] = this.selectedModel.split("-");
    if (suppString.length > 1 && suppString[0] == "version") {
      this.selectedModel = this.splittedNumber.concat(this.selectedModel);
    }

  }

  openSearchedFolder(folderName: string, index) {
    if (this.searchResult.folderPaths[index] != 'root') {
      folderName = this.searchResult.folderPaths[index].concat("&" + folderName);
    }
    folderName = folderName.replace(/\//g, '&');
    this.router.navigate(['/pubf', folderName]);
    this.searched = false;
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

  renameFolder(currentFolderName, newFolderName) {
    this.existingFolder = '';
    this.alertFolder = false;
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
    this._userService.renamePublicModel(currentModelName, newModelName, selectedModelPath).subscribe((success) => {
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

  moveModel() {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingModel = '';
    this.currentPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
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

  exportPublicModel() {
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.exportPublicModelService(this.selectedModel, selectedModelPath).subscribe((response) => {
      var prova = response;
      window.location.href = "http://localhost:8080/api/exportPublicModelService/" + this.selectedModel + "+" + selectedModelPath;
    });
  }

  openModel(modelName) {
    var suppString: string[] = modelName.split("-");
    if (suppString.length > 1 && suppString[0] == "version") {
      modelName = this.splittedNumber.concat(modelName);
    }
    var selectedModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this.router.navigate(['/bpmnpub', selectedModelPath.concat("&" + modelName)]);
  }

  shareModel() {
    this.shareModelPath = this.searchResult.modelPaths[this.selectedFolderIndex].replace(/\//g, '&');
    this._userService.getPublicShareCode(this.selectedModel, this.shareModelPath).subscribe((shareString) => {
      this.getShareString = shareString.shareCode;
      this._userService.sharePublicModelService(this.selectedModel, this.fName).subscribe((success) => {
        this.shareURL = "http://localhost:8080/api/downloadPublicSharedModel/" + this.getShareString;
        this.selectedModel + "+" + this.shareModelPath;
      });
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

  keyDownFunction(event, searchBarElement, select) {
    this.alertFolder = false;
    this.alertModel = false;
    this.alertModelName = false;
    this.alertFolderName = false;
    this.alertSearch = false;
    this.existingSearch = '';
    console.log(select)
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

  clear() {
    this.newPathMoveModel = '';
    this._userService.showPublicFolders().subscribe((folders) => {
      this.userFolderTree = folders; this._userService.showPublicFolders().subscribe((folders) => {
        this.userFolderTree = folders;
      });
    });
    this.pathFolder = false;
  }


}