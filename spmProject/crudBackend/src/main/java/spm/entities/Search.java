package spm.entities;

import java.util.List;

public class Search {
	private List<Folder> folders; 
	private List<BpmnModel> models;
	private List<String> folderPaths;
	private List<String> modelPaths;
	
	public List<Folder> getFolders() {
		return folders;
	}
	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}
	public List<BpmnModel> getModels() {
		return models;
	}
	public void setModels(List<BpmnModel> models) {
		this.models = models;
	}
	public List<String> getFolderPaths() {
		return folderPaths;
	}
	public void setFolderPaths(List<String> folderPaths) {
		this.folderPaths = folderPaths;
	}
	public List<String> getModelPaths() {
		return modelPaths;
	}
	public void setModelPaths(List<String> modelPaths) {
		this.modelPaths = modelPaths;
	}
	
	
}
