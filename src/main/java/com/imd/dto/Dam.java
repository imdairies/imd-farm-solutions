package com.imd.dto;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import com.imd.util.IMDException;
import com.imd.util.Util;

public class Dam extends Animal {
	private MilkingDetail milkingDetailAtPurchase;
	
	/**
	 * Each date can have 1 or more milking events. Therefore to store the milking details we 
	 * use a HashMap (keyed on Date) of a HashMap (keyed on milking event number of the day). 
	 */
	private Map<LocalDate, HashMap<Short,MilkingDetail>> milkingRecord;

	public Dam(String tagNumber) throws IMDException {
		super(tagNumber);
		milkingRecord = new HashMap <>();
		setGender('F');
	}
	public Dam(String orgID, String tag, DateTime dob, boolean isAgeEstimated, double purchasePrice, String currency) throws IMDException {
		super(orgID, tag, dob, isAgeEstimated, purchasePrice, currency);
		milkingRecord = new HashMap <>();
		setGender('F');
	}
	public MilkingDetail getMilkingAverageAtPurchase() {
		return milkingDetailAtPurchase;
	}

	public void setMilkingAverageAtPurchase(MilkingDetail milkingAverageAtPurchase) {
		this.milkingDetailAtPurchase = milkingAverageAtPurchase;
	}
	public Map getCompleteMilkingRecord() {
		return milkingRecord;
	}
	public void setCompleteMilkingRecord(HashMap<LocalDate, HashMap<Short,MilkingDetail>> milkingRecord) {
		this.milkingRecord = milkingRecord;
	}
	
	public void addToMilkingRecord(MilkingDetail milkingInformation) throws IMDException {
		Util.throwExceptionIfNull(milkingInformation, "Milking Information");
		Util.throwExceptionIfNull(milkingInformation.getRecordDate(), "Milking Date");
		Util.throwExceptionIfNull(milkingInformation.getMilkingEventNumber(), "Milking Sequence Number");
		if (milkingRecord == null)
			milkingRecord = new HashMap<>();

		HashMap <Short, MilkingDetail> milkingRecordsForTheDay = milkingRecord.get(milkingInformation.getRecordDate());
		if (milkingRecordsForTheDay == null) {
			// this is the first time we are adding record for this date.
			milkingRecordsForTheDay = new HashMap <> ();
		}
		milkingRecordsForTheDay.put(milkingInformation.getMilkingEventNumber(), milkingInformation);
		//milkingRecord.put(milkingInformation.getRecordDate(), milkingRecordsForTheDay);
	}
	
	/**
	 * returns a specific milking event of a particular day.
	 * @param recordDate Date of the milking event information is required
	 * @param milkingEventNumber Milking event number for which the milking detail is required.
	 * @return MilkingDetail
	 * @throws IMDException
	 */
	public MilkingDetail getMilkingEvent(LocalDate recordDate, Integer milkingEventNumber) throws IMDException {
		Util.throwExceptionIfNull(recordDate, "Record Date");
		Util.throwExceptionIfNull(recordDate, "Milking Event Number");
		return milkingRecord.get(recordDate).get(milkingEventNumber);
	}
	
	public float getDailyMilkTotal(LocalDate recordDate) throws IMDException {

		float dailyVolume = 0.0f;
		HashMap<Short,MilkingDetail> milkingRecForTheDay = milkingRecord.get(recordDate);
		Util.throwExceptionIfNull(milkingRecForTheDay,"[Milking record for the day does not exist]");
		
		for (Map.Entry<Short,MilkingDetail> entry : milkingRecForTheDay.entrySet() ) {
			dailyVolume += entry.getValue().getMilkVolume();
		}
		return dailyVolume;
	}
	
	public MilkingDetail getLifetimeMilkTotals() throws IMDException {
		return getMilkTotalsForSpecifiedPeriod(null, null);
	}
	
	/**
	 * return the milking average for life of this cow.
	 * @throws IMDException 
	 */
	public float getLifeTimeDailyMilkingAverage() throws IMDException {
		MilkingDetail lifetimeMilkingTotals = getLifetimeMilkTotals();
		return (lifetimeMilkingTotals.getMilkVolume() / lifetimeMilkingTotals.getMilkingEventNumber());
	}
	
	/**
	 * return the milking average for life of this cow.
	 * @throws IMDException 
	 */
	public float geDailyMilkingAverageForSpecifiedPeriod(LocalDate startDate, LocalDate endDate) throws IMDException {
		MilkingDetail lifetimeMilkingTotals = getMilkTotalsForSpecifiedPeriod(startDate, endDate);
		return (lifetimeMilkingTotals.getMilkVolume() / lifetimeMilkingTotals.getMilkingEventNumber());
	}
	/**
	 * returns the milk statistics for the given duration (inclusive of start and end dates).
	 * If any or both dates are null then it returns lifetime statistics.
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws IMDException
	 */
	public MilkingDetail getMilkTotalsForSpecifiedPeriod(LocalDate startDate, LocalDate endDate) throws IMDException {
		float milkVolume = 0.0f;
		short numberOfDays = 0;
		MilkingDetail lifetimeMilkingTotals = null;//new MilkingDetail((short)3, true, LocalDate.now(), LocalTime.now(), 0.0f,(short)1);
		Util.throwExceptionIfNull(milkingRecord,"[Milking record does not exist]");
		for (Map.Entry<LocalDate,HashMap<Short, MilkingDetail>> recordEntry : milkingRecord.entrySet() ) {
			if ( startDate == null || endDate == null || ((recordEntry.getKey().isEqual(startDate) || recordEntry.getKey().isAfter(startDate) ) &&
				 (recordEntry.getKey().isEqual(endDate)   || recordEntry.getKey().isBefore(endDate)))) {
				milkVolume += processDailyMilkingEvent(recordEntry);
				numberOfDays++;
			}
		}
		lifetimeMilkingTotals.setMilkVolume(milkVolume);
		lifetimeMilkingTotals.setMilkingEventNumber(numberOfDays);
		return lifetimeMilkingTotals;
	}
	private float processDailyMilkingEvent(Entry<LocalDate, HashMap<Short, MilkingDetail>> recordEntry) {
		float milkVolume = 0.0f;
		for (Map.Entry<Short,MilkingDetail> entry : recordEntry.getValue().entrySet() ) {
			milkVolume += entry.getValue().getMilkVolume();	
		}
		return milkVolume;
	}
}
