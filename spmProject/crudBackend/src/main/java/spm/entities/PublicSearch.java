package spm.entities;

import java.util.List;

public class PublicSearch {
	private List<PublicFolder> folders; 
	private List<BpmnModel> models;
	private List<String> folderPaths;
	private List<String> modelPaths;
	
	public List<PublicFolder> getFolders() {
		return folders;
	}
	public void setFolders(List<PublicFolder> folders) {
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
