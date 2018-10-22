package com.imd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imd.dto.LifeCycleEvent;
import com.imd.dto.MilkingDetail;
import com.opencsv.CSVReader;

/**
 * This class receives milking information in bulk and creates MilkingDetail objects from it.
 * @author kashif.manzoor
 *
 */
public class ParseMilkingRecord {
	
	final static String TOTAL = "Total";
	final static String  AVG = "Average";
	final static String  MAX = "Max";

	public static HashMap readMilkingRecordsFromSMS(String singleMilkingRecord, LocalDate recordDate, short milkingFrequency, Short milkingEventNumber) throws IMDException{
		HashMap <String, MilkingDetail> milkingInformation = new HashMap<>();
		String [] allCowsRecordForSingleMilking = singleMilkingRecord.split("\n");
		for (int i=0; i< allCowsRecordForSingleMilking.length; i++) {
			// any non alpha numeric character will indicate the boundary between the tag number and the milking volume.
			String[] singleCowSingleMilking = allCowsRecordForSingleMilking[i].split("[^0-9^.^a-z^A-Z]");
			try {
				String animalTag =singleCowSingleMilking[0];
				String vol =  singleCowSingleMilking[1];
				MilkingDetail singleCowRecord = new MilkingDetail(milkingFrequency, true, recordDate, null, Float.parseFloat(vol),milkingEventNumber);
				milkingInformation.put(animalTag, singleCowRecord);
			} catch (Exception ex) {
				System.out.println( " Error occurred during parsing will skip this record " + allCowsRecordForSingleMilking[i]);
			}
		}	
		return milkingInformation;
	}
	
	/**
	 * This file takes in a CSV file which contains daily milking record for a particular animal. Each line of the file should
	 * represent a single day of milking. Each line should be in the following format: YYYY-MM-DD,morningVolume,afternoonVolume,NightVolume
	 * The last three lines of the file should contain the following consolidated statistics: Total, Average, Max
	 * @param csvFile
	 * @return HashMap of MilkingDetail record keyed on date.
	 * @throws IMDException 
	 */
	public static Map<LocalDate, HashMap<Integer,MilkingDetail>> readMilkingRecordsFromCSV(String csvFile) throws IMDException {
        Map<LocalDate, HashMap<Integer,MilkingDetail>> milkingRecords = new HashMap<>();
        BufferedReader br = null;
        String line = "";
        // this ensures that if the values have comma and they are preceded by backslash then we do not split on them by accident.
        String cvsSplitBy = "(?<!\\\\),"; 
        int recordCount = 0;
        try {

            br = new BufferedReader(new FileReader(csvFile));
            Float firstMilkingVol = 0f;
    			Float secondMilkingVol = 0f;
    			Float thirdMilkingVol = 0f;
        		MilkingDetail firstMilkingRecord = null;
        		MilkingDetail secondMilkingRecord = null;
        		MilkingDetail thirdMilkingRecord = null;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] dailyRecordText = line.split(cvsSplitBy);
                String firstColumn = dailyRecordText[0];

                if (!firstColumn.equalsIgnoreCase(TOTAL) && !firstColumn.equalsIgnoreCase(AVG) && !firstColumn.equalsIgnoreCase(MAX) ) {
                		LocalDate date = LocalDate.parse(firstColumn);
                		firstMilkingVol = -1f;
                		secondMilkingVol = -1f;
                		thirdMilkingVol = -1f;
                		firstMilkingRecord = null;
                		secondMilkingRecord = null;
                		thirdMilkingRecord = null;
	            		HashMap <Integer, MilkingDetail> dailyRecord = new HashMap<>();
	            		short frequency = Short.parseShort(dailyRecordText[4]);
	            		int eventSequence = 1;
	            		boolean wasAnimalMilked = false;
	            		if (frequency > 0) {
	                		if (!dailyRecordText[1].isEmpty() && !dailyRecordText[1].equalsIgnoreCase("0") && !dailyRecordText[1].equalsIgnoreCase("0.0")) {
	                			 firstMilkingVol = Float.parseFloat(dailyRecordText[1]);
	                			 firstMilkingRecord = new MilkingDetail(frequency, true, date, null, firstMilkingVol,(short)1);
	                			 dailyRecord.put(eventSequence++, firstMilkingRecord);
	                			 wasAnimalMilked = true;
	                		}
	                		if (!dailyRecordText[2].isEmpty() && !dailyRecordText[2].equalsIgnoreCase("0") && !dailyRecordText[2].equalsIgnoreCase("0.0")) {
	                			secondMilkingVol = Float.parseFloat(dailyRecordText[2]);
	                			secondMilkingRecord = new MilkingDetail(frequency, true, date, null, secondMilkingVol,(short)2);
	                			dailyRecord.put(eventSequence++, secondMilkingRecord);
	                			 wasAnimalMilked = true;
	                		}
	                		if (!dailyRecordText[3].isEmpty() && !dailyRecordText[3].equalsIgnoreCase("0") && !dailyRecordText[3].equalsIgnoreCase("0.0")) {
	                			thirdMilkingVol = Float.parseFloat(dailyRecordText[3]);
	                			thirdMilkingRecord = new MilkingDetail(frequency, true, date, null, thirdMilkingVol,(short)3);
	                			dailyRecord.put(eventSequence++, thirdMilkingRecord);
	                			 wasAnimalMilked = true;
	                		}
	                		if (wasAnimalMilked)
	                			milkingRecords.put(date,dailyRecord);
	            		}
                }
                recordCount++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return milkingRecords;
	}
	
	
	/**
	 * This file takes in a CSV file which contains Life Cycle Events for a particular animal. Each line of the file should
	 * represent a single life cycle event. Each line should be in the following format: 
	 * Event Primary Key, short description, long description, instance id, instance timestamp, operator, comments
	 * If the string has a comma in it then it needs to be properly escaped.
	 * @param csvFile
	 * @param boolean continueOnError Set it to false if you want the parsing of the file to stop when it encounters any ill-formed record. 
	 * If it is set to true then the parsing will jump to the next record if an erroneous record is found.
	 * @return Array of LifeCycleEvent record keyed on transaction id.
	 * @throws IMDException 
	 */
	public static ArrayList<LifeCycleEvent> readLifeCycleEventFromCSV(String csvFile, boolean continueOnError) throws IMDException {
		ArrayList<LifeCycleEvent> events = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        // this ensures that if the values have comma and they are preceded by backslash then we do not split on them by accident.
        String cvsSplitBy = "(?<!\\\\),"; 
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
	            	try {
	            		events.add(new LifeCycleEvent(line));
	            	} catch (IMDException ex) {
	            		if (!continueOnError)
	            			throw ex;
	            	}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return events;
	}	
	
	
	
	
}
