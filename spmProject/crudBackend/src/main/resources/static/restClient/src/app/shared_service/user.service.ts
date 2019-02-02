import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { User } from '../user';
import { StringDecoder } from 'string_decoder';
import { HttpClient, HttpRequest, HttpEvent } from '@angular/common/http';
import { Xmlfile } from '../xmlfile';

@Injectable()
export class UserService {
  private baseUrl: string = 'http://localhost:8080/api';
  private headers = new Headers({ 'Content-Type': 'application/json' });
  private options = new RequestOptions({ headers: this.headers });
  private user = new User();
  private xmlToSave = new Xmlfile;
  constructor(private http: HttpClient, private _http: Http) { }

  getUsers() {
    return this._http.get(this.baseUrl + '/users', this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  loginUser(user: User) {
    return this._http.post(this.baseUrl + '/userLogin', JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  getUser(id: Number) {
    return this._http.get(this.baseUrl + '/user/' + id, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  sendMail(user, password) {
    return this._http.post(this.baseUrl + '/mail/' + password, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deleteUser(id: Number) {
    return this._http.delete(this.baseUrl + '/user/' + id, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  createUser(user: User) {
    return this._http.post(this.baseUrl + '/user', JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  updateUser(user: User, password) {
    return this._http.post(this.baseUrl + '/userUpdate/' + password, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  createFolder(user, folderName) {
    return this._http.post(this.baseUrl + '/createFolder/' + folderName, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  createPublicInnerFolder(currentFolder, createdName, username) {
   
    return this._http.post(this.baseUrl + '/createPublicInnerFolder/' + currentFolder + "+" + createdName + "+" + username, this.options).map((response: Response) => response.json())
     .catch(this.errorHandler);
  }

  createInnerFolder(user, currentFolder, createdName) {
    return this._http.post(this.baseUrl + '/createInnerFolder/' + currentFolder + "+" + createdName, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  createPublicFolder(createdName, username) {
    
    return this._http.post(this.baseUrl + '/createPublicFolder/' + createdName + "+" + username, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showFolders(user) {
    return this._http.post(this.baseUrl + '/showFolders', JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showPublicFolders() {
    return this._http.post(this.baseUrl + '/showPublicFolders', this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showPublicInnerFolders(currentFolder) {
    return this._http.post(this.baseUrl + '/showPublicInnerFolders/' + currentFolder, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showModels(user, currentFolder) {
    return this._http.post(this.baseUrl + '/showModels/' + currentFolder, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showPublicModels(currentFolder) {
    return this._http.post(this.baseUrl + '/showPublicModels/' + currentFolder, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showInnerFolders(user, currentFolder) {
    return this._http.post(this.baseUrl + '/showInnerFolders/' + currentFolder, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showAllFolders(user) {
    return this._http.post(this.baseUrl + '/showAllFolders', JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  renameFolder(user, currentName, newName, folderPath, renameType) {
    return this._http.post(this.baseUrl + '/renameFolder/' + currentName + "+" + newName + "+" + folderPath + "+" + renameType, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  renamePublicFolder(currentName, newName, folderPath, renameType) {
    return this._http.post(this.baseUrl + '/renamePublicFolder/' + currentName + "+" + newName + "+" + folderPath + "+" + renameType, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  renamePublicModel(currentName, newName, modelPath) {
    console.log(currentName, newName, modelPath);
    return this._http.post(this.baseUrl + '/renamePublicModel/' + currentName + "+" + newName + "+" + modelPath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  renameModel(user, currentName, newName, modelPath) {
    return this._http.post(this.baseUrl + '/renameModel/' + currentName + "+" + newName + "+" + modelPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  moveModel(user, newPathMoveModel, modelName, currentPath) {
    return this._http.post(this.baseUrl + '/moveModel/' + newPathMoveModel + "+" + modelName + "+" + currentPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  movePublicModel(newPathMoveModel, modelName, currentPath) {
    return this._http.post(this.baseUrl + '/movePublicModel/' + newPathMoveModel + "+" + modelName + "+" + currentPath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  checkFolderPath(user, folderPath) {
    return this._http.post(this.baseUrl + '/checkFolderPath/' + folderPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  checkPublicFolderPath(folderPath) {
    return this._http.post(this.baseUrl + '/checkPublicFolderPath/' + folderPath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  addPublicVersionService(selectedModel, versionDescription, modelPath, username) {
    return this._http.post(this.baseUrl + '/addPublicVersionService/' + selectedModel + "+" + versionDescription + "+" + modelPath + "+" + username, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  addVersionService(user, selectedModel, versionDescription, modelPath) {
    return this._http.post(this.baseUrl + '/addVersionService/' + selectedModel + "+" + versionDescription + "+" + modelPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showPublicVersionsService(selectedModel, modelPath) {
    return this._http.post(this.baseUrl + '/showPublicVersionsService/' + selectedModel + "+" + modelPath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  showVersionsService(user, selectedModel, modelPath) {
    return this._http.post(this.baseUrl + '/showVersionsService/' + selectedModel + "+" + modelPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deleteModelService(user, selectedModel, modelPath) {
    return this._http.post(this.baseUrl + '/deleteModelService/' + selectedModel + "+" + modelPath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deletePublicModelService(selectedModel, modelPath) {
    return this._http.post(this.baseUrl + '/deletePublicModelService/' + selectedModel + "+" + modelPath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deleteFolderService(user, selectedFolder, folderPath, deleteType) {
    return this._http.post(this.baseUrl + '/deleteFolderService/' + selectedFolder + "+" + folderPath + "+" + deleteType, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deletePublicFolderService(selectedFolder, folderPath, deleteType) {
    return this._http.post(this.baseUrl + '/deletePublicFolderService/' + selectedFolder + "+" + folderPath + "+" + deleteType, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }
  errorHandler(error: Response) {
    return Observable.throw(error || "SERVER ERROR");
  }

  setter(user: User) {
    this.user = user;
  }

  getter() {
    return this.user;
  }

  uploadFileService(user, folderPath, fileName) {
    return this._http.post(this.baseUrl + '/uploadFileService/' + folderPath + "+" + fileName, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  uploadPublicFileService(folderPath, fileName, username) {
    return this._http.post(this.baseUrl + '/uploadPublicFileService/' + folderPath + "+" + fileName + "+" + username, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  searchElements(user, searchedElement, searchFilter) {
    return this._http.post(this.baseUrl + '/searchElements/' + searchedElement + "+" + searchFilter, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  publicSearchElements(searchedElement, searchFilter) {
    return this._http.post(this.baseUrl + '/publicSearchElements/' + searchedElement + "+" + searchFilter, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  pushFileToStorage(file: File, folderPath, user): Observable<HttpEvent<{}>> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    const req = new HttpRequest('POST', 'http://localhost:8080/api/postFile/' + folderPath + "+" + this.user.username, formdata, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  pushPublicFileToStorage(file: File, folderPath, user): Observable<HttpEvent<{}>> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    const req = new HttpRequest('POST', 'http://localhost:8080/api/postPublicFile/' + folderPath + "+" + this.user.username, formdata, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  exportModelService(fileName, filePath) {
    var req = new HttpRequest('GET', 'http://localhost:8080/api/exportModelService/' + fileName + filePath + this.user.username, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  exportModelCollection(folderPath, modelList, username) {
    return this._http.post(this.baseUrl + '/exportModelCollection/' + folderPath + "+" + username, JSON.stringify(modelList), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }

  exportModelCollectionService() {
    var req = new HttpRequest('GET', 'http://localhost:8080/api/exportModelService', {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  exportPublicModelCollection(folderPath, modelList) {
    return this._http.post(this.baseUrl + '/exportPublicModelCollection/' + folderPath, JSON.stringify(modelList), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }

  exportPublicModelCollectionService() {
    var req = new HttpRequest('GET', 'http://localhost:8080/api/exportPublicModelCollectionService', {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  exportPublicModelService(fileName, filePath) {
    var req = new HttpRequest('GET', 'http://localhost:8080/api/exportModelService/' + fileName + filePath, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  createModel(user, folderPath, fileName) {
    return this._http.post(this.baseUrl + '/createModel/' + folderPath + "+" + fileName, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  saveModel(username, folderPath, fileName, xml) {
    this.xmlToSave.xml = xml;
    console.log(this.xmlToSave.xml);
    return this._http.post(this.baseUrl + '/saveModel/' + username + "+" + folderPath + "+" + fileName, JSON.stringify(this.xmlToSave), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  saveModelAsNewVersion(username, folderPath, fileName, versionDescription, xml) {
    this.xmlToSave.xml = xml;
    console.log(this.xmlToSave.xml);
    return this._http.post(this.baseUrl + '/saveModelAsNewVersion/' + username + "+" + folderPath + "+" + fileName + "+" + versionDescription, JSON.stringify(this.xmlToSave), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  savePublicModelAsNewVersion(username, folderPath, fileName, versionDescription, xml) {
    this.xmlToSave.xml = xml;
    console.log(this.xmlToSave.xml);
    return this._http.post(this.baseUrl + '/savePublicModelAsNewVersion/' + username + "+" + folderPath + "+" + fileName + "+" + versionDescription, JSON.stringify(this.xmlToSave), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  savePublicModel(folderPath, fileName, xml) {
    this.xmlToSave.xml = xml;
    console.log(this.xmlToSave.xml);
    return this._http.post(this.baseUrl + '/savePublicModel/' + folderPath + "+" + fileName, JSON.stringify(this.xmlToSave), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  createPublicModel(username, folderPath, fileName) {
    return this._http.post(this.baseUrl + '/createPublicModel/' + username + "+" + folderPath + "+" + fileName, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  shareModelService(fileName, filePath) {

    var req = new HttpRequest('GET', 'http://localhost:8080/api/shareModelService/' + fileName + "+" + filePath + "+" + this.user.username, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  sharePublicModelService(fileName, filePath) {
    var req = new HttpRequest('GET', 'http://localhost:8080/api/sharePublicModelService/' + fileName + "+" + filePath, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  getShareCode(fileName, filePath, user) {

    return this._http.post(this.baseUrl + '/getShareCode/' + fileName + "+" + filePath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }
  

  getPublicShareCode(fileName, filePath) {
    return this._http.post(this.baseUrl + '/getPublicShareCode/' + fileName + "+" + filePath, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }

  downloadSharedModel(shareCode) {
    return this._http.post(this.baseUrl + '/downloadSharedModel/' + shareCode, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  downloadPublicSharedModel(shareCode) {
    return this._http.post(this.baseUrl + '/downloadPublicSharedModel/' + shareCode, this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }



  publishModelService(fileName, filePath, user) {
    return this._http.post(this.baseUrl + '/publishModelService/' + fileName + "+" + filePath, JSON.stringify(user), this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }



  deleteCollection() {
    console.log("delete service");
    return this._http.post(this.baseUrl + '/deleteCollection', this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);
  }

  deletePublicCollection() {
    console.log("delete service2");
    return this._http.post(this.baseUrl + '/deletePublicCollection', this.options).map((response: Response) => response.json())
      .catch(this.errorHandler);

  }



  openModel(fileName, filePath) {

    var req = new HttpRequest('GET', 'http://localhost:8080/api/openModel' + fileName + filePath + this.user.username, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  openPublicModel(fileName, filePath) {

    var req = new HttpRequest('GET', 'http://localhost:8080/api/openModel' + fileName + filePath, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);
  }

  verifyValidity(username, folderPath, fileName, xml) {
    /*var req = new HttpRequest('POST', 'http://localhost:8080/api/verifyValidity/' + username + folderPath + fileName, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(req);*/
    var result;
    console.log("ENTRATO");
    var xhttp = new XMLHttpRequest();
      console.log("BLABLA-1")
    xhttp.open("POST", "http://pros.unicam.it:8080/S3/rest/BPMN/Verifier", true);
    console.log("BLABLA0")
    xhttp.onload = function() {
      console.log("BLABLA1")

      result=this.responseText;
      console.log(result);
      console.log("BLABLA2")
    }
    xhttp.send();
    //return this._http.post('http://pros.unicam.it:8080/S3/rest/BPMN/Verifier', JSON.stringify(this.xmlToSave), this.options).map((response: Response) => response.text())
     // .catch(this.errorHandler);
  }

}