package spm.entities;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Generated;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Document(collection = "usersFileManager")
public class User {
	
	@Id
	private BigInteger id;

	private String username;

	private String password;

	private String affiliation;

	private String name;

	private String surname;

	private String email;

	private String date_of_birth;
	
	private List<Folder>folders;

	private BigInteger foldersId;
	
	private BigInteger modelsId;
	
	private BigInteger versionsId;

	public BigInteger getModelsId() {
		return modelsId;
	}

	public void setModelsId(BigInteger modelsId) {
		this.modelsId = modelsId;
	}

	public BigInteger getVersionsId() {
		return versionsId;
	}

	public void setVersionsId(BigInteger versionsId) {
		this.versionsId = versionsId;
	}

	public BigInteger getFoldersId() {
		return foldersId;
	}

	public void setFoldersId(BigInteger foldersId) {
		this.foldersId = foldersId;
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}

	public BigInteger getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + "]";
	}

	public User() {
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDate_of_birth() {
		return date_of_birth;
	}

	public void setDate_of_birth(String date_of_birth) {
		this.date_of_birth = date_of_birth;
	}
	
	

}
