package com.imd.dto;



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

	public String createUpdateString() {
		String updateString = "";		
		updateString = " ACTIVE_IND=" + (isActive()? "'Y'": "'N'") + ",";
		if (eventShortDescription != null && !eventShortDescription.isEmpty())
			updateString += " SHORT_DESCR='" + eventShortDescription + "',";
		if (eventLongDescription != null && !eventLongDescription.isEmpty())	
			updateString += " LONG_DESCR='" + eventLongDescription + "',";
		updateString += super.createUpdateString();
		if (!updateString.isEmpty())
			updateString = updateString.substring(0, updateString.lastIndexOf(","));		
		return updateString;
	}
	
	public String toString() {
		return  "\nEVENT_CD=" + this.eventCode + 
				"\nACTIVE_IND=" + this.isActive +
				"\nSHORT_DESCR=" + this.eventShortDescription + 
				"\nLONG_DESCR=" + this.eventLongDescription + super.toString();
	}
}







