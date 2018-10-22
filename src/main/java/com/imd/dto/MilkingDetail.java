package com.imd.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

import com.imd.util.IMDException;

public class MilkingDetail {
	/**
	 * average daily milk production averaged over the last 7 days.
	 */
	private float averageDailyProduction;
	private Short dailyMilkingFrequency;
	private boolean isMilkedOnMachine;
	private LocalDate recordDate;
	private LocalTime recordTime;
	private float milkVolume;
	private float lrValue;
	private float fatValue;
	private float temperatureInCentigrade;
	private LocalDate lastParturationDate;
	private float humidity;
	private String comments;
	/**
	 * indicates which milking does this reading pertains to e.g. will be 1 if this is the first milking of recordDate,
	 *  will be 2 if this is second milking of record Date, will be 3 if this is third milking of record date.
	 */
	private Short milkingEventNumber;
	
	/**
	 * 
	 * @param frequency  Number of times the cow is milked every day. Would usually be 2 or 3.
	 * @param machineMilked set to true if the cow was milked on machine
	 * @param recDate set to the date when this milking was conducted.
	 * @param recTime set to the time when this milking was conducted.
	 * @param milkVol the raw volume of the milk
	 * @param lrVal the LR value of the milk
	 * @param fatVal the Fat value of the milk
	 * @param temperatureCelsius The temperature in Celsius of when the milking was conducted.
	 * @param humidity the %Humidity of when the milking was conducted.
	 * @param milkingCount indicate the number that indicates the sequence of this milking for the day i.e. set it to 1 if this was the first milking of the day, 2 if this was the second milking of the day and so on.
	 */

	public MilkingDetail(short frequency, boolean machineMilked, LocalDate recDate, LocalTime recTime, float milkVol, short milkSeqNbr)  throws IMDException {
		if (recDate == null ) 
			throw new IMDException("Record Date can't be null");
		else {
			this.dailyMilkingFrequency = frequency;
			this.isMilkedOnMachine = machineMilked;
			this.recordDate = recDate;
			this.recordTime= recTime;
			this.milkVolume = milkVol;
			this.milkingEventNumber = milkSeqNbr;
		}	}
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
	public float getMilkVolume() {
		return milkVolume;
	}
	public void setMilkVolume(float milkVolume) {
		this.milkVolume = milkVolume;
	}
	public float getLrValue() {
		return lrValue;
	}
	public void setLrValue(float lrValue) {
		this.lrValue = lrValue;
	}
	public float getFatValue() {
		return fatValue;
	}
	public void setFatValue(float fatValue) {
		this.fatValue = fatValue;
	}
	public float getTemperatureInCentigrade() {
		return temperatureInCentigrade;
	}
	public void setTemperatureInCentigrade(float temperatureInCentigrade) {
		this.temperatureInCentigrade = temperatureInCentigrade;
	}
	public float getHumidity() {
		return humidity;
	}
	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Short getMilkingEventNumber() {
		return milkingEventNumber;
	}
	public void setMilkingEventNumber(Short milkingNumber) {
		this.milkingEventNumber = milkingNumber;
	}
	
	
	public LocalDate getLastParturationDate() {
		return lastParturationDate;
	}
	public void setLastParturationDate(LocalDate lastParturationDate) {
		this.lastParturationDate = lastParturationDate;
	}
	/**
	 * This method retrieves the period between the last parturation date and the recod date.
	 * If any of the dates are null it returns a 0 day period.
	 * @return
	 */
	public Period getDaysSinceLastParturation() {
		if (recordDate != null && lastParturationDate != null) {
			return Period.between(lastParturationDate, recordDate);
		}
		else
			return Period.parse("0");
	}
}
