package com.imd.services.bean;

public class MessageBean {
	private String orgId;
	private String userId;
	private String languageCD;
	private String loginToken;
	private String messageCD;
	private String messageText;
	
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getLoginToken() {
		return loginToken;
	}
	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	public String getLanguageCD() {
		return languageCD;
	}
	public void setLanguageCD(String languageCD) {
		this.languageCD = languageCD;
	}
	public String getMessageCD() {
		return messageCD;
	}
	public void setMessageCD(String messageCD) {
		this.messageCD = messageCD;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String toString() {
		return  "\n orgId: " + this.orgId +
				"\n userId: " + this.userId +
				"\n languageCD: " + this.languageCD +
				"\n messageCD: " + this.messageCD +
				"\n messageText: " + this.messageText +
				"\n loginToken: " + this.loginToken;
	}

}
