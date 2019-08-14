package com.imd.dto;

import java.util.Iterator;
import java.util.List;

import org.joda.time.format.DateTimeFormatter;

import com.imd.util.Util;

public class FeedPlan extends IMDairiesDTO {

	/**
	 * The cohort to which this plan applies e.g. FEMALECALF, BULL etc.
	 */
	private FeedCohort feedCohort;
	/**
	 * list of all all feed items that will/should be given to this cohort
	 */
	private List<FeedItem> feedPlan;
	
	private Float planDM;
	private Float planCP;
	private Float planME;
	
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
	
	private String stringify(String prefix) {
		String feedItemAnalysisMessages = "";
		String itemsJson = "";
		int count = 0;
		if (feedPlan != null && !feedPlan.isEmpty()) {
			Iterator<FeedItem> it = feedPlan.iterator();
			while (it.hasNext()) {
				count++;
				FeedItem item = it.next();
				feedItemAnalysisMessages += (item.getPersonalizedFeedMessage() == null ? "" : "\n " + item.getPersonalizedFeedMessage());
				itemsJson += "{\n" + item.dtoToJson(prefix,false) + "}" + (feedPlan.size() == count ? "" : ",\n");
			}
		}
		return  prefix + fieldToJson("planAnalysisComments", (planAnalysisComments == null ? "": planAnalysisComments) + feedItemAnalysisMessages ) + ",\n" +
				prefix + fieldToJson("planAchievedDM", (this.planDM == null ? 0f: new Float(Util.formatTwoDecimalPlaces(this.planDM.floatValue())))) + ",\n" +
				prefix + fieldToJson("planAchievedCP", (this.planCP == null ? 0f: new Float(Util.formatTwoDecimalPlaces(this.planCP.floatValue())))) + ",\n" +
				prefix + fieldToJson("planAchievedME", (this.planME == null ? 0f: new Float(Util.formatTwoDecimalPlaces(this.planME.floatValue())))) + ",\n" +
				prefix +"\"feedPlanItems\" :[" + itemsJson + "]";
	}
	
	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + ",\n" + super.dtoToJson(prefix);
	}

	public String dtoToJson(String prefix, boolean appendSuperJson)  {		
		if (appendSuperJson)
			return dtoToJson(prefix);
		else
			return stringify(prefix);
	}
	

	public String dtoToJson(String prefix, boolean appendSuperJson, DateTimeFormatter fmt)  {		
		if (appendSuperJson)
			return dtoToJson(prefix,fmt);
		else
			return stringify(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) +  ",\n" + super.dtoToJson(prefix, fmt));
	}
	public Float getPlanDM() {
		return planDM;
	}
	public void setPlanDM(Float planDM) {
		this.planDM = planDM;
	}
	public Float getPlanCP() {
		return planCP;
	}
	public void setPlanCP(Float planCP) {
		this.planCP = planCP;
	}
	public Float getPlanME() {
		return planME;
	}
	public void setPlanME(Float planME) {
		this.planME = planME;
	}	
	
	
}
