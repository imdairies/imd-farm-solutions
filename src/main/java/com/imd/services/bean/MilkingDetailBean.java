package com.imd.services.bean;


import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.imd.util.IMDProperties;


public class MilkingDetailBean {
	private String animalTag;
	private String orgID;
	private String milkingDateStr;
	private String milkingTimeStr;
	private short milkingEventNumber;
	private Float milkVolume;
	private Float fatValue;
	private Float lrValue;
	private Float toxinValue;
	private Float temperatureInCentigrade;
	private Float humidity;
	private String comments;
	private String loginToken;
	private Float phValue;
	private Float forCalvesUse;
	private Float forFarmUse;
	private Float forPersonalUse;
	private Float forFamilyUse;
	private Float forOtherUse;
	private Float forWasteAdj;
	private String dataStartReferenceEvent;
	
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
	
	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public MilkingDetailBean() {
	}
	public MilkingDetailBean(String animalTag2, String orgID2, String milkingDateStr2, String milkingTimeStr2, short milkingEventNumber2,
			float milkVolume2, Float fatValue2, Float lrValue2, Float toxinValue2, Float temperatureInCentigrade2,
			Float humidity2, String comments2) {
		this.animalTag = animalTag2;
		this.orgID = orgID2;
		this.milkingDateStr = milkingDateStr2;
		this.milkingTimeStr = milkingTimeStr2;
		this.milkingEventNumber = milkingEventNumber2;
		this.milkVolume = milkVolume2;
		this.fatValue = fatValue2;
		this.lrValue = lrValue2;
		this.toxinValue = toxinValue2;
		this.temperatureInCentigrade = temperatureInCentigrade2;
		this.humidity = humidity2;
		this.comments = comments2;
	}
	public MilkingDetailBean(MilkingDetailBean searchBean) {
		this.animalTag = searchBean.getAnimalTag();
		this.orgID = searchBean.getOrgID();
		this.milkingDateStr = searchBean.getMilkingDateStr();
		this.milkingTimeStr = searchBean.getMilkingTimeStr();
		this.milkingEventNumber = searchBean.getMilkingEventNumber();
		this.milkVolume = searchBean.getMilkVolume();
		this.fatValue = searchBean.getFatValue();
		this.lrValue = searchBean.getLrValue();
		this.toxinValue = searchBean.getToxinValue();
		this.temperatureInCentigrade = searchBean.getTemperatureInCentigrade();
		this.humidity = searchBean.getHumidity();
		this.comments = searchBean.getComments();
		this.phValue = searchBean.getPhValue();
		this.forCalvesUse = searchBean.getForCalvesUse();
		this.forFarmUse = searchBean.getForFarmUse();
		this.forPersonalUse = searchBean.getForPersonalUse();
		this.forFamilyUse = searchBean.getForFamilyUse();
		this.forOtherUse = searchBean.getForOtherUse();
		this.forWasteAdj = searchBean.getForWasteAdj();

	
	}
	public MilkingDetailBean(FarmMilkingDetailBean milkingEventRecord) {
		this.setOrgID(milkingEventRecord.getOrgID());
		this.setRecordDate(milkingEventRecord.getRecordDate());
		this.setRecordTime(milkingEventRecord.getRecordTime());
		this.setMilkingEventNumber(milkingEventRecord.getMilkingEventNumber());
		this.setLrValue(milkingEventRecord.getLrValue());
		this.setFatValue(milkingEventRecord.getFatValue());
		this.setToxinValue(milkingEventRecord.getToxinValue());
		this.setTemperatureInCentigrade(milkingEventRecord.getTemperatureInCentigrade());
		this.setHumidity(milkingEventRecord.getHumidity());
		this.setComments(milkingEventRecord.getComments());
		this.setPhValue(milkingEventRecord.getPhValue());
		this.setForCalvesUse(milkingEventRecord.getForCalvesUse());
		this.setForFarmUse(milkingEventRecord.getForFarmUse());
		this.setForPersonalUse(milkingEventRecord.getForPersonalUse());
		this.setForFamilyUse(milkingEventRecord.getForFamilyUse());
		this.setForOtherUse(milkingEventRecord.getForOtherUse());
		this.setForWasteAdj(milkingEventRecord.getForWasteAdj());
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
	public LocalDate getRecordDate() {
		return new LocalDate(milkingDateStr, IMDProperties.getServerTimeZone());
	}
	public LocalTime getRecordTime() {
		return new LocalTime(this.milkingTimeStr, IMDProperties.getServerTimeZone());
	}
	public String toString() {
		return "\nanimalTag: " + this.animalTag + "\n" + 
		"orgID:" + this.orgID + "\n" + 
		"milkingDateStr:" +  this.milkingDateStr + "\n" +
		"milkingTimeStr:" +  this.milkingTimeStr + "\n" +
		"milkingEventNumber:" +  this.milkingEventNumber + "\n" + 
		"milkVolume:" +  this.milkVolume + "\n" + 
		"fatValue:" + this.fatValue + "\n" + 
		"lrValue::" + this.lrValue + "\n" + 
		"toxinValue:" +  this.toxinValue + "\n" + 
		"temperatureInCentigrade:" +  this.temperatureInCentigrade + "\n" + 
		"humidity:" +  this.humidity + "\n" + 
		"comments:" +  this.comments + "\n" +
		"forCalvesUse:" +  this.forCalvesUse + "\n" +		
		"forFarmUse:" +  this.forFarmUse + "\n" +
		"forPersonalUse:" +  this.forPersonalUse + "\n" +
		"forFamilyUse:" +  this.forFamilyUse + "\n" +
		"forOtherUse:" +  this.forOtherUse + "\n" +
		"forWasteAdj:" +  this.forWasteAdj + "\n";
	}
	public String getMilkingDateStr() {
		return milkingDateStr;
	}
	public void setMilkingDateStr(String milkiingDateStr) {
		this.milkingDateStr = milkiingDateStr;
	}
	public String getMilkingTimeStr() {
		return milkingTimeStr;
	}
	public void setMilkingTimeStr(String milkingTimeStr) {
		this.milkingTimeStr = milkingTimeStr;
	}
	public void setRecordDate(LocalDate recordDate) {
		this.milkingDateStr = recordDate.toString();		
	}
	public void setRecordTime(LocalTime recordTime) {
		this.milkingTimeStr = recordTime.toString();
		
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
	public short getMilkingEventNumber() {
		return milkingEventNumber;
	}
	public void setMilkingEventNumber(short milkingEventNumber) {
		this.milkingEventNumber = milkingEventNumber;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Float getTemperatureInCentigrade() {
		return temperatureInCentigrade;
	}
	public void setTemperatureInCentigrade(Float temperatureInCentigrade) {
		this.temperatureInCentigrade = temperatureInCentigrade;
	}
	public Float getToxinValue() {
		return toxinValue;
	}
	public void setToxinValue(Float toxinValue) {
		this.toxinValue = toxinValue;
	}
	public Float getHumidity() {
		return humidity;
	}
	public void setHumidity(Float humidity) {
		this.humidity = humidity;
	}

	public String getDataStartReferenceEvent() {
		return dataStartReferenceEvent;
	}

	public void setDataStartReferenceEvent(String dataStartReferenceEvent) {
		this.dataStartReferenceEvent = dataStartReferenceEvent;
	}

}
