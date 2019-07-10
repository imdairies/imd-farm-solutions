package com.imd.dto;

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
	public Float getMaximumFulfillment() {
		return maximumFulfillment;
	}
	public void setMaximumFulfillment(Float maximumFulfimment) {
		this.maximumFulfillment = maximumFulfimment;
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

}
