package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.imd.util.IMDException;

public class Sire extends Animal {
	private Contact marketedByCompany;
	private Contact ownerCompany;
	private String controller;
	private String sirePhoto;
	private String sireDataSheet;
	private String semenInd;
	private Float currentSexListPrice;
	private Float discountSexPercentage;
	private Float currentConventionalListPrice;
	private Float discountConventionalPercentage;
	private Integer semenUsageCount;
	private Integer semenSuccessCount;
	private Integer semenFailureCount;
	private Integer semenTbdCount;
	public Sire(String orgID, String tagNumber, DateTime dateOfBirth, boolean isDobEstimated, double purPrice,
			String priceCurr) throws IMDException {
			super(orgID, tagNumber, dateOfBirth, isDobEstimated, purPrice, priceCurr);
			setGender('M');
	}
	
	private String stringify(String prefix) {
		return  prefix + fieldToJson("controller", this.controller) + ",\n" +
				prefix + fieldToJson("sirePhoto", this.sirePhoto) + ",\n" + 
				prefix + fieldToJson("semenInd", this.semenInd) + ",\n" +
				prefix + fieldToJson("semenUsageCount", this.semenUsageCount) + ",\n" +
				prefix + fieldToJson("semenSuccessCount", this.semenSuccessCount) + ",\n" +
				prefix + fieldToJson("semenFailureCount", this.semenFailureCount) + ",\n" +
				prefix + fieldToJson("semenTbdCount", this.semenTbdCount) + ",\n" +
				prefix + fieldToJson("currentConventionalListPrice", this.currentConventionalListPrice) + ",\n" +
				prefix + fieldToJson("discountConventionalPercentage", this.discountConventionalPercentage) + ",\n" +
				prefix + fieldToJson("currentSexListPrice", this.currentSexListPrice) + ",\n" +
				prefix + fieldToJson("discountSexPercentage", this.discountSexPercentage) + ",\n" +
				prefix + fieldToJson("sireDataSheet", this.sireDataSheet) + ",\n";
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	public String toString() {
		return stringify("   ") + super.dtoToJson("   ");		
	}
	
	public Sire(String OrgID, String tagNumber) throws IMDException  {
		super(OrgID, tagNumber);
		setGender('M');
	}
	public Contact getMarketedByCompany() {
		return marketedByCompany;
	}
	public void setMarketedByCompany(Contact marketedByCompany) {
		this.marketedByCompany = marketedByCompany;
	}
	public Contact getOwnerCompany() {
		return ownerCompany;
	}
	public void setOwnerCompany(Contact ownerCompany) {
		this.ownerCompany = ownerCompany;
	}
	public String getSireSpecification() {
		return sireDataSheet;
	}
	public void setSireSpecification(String sireSpecification) {
		this.sireDataSheet = sireSpecification;
	}

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}


	public String getSirePhoto() {
		return sirePhoto;
	}

	public void setSirePhoto(String sirePhoto) {
		this.sirePhoto = sirePhoto;
	}

	public String getSemenInd() {
		return semenInd;
	}

	public void setSemenInd(String semenInd) {
		this.semenInd = semenInd;
	}

	public Float getCurrentSexListPrice() {
		return currentSexListPrice;
	}

	public void setCurrentSexListPrice(Float currentSexListPrice) {
		this.currentSexListPrice = currentSexListPrice;
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

	public Float getDiscountConventionalPercentage() {
		return discountConventionalPercentage;
	}

	public void setDiscountConventionalPercentage(Float discountConventionalPercentage) {
		this.discountConventionalPercentage = discountConventionalPercentage;
	}

	public Integer getSemenUsageCount() {
		return semenUsageCount;
	}

	public void setSemenUsageCount(Integer semenUsageCount) {
		this.semenUsageCount = semenUsageCount;
	}

	public Integer getSemenSuccessCount() {
		return semenSuccessCount;
	}

	public void setSemenSuccessCount(Integer semenSuccessCount) {
		this.semenSuccessCount = semenSuccessCount;
	}

	public Integer getSemenFailureCount() {
		return semenFailureCount;
	}

	public void setSemenFailureCount(Integer semenFailureCount) {
		this.semenFailureCount = semenFailureCount;
	}

	public Integer getSemenTbdCount() {
		return semenTbdCount;
	}

	public void setSemenTbdCount(Integer semenTbdCount) {
		this.semenTbdCount = semenTbdCount;
	}

}
