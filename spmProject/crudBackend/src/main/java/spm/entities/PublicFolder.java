package spm.entities;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "publicRepo")
public class PublicFolder {
	
	@Id
	private BigInteger id;
	
	private String fName;
	
	private List<PublicFolder>folders; 
	
	private List<BpmnModel>models; 

	private BigInteger foldersId;
	
	private BigInteger modelsId;
	
	private BigInteger versionsId;
	
	private String creationDate;
	
	private String author;


	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public BigInteger getVersionsId() {
		return versionsId;
	}

	public void setVersionsId(BigInteger versionsId) {
		this.versionsId = versionsId;
	}

	public BigInteger getModelsId() {
		return modelsId;
	}

	public void setModelsId(BigInteger modelsId) {
		this.modelsId = modelsId;
	}

	public BigInteger getFoldersId() {
		return foldersId;
	}

	public void setFoldersId(BigInteger foldersId) {
		this.foldersId = foldersId;
	}

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

	public BigInteger getfId() {
		return id;
	}

	public void setfId(BigInteger fId) {
		this.id = id;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public PublicFolder(BigInteger id, String fName) {
		super();
		this.id = id;
		this.fName = fName;
	}

	public PublicFolder() {
		super();
	}
	
	
}
