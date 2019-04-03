package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.LookupValuesBean;

public class LookupValues extends IMDairiesDTO{
	private String categoryCode;
	private String lookupValueCode;
	private String shortDescription;
	private String longDescription;
	private String activeIndicator;
	private String additionalField1;
	private String additionalField2;
	private String additionalField3;
	
	public LookupValues(String categoryCd, String valueCD, String shortDescr, String longDescr) {
		this.lookupValueCode = valueCD;
		this.categoryCode = categoryCd;
		this.shortDescription = shortDescr;
		this.longDescription = longDescr;		
	}
	
	public LookupValues(LookupValuesBean luValueBean) {
		this.categoryCode = luValueBean.getCategoryCode();
		this.lookupValueCode  = luValueBean.getLookupValueCode();
		this.shortDescription = luValueBean.getShortDescription();
		this.longDescription = luValueBean.getLongDescription();
		this.activeIndicator = luValueBean.getActiveIndicator();
		this.additionalField1 = luValueBean.getAdditionalField1();
		this.additionalField2 = luValueBean.getAdditionalField2();
		this.additionalField3 = luValueBean.getAdditionalField3();
	}

	public String getLookupValueCode() {
		return lookupValueCode;
	}
	public void setLookupValueCode(String lvCode) {
		this.lookupValueCode = lvCode;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String eventShortDescription) {
		this.shortDescription = eventShortDescription;
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String eventLongDescription) {
		this.longDescription = eventLongDescription;
	}

	public boolean isActive() {
		return (activeIndicator != null && activeIndicator.equalsIgnoreCase("Y") ? true : false);
	}

	public void markActive() {
		this.activeIndicator = "Y";
	}

	public void setActiveIndicator(String activeInd) {
		this.activeIndicator = activeInd;
	}
	
	public void markInActive() {
		this.activeIndicator = "N";
	}
	
	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getAdditionalField1() {
		return additionalField1;
	}

	public void setAdditionalField1(String additionalField1) {
		this.additionalField1 = additionalField1;
	}

	public String getAdditionalField2() {
		return additionalField2;
	}

	public void setAdditionalField2(String additionalField2) {
		this.additionalField2 = additionalField2;
	}

	public String getAdditionalField3() {
		return additionalField3;
	}

	public void setAdditionalField3(String additionalField3) {
		this.additionalField3 = additionalField3;
	}

	public String toString() {
		return 	"CATEGORY_CD=" + getCategoryCode() + "\n" +
				"LOOKUP_CD=" + getLookupValueCode() + "\n" +
				"ACTIVE_IND=" + (isActive() ? "Y":"N") + "\n" +
				"SHORT_DESCR=" + getShortDescription() + "\n" +
				"LONG_DESCR=" + getLongDescription() + "\n" +
				"ADDITIONAL_FLD1=" + getAdditionalField1() + "\n" +
				"ADDITIONAL_FLD2=" + getAdditionalField2() + "\n" +
				"ADDITIONAL_FLD3=" + getAdditionalField3() + "\n" +	super.toString();
	}
	private String stringify(String prefix) {
		return prefix + fieldToJson("categoryCode", this.categoryCode) + ",\n" +
				prefix + fieldToJson("lookupValueCode", this.lookupValueCode) + ",\n" + 
				prefix + fieldToJson("isActive", this.isActive()) + ",\n" + 
				prefix + fieldToJson("shortDescription", this.shortDescription) + ",\n" + 
				prefix + fieldToJson("longDescription", this.longDescription) + ",\n" + 
				prefix + fieldToJson("activeIndicator", this.activeIndicator) + ",\n" + 
				prefix + fieldToJson("additionalField1", this.additionalField1) + ",\n" + 
				prefix + fieldToJson("additionalField2", this.additionalField2) + ",\n" + 
				prefix + fieldToJson("additionalField3", this.additionalField3) + ",\n";
	}
	
	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}

}
