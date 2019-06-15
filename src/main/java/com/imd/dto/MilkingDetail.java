package com.imd.dto;


import java.util.HashMap;
import java.util.Iterator;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.MilkingDetailBean;
import com.imd.util.IMDException;

public class MilkingDetail extends IMDairiesDTO{
	private String animalTag;
	private String orgID;
	private float averageDailyProduction;
	private short dailyMilkingFrequency;
	private boolean isMilkedOnMachine;
	private LocalDate recordDate;
	private LocalTime recordTime;
	private Float milkVolume;
	private String volUnit;
	private Float lrValue;
	private Float fatValue;
	private Float toxinValue;
	private Float temperatureInCentigrade;
	private Float humidity;
	private String comments;
	private HashMap<String, Float> additionalStatistics = new HashMap<String, Float>();
	private short milkingEventNumber;

	private String stringify(String prefix) {
		String retValue =  prefix + fieldToJson("orgID", this.orgID) + ",\n" + 
				prefix + fieldToJson("animalTag", this.animalTag) + ",\n" +
				prefix + fieldToJson("milkingEventNumber", this.getMilkingEventNumber()) + ",\n" + 
				prefix + fieldToJson("recordDate", this.recordDate) + ",\n" + 
				prefix + fieldToJson("recordTime", this.recordTime) + ",\n" + 
				prefix + fieldToJson("dailyMilkingFrequency", this.getDailyMilkingFrequency()) + ",\n" + 
				prefix + fieldToJson("isMilkedOnMachine", this.isMilkedOnMachine) + ",\n" + 
				prefix + fieldToJson("milkVolume", this.milkVolume) + ",\n" + 
				prefix + fieldToJson("volUnit", this.volUnit) + ",\n" + 
				prefix + fieldToJson("lrValue", this.lrValue == null ? 0: this.lrValue) + ",\n" + 
				prefix + fieldToJson("fatValue", this.fatValue == null ? 0: this.fatValue) + ",\n" + 
				prefix + fieldToJson("toxinValue", this.toxinValue == null ? 0: this.toxinValue) + ",\n" + 
				prefix + fieldToJson("temperatureInCentigrade", this.temperatureInCentigrade == null ? 0 : this.temperatureInCentigrade) + ",\n" + 
				prefix + fieldToJson("humidity", this.humidity == null ? 0:this.humidity) + ",\n" + 
				prefix + fieldToJson("comments", this.comments) + ",\n";
		if (additionalStatistics != null) {
			Iterator<String> additionalValues = additionalStatistics.keySet().iterator();
			while(additionalValues.hasNext()) {
				String key = additionalValues.next();
				Float value = additionalStatistics.get(key);
				retValue += prefix + fieldToJson(key, value) + ",\n";
			}
		}
		return retValue;
	}

	public String toString() {
		return  stringify(" ");
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
 
	
	public MilkingDetail(String orgID, String tagNbr, short frequency, boolean machineMilked, LocalDate recDate, LocalTime recTime, Float milkVol, short milkSeqNbr)  throws IMDException {
		this.dailyMilkingFrequency = frequency;
		this.isMilkedOnMachine = machineMilked;
		this.recordDate = recDate;
		this.recordTime= recTime;
		this.milkVolume = milkVol;
		this.milkingEventNumber = milkSeqNbr;
		this.orgID = orgID;
		this.animalTag = tagNbr;
	}
	
	
	public MilkingDetail() {
	}
	
	public float getAverageDailyProduction() {
		return averageDailyProduction;
	}
	public void setAverageDailyProduction(float averageDailyProduction) {
		this.averageDailyProduction = averageDailyProduction;
	}
	public short getDailyMilkingFrequency() {
		return dailyMilkingFrequency;
	}
	public void setDailyMilkingFrequency(short dailyMilkingFrequency) {
		this.dailyMilkingFrequency = dailyMilkingFrequency;
	}
	public boolean isMilkedOnMachine() {
		return isMilkedOnMachine;
	}
	public void setMilkedOnMachine(boolean isMilkedOnMachine) {
		this.isMilkedOnMachine = isMilkedOnMachine;
	}
	public LocalDate getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(LocalDate recordDate) {
		this.recordDate = recordDate;
	}
	public LocalTime getRecordTime() {
		return recordTime;
	}
	public void setRecordTime(LocalTime recordTime) {
		this.recordTime = recordTime;
	}
	public Float getMilkVolume() {
		return milkVolume;
	}
	public void setMilkVolume(Float milkVolume) {
		this.milkVolume = milkVolume;
	}
	public Float getLrValue() {
		return lrValue;
	}
	public void setLrValue(Float lrValue) {
		this.lrValue = lrValue;
	}
	public Float getFatValue() {
		return fatValue;
	}
	public void setFatValue(Float fatValue) {
		this.fatValue = fatValue;
	}
	public Float getTemperatureInCentigrade() {
		return temperatureInCentigrade;
	}
	public void setTemperatureInCentigrade(Float temperatureInCentigrade) {
		this.temperatureInCentigrade = temperatureInCentigrade;
	}
	public Float getHumidity() {
		return humidity;
	}
	public void setHumidity(Float humidity) {
		this.humidity = humidity;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public short getMilkingEventNumber() {
		return milkingEventNumber;
	}
	public void setMilkingEventNumber(Short milkingNumber) {
		this.milkingEventNumber = milkingNumber;
	}
	
	
//	public LocalDate getLastParturationDate() {
//		return lastParturationDate;
//	}
//	public void setLastParturationDate(LocalDate lastParturationDate) {
//		this.lastParturationDate = lastParturationDate;
//	}
	/**
	 * This method retrieves the period between the last parturation date and the recod date.
	 * If any of the dates are null it returns a 0 day period.
	 * @return
	 */
//	public Period getDaysSinceLastParturation() {
//		if (recordDate != null && lastParturationDate != null) {
//			return Period.between(lastParturationDate, recordDate);
//		}
//		else
//			return Period.parse("0");
//	}
	public Float getToxinValue() {
		return toxinValue;
	}
	public void setToxinValue(Float toxinValue) {
		this.toxinValue = toxinValue;
	}
	public String getAnimalTag() {
		return animalTag;
	}
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}
	public String getVolUnit() {
		return volUnit;
	}
	public void setVolUnit(String volUnit) {
		this.volUnit = volUnit;
	}
	public MilkingDetailBean getMilkingDetailBean() {
		return new MilkingDetailBean(this.animalTag,this.orgID, this.recordDate.toString(),this.recordTime.toString(),
				this.milkingEventNumber, this.milkVolume, this.fatValue, this.lrValue, this.toxinValue, this.temperatureInCentigrade,
				this.humidity, this.comments);
//		return null;
	}

	public HashMap<String, Float> getAdditionalStatistics() {
		return additionalStatistics;
	}

	public void setAdditionalStatistics(HashMap<String, Float> averages) {
		this.additionalStatistics = averages;
	}
	public void addToAdditionalStatistics(String key, Float value) {
		if (this.additionalStatistics == null)
			additionalStatistics = new HashMap<String, Float>();
		additionalStatistics.put(key, value);
	}
}
