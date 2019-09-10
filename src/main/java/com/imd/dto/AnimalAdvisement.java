package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class AnimalAdvisement extends IMDairiesDTO {
	private String animalTag;
	private String severityLevel;
	private String ruleOutcomeLongMessage;
	private String appliedAdvisementRule;
	private String animalSpecificMessage;
	
	public AnimalAdvisement(String animalTag2, String severityThreshold1, String longNote, String animalSpecificMessage, String advisementRule) {
		this.animalTag = animalTag2;
		this.severityLevel = severityThreshold1;
		this.ruleOutcomeLongMessage = longNote;
		this.appliedAdvisementRule = advisementRule;
		this.animalSpecificMessage = animalSpecificMessage;
	}
	public AnimalAdvisement(AnimalAdvisement cloneFrom) {
		 this(cloneFrom.getAnimalTag(),cloneFrom.getSeverityLevel(),cloneFrom.getRuleOutcomeLongMessage(),cloneFrom.getAnimalSpecificMessage(),cloneFrom.getAppliedAdvisementRule());
	}
	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	public String getSeverityLevel() {
		return severityLevel;
	}
	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}
	public String getRuleOutcomeLongMessage() {
		return ruleOutcomeLongMessage;
	}
	public void setRuleOutcomeLongMessage(String ruleOutcomeLongMessage) {
		this.ruleOutcomeLongMessage = ruleOutcomeLongMessage;
	}
	public String getAppliedAdvisementRule() {
		return appliedAdvisementRule;
	}
	public void setAppliedAdvisementRule(String appliedAdvisementRule) {
		this.appliedAdvisementRule = appliedAdvisementRule;
	}
	
	public String toString() {
		return stringify(" ");
	}

	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("animalTag", this.animalTag) + ",\n" +
				prefix + fieldToJson("severityLevel", this.severityLevel) + ",\n" +
				prefix + fieldToJson("ruleOutcomeLongMessage", this.ruleOutcomeLongMessage) + ",\n" +
				prefix + fieldToJson("appliedAdvisementRule", this.appliedAdvisementRule) + "\n" ;
	}
	

	public String dtoToJson(String prefix)  {
//		return stringify(prefix) + super.dtoToJson(prefix);
		return stringify(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
//		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
		return stringify(prefix);
	}
	public String getAnimalSpecificMessage() {
		return animalSpecificMessage;
	}
	public void setAnimalSpecificMessage(String animalSpecificMessage) {
		this.animalSpecificMessage = animalSpecificMessage;
	}	
	
}
