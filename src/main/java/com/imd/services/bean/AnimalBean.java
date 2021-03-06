package com.imd.services.bean;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnimalBean {
	private String orgId;
	private String animalTag;
	private String alias;
	private boolean activeOnly;
	private String animalType;
	private String gender;
	private String dateOfBirthStr;
	private String dobFrom;
	private String dobTo;
	private String ageInMonthsFrom;
	private String ageInMonthsTo;
	private String dobAccuracyInd;
	private String aiIndicator;
	private String breed;
	private String dam;
	private String sire;
	private String aiInd;
	private String frontPoseImage;
	private String backPoseImage;
	private String rightPoseImage;
	private String leftPoseImage;
	private String herdJoiningDttmStr;
	private String loginToken;
	private String operatorAtBirth;
	
	@JsonProperty("animalTagsList")
    private List<String> animalTagsList;

	public List<String> getAnimalTagsList() {
		return animalTagsList;
	}
	public void setAnimalTagsList(List<String> tagList) {
		animalTagsList = tagList;
	}


	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public boolean getActiveOnly() {
		return activeOnly;
	}
	public void setActiveOnly(boolean activeOnly) {
		this.activeOnly = activeOnly;
	}
	
	public String getAnimalType() {
		return animalType;
	}
	public void setAnimalType(String animalType) {
		this.animalType = animalType;
	}
	
	public String toString() {
		return 	"\n orgID:" + orgId + 
				"\n animalTag:" + animalTag + 
				"\n animalTagsList:" + getValueList(animalTagsList) + 
				"\n animalType:" + animalType + 
				"\n loginToken: " + loginToken +
				"\n alias:" + alias + 
				"\n gender:" + gender + 
				"\n dateOfBirthStr:" + dateOfBirthStr + 
				"\n dobAccuracyInd:" + dobAccuracyInd + 
				"\n herdJoiningDttmStr:" + herdJoiningDttmStr + 				
				"\n dam:" + dam + 
				"\n sire:" + sire + 
				"\n aiInd:" + aiInd +
				"\n frontPoseImage:" + frontPoseImage +
				"\n backPoseImage:" + backPoseImage +
				"\n rightPoseImage:" + rightPoseImage +
				"\n leftPoseImage:" + leftPoseImage +
				"\n activeOnly:" + activeOnly;
	}
	private String getValueList(List<String> tagsList) {
		String valueList = "";
		if (tagsList != null && !tagsList.isEmpty()) {
			Iterator<String> it = tagsList.iterator();
			while(it.hasNext()) {
				valueList += (valueList.isEmpty() ? "" : ",") + it.next().toString();
			}
		}
		return valueList;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgID) {
		this.orgId = orgID;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public void setGender(char gender) {
		this.gender = gender + "";
	}
	public String getDobAccuracyInd() {
		return dobAccuracyInd;
	}
	public void setDobAccuracyInd(String dobAccuracyInd) {
		this.dobAccuracyInd = dobAccuracyInd;
	}
	public String getAiInd() {
		return aiInd;
	}
	public void setAiInd(String aiInd) {
		this.aiInd = aiInd;
	}
	public String getDam() {
		return dam;
	}
	public void setDam(String dam) {
		this.dam = dam;
	}
	public String getSire() {
		return sire;
	}
	public void setSire(String sire) {
		this.sire = sire;
	}
	public String getFrontPoseImage() {
		return frontPoseImage;
	}
	public void setFrontPoseImage(String frontPoseImage) {
		this.frontPoseImage = frontPoseImage;
	}
	public String getBackPoseImage() {
		return backPoseImage;
	}
	public void setBackPoseImage(String backPoseImage) {
		this.backPoseImage = backPoseImage;
	}
	public String getRightPoseImage() {
		return rightPoseImage;
	}
	public void setRightPoseImage(String rightPoseImage) {
		this.rightPoseImage = rightPoseImage;
	}
	public String getLeftPoseImage() {
		return leftPoseImage;
	}
	public void setLeftPoseImage(String leftPoseImage) {
		this.leftPoseImage = leftPoseImage;
	}
	public DateTime getDateOfBirth() {	
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return (DateTime.parse(dateOfBirthStr, fmt));
	}
	public DateTime getDateOfBirth(String string) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(string);
		return (DateTime.parse(dateOfBirthStr, fmt));
	}
	public DateTime getHerdJoiningDate(String string) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(string);
		return (DateTime.parse(herdJoiningDttmStr, fmt));
	}

	public String getAiIndicator() {
		return aiIndicator;
	}
	public void setAiIndicator(String aiIndicator) {
		this.aiIndicator = aiIndicator;
	}
	public String getBreed() {
		return this.breed;
	}
	public void setBreed (String animalBreed) {
		this.breed = animalBreed;
	}
	public String getHerdJoiningDttmStr() {
		return herdJoiningDttmStr;
	}
	public void setHerdJoiningDttmStr(String herJoiningDttmStr) {
		this.herdJoiningDttmStr = herJoiningDttmStr;
	}
	public String getDateOfBirthStr() {
		return dateOfBirthStr;
	}
	public void setDateOfBirthStr(String dobStr) {
		dateOfBirthStr = dobStr;
	}
	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	public String getOperatorAtBirth() {
		return operatorAtBirth;
	}
	public void setOperatorAtBirth(String operatorAtBirth) {
		this.operatorAtBirth = operatorAtBirth;
	}
	public String getDobFrom() {
		return dobFrom;
	}
	public void setDobFrom(String dobFrom) {
		this.dobFrom = dobFrom;
	}
	public String getDobTo() {
		return dobTo;
	}
	public void setDobTo(String dobTo) {
		this.dobTo = dobTo;
	}
	public String getAgeInMonthsFrom() {
		return ageInMonthsFrom;
	}
	public void setAgeInMonthsFrom(String ageInMonthsFrom) {
		this.ageInMonthsFrom = ageInMonthsFrom;
	}
	public String getAgeInMonthsTo() {
		return ageInMonthsTo;
	}
	public void setAgeInMonthsTo(String ageInMonthsTo) {
		this.ageInMonthsTo = ageInMonthsTo;
	}
}
