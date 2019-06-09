package com.imd.dto;

import java.util.List;

public class FeedPlan extends IMDairiesDTO {

	/**
	 * The cohort to which this plan applies e.g. FEMALECALF, BULL etc.
	 */
	private FeedCohort feedCohort;
	/**
	 * list of all all feed items that will/should be given to this cohort
	 */
	private List<FeedItem> feedPlan;
	
	private String planAnalysisComments;
	
	public FeedCohort getFeedCohort() {
		return feedCohort;
	}
	public void setFeedCohort(FeedCohort feedCohort) {
		this.feedCohort = feedCohort;
	}
	public List<FeedItem> getFeedPlan() {
		return feedPlan;
	}
	public void setFeedPlan(List<FeedItem> feedPlan) {
		this.feedPlan = feedPlan;
	}
	public String getPlanAnalysisComments() {
		return planAnalysisComments;
	}
	public void setPlanAnalysisComments(String planAnalysisComments) {
		this.planAnalysisComments = planAnalysisComments;
	}
}
