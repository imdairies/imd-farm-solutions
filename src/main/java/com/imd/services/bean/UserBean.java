package com.imd.services.bean;


public class UserBean {
	private String orgId;
	private String userId;
	private String loginToken;
	private String preferredLanguage;
	private String preferredCurrency;
	private String password;
	private String userPreference1;
	private String userPreference2;
	private String userPreference3;
	private String userPreference4;
	private String userPreference5;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

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
	public String toString() {
		return 	"\n orgID:" + this.orgId +
				"\n userId:" + userId +
				"\n preferredLanguage:" + preferredLanguage +
				"\n preferredCurrency:" + preferredCurrency +
				"\n userPreference1:" + userPreference1 +
				"\n userPreference2:" + userPreference2 +
				"\n userPreference3:" + userPreference3 +
				"\n userPreference4:" + userPreference4 +
				"\n userPreference5:" + userPreference5 +
				"\n loginToken:" + loginToken;
	}
	
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	public void setPreferredLanguage(String languageCode) {
		this.preferredLanguage = languageCode;
	}
	
	public String getPreferredCurrency() {
		return preferredCurrency;
	}
	public void setPreferredCurrency(String currencyCode) {
		this.preferredCurrency = currencyCode;
	}
	
	public String getUserPreference1() {
		return userPreference1;
	}
	public void setUserPreference1(String userPreference1) {
		this.userPreference1 = userPreference1;
	}
	public String getUserPreference2() {
		return userPreference2;
	}
	public void setUserPreference2(String userPreference2) {
		this.userPreference2 = userPreference2;
	}
	public String getUserPreference5() {
		return userPreference5;
	}
	public void setUserPreference5(String userPreference5) {
		this.userPreference5 = userPreference5;
	}
	public String getUserPreference4() {
		return userPreference4;
	}
	public void setUserPreference4(String userPreference4) {
		this.userPreference4 = userPreference4;
	}
	public String getUserPreference3() {
		return userPreference3;
	}
	public void setUserPreference3(String userPreference3) {
		this.userPreference3 = userPreference3;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
