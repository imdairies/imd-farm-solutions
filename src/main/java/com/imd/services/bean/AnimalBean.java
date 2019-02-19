package com.imd.services.bean;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.Person;
import com.imd.util.Util;

public class AnimalBean {
	private String orgID;
	private String animalTag;
	private String alias;
	private boolean activeOnly;
	private String animalType;
	private char gender;
	private String dateOfBirthStr;
	private String dobAccuracyInd;
	private String aiIndicator;
	private String dam;
	private String sire;
	private String aiInd;
	private String frontPoseImage;
	private String backPoseImage;
	private String rightPoseImage;
	private String leftPoseImage;
//	private DateTime dateOfBirth;
//	private boolean isDateOfBirthEstimated;
//	private Sire animalSire;
//	private Dam animalDam;
//	private String animalStatus;
//	
//	private double purchasePrice;
//	private String purchaseCurrency = "PKR";
//	private Contact purchaseFromContact;
//	private DateTime purchaseDate;
//	public char getGender() {
//		return gender;
//	}
//	public void setGender(char gender) {
//		this.gender = gender;
//	}
//	public DateTime getDateOfBirth() {
//		return dateOfBirth;
//	}
//	public void setDateOfBirth(DateTime dateOfBirth) {
//		this.dateOfBirth = dateOfBirth;
//	}
//	public boolean isDateOfBirthEstimated() {
//		return isDateOfBirthEstimated;
//	}
//	public void setDateOfBirthEstimated(boolean isDateOfBirthEstimated) {
//		this.isDateOfBirthEstimated = isDateOfBirthEstimated;
//	}
//	public Sire getAnimalSire() {
//		return animalSire;
//	}
//	public void setAnimalSire(Sire animalSire) {
//		this.animalSire = animalSire;
//	}
//	public Dam getAnimalDam() {
//		return animalDam;
//	}
//	public void setAnimalDam(Dam animalDam) {
//		this.animalDam = animalDam;
//	}
//	public String getAnimalStatus() {
//		return animalStatus;
//	}
//	public void setAnimalStatus(String animalStatus) {
//		this.animalStatus = animalStatus;
//	}
//	public double getPurchasePrice() {
//		return purchasePrice;
//	}
//	public void setPurchasePrice(double purchasePrice) {
//		this.purchasePrice = purchasePrice;
//	}
//	public String getPurchaseCurrency() {
//		return purchaseCurrency;
//	}
//	public void setPurchaseCurrency(String purchaseCurrency) {
//		this.purchaseCurrency = purchaseCurrency;
//	}
//	public Contact getPurchaseFromContact() {
//		return purchaseFromContact;
//	}
//	public void setPurchaseFromContact(Contact purchaseFromContact) {
//		this.purchaseFromContact = purchaseFromContact;
//	}
//	public DateTime getPurchaseDate() {
//		return purchaseDate;
//	}
//	public void setPurchaseDate(DateTime purchaseDate) {
//		this.purchaseDate = purchaseDate;
//	}

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
		return 	"\n orgID:" + orgID + 
				"\n animalTag:" + animalTag + 
				"\n animalType:" + animalType + 
				"\n alias:" + alias + 
				"\n gender:" + gender + 
				"\n dateOfBirthStr:" + dateOfBirthStr + 
				"\n dobAccuracyInd:" + dobAccuracyInd + 
				"\n dam:" + dam + 
				"\n sire:" + sire + 
				"\n aiInd:" + aiInd +
				"\n frontPoseImage:" + frontPoseImage +
				"\n backPoseImage:" + backPoseImage +
				"\n rightPoseImage:" + rightPoseImage +
				"\n leftPoseImage:" + leftPoseImage +
				"\n activeOnly:" + activeOnly;
	}
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}
	public char getGender() {
		return gender;
	}
	public void setGender(char gender) {
		this.gender = gender;
	}
	public String getDateOfBirthStr() {
		return dateOfBirthStr;
	}
	public void setDateOfBirthStr(String dobStr) {
		dateOfBirthStr = dobStr;
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
	public String getAiIndicator() {
		return aiIndicator;
	}
	public void setAiIndicator(String aiIndicator) {
		this.aiIndicator = aiIndicator;
	}
	
}
