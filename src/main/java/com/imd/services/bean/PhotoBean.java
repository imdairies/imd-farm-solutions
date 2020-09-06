package com.imd.services.bean;


public class PhotoBean {
	private String orgId;
	private String photoIdentifier1;
	private String photoIdentifier2;
	private String photoIdentifier3;
	private String photoIdentifier1Value;
	private String photoIdentifier2Value;
	private String photoIdentifier3Value;
	private String loginToken;

	
	public String toString() {
		return 	"\n orgID:" + orgId + 
				"\n photoIdentifier1:" + photoIdentifier1 + 
				"\n photoIdentifier1Value:" + photoIdentifier1Value + 
				"\n photoIdentifier2:" + photoIdentifier2 + 
				"\n photoIdentifier2Value:" + photoIdentifier2Value + 
				"\n photoIdentifier3:" + photoIdentifier3 +
				"\n photoIdentifier3Value:" + photoIdentifier3Value;
	}
	public String getPhotoIdentifier3() {
		return photoIdentifier3;
	}
	public void setPhotoIdentifier3(String photoIdentifier) {
		this.photoIdentifier3 = photoIdentifier;
	}
	public String getPhotoIdentifier3Value() {
		return photoIdentifier3Value;
	}
	public void setPhotoIdentifier3Value(String photoIdentifierValue) {
		this.photoIdentifier3Value = photoIdentifierValue;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgID) {
		this.orgId = orgID;
	}
	public String getPhotoIdentifier1() {
		return photoIdentifier1;
	}
	public void setPhotoIdentifier1(String photoIdentifier) {
		this.photoIdentifier1 = photoIdentifier;
	}
	public String getPhotoIdentifier2() {
		return photoIdentifier2;
	}
	public void setPhotoIdentifier2(String photoIdentifier) {
		this.photoIdentifier2 = photoIdentifier;
	}
	public String getPhotoIdentifier1Value() {
		return photoIdentifier1Value;
	}
	public void setPhotoIdentifier1Value(String photoIdentifierValue) {
		this.photoIdentifier1Value = photoIdentifierValue;
	}
	public String getPhotoIdentifier2Value() {
		return photoIdentifier2Value;
	}
	public void setPhotoIdentifier2Value(String photoIdentifierValue) {
		this.photoIdentifier2Value = photoIdentifierValue;
	}
	public String getLoginToken() {
		return loginToken;
	}
	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

}
