package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class Advisement extends IMDairiesDTO {
	private String advisementID;
	private String shortDescription;
	private String longDescription;
	private boolean isEnabled;
	private boolean isEnabledForOrg;
	private float firstThreshold;
	private float secondThreshold;
	private float thirdThreshold;
	private String firstThresholdMessageCode;
	private String secondThresholdMessageCode;
	private String thirdThresholdMessageCode;
	private String firstThresholdMessage;
	private String secondThresholdMessage;
	private String thirdThresholdMessage;
	private String advisementRuleOutcomeMessage;	
	private String emailInd;
	private String smsInd;
	private String webInd;
	private String auxInfo1;
	private String auxInfo2;
	private String auxInfo3;
	private String auxInfo4;
	private String auxInfo5;
	

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
		return 	stringify(" ");
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
		return  prefix + fieldToJson("orgID", getOrgId()) + ",\n" + 
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
				prefix + fieldToJson("auxInfo1", this.auxInfo1) + ",\n" +
				prefix + fieldToJson("auxInfo2", this.auxInfo2) + ",\n" +
				prefix + fieldToJson("auxInfo3", this.auxInfo3) + ",\n" +
				prefix + fieldToJson("auxInfo4", this.auxInfo4) + ",\n" +
				prefix + fieldToJson("auxInfo5", this.auxInfo5) + ",\n" +
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
	public String getAuxInfo1() {
		return auxInfo1;
	}
	public void setAuxInfo1(String auxInfo1) {
		this.auxInfo1 = auxInfo1;
	}
	public String getAuxInfo2() {
		return auxInfo2;
	}
	public void setAuxInfo2(String auxInfo2) {
		this.auxInfo2 = auxInfo2;
	}
	public String getAuxInfo3() {
		return auxInfo3;
	}
	public void setAuxInfo3(String auxInfo3) {
		this.auxInfo3 = auxInfo3;
	}
	public String getAuxInfo4() {
		return auxInfo4;
	}
	public void setAuxInfo4(String auxInfo4) {
		this.auxInfo4 = auxInfo4;
	}
	public String getAuxInfo5() {
		return auxInfo5;
	}
	public void setAuxInfo5(String auxInfo5) {
		this.auxInfo5 = auxInfo5;
	}
	public String getFirstThresholdMessageCode() {
		return firstThresholdMessageCode;
	}
	public void setFirstThresholdMessageCode(String firstThresholdMessageCode) {
		this.firstThresholdMessageCode = firstThresholdMessageCode;
	}
	public String getSecondThresholdMessageCode() {
		return secondThresholdMessageCode;
	}
	public void setSecondThresholdMessageCode(String secondThresholdMessageCode) {
		this.secondThresholdMessageCode = secondThresholdMessageCode;
	}
	public String getThirdThresholdMessageCode() {
		return thirdThresholdMessageCode;
	}
	public void setThirdThresholdMessageCode(String thirdThresholdMessageCode) {
		this.thirdThresholdMessageCode = thirdThresholdMessageCode;
	}
}
