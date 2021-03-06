package com.imd.services.bean;


public class LookupValuesBean {

	private String orgID;
	private String categoryCode;
	private String lookupValueCode;
	private String shortDescription;
	private String longDescription;
	private String shortDescriptionMessageCd;
	private String longDescriptionMessageCd;
	private String activeIndicator;
	private String additionalField1;
	private String additionalField2;
	private String additionalField3;
	private String loginToken;

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	
	
	public LookupValuesBean() {
		
	}
	
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}
	public LookupValuesBean(String ctgryCd, String lookupCode) {
		this.categoryCode = ctgryCd;
		this.lookupValueCode = lookupCode;
	}

	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String valueShortDescription) {
		this.shortDescription = valueShortDescription;
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String valueLongDescription) {
		this.longDescription = valueLongDescription;
	}

	public boolean isActive() {
		return (activeIndicator != null && activeIndicator.equalsIgnoreCase("Y") ? true : false);
	}

	public String getActiveIndicator() {
		return activeIndicator;
	}

	public void setActiveIndicator(String activeIndicator) {
		this.activeIndicator = activeIndicator;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getAdditionalField1() {
		return additionalField1;
	}

	public void setAdditionalField1(String additionalField1) {
		this.additionalField1 = additionalField1;
	}

	public String getAdditionalField2() {
		return additionalField2;
	}

	public void setAdditionalField2(String additionalField2) {
		this.additionalField2 = additionalField2;
	}

	public String getAdditionalField3() {
		return additionalField3;
	}

	public void setAdditionalField3(String additionalField3) {
		this.additionalField3 = additionalField3;
	}

	public String getLookupValueCode() {
		return lookupValueCode;
	}

	public void setLookupValueCode(String lookupValueCode) {
		this.lookupValueCode = lookupValueCode;
	}

	public String toString() {
		return "categoryCode = " + this.categoryCode +
				"\n lookupValueCode = " + this.lookupValueCode +
				"\n activeIndicator = " + this.activeIndicator +
				"\n shortDescription = " + this.shortDescription +
				"\n longDescription = " + this.longDescription +
				"\n shortDescriptionMessageCd = " + this.shortDescriptionMessageCd +
				"\n longDescriptionMessageCd = " + this.longDescriptionMessageCd +
				"\n additionalField1 = " + this.additionalField1 +
				"\n additionalField2 = " + this.additionalField2 +
				"\n additionalField3 = " + this.additionalField3;
	}

	public String getShortDescriptionMessageCd() {
		return shortDescriptionMessageCd;
	}

	public void setShortDescriptionMessageCd(String shortDescriptionMessageCd) {
		this.shortDescriptionMessageCd = shortDescriptionMessageCd;
	}

	public String getLongDescriptionMessageCd() {
		return longDescriptionMessageCd;
	}

	public void setLongDescriptionMessageCd(String longDescriptionMessageCd) {
		this.longDescriptionMessageCd = longDescriptionMessageCd;
	}
}
