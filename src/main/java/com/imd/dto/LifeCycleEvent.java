package com.imd.dto;

import java.time.LocalDateTime;
import java.util.HashMap;

import com.imd.util.IMDException;
import com.imd.util.Util;

public class LifeCycleEvent {

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
	 * This field pertains to the instansiation of this event for the particular cow.
	 * This is the transaction id of this event.
	 */
	private int eventTransactionID;
	/**
	 * This field pertains to the instansiation of this event for the particular cow.
	 * Notes/Comments pertaining to this event.
	 */
	private String eventNote;
	/**
	 * This field pertains to the instansiation of this event for the particular cow.
	 * The time stamp of this particular event.
	 */
	private LocalDateTime eventTimeStamp;
	/**
	 * The name of the person who performed this event if applicable.
	 */
	private String eventOperator;


	public LifeCycleEvent(String lifeCycleEventID, int eventTransID) throws IMDException{
		Util.throwExceptionIfNullOrEmpty(lifeCycleEventID, " LifeCycle Event ID"); 
		this.eventCode = lifeCycleEventID;
		this.eventTransactionID = eventTransID;
	}


	/**
	 * The comma separated string that will populate majority of the fields of this object.
	 * The string should be in the following format:
	 * Event Primary Key, short description, long description, instance id, instance timestamp, operator, comments
	 * If the string has a comma in it then it needs to be properly escaped.
	 * Example:
	 * 1,De-worming,Usual treatment is to use Oxafandole or Thunder de-wormer,1,2016-12-01T00:00:00,Kashif Manzoor,Levamisole salt was used")	 * 
	 * @param commaSeparatedValues
	 * 
	 */
	public LifeCycleEvent(String commaSeparatedValues) throws IMDException{
		String parsingProgress = "starting";
		try
		{
            boolean spaceAdded = false;
	    		if (commaSeparatedValues.endsWith(",") && !commaSeparatedValues.endsWith("\\,"))  {
	    			// If the last field is empty, this would result in the split function to 
	    			// ignore this field and conclude that there is one less value. This is why we have to add a suffix to 
	    			// ensure the split function does not lead to erroneous results. The second check
	    			// is needed in case we have a legitimate last field whose last character is a comma ...
	    			// in which case the comma will be escaped.
	    			commaSeparatedValues = commaSeparatedValues + " ";
	    			spaceAdded = true;
	    		}
			
			String [] information = commaSeparatedValues.split("(?<!\\\\),");
			parsingProgress = "Split on comma";
			System.out.println("//////////[" + information.length + "]/////////");
			if (information == null || information.length !=7 ) {
				throw new IMDException ("The Life Cycle Event CSV record should have exactly 7 fields. This particular record does not have exactly 7 fields");
			}
			this.eventCode =information[0];
			parsingProgress = "<Event Code> was successfully parsed with the value: " + this.eventCode;
			this.eventShortDescription =  Util.removeEscapeCharacter("\\\\",information[1]);
			parsingProgress = "<eventShortDescription> was successfully parsed with the value: " + this.eventShortDescription;
			this.eventLongDescription =   Util.removeEscapeCharacter("\\\\",information[2]);
			parsingProgress = "<eventLongDescription> was successfully parsed with the value: "  + this.eventLongDescription;
			this.eventTransactionID =  Integer.parseInt(information[3]);
			parsingProgress = "<eventTransactionID> was successfully parsed with the value:  " + this.eventTransactionID;
			this.eventTimeStamp = LocalDateTime.parse(information[4]);
			parsingProgress = "<eventTimeStamp> was successfully parsed with the value: " + this.eventTimeStamp;
			this.eventOperator =  Util.removeEscapeCharacter("\\\\",information[5]);
			parsingProgress = "<eventOperator> was successfully parsed with the value: " + this.eventOperator;
			String note =  Util.removeEscapeCharacter("\\\\",information[6]);
	    		if (spaceAdded) {
	    			// since we added a space, we now have to undo that action.
	    			this.eventNote = note.substring(0, note.length()-1);
	    		}
	    		else 
	    			this.eventNote = note;
			parsingProgress = "<eventNotes> was successfully parsed with the value:" + this.eventNote;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IMDException(ex.getMessage() + ". Exception was encountered right after the field " + parsingProgress);
		}
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
}
