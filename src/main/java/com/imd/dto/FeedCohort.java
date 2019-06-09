package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class FeedCohort extends IMDairiesDTO{
	private String feedCohortTypeCD;
	private String feedCohortTypeShortDescription;
	private String feedCohortDeterminatationCriteria;
	private String animalFeedCohortDeterminatationMessage;
	public FeedCohort(String orgID, String cohortCD, String cohortShortDescr) {
		this.setOrgID(orgID);
		this.feedCohortTypeCD = cohortCD;		
		this.feedCohortTypeShortDescription = cohortShortDescr;
		
	}
	public String getFeedCohortTypeCD() {
		return feedCohortTypeCD;
	}
	public void setFeedCohortTypeCD(String feedCohortTypeCD) {
		this.feedCohortTypeCD = feedCohortTypeCD;
	}
	public String getFeedCohortTypeShortDescription() {
		return feedCohortTypeShortDescription;
	}
	public void setFeedCohortTypeShortDescription(String feedCohortTypeShortDescription) {
		this.feedCohortTypeShortDescription = feedCohortTypeShortDescription;
	}
	public String getFeedCohortDeterminatationCriteria() {
		return feedCohortDeterminatationCriteria;
	}
	public void setFeedCohortDeterminatationCriteria(String feedCohortDeterminatationCriteria) {
		this.feedCohortDeterminatationCriteria = feedCohortDeterminatationCriteria;
	}
	public String getAnimalFeedCohortDeterminatationMessage() {
		return animalFeedCohortDeterminatationMessage;
	}
	public void setAnimalFeedCohortDeterminatationMessage(String animalFeedCohortDeterminatationMessage) {
		this.animalFeedCohortDeterminatationMessage = animalFeedCohortDeterminatationMessage;
	}

	private String stringify(String prefix) {
		return  prefix + fieldToJson("feedCohortTypeCD", feedCohortTypeCD) + ",\n" + 
				prefix + fieldToJson("feedCohortTypeShortDescription", feedCohortTypeShortDescription == null ? "" : this.feedCohortTypeShortDescription) + ",\n" +
				prefix + fieldToJson("feedCohortDeterminatationCriteria", this.feedCohortDeterminatationCriteria == null ? "" : this.feedCohortDeterminatationCriteria) + ",\n" +
				prefix + fieldToJson("animalFeedCohortDeterminatationMessage", this.animalFeedCohortDeterminatationMessage == null ? "" : this.animalFeedCohortDeterminatationMessage) + ",\n";
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}

}