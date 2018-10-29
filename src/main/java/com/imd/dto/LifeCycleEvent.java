package com.imd.dto;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.util.IMDException;
import com.imd.util.Util;

public class LifeCycleEvent {
	
	private String orgCode;
	
	private Person createdBy;
	
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

	/**
	 * This field pertains to the instantiation of this event for the particular cow.
	 * This is the transaction id of this event.
	 */
	private int eventTransactionID;
	/**
	 * This field pertains to the instantiation of this event for the particular cow.
	 * Notes/Comments pertaining to this event.
	 */
	private String eventNote;
	/**
	 * This field pertains to the instantiation of this event for the particular cow.
	 * The time stamp of this particular event.
	 */
	private LocalDateTime eventTimeStamp;
	/**
	 * The name of the person who performed this event if applicable.
	 */
	private String eventOperator;

	private Person updatedBy;

	private DateTime updatedDTTM;

	private DateTime createdDTTM;

	private boolean isActive;


	public LifeCycleEvent(String organization, String lifeCycleEventCode, String shortDescr, String longDescr) throws IMDException{
		Util.throwExceptionIfNullOrEmpty(lifeCycleEventCode, " LifeCycleEvent Code"); 
		this.eventCode = lifeCycleEventCode;
		this.eventShortDescription = shortDescr;
		this.eventLongDescription = longDescr;
		this.setOrgCode(organization);
	}


//	/**
//	 * The comma separated string that will populate majority of the fields of this object.
//	 * The string should be in the following format:
//	 * Event Primary Key, short description, long description, instance id, instance timestamp, operator, comments
//	 * If the string has a comma in it then it needs to be properly escaped.
//	 * Example:
//	 * 1,De-worming,Usual treatment is to use Oxafandole or Thunder de-wormer,1,2016-12-01T00:00:00,Kashif Manzoor,Levamisole salt was used")	 * 
//	 * @param commaSeparatedValues
//	 * 
//	 */
//	public LifeCycleEvent(String commaSeparatedValues) throws IMDException{
//		String parsingProgress = "starting";
//		try
//		{
//            boolean spaceAdded = false;
//	    		if (commaSeparatedValues.endsWith(",") && !commaSeparatedValues.endsWith("\\,"))  {
//	    			// If the last field is empty, this would result in the split function to 
//	    			// ignore this field and conclude that there is one less value. This is why we have to add a suffix to 
//	    			// ensure the split function does not lead to erroneous results. The second check
//	    			// is needed in case we have a legitimate last field whose last character is a comma ...
//	    			// in which case the comma will be escaped.
//	    			commaSeparatedValues = commaSeparatedValues + " ";
//	    			spaceAdded = true;
//	    		}
//			
//			String [] information = commaSeparatedValues.split("(?<!\\\\),");
//			parsingProgress = "Split on comma";
//			System.out.println("//////////[" + information.length + "]/////////");
//			if (information == null || information.length !=7 ) {
//				throw new IMDException ("The Life Cycle Event CSV record should have exactly 7 fields. This particular record does not have exactly 7 fields");
//			}
//			this.eventCode =information[0];
//			parsingProgress = "<Event Code> was successfully parsed with the value: " + this.eventCode;
//			this.eventShortDescription =  Util.removeEscapeCharacter("\\\\",information[1]);
//			parsingProgress = "<eventShortDescription> was successfully parsed with the value: " + this.eventShortDescription;
//			this.eventLongDescription =   Util.removeEscapeCharacter("\\\\",information[2]);
//			parsingProgress = "<eventLongDescription> was successfully parsed with the value: "  + this.eventLongDescription;
//			this.eventTransactionID =  Integer.parseInt(information[3]);
//			parsingProgress = "<eventTransactionID> was successfully parsed with the value:  " + this.eventTransactionID;
//			this.eventTimeStamp = LocalDateTime.parse(information[4]);
//			parsingProgress = "<eventTimeStamp> was successfully parsed with the value: " + this.eventTimeStamp;
//			this.eventOperator =  Util.removeEscapeCharacter("\\\\",information[5]);
//			parsingProgress = "<eventOperator> was successfully parsed with the value: " + this.eventOperator;
//			String note =  Util.removeEscapeCharacter("\\\\",information[6]);
//	    		if (spaceAdded) {
//	    			// since we added a space, we now have to undo that action.
//	    			this.eventNote = note.substring(0, note.length()-1);
//	    		}
//	    		else 
//	    			this.eventNote = note;
//			parsingProgress = "<eventNotes> was successfully parsed with the value:" + this.eventNote;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			throw new IMDException(ex.getMessage() + ". Exception was encountered right after the field " + parsingProgress);
//		}
//	}


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


	public String getEventNote() {
		return eventNote;
	}


	public void setEventNote(String eventNotes) {
		this.eventNote = eventNotes;
	}


	public LocalDateTime getEventTimeStamp() {
		return eventTimeStamp;
	}


	public void setEventTimeStamp(LocalDateTime eventDateTime) {
		this.eventTimeStamp = eventDateTime;
	}


	public int getEventTransactionID() {
		return eventTransactionID;
	}

	public void setEventTransactionID(int eventTransactionID) {
		this.eventTransactionID = eventTransactionID;
	}

	public String getEventOperator() {
		return eventOperator;
	}

	public void setEventOperator(String eventOperator) {
		this.eventOperator = eventOperator;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public Person getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Person user) {
		this.createdBy = user;
	}

	public Person getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(Person user) {
		this.updatedBy = user;
	}

	public org.joda.time.DateTime getCreatedDTTM() {
		return createdDTTM;
	}
	public void setCreatedDTTM(DateTime createdDateTime) {
		this.createdDTTM = createdDateTime;
	}

	public org.joda.time.DateTime getUpdatedDTTM() {
		return updatedDTTM;
	}
	public void setUpdatedDTTM(DateTime updatedDateTime) {
		this.updatedDTTM = updatedDateTime;
	}


	public String getCreatedDTTMSQLFormat() {		
		return getDateInSQLFormart(createdDTTM);
	}

	public String getUpdatedDTTMSQLFormat() {		
		return getDateInSQLFormart(updatedDTTM);
	}
	
	private String getDateInSQLFormart(DateTime dttm) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return fmt.print(dttm);
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
		if (createdBy != null && !createdBy.getUserID().isEmpty())
			updateString += " CREATED_BY='" + createdBy.getUserID() + "',";
		if (updatedBy != null && !updatedBy.getUserID().isEmpty())
			updateString += " UPDATED_BY='" + updatedBy.getUserID() + "',";
		if (createdDTTM != null)
			updateString += " CREATED_DTTM='" + getCreatedDTTMSQLFormat() + "',";
		if (updatedDTTM != null)
			updateString += " UPDATED_DTTM='" + getUpdatedDTTMSQLFormat() + "',";
		if (!updateString.isEmpty())
			updateString = updateString.substring(0, updateString.lastIndexOf(","));
		
		return updateString;
	}
	
	public String toString() {
		return "ORG_ID=" + this.orgCode + 
				"\nEVENT_CD=" + this.eventCode + 
				"\nACTIVE_IND=" + this.isActive +
				"\nSHORT_DESCR=" + this.eventShortDescription + 
				"\nLONG_DESCR=" + this.eventLongDescription +
				"\nCREATED_BY=" + this.createdBy.getUserID() + 
				"\nREATED_DTTM=" + this.createdDTTM  +
				"\nUPDATED_BY=" + this.updatedBy.getUserID() + 
				"\nUPDATED_DTTM=" + this.updatedDTTM;
	}
}







