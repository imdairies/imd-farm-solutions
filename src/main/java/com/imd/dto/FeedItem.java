package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class FeedItem extends IMDairiesDTO {
	private LookupValues feedCohortLookupValue;
//	private LookupValues feedCohortCD;
	private LookupValues feedItemLookupValue;
	private Float start;
	private Float end;
	private Float minimumFulfillment;
	private Float fulfillmentPct;
	private Float maximumFulfillment;
	private String fulFillmentTypeCD;
	private String units;
	private Integer dailyFrequency;
	private String comments;
	private String personalizedFeedMessage;
	private Float personalizedQuantityToFeed;
	private FeedItemNutritionalStats feedItemNutritionalStats;
	private Float dailyIntake;
	private String intakeType;

	public LookupValues getFeedItemLookupValue() {
		return feedItemLookupValue;
	}
	public void setFeedItemLookupValue(LookupValues feedItem) {
		this.feedItemLookupValue = feedItem;
	}
	public LookupValues getFeedCohortCD() {
		return feedCohortLookupValue;
	}
	public void setFeedCohortCD(LookupValues feedCohort) {
		this.feedCohortLookupValue = feedCohort;
	}
	public Float getStart() {
		return start;
	}
	public void setStart(Float start) {
		this.start = start;
	}
	public Float getEnd() {
		return end;
	}
	public void setEnd(Float end) {
		this.end = end;
	}
	public Float getFulfillmentPct() {
		return fulfillmentPct;
	}
	public void setFulfillmentPct(Float fulfillmentPct) {
		this.fulfillmentPct = fulfillmentPct;
	}
	public String getFulFillmentTypeCD() {
		return fulFillmentTypeCD;
	}
	public void setFulFillmentTypeCD(String fulFillmentTypeCD) {
		this.fulFillmentTypeCD = fulFillmentTypeCD;
	}
	public FeedItemNutritionalStats getFeedItemNutritionalStats() {
		return feedItemNutritionalStats;
	}
	public void setFeedItemNutritionalStats(FeedItemNutritionalStats feedItemNutritionalStats) {
		this.feedItemNutritionalStats = feedItemNutritionalStats;
	}
	public Float getMinimumFulfillment() {
		return minimumFulfillment;
	}
	public void setMinimumFulfillment(Float minimumFulfimment) {
		this.minimumFulfillment = minimumFulfimment;
	}
	public void setMinimumFulfillment(String minFulfillment) {
		if (minFulfillment !=  null)
			setMinimumFulfillment(new Float(minFulfillment));
		else
			this.minimumFulfillment = null;
	}
	public Float getMaximumFulfillment() {
		return maximumFulfillment;
	}
	public void setMaximumFulfillment(Float maximumFulfimment) {
		this.maximumFulfillment = maximumFulfimment;
	}
	public void setMaximumFulfillment(String maxFulfillment) {
		if (maxFulfillment !=  null)
			setMaximumFulfillment(new Float(maxFulfillment));
		else
			this.maximumFulfillment = null;
	}
	public Integer getDailyFrequency() {
		return dailyFrequency;
	}
	public void setDailyFrequency(Integer dailyFrequency) {
		this.dailyFrequency = dailyFrequency;
	}
	public void setDailyFrequency(String strDailyFrequency) {
		this.dailyFrequency = (strDailyFrequency == null || strDailyFrequency.isEmpty() ? null: Integer.parseInt(strDailyFrequency));
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getPersonalizedFeedMessage() {
		return personalizedFeedMessage;
	}
	public void setPersonalizedFeedMessage(String personalizedFeedMessage) {
		this.personalizedFeedMessage = personalizedFeedMessage;
	}
	public Float getPersonalizedQuantityToFeed() {
		return personalizedQuantityToFeed;
	}
	public void setPersonalizedQuantityToFeed(Float personalizedQuantityToFeed) {
		this.personalizedQuantityToFeed = personalizedQuantityToFeed;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + ",\n" + super.dtoToJson(prefix);
	}

//	private String stringify(String prefix) {
//		return  (feedItemLookupValue != null ? feedItemLookupValue.dtoToJson(prefix) + ",\n" : "") + 
//				prefix + fieldToJson("units",units) + ",\n" + 
//				prefix + fieldToJson("dailyIntake",dailyIntake);
//	}
	private String stringify(String prefix) {
		return  (feedItemLookupValue != null ? feedItemLookupValue.dtoToJson(prefix) + ",\n" : "") + 
				prefix + fieldToJson("start",start) + ",\n" + 
				prefix + fieldToJson("end",end) + ",\n" + 
				prefix + fieldToJson("minimumFulfillment",minimumFulfillment) + ",\n" + 
				prefix + fieldToJson("fulfillmentPct",fulfillmentPct) + ",\n" + 
				prefix + fieldToJson("maximumFulfillment",maximumFulfillment) + ",\n" + 
				prefix + fieldToJson("fulFillmentTypeCD",fulFillmentTypeCD) + ",\n" + 
				prefix + fieldToJson("units",units) + ",\n" + 
				prefix + fieldToJson("dailyFrequency",dailyFrequency) + ",\n" + 
				prefix + fieldToJson("comments",comments) + ",\n" + 
				prefix + fieldToJson("intakeType",intakeType) + ",\n" + 
				prefix + fieldToJson("dailyIntake",dailyIntake);
	}
	public String dtoToJson(String prefix, boolean appendSuperJson)  {		
		if (appendSuperJson)
			return dtoToJson(prefix);
		else
			return stringify(prefix);
	}
	
	public String toString() {
		return stringify("");
	}

	public String dtoToJson(String prefix, boolean appendSuperJson, DateTimeFormatter fmt)  {		
		if (appendSuperJson)
			return dtoToJson(prefix,fmt);
		else
			return stringify(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + ",\n" + super.dtoToJson(prefix, fmt));
	}
	public Float getDailyIntake() {
		return dailyIntake;
	}
	public void setDailyIntake(Float dailyIntake) {
		this.dailyIntake = dailyIntake;
	}
	public String getIntakeType() {
		return intakeType;
	}
	public void setIntakeType(String intakeType) {
		this.intakeType = intakeType;
	}	
	
	
}
