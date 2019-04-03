package com.imd.dto;



import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import com.imd.services.bean.LifeCycleEventCodeBean;
import com.imd.util.IMDException;
import com.imd.util.Util;

public class LifeCycleEventCode extends IMDairiesDTO{	
	/** 
	 * This is the unique code of this particular event. This comes from the master list of event codes.
	 */
	private String eventCode;
	/**
	 * Short description of the event which corresponds to the eventCode.
	 */
	private String eventShortDescription;
	/**
	 * Long description of the event which corresponds to the eventCode.
	 */
	private String eventLongDescription;

	private boolean isActive;
	
	private String field1Label;
	private String field2Label;
	private String field1DataType;
	private String field2DataType;
	private String field1DataUnit;
	private String field2DataUnit;

	public LifeCycleEventCode(String lifeCycleEventCode, String shortDescr, String longDescr) throws IMDException{
		super();
		Util.throwExceptionIfNullOrEmpty(lifeCycleEventCode, " LifeCycleEvent Code"); 
		this.eventCode = lifeCycleEventCode;
		this.eventShortDescription = shortDescr;
		this.eventLongDescription = longDescr;
	}

	public LifeCycleEventCode(LifeCycleEventCodeBean eventBean) {
		this.eventCode = eventBean.getEventCode();
		this.eventShortDescription = eventBean.getEventShortDescription();
		this.eventLongDescription = eventBean.getEventLongDescription();
		this.isActive = eventBean.isActive();
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventID) {
		this.eventCode = eventID;
	}

	public String getEventShortDescription() {
		return eventShortDescription;
	}


	public void setEventShortDescription(String eventShortDescription) {
		this.eventShortDescription = eventShortDescription;
	}


	public String getEventLongDescription() {
		return eventLongDescription;
	}


	public void setEventLongDescription(String eventLongDescription) {
		this.eventLongDescription = eventLongDescription;
	}


	public void markActive() {
		this.isActive = true;		
	}
	public void markInActive() {
		this.isActive = false;		
	}
	public boolean isActive() {
		return this.isActive;
	}
	
	public String toString() {
		return  stringify(" ");
	}
	
	public String stringify(String prefix)  {
		return  prefix + fieldToJson("eventCode", this.eventCode) + ",\n" +
				prefix + fieldToJson("eventShortDescription", this.eventShortDescription) + ",\n" + 
				prefix + fieldToJson("eventLongDescription", this.eventLongDescription) + ",\n" + 
				prefix + fieldToJson("eventField1Label", this.field1Label) + ",\n" + 
				prefix + fieldToJson("eventField1DataType", this.field1DataType) + ",\n" +
				prefix + fieldToJson("eventField1DataUnit", this.field1DataUnit) + ",\n" + 
				prefix + fieldToJson("eventField2Label", this.field2Label) + ",\n" + 
				prefix + fieldToJson("eventField2DataType", this.field2DataType) + ",\n" +
				prefix + fieldToJson("eventField2DataUnit", this.field2DataUnit) + ",\n" + 
				prefix + fieldToJson("isActive", this.isActive) + ",\n";
	}

	public String dtoToJson(String prefix)  {
		return dtoToJson(prefix,true);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return dtoToJson(prefix,fmt,true);
	}
	public String dtoToJson(String prefix, boolean shouldIncludeAuditFields)  {		
		return stringify(prefix) + (shouldIncludeAuditFields ? super.dtoToJson(prefix) : "");
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt, boolean shouldIncludeAuditFields)   {
		return stringify(prefix) + (shouldIncludeAuditFields ? super.dtoToJson(prefix, fmt) : "");
	}

	public String getField1Label() {
		return field1Label;
	}

	public void setField1Label(String field1Label) {
		this.field1Label = field1Label;
	}

	public String getField2Label() {
		return field2Label;
	}

	public void setField2Label(String field2Label) {
		this.field2Label = field2Label;
	}

	public String getField1DataType() {
		return field1DataType;
	}

	public void setField1DataType(String field1DataType) {
		this.field1DataType = field1DataType;
	}

	public String getField2DataType() {
		return field2DataType;
	}

	public void setField2DataType(String field2DataType) {
		this.field2DataType = field2DataType;
	}
	public void setField1DataUnit(String fieldUnit) {
		this.field1DataUnit = fieldUnit;
	}

	public String getField1DataUnit() {
		return field1DataUnit;
	}
	public void setField2DataUnit(String fieldUnit) {
		this.field2DataUnit = fieldUnit;
	}

	public String getField2DataUnit() {
		return field2DataUnit;
	}

}







