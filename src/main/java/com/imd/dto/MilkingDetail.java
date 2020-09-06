package com.imd.dto;


import java.util.HashMap;
import java.util.Iterator;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.util.IMDException;
import com.imd.util.Util;

public class MilkingDetail extends IMDairiesDTO{
	private String animalTag;
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
	private Float phValue;
	private Float forCalvesUse;
	private Float forFarmUse;
	private Float forPersonalUse;
	private Float forFamilyUse;
	private Float forOtherUse;
	private Float forWasteAdj;
	

	public Float getPhValue() {
		return phValue;
	}

	public void setPhValue(Float phValue) {
		this.phValue = phValue;
	}

	public Float getForCalvesUse() {
		return forCalvesUse;
	}

	public void setForCalvesUse(Float forCalvesUse) {
		this.forCalvesUse = forCalvesUse;
	}

	public Float getForFarmUse() {
		return forFarmUse;
	}

	public void setForFarmUse(Float forFarmUse) {
		this.forFarmUse = forFarmUse;
	}

	public Float getForPersonalUse() {
		return forPersonalUse;
	}

	public void setForPersonalUse(Float forPersonalUse) {
		this.forPersonalUse = forPersonalUse;
	}

	public Float getForFamilyUse() {
		return forFamilyUse;
	}

	public void setForFamilyUse(Float forFamilyUse) {
		this.forFamilyUse = forFamilyUse;
	}

	public Float getForOtherUse() {
		return forOtherUse;
	}

	public void setForOtherUse(Float forOtherUse) {
		this.forOtherUse = forOtherUse;
	}

	public Float getForWasteAdj() {
		return forWasteAdj;
	}

	public void setForWasteAdj(Float forWasteAdj) {
		this.forWasteAdj = forWasteAdj;
	}

	public void setMilkingEventNumber(short milkingEventNumber) {
		this.milkingEventNumber = milkingEventNumber;
	}
	private HashMap<String, Float> additionalStatistics = new HashMap<String, Float>();
	private short milkingEventNumber;

	private String stringify(String prefix, DateTimeFormatter fmt) {
		if (fmt == null)
			fmt = DateTimeFormat.forPattern("MMMM d, yyyy");
		String retValue =  prefix + fieldToJson("orgID", this.getOrgId()) + ",\n" + 
				prefix + fieldToJson("animalTag", this.animalTag) + ",\n" +
				prefix + fieldToJson("milkingEventNumber", this.getMilkingEventNumber()) + ",\n" + 
				prefix + fieldToJson("recordDate", (this.recordDate != null ? fmt.print(this.recordDate) : "")) + ",\n" + 
				prefix + fieldToJson("recordTime", (this.recordTime != null ? Util.getTimeInSQLFormart(this.recordTime) : "")) + ",\n" + 
				prefix + fieldToJson("dailyMilkingFrequency", this.getDailyMilkingFrequency()) + ",\n" + 
				prefix + fieldToJson("isMilkedOnMachine", this.isMilkedOnMachine) + ",\n" + 
				prefix + fieldToJson("milkVolume", this.milkVolume) + ",\n" + 
				prefix + fieldToJson("volUnit", this.volUnit) + ",\n" + 
				prefix + fieldToJson("lrValue", this.lrValue == null ? 0: this.lrValue) + ",\n" + 
				prefix + fieldToJson("fatValue", this.fatValue == null ? 0: this.fatValue) + ",\n" + 
				prefix + fieldToJson("toxinValue", this.toxinValue == null ? 0: this.toxinValue) + ",\n" + 
				prefix + fieldToJson("temperatureInCentigrade", this.temperatureInCentigrade == null ? 0 : this.temperatureInCentigrade) + ",\n" + 
				prefix + fieldToJson("humidity", this.humidity == null ? 0:this.humidity) + ",\n" + 
				prefix + fieldToJson("comments", this.comments) + ",\n" +
				prefix + fieldToJson("phValue",this.phValue == null ? 0:this.phValue) + ",\n" + 		
				prefix + fieldToJson("forCalvesUse",this.forCalvesUse == null ? 0:this.forCalvesUse) + ",\n" + 		
				prefix + fieldToJson("forFarmUse",this.forFarmUse == null ? 0:this.forFarmUse) + ",\n" + 
				prefix + fieldToJson("forPersonalUse",this.forPersonalUse == null ? 0:this.forPersonalUse) + ",\n" + 
				prefix + fieldToJson("forFamilyUse",this.forFamilyUse == null ? 0:this.forFamilyUse) + ",\n" + 
				prefix + fieldToJson("forOtherUse",this.forOtherUse == null ? 0:this.forOtherUse) + ",\n" + 
				prefix + fieldToJson("forWasteAdj",this.forWasteAdj == null ? 0:this.forWasteAdj) + ",\n";
		
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
		return  stringify(" ",null);
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix,null) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix,fmt) + super.dtoToJson(prefix, fmt));
	}
 
	
	public MilkingDetail(String orgID, String tagNbr, short frequency, boolean machineMilked, LocalDate recDate, LocalTime recTime, Float milkVol, short milkSeqNbr)  throws IMDException {
		this.dailyMilkingFrequency = frequency;
		this.isMilkedOnMachine = machineMilked;
		this.recordDate = recDate;
		this.recordTime= recTime;
		this.milkVolume = milkVol;
		this.milkingEventNumber = milkSeqNbr;
		this.setOrgId(orgID);
		this.animalTag = tagNbr;
	}
	
	public MilkingDetail(FarmMilkingDetailBean farmMilkingRecord) {
		this.setOrgId(farmMilkingRecord.getOrgID());
		this.setRecordDate(farmMilkingRecord.getRecordDate());
		this.setRecordTime(farmMilkingRecord.getRecordTime());
		this.setMilkingEventNumber(farmMilkingRecord.getMilkingEventNumber());
		this.setLrValue(farmMilkingRecord.getLrValue());
		this.setFatValue(farmMilkingRecord.getFatValue());
		this.setToxinValue(farmMilkingRecord.getToxinValue());
		this.setTemperatureInCentigrade(farmMilkingRecord.getTemperatureInCentigrade());
		this.setHumidity(farmMilkingRecord.getHumidity());
		this.setComments(farmMilkingRecord.getComments());
		this.setPhValue(farmMilkingRecord.getPhValue());
		this.setForCalvesUse(farmMilkingRecord.getForCalvesUse());
		this.setForFarmUse(farmMilkingRecord.getForFarmUse());
		this.setForPersonalUse(farmMilkingRecord.getForPersonalUse());
		this.setForFamilyUse(farmMilkingRecord.getForFamilyUse());
		this.setForOtherUse(farmMilkingRecord.getForOtherUse());
		this.setForWasteAdj(farmMilkingRecord.getForWasteAdj());
	}

	public MilkingDetail(MilkingDetailBean milkingRecord) {
		this.setOrgId(milkingRecord.getOrgID());
		this.setAnimalTag(milkingRecord.getAnimalTag());
		this.setRecordDate(milkingRecord.getRecordDate());
		this.setRecordTime(milkingRecord.getRecordTime());
		this.setMilkVolume(milkingRecord.getMilkVolume());
		this.setMilkingEventNumber(milkingRecord.getMilkingEventNumber());
		this.setLrValue(milkingRecord.getLrValue());
		this.setFatValue(milkingRecord.getFatValue());
		this.setToxinValue(milkingRecord.getToxinValue());
		this.setTemperatureInCentigrade(milkingRecord.getTemperatureInCentigrade());
		this.setHumidity(milkingRecord.getHumidity());
		this.setComments(milkingRecord.getComments());
		this.setPhValue(milkingRecord.getPhValue());
		this.setForCalvesUse(milkingRecord.getForCalvesUse());
		this.setForFarmUse(milkingRecord.getForFarmUse());
		this.setForPersonalUse(milkingRecord.getForPersonalUse());
		this.setForFamilyUse(milkingRecord.getForFamilyUse());
		this.setForOtherUse(milkingRecord.getForOtherUse());
		this.setForWasteAdj(milkingRecord.getForWasteAdj());
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
	public String getVolUnit() {
		return volUnit;
	}
	public void setVolUnit(String volUnit) {
		this.volUnit = volUnit;
	}
	public MilkingDetailBean getMilkingDetailBean() {
		return new MilkingDetailBean(this.animalTag,this.getOrgId(), this.recordDate.toString(),this.recordTime == null ? "00:00" : this.recordTime.toString(),
				this.milkingEventNumber, this.milkVolume, this.fatValue, this.lrValue, this.toxinValue, this.temperatureInCentigrade,
				this.humidity, this.comments);
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
