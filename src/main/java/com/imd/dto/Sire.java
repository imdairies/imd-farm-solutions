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
	public Sire(String orgID, String tagNumber, DateTime dateOfBirth, boolean isDobEstimated, double purPrice,
			String priceCurr) throws IMDException {
			super(orgID, tagNumber, dateOfBirth, isDobEstimated, purPrice, priceCurr);
			setGender('M');
	}
	
	private String stringify(String prefix) {
		return  prefix + fieldToJson("controller", this.controller) + ",\n" +
				prefix + fieldToJson("sirePhoto", this.sirePhoto) + ",\n" + 
				prefix + fieldToJson("sireDataSheet", this.sireDataSheet) + ",\n";
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	
	
	public Sire(String tagNumber) throws IMDException  {
		super(tagNumber);
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

}
