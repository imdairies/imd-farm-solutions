package com.imd.dto;

import java.net.URI;
import java.time.LocalDate;

import com.imd.util.IMDException;

public class Sire extends Animal {
	private Contact marketedByCompany;
	private Contact ownerCompany;
	private URI sireSpecification;
	public Sire(String systemID, String tagNumber, LocalDate dateOfBirth, boolean isDobEstimated, double purPrice,
			String priceCurr) throws IMDException {
			super(systemID, tagNumber, dateOfBirth, isDobEstimated, purPrice, priceCurr);
			super.setGender('M');
	}
	
	
	
	/**
	 * Gender will always be 'Male' for Sire. So we won't allow you to change the gender, irrespective of what you set we will set the gender to 'M'.
	 */
	@Override
	public void setGender(char gender) {
		super.setGender('M');
	}
	public Sire(String tagNumber) throws IMDException  {
		super(tagNumber);
		super.setGender('M');
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
	public URI getSireSpecification() {
		return sireSpecification;
	}
	public void setSireSpecification(URI sireSpecification) {
		this.sireSpecification = sireSpecification;
	}

}
