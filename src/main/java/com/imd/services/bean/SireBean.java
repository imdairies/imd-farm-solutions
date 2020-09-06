package com.imd.services.bean;


public class SireBean {
	private String orgID;
	private String animalTag;
	private String breed;
	private String alias;
	private String semenInd;
	private String recordURL;
	private String photoURL;
	private String controller;
	private String semenCompany;
	private Float currentSexListPrice;
	private Float discountSexPercentage;
	private Float currentConventionalListPrice;
	private Float discountConventionalPercentage;
	private String loginToken;

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}
	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	public Float getDiscountConventionalPercentage() {
		return discountConventionalPercentage;
	}
	public void setDiscountConventionalPercentage(Float discountConventionalPercentage) {
		this.discountConventionalPercentage = discountConventionalPercentage;
	}
	public Float getCurrentConventionalListPrice() {
		return currentConventionalListPrice;
	}
	public void setCurrentConventionalListPrice(Float currentConventionalListPrice) {
		this.currentConventionalListPrice = currentConventionalListPrice;
	}
	public Float getDiscountSexPercentage() {
		return discountSexPercentage;
	}
	public void setDiscountSexPercentage(Float discountSexPercentage) {
		this.discountSexPercentage = discountSexPercentage;
	}
	public Float getCurrentSexListPrice() {
		return currentSexListPrice;
	}
	public void setCurrentSexListPrice(Float currentSexListPrice) {
		this.currentSexListPrice = currentSexListPrice;
	}
	public String getSemenCompany() {
		return semenCompany;
	}
	public void setSemenCompany(String semenCompany) {
		this.semenCompany = semenCompany;
	}
	public String getController() {
		return controller;
	}
	public void setController(String controller) {
		this.controller = controller;
	}
	public String getPhotoURL() {
		return photoURL;
	}
	public void setPhotoURL(String photoURL) {
		this.photoURL = photoURL;
	}
	public String getRecordURL() {
		return recordURL;
	}
	public void setRecordURL(String recordURL) {
		this.recordURL = recordURL;
	}
	public String getSemenInd() {
		return semenInd;
	}
	public void setSemenInd(String semenInd) {
		this.semenInd = semenInd;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getBreed() {
		return breed;
	}
	public void setBreed(String breed) {
		this.breed = breed;
	}
	
	public String toString() {
		
		return 	"orgID = " + orgID + "\n" +
				"animalTag = " + animalTag  + "\n" +
				"breed = " + breed  + "\n" +
				"alias = " + alias  + "\n" +
				"semenInd = " + semenInd  + "\n" +
				"recordURL = " + recordURL  + "\n" +
				"photoURL = " + photoURL  + "\n" +
				"controller = " + controller  + "\n" +
				"semenCompany = " + semenCompany  + "\n" +
				"currentSexListPrice = " + currentSexListPrice  + "\n" +
				"discountSexPercentage = " + discountSexPercentage  + "\n" +
				"currentConventionalListPrice = " + currentConventionalListPrice  + "\n" + 
				"discountConventionalPercentage = " + discountConventionalPercentage  + "\n";
	}
}
