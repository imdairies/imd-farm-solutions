package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class Advisement extends IMDairiesDTO {
	private String orgId;
	private String advisementID;
	private String shortDescription;
	private String longDescription;
	private boolean isEnabled;
	private boolean isEnabledForOrg;
	private float firstThreshold;
	private float secondThreshold;
	private float thirdThreshold;
	private String firstThresholdMessage;
	private String secondThresholdMessage;
	private String thirdThresholdMessage;
	private String advisementRuleOutcomeMessage;	
	private String emailInd;
	private String smsInd;
	private String webInd;
	
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public float getSecondThreshold() {
		return secondThreshold;
	}
	public void setSecondThreshold(float secondThreshold) {
		this.secondThreshold = secondThreshold;
	}
	public float getFirstThreshold() {
		return firstThreshold;
	}
	public void setFirstThreshold(float firstThreshold) {
		this.firstThreshold = firstThreshold;
	}
	public boolean isEnabledForOrg() {
		return isEnabledForOrg;
	}
	public void markEnabledForOrg(boolean isEnabledForOrg) {
		this.isEnabledForOrg = isEnabledForOrg;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void markEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getAdvisementID() {
		return advisementID;
	}
	public void setAdvisementID(String advisementID) {
		this.advisementID = advisementID;
	}
	public String getEmailInd() {
		return emailInd;
	}
	public void setEmailInd(String emailInd) {
		this.emailInd = emailInd;
	}
	public String getSMSInd() {
		return smsInd;
	}
	public void setSMSInd(String smsInd) {
		this.smsInd = smsInd;
	}
	public String getWebInd() {
		return webInd;
	}
	public void setWebInd(String webInd) {
		this.webInd = webInd;
	}
	public String toString() {
		return 	"ORG_ID=" + orgId + "\n" +
				"ADVISEMENT_ID=" + advisementID + "\n" +
				"SHORT_DESCR=" + getShortDescription() + "\n" +
				"LONG_DESCR=" + getLongDescription() + "\n" +
				"ENABLED_IND=" + isEnabled + "\n" +
				"ORG_ENABLED_IND=" + isEnabledForOrg + "\n" +				
				"THRESHOLD1=" + firstThreshold + "\n" +
				"THRESHOLD2=" + secondThreshold + "\n" +
				"THRESHOLD3=" + thirdThreshold + "\n" +
				"THRESHOLD1_MSG=" + firstThresholdMessage + "\n" +
				"THRESHOLD2_MSG=" + secondThresholdMessage + "\n" +
				"THRESHOLD3_MSG=" + thirdThresholdMessage + "\n" +
				"EMAIL_IND=" + emailInd + "\n" +
				"SMS_IND=" + smsInd + "\n" +
				"WEB_IND=" + webInd + "\n" + 
				"advisementRuleOutcomeMessage=" + advisementRuleOutcomeMessage + "\n";
	}
	public float getThirdThreshold() {
		return thirdThreshold;
	}
	public void setThirdThreshold(float thirdThreshold) {
		this.thirdThreshold = thirdThreshold;
	}
	public String getFirstThresholdMessage() {
		return firstThresholdMessage;
	}
	public void setFirstThresholdMessage(String firstThresholdMessage) {
		this.firstThresholdMessage = firstThresholdMessage;
	}
	public String getSecondThresholdMessage() {
		return secondThresholdMessage;
	}
	public void setSecondThresholdMessage(String secondThresholdMessage) {
		this.secondThresholdMessage = secondThresholdMessage;
	}
	public String getThirdThresholdMessage() {
		return thirdThresholdMessage;
	}
	public void setThirdThresholdMessage(String thirdThresholdMessage) {
		this.thirdThresholdMessage = thirdThresholdMessage;
	}
		
	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("advisementID", this.advisementID) + ",\n" +
				prefix + fieldToJson("shortDescription", this.shortDescription) + ",\n" +
				prefix + fieldToJson("longDescription", this.longDescription) + ",\n" +
				prefix + fieldToJson("isEnabled", this.isEnabled) + ",\n" + 
				prefix + fieldToJson("isEnabledForOrg", this.isEnabledForOrg) + ",\n" + 
				prefix + fieldToJson("firstThreshold", this.firstThreshold) + ",\n" + 
				prefix + fieldToJson("secondThreshold", this.secondThreshold) + ",\n" + 
				prefix + fieldToJson("thirdThreshold", this.thirdThreshold) + ",\n" + 
				prefix + fieldToJson("firstThresholdMessage", this.firstThresholdMessage) + ",\n" +
				prefix + fieldToJson("secondThresholdMessage", this.secondThresholdMessage) + ",\n" +				
				prefix + fieldToJson("thirdThresholdMessage", this.thirdThresholdMessage) + ",\n" + 
				prefix + fieldToJson("advisementRuleOutcomeMessage", this.advisementRuleOutcomeMessage) + ",\n" + 
				prefix + fieldToJson("emailInd", this.emailInd) + ",\n" +
				prefix + fieldToJson("smsInd", this.smsInd) + ",\n" +
				prefix + fieldToJson("webInd", this.webInd) + ",\n";
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	public String getAdvisementRuleOutcomeMessage() {
		return advisementRuleOutcomeMessage;
	}
	public void setAdvisementRuleOutcomeMessage(String advisementRuleOutcomeMessage) {
		this.advisementRuleOutcomeMessage = advisementRuleOutcomeMessage;
	}
}
