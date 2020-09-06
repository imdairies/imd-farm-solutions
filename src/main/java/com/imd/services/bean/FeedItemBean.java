package com.imd.services.bean;


public class FeedItemBean {
	private String feedItemCD;
	private Float minimumFulfillment;
	private Float fulfillmentPct;
	private Float maximumFulfillment;
	private String fulFillmentTypeCD;
	private String units;
	
	
	public String toString() {
		String stringifiedValue = 	
				"\n feedItemCD:" + feedItemCD + 
				"\n minimumFulfillment:" + minimumFulfillment + 
				"\n fulfillmentPct: " + fulfillmentPct +
				"\n maximumFulfillment: " + maximumFulfillment + 
				"\n fulFillmentTypeCD: " + fulFillmentTypeCD + 				
				"\n units: " + units;		
		return stringifiedValue;
	}	
		
	
	
	public String getFeedItemCD() {
		return feedItemCD;
	}
	public void setFeedItemCD(String feedItemCD) {
		this.feedItemCD = feedItemCD;
	}
	public Float getMinimumFulfillment() {
		return minimumFulfillment;
	}
	public void setMinimumFulfillment(Float minimumFulfillment) {
		this.minimumFulfillment = minimumFulfillment;
	}
	public Float getFulfillmentPct() {
		return fulfillmentPct;
	}
	public void setFulfillmentPct(Float fulfillmentPct) {
		this.fulfillmentPct = fulfillmentPct;
	}
	public Float getMaximumFulfillment() {
		return maximumFulfillment;
	}
	public void setMaximumFulfillment(Float maximumFulfillment) {
		this.maximumFulfillment = maximumFulfillment;
	}
	public String getFulFillmentTypeCD() {
		return fulFillmentTypeCD;
	}
	public void setFulFillmentTypeCD(String fulFillmentTypeCD) {
		this.fulFillmentTypeCD = fulFillmentTypeCD;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}

}
