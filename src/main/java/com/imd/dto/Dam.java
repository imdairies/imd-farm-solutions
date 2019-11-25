package com.imd.dto;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
		milkingRecord = new HashMap <LocalDate, HashMap<Short,MilkingDetail>> ();
		setGender(Util.GENDER_CHAR.FEMALE);
	}
	public Dam(String orgID, String tag, DateTime dob, boolean isAgeEstimated, double purchasePrice, String currency) throws IMDException {
		super(orgID, tag, dob, isAgeEstimated, purchasePrice, currency);
		milkingRecord = new HashMap <LocalDate, HashMap<Short,MilkingDetail>> ();
		setGender('F');
	}
	public MilkingDetail getMilkingAverageAtPurchase() {
		return milkingDetailAtPurchase;
	}

	public void setMilkingAverageAtPurchase(MilkingDetail milkingAverageAtPurchase) {
		this.milkingDetailAtPurchase = milkingAverageAtPurchase;
	}
	public Map<LocalDate, HashMap<Short,MilkingDetail>> getCompleteMilkingRecord() {
		return milkingRecord;
	}
	public void setCompleteMilkingRecord(HashMap<LocalDate, HashMap<Short,MilkingDetail>> milkingRecord) {
		this.milkingRecord = milkingRecord;
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
		return milkingRecord.get(recordDate).get(milkingEventNumber.shortValue()); 
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
}
