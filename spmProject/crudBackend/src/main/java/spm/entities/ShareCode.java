package spm.entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ShareCodes")
public class ShareCode {
	private String shareCode;

	public String getShareCode() {
		return shareCode;
	}

	public void setShareCode(String shareCode) {
		this.shareCode = shareCode;
	}

	public ShareCode() {
		super();
	}

	public ShareCode(String shareCode) {
		super();
		this.shareCode = shareCode;
	}
}
