package com.imd.services.bean;


public class UserBean {
	private String userId;
	private String orgId;
	private String loginToken;
//	private DateTime tokenIssueDttm;
//	private DateTime tokenExpiryDttm;
//	private boolean isExpired;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
//	public boolean isActive() {
//		return isExpired;
//	}
//	public void setActive(boolean isActive) {
//		this.isExpired = isActive;
//	}

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
//	public DateTime getTokenIssueDttm() {
//		return tokenIssueDttm;
//	}
//	public void setTokenIssueDttm(DateTime tokenIssueDttm) {
//		this.tokenIssueDttm = tokenIssueDttm;
//	}
//	public DateTime getTokenExpiryDttm() {
//		return tokenExpiryDttm;
//	}
//	public void setTokenExpiryDttm(DateTime tokenExpiryDttm) {
//		this.tokenExpiryDttm = tokenExpiryDttm;
//	}	
	public String toString() {
		return 	"\n orgID:" + this.orgId + 
				"\n userId:" + userId + 
				"\n loginToken:" + loginToken;
	}
}
