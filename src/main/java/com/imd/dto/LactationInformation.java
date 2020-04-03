package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.imd.util.Util;

public class LactationInformation extends IMDairiesDTO {
	private Integer lactationNumber;
	private DateTime lactationStartTimestamp;
	private DateTime lactationEndTimestamp;
	private Integer inseminationAttemptCount;
	private String calfTag;
	private Float milkingProduction;
	private int daysInMilking;
	private Float lpd;
	private Float maxDailyProduction;
	
	public Integer getLactationNumber() {
		return lactationNumber;
	}
	public void setLactationNumber(Integer lactationNumber) {
		this.lactationNumber = lactationNumber;
	}
	public DateTime getLactationStartTimestamp() {
		return lactationStartTimestamp;
	}
	public void setLactationStartTimestamp(DateTime lactationStartTimestamp) {
		this.lactationStartTimestamp = lactationStartTimestamp;
	}
	public DateTime getLactationEndTimestamp() {
		return lactationEndTimestamp;
	}
	public void setLactationEndTimestamp(DateTime lactationEndTimestamp) {
		this.lactationEndTimestamp = lactationEndTimestamp;
	}
	public Integer getInseminationAttemptCount() {
		return inseminationAttemptCount;
	}
	public void setInseminationAttemptCount(Integer inseminationAttemptCount) {
		this.inseminationAttemptCount = inseminationAttemptCount;
	}
	public String getCalfTag() {
		return calfTag;
	}
	public void setCalfTag(String calfTag) {
		this.calfTag = calfTag;
	}
	public Float getLpd() {
		return lpd;
	}
	public void setLpd(Float lpd) {
		this.lpd = lpd;
	}
	public int getDaysInMilking() {
		return daysInMilking;
	}
	public void setDaysInMilking(int durationInMilking) {
		this.daysInMilking = durationInMilking;
	}
	public Float getMilkingProduction() {
		return milkingProduction;
	}
	public void setMilkingProduction(Float milkingProduction) {
		this.milkingProduction = milkingProduction;
	}

	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("lactationNumber", this.lactationNumber) + ",\n" +
				prefix + fieldToJson("lactationStartTimestamp", this.lactationStartTimestamp) + ",\n" +
				prefix + fieldToJson("lactationEndTimestamp", this.lactationEndTimestamp) + ",\n" +
				prefix + fieldToJson("inseminationAttemptCount", this.inseminationAttemptCount) + ",\n" +
				prefix + fieldToJson("calfTag", this.calfTag) + ",\n" +
				prefix + fieldToJson("milkingProduction", this.milkingProduction) + ",\n" +
				prefix + fieldToJson("maxDailyProduction", Util.formatToSpecifiedDecimalPlaces(this.maxDailyProduction,1)) + ",\n" +
				prefix + fieldToJson("daysInMilking", this.daysInMilking) + ",\n" +
				prefix + fieldToJson("lpd", Util.formatToSpecifiedDecimalPlaces(this.lpd,1)) + ",\n";
	}
	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	
	public String toString() {
		return this.stringify("  ");
	}
	public Float getMaxDailyProduction() {
		return maxDailyProduction;
	}
	public void setMaxDailyProduction(Float maxDailyProduction) {
		this.maxDailyProduction = maxDailyProduction;
	}

}




