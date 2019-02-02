package spm.entities;

import java.math.BigInteger;

public class Version {
	
	private BigInteger vId;
	
	private String vName;
	
	private String vDescription;
	
	private String vDate;

	public BigInteger getvId() {
		return vId;
	}

	public void setvId(BigInteger vId) {
		this.vId = vId;
	}

	public String getvName() {
		return vName;
	}

	public void setvName(String vName) {
		this.vName = vName;
	}

	public String getvDate() {
		return vDate;
	}

	public String getvDescription() {
		return vDescription;
	}

	public void setvDescription(String vDescription) {
		this.vDescription = vDescription;
	}

	public void setvDate(String vDate) {
		this.vDate = vDate;
	}

	

	public Version(BigInteger vId, String vName, String vDescription, String vDate) {
		super();
		this.vId = vId;
		this.vName = vName;
		this.vDescription = vDescription;
		this.vDate = vDate;
	}

	public Version() {
		super();
	} 
	
	
}
