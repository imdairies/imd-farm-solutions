package com.imd.dto;

import java.net.URI;
import java.time.LocalDate;

import org.joda.time.DateTime;

import com.imd.util.IMDException;

public class Sire extends Animal {
	private Contact marketedByCompany;
	private Contact ownerCompany;
	private URI sireSpecification;
	public Sire(String orgID, String tagNumber, DateTime dateOfBirth, boolean isDobEstimated, double purPrice,
			String priceCurr) throws IMDException {
			super(orgID, tagNumber, dateOfBirth, isDobEstimated, purPrice, priceCurr);
			setGender('M');
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
	public URI getSireSpecification() {
		return sireSpecification;
	}
	public void setSireSpecification(URI sireSpecification) {
		this.sireSpecification = sireSpecification;
	}

}
