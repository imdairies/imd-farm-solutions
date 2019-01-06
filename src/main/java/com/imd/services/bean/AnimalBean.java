package com.imd.services.bean;

import org.joda.time.DateTime;

import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.Sire;

public class AnimalBean {
	private String orgID;
	private String animalTag;
	private String alias;
	private boolean activeOnly;
	private String animalType;
	private char gender;
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
	
}
