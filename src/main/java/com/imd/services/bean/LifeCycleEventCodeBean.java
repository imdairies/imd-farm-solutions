package com.imd.services.bean;


public class LifeCycleEventCodeBean {
	private String eventCode;
	private String eventShortDescription;
	private String eventLongDescription;
	private String activeIndicator;
	private String loginToken;

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	
	public LifeCycleEventCodeBean() {
		
	}
	
	public String getEventCode() {
		return eventCode;
	}
	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
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

	public boolean isActive() {
		return (activeIndicator != null && activeIndicator.equalsIgnoreCase("Y") ? true : false);
	}

	public String getActiveIndicator() {
		return activeIndicator;
	}

	public void setActiveIndicator(String activeIndicator) {
		this.activeIndicator = activeIndicator;
	}


}
