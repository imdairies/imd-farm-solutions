package com.imd.services.bean;


import org.joda.time.LocalDate;
import org.joda.time.LocalTime;


public class MilkingDetailBean {
	private String animalTag;
	private String orgID;
	private String milkingDateStr;
	private String milkingTimeStr;
	private short milkingEventNumber;
	private float milkVolume;
	private Float fatValue;
	private Float lrValue;
	private Float toxinValue;
	private Float temperatureInCentigrade;
	private Float humidity;
	private String comments;

	public MilkingDetailBean() {
		// TODO Auto-generated constructor stub
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
		return new LocalDate(milkingDateStr);
		//return new LocalDate();
	}
	public LocalTime getRecordTime() {
		return new LocalTime(this.milkingTimeStr);
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
		"comments:" +  this.comments + "\n";
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
	public float getMilkVolume() {
		return milkVolume;
	}
	public void setMilkVolume(float milkVolume) {
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
}
