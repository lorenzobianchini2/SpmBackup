package spm.entities;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "usersFileManager")
public class Folder {
	
	private BigInteger fId;
	
	private String fName;
	
	private BigInteger modelsId;
	
	private String shareCode;
	
	private String creationDate;
	
	
	
	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getShareCode() {
		return shareCode;
	}

	public void setShareCode(String shareCode) {
		this.shareCode = shareCode;
	}

	public BigInteger getModelsId() {
		return modelsId;
	}

	public void setModelsId(BigInteger modelsId) {
		this.modelsId = modelsId;
	}

	private List<Folder>folders; 
	
	private List<BpmnModel>models; 

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

	public BigInteger getfId() {
		return fId;
	}

	public void setfId(BigInteger fId) {
		this.fId = fId;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public Folder(BigInteger fId, String fName) {
		super();
		this.fId = fId;
		this.fName = fName;
	}

	public Folder() {
		super();
	}
	
	
}
