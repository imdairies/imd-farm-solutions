package com.imd.dto;



import java.util.ArrayList;
import java.util.List;

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
		return  "\nEVENT_CD=" + this.eventCode + 
				"\nACTIVE_IND=" + this.isActive +
				"\nSHORT_DESCR=" + this.eventShortDescription + 
				"\nLONG_DESCR=" + this.eventLongDescription + 
				"\nCREATED_BY" + (this.getCreatedBy() != null ? this.getCreatedBy().getUserId() : "null") + 
				"\nCREATED_DTTM" + this.getCreatedDTTMSQLFormat() + ",\n" +
				"\nUPDATED_BY" + (this.getUpdatedBy() != null ? this.getCreatedBy().getUserId() : "null")+
				"\nUPDATED_DTTM" + this.getUpdatedDTTMSQLFormat();
	}
	
	public String dtoToJson(String prefix)  {
		String json =  prefix + fieldToJson("eventCode", this.eventCode) + ",\n" +
				prefix + fieldToJson("eventShortDescription", this.eventShortDescription) + ",\n" + 
				prefix + fieldToJson("eventLongDescription", this.eventLongDescription) + ",\n" + 
				prefix + fieldToJson("isActive", this.isActive) + ",\n" + 
				super.dtoToJson(prefix);
		return json;
	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		String json =  prefix + fieldToJson("eventCode", this.eventCode) + ",\n" +
				prefix + fieldToJson("eventShortDescription", this.eventShortDescription) + ",\n" + 
				prefix + fieldToJson("eventLongDescription", this.eventLongDescription) + ",\n" + 
				prefix + fieldToJson("isActive", this.isActive) + ",\n" + 
				super.dtoToJson(prefix, fmt);
		return json;
	}
	
	
}







