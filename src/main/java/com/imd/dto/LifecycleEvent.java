package com.imd.dto;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDException;
import com.imd.util.Util;

public class LifecycleEvent extends IMDairiesDTO{
	private int eventTransactionID;
	private LifeCycleEventCode eventType;
	private String animalTag;
	private String eventComments;
	private DateTime eventTimeStamp;
	private Person eventOperator;
	private String auxField1Value;
	private String auxField2Value;
	private String auxField3Value;
	

	public LifecycleEvent(String orgID, int transactionID, String animalTag, String eventCode) throws IMDException {
		this.setOrgID(orgID);
		this.eventTransactionID = transactionID;
		this.animalTag = animalTag;
		this.eventType = new LifeCycleEventCode(eventCode, "", "");
	}


	public LifecycleEvent(LifeCycleEventBean eventBean) throws IMDException {
		this(eventBean,null);
	}

	public LifecycleEvent(LifeCycleEventBean eventBean, String dateTimeFormat) throws IMDException {
		
		if (dateTimeFormat == null || dateTimeFormat.trim().isEmpty())
			dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
		this.setOrgID(eventBean.getOrgID());
		this.animalTag = eventBean.getAnimalTag();
		this.auxField1Value = eventBean.getAuxField1Value();
		this.auxField2Value = eventBean.getAuxField2Value();
		this.auxField3Value = eventBean.getAuxField3Value();
		this.eventComments = eventBean.getEventComments();
		this.eventType = new LifeCycleEventCode(eventBean.getEventCode(),null,null);
		this.setEventTimeStamp(eventBean.getEventTimeStamp() == null ? null : DateTime.parse(eventBean.getEventTimeStamp(), DateTimeFormat.forPattern(dateTimeFormat)));
//		this.setEventTimeStamp(eventBean.getEventTimeStamp() == null ? null : DateTime.parse(eventBean.getEventTimeStamp()));
		this.eventOperator = new Person(eventBean.getOperatorID(),null,null,null);
		this.eventTransactionID = (eventBean.getEventTransactionID() != null ? Integer.parseInt(eventBean.getEventTransactionID()) : 0);
		
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
		
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix, fmt) + super.dtoToJson(prefix, fmt));
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix, null) + super.dtoToJson(prefix);
	}
	
	
	public String stringify(String prefix, DateTimeFormatter fmt)  {
		String json = prefix + fieldToJson("orgID", this.getOrgID()) + ",\n" +
				prefix + fieldToJson("eventTransactionID",this.eventTransactionID) + ",\n" + 
				prefix + fieldToJson("eventCode", this.eventType.getEventCode()) + ",\n" + 
				prefix + fieldToJson("eventShortDescription", this.eventType.getEventShortDescription()) + ",\n" + 				
				prefix + fieldToJson("animalTag", this.animalTag) + ",\n" + 
				prefix + fieldToJson("eventComments", this.eventComments) + ",\n";
		if (fmt == null) 
			json += prefix + fieldToJson("eventTimeStamp",this.eventTimeStamp) + ",\n";
		else 
			json += prefix + fieldToJson("eventTimeStamp",getDateInSQLFormart(this.eventTimeStamp, fmt)) + ",\n";
		json += prefix + fieldToJson("eventOperator", this.eventOperator) + ",\n" +
				prefix + fieldToJson("auxField1Value", this.auxField1Value) + ",\n" +
				prefix + fieldToJson("auxField2Value", this.eventOperator) + ",\n" +
				prefix + fieldToJson("auxField3Value", this.auxField3Value) + ",\n";
		return json;
	}


	public void setAuxField1Value(String value) {
		this.auxField1Value = value;
	}
	public void setAuxField2Value(String value) {
		this.auxField2Value = value;
	}
	public void setAuxField3Value(String value) {
		this.auxField3Value = value;
	}
	
	

	public String getAuxField1Value() {
		return this.auxField1Value;
	}

	public String getAuxField2Value() {
		return this.auxField2Value;
	}

	public String getAuxField3Value() {
		return this.auxField3Value;
	}
}
