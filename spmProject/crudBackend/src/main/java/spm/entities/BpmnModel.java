package spm.entities;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usersFileManager")
public class BpmnModel {

	private BigInteger mId;
	private String mName;
	private List<BpmnModel>models;
	private List<Version>versions;
	private String shareCode;
	private String versionDescription;
	private String creationDate;
	private String author;
	private int versionNumber;
	private int modelNumber;
	
	
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
	public String getVersionDescription() {
		return versionDescription;
	}
	public void setVersionDescription(String versionDescription) {
		this.versionDescription = versionDescription;
	}
	public int getModelNumber() {
		return modelNumber;
	}
	public void setModelNumber(int modelNumber) {
		this.modelNumber = modelNumber;
	}
	public int getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	public String getShareCode() {
		return shareCode;
	}
	public void setShareCode(String shareCode) {
		this.shareCode = shareCode;
	}
	public List<Version> getVersions() {
		return versions;
	}
	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}
	public List<BpmnModel> getModels() {
		return models;
	}
	public void setModels(List<BpmnModel> models) {
		this.models = models;
	}
	
	
	public BigInteger getmId() {
		return mId;
	}
	public void setmId(BigInteger mId) {
		this.mId = mId;
	}
	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	public BpmnModel(BigInteger mId, String mName) {
		super();
		this.mId = mId;
		this.mName = mName;
	}
	public BpmnModel() {
		super();
	}
	
	
	
}
