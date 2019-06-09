package com.imd.services.bean;

public class LifeCycleEventBean {
	private String eventTransactionID;
	private String orgID;
	private String eventCode;
	private String animalTag;
	private String eventTimeStamp;
	private String operatorID;
	private String eventComments;
	private String auxField1Value;
	private String auxField2Value;
	private String auxField3Value;
	private String auxField4Value;
	private String shouldUpdateInventory;
	private String nextLifeCycleStage;
	
	public LifeCycleEventBean() {
		
	}
	public String getEventCode() {
		return eventCode;
	}
	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}
	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	public String getEventTimeStamp() {
		return eventTimeStamp;
	}
	public void setEventTimeStamp(String eventTimeStamp) {
		this.eventTimeStamp = eventTimeStamp;
	}
	public String getOperatorID() {
		return operatorID;
	}
	public void setOperatorID(String operatorID) {
		this.operatorID = operatorID;
	}
	public String getEventComments() {
		return eventComments;
	}
	public void setEventComments(String eventComments) {
		this.eventComments = eventComments;
	}
	public String getAuxField1Value() {
		return auxField1Value;
	}
	public void setAuxField1Value(String auxField1Value) {
		this.auxField1Value = auxField1Value;
	}
	public String getAuxField2Value() {
		return auxField2Value;
	}
	public void setAuxField2Value(String auxField2Value) {
		this.auxField2Value = auxField2Value;
	}
	public void setAuxField3Value(String auxField3Value) {
		this.auxField3Value = auxField3Value;
	}
	public String getAuxField3Value() {
		return auxField3Value;
	}
	public String getOrgID() {
		return this.orgID;
	}
	public String setOrgID(String orgID) {
		return this.orgID = orgID;
	}
	public String toString() {
	
		return "\norgID : " + orgID + 
			"\neventCode : " + eventCode + 
			"\neventTransactionID : " + eventTransactionID + 
			"\nanimalTag : " + animalTag + 
			"\nshouldUpdateInventory : " + shouldUpdateInventory + 
			"\nauxField1Value : " + auxField1Value + 
			"\nauxField2Value : " + auxField2Value + 
			"\nauxField3Value : " + auxField3Value + 
			"\nauxField4Value : " + auxField4Value + 
			"\neventTimeStamp : " + eventTimeStamp + 
			"\noperatorID : " + operatorID + 
			"\neventComments : " + eventComments +
			"\nextLifeCycleStage : " + nextLifeCycleStage;
	}
	public String getEventTransactionID() {
		return eventTransactionID;
	}
	public void setEventTransactionID(String eventTransactionID) {
		this.eventTransactionID = eventTransactionID;
	}
	public String getNextLifeCycleStage() {
		return nextLifeCycleStage;
	}
	public void setNextLifeCycleStage(String nextLifeCycleStage) {
		this.nextLifeCycleStage = nextLifeCycleStage;
	}
	public String getAuxField4Value() {
		return auxField4Value;
	}
	public void setAuxField4Value(String auxField4Value) {
		this.auxField4Value = auxField4Value;
	}
	public String getShouldUpdateInventory() {
		return shouldUpdateInventory;
	}
	public void setShouldUpdateInventory(String update) {
		this.shouldUpdateInventory = update;
	}
	
	
}
