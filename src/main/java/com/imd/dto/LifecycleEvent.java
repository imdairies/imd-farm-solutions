package com.imd.dto;


import org.joda.time.DateTime;

import com.imd.util.IMDException;

public class LifecycleEvent extends IMDairiesDTO{
	private String orgID;
	private int eventTransactionID;
	private LifeCycleEventCode eventType;
	private String animalTag;
	private String eventComments;
	private DateTime eventTimeStamp;
	private Person eventOperator;
	

	public LifecycleEvent(String orgID, int transactionID, String animalTag, String eventCode) throws IMDException {
		this.orgID = orgID;
		this.eventTransactionID = transactionID;
		this.animalTag = animalTag;
		this.eventType = new LifeCycleEventCode(eventCode, "", "");
	}


	public String getEventNote() {
		return eventComments;
	}


	public void setEventNote(String eventNotes) {
		this.eventComments = eventNotes;
	}


	public DateTime getEventTimeStamp() {
		return eventTimeStamp;
	}


	public void setEventTimeStamp(DateTime eventDateTime) {
		this.eventTimeStamp = eventDateTime;
	}


	public int getEventTransactionID() {
		return eventTransactionID;
	}

	public void setEventTransactionID(int eventTransactionID) {
		this.eventTransactionID = eventTransactionID;
	}

	public Person getEventOperator() {
		return eventOperator;
	}

	public void setEventOperator(Person eventOperator) {
		this.eventOperator = eventOperator;
	}


	public LifeCycleEventCode getEventType() {
		return eventType;
	}


	public void setEventType(LifeCycleEventCode eventCode) {
		this.eventType = eventCode;
	}


	public String getOrgID() {
		return orgID;
	}


	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}


	public int getEventID() {
		return eventTransactionID;
	}


	public void setEventID(int eventID) {
		this.eventTransactionID = eventID;
	}


	public String getAnimalTag() {
		return animalTag;
	}


	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}


	public String getEventTimeStampSQLFormat() {
		return super.getDateInSQLFormart(this.getEventTimeStamp());
	}

	public String createUpdateString() {
		String updateString = "";
		updateString = " EVENT_CD='" + this.eventType.getEventCode() + "',";
		if (eventTimeStamp != null)
			updateString += " EVENT_DTTM='" + getEventTimeStampSQLFormat() + "',";
		
		if (this.eventComments != null && !eventComments.isEmpty())
			updateString += " COMMENTS='" + eventComments + "',";
		if (eventOperator != null)	
			updateString += " OPERATOR='" + eventOperator.getPersonID() + "',";
		updateString += super.createUpdateString();
		if (!updateString.isEmpty())
			updateString = updateString.substring(0, updateString.lastIndexOf(","));		
		return updateString;
	}
	
	
}
