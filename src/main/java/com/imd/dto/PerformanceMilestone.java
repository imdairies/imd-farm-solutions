package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

public class PerformanceMilestone extends IMDairiesDTO {
	private String milestoneID;
	private String shortDescription;
	private String longDescription;
	private String shortDescriptionCd;
	private String longDescriptionCd;
	private boolean isEnabled;
	private boolean isEnabledForOrg;
	private Float oneStarThreshold;
	private Float twoStarThreshold;
	private Float threeStarThreshold;
	private Float fourStarThreshold;
	private Float fiveStarThreshold;
	private String auxInfo1;
	private String auxInfo2;
	private String auxInfo3;
	private String auxInfo4;
	private String auxInfo5;
	private Float starRating;
	private String animalTag;
	private String evaluationValue;
	private String evaluationResultMessage;
	
	public String getMilestoneID() {
		return milestoneID;
	}
	public void setMilestoneID(String milestoneID) {
		this.milestoneID = milestoneID;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
	public String getShortDescriptionCd() {
		return shortDescriptionCd;
	}
	public void setShortDescriptionCd(String shortDescriptionCd) {
		this.shortDescriptionCd = shortDescriptionCd;
	}
	public String getLongDescriptionCd() {
		return longDescriptionCd;
	}
	public void setLongDescriptionCd(String longDescriptionCd) {
		this.longDescriptionCd = longDescriptionCd;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public Float getTwoStarThreshold() {
		return twoStarThreshold;
	}
	public void setTwoStarThreshold(Float twoStarThreshold) {
		this.twoStarThreshold = twoStarThreshold;
	}
	public String getAuxInfo5() {
		return auxInfo5;
	}
	public void setAuxInfo5(String auxInfo5) {
		this.auxInfo5 = auxInfo5;
	}
	public String getAuxInfo4() {
		return auxInfo4;
	}
	public void setAuxInfo4(String auxInfo4) {
		this.auxInfo4 = auxInfo4;
	}
	public String getAuxInfo3() {
		return auxInfo3;
	}
	public void setAuxInfo3(String auxInfo3) {
		this.auxInfo3 = auxInfo3;
	}
	public String getAuxInfo2() {
		return auxInfo2;
	}
	public void setAuxInfo2(String auxInfo2) {
		this.auxInfo2 = auxInfo2;
	}
	public String getAuxInfo1() {
		return auxInfo1;
	}
	public void setAuxInfo1(String auxInfo1) {
		this.auxInfo1 = auxInfo1;
	}
	public Float getFiveStarThreshold() {
		return fiveStarThreshold;
	}
	public void setFiveStarThreshold(Float fiveStarThreshold) {
		this.fiveStarThreshold = fiveStarThreshold;
	}
	public Float getFourStarThreshold() {
		return fourStarThreshold;
	}
	public void setFourStarThreshold(Float fourStarThreshold) {
		this.fourStarThreshold = fourStarThreshold;
	}
	public Float getThreeStarThreshold() {
		return threeStarThreshold;
	}
	public void setThreeStarThreshold(Float threeStarThreshold) {
		this.threeStarThreshold = threeStarThreshold;
	}
	public Float getOneStarThreshold() {
		return oneStarThreshold;
	}
	public void setOneStarThreshold(Float oneStarThreshold) {
		this.oneStarThreshold = oneStarThreshold;
	}
	public boolean isEnabledForOrg() {
		return isEnabledForOrg;
	}
	public void setEnabledForOrg(boolean isEnabledForOrg) {
		this.isEnabledForOrg = isEnabledForOrg;
	}

	public String toString() {
		return stringify(" ");
	}
	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("milestoneID", this.milestoneID) + ",\n" +
				prefix + fieldToJson("shortDescription", this.shortDescription) + ",\n" +
				prefix + fieldToJson("longDescription", this.longDescription) + ",\n" +
				prefix + fieldToJson("shortDescriptionCd", this.shortDescriptionCd) + ",\n" +
				prefix + fieldToJson("longDescriptionCd", this.longDescriptionCd) + ",\n" +
				prefix + fieldToJson("isEnabled", this.isEnabled) + ",\n" + 
				prefix + fieldToJson("isEnabledForOrg", this.isEnabledForOrg) + ",\n" + 
				prefix + fieldToJson("oneStarThreshold", this.oneStarThreshold) + ",\n" + 
				prefix + fieldToJson("twoStarThreshold", this.twoStarThreshold) + ",\n" + 
				prefix + fieldToJson("threeStarThreshold", this.threeStarThreshold) + ",\n" + 
				prefix + fieldToJson("fourStarThreshold", this.fourStarThreshold) + ",\n" +
				prefix + fieldToJson("fiveStarThreshold", this.fiveStarThreshold) + ",\n" +				
				prefix + fieldToJson("starRating", this.starRating) + ",\n" +
				prefix + fieldToJson("auxInfo1", this.auxInfo1) + ",\n" +
				prefix + fieldToJson("auxInfo2", this.auxInfo2) + ",\n" +
				prefix + fieldToJson("auxInfo3", this.auxInfo3) + ",\n" +
				prefix + fieldToJson("auxInfo4", this.auxInfo4) + ",\n" +
				prefix + fieldToJson("auxInfo5", this.auxInfo5) + ",\n" +
				prefix + fieldToJson("evaluationValue", this.evaluationValue) + ",\n" +
				prefix + fieldToJson("evaluationResultMessage", this.evaluationResultMessage) + ",\n";
	}
	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	public Float getStarRating() {
		return this.starRating;
	}
	public Float setStarRating(Float starRating) {
		return this.starRating = starRating;
	}
	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	public String getEvaluationValue() {
		return evaluationValue;
	}
	public void setEvaluationValue(String evaluationValue) {
		this.evaluationValue = evaluationValue;
	}
	public String getEvaluationResultMessage() {
		return evaluationResultMessage;
	}
	public void setEvaluationResultMessage(String evaluationResultMessage) {
		this.evaluationResultMessage = evaluationResultMessage;
	}

}









