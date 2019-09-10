package com.imd.services;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.imd.dto.Animal;
import com.imd.dto.MilkingDetail;
import com.imd.loader.AnimalLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.InputDelimitedFileBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.TagVolumeCommentTriplet;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;;

@Path("/milkinginfo")
public class MilkingInformationSrvc {

	@POST
	@Path("/lactatingcowsmilkrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimalsMilkRecord(MilkingDetailBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		MilkingDetailLoader milkingLoader = new MilkingDetailLoader();
			List<Animal> animalValues = loader.retrieveActiveLactatingAnimals(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		searchBean.setAnimalTag(animalValue.getAnimalTag());
	    		//animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
	    		// add milking detail information to the data
	    		animalValueResult += appendMilkingDetails(milkingLoader.retrieveSingleMilkingRecordsOfCow(searchBean, false), searchBean);
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	

	
	@POST
	@Path("/milkingrecordofyear")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedYear(MilkingDetailBean searchBean){
		String milkMonthList = "";
		String monthlyVolList = "";
		String monthlyAverageList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			List <MilkingDetail>  milkRecords = loader.retrieveFarmMonthlyMilkVolumeForSpecifiedYear(new LocalDate(searchBean.getRecordDate()), true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			while (it.hasNext()) {
				milkRec = it.next();
				milkMonthList += "\"" + Util.getDateInSpecifiedFormart(milkRec.getRecordDate(),"MMM") + "\",";
				monthlyVolList += milkRec.getMilkVolume() + ",";
				monthlyAverageList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE) == null ? 0.0 : Math.round(((float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE))*10))/10 + ",";

			}
			if (monthlyAverageList != null) {
				int commatoremove = monthlyAverageList.lastIndexOf(",");
				monthlyAverageList = monthlyAverageList.substring(0,commatoremove);
			}
			if (milkMonthList != null) {
				int commatoremove = milkMonthList.lastIndexOf(",");
				milkMonthList = milkMonthList.substring(0,commatoremove);
			}
			if (monthlyVolList != null) {
				int commatoremove = monthlyVolList.lastIndexOf(",");
				monthlyVolList = monthlyVolList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" + "\n" + prefix + prefix + "\"days\":[" + milkMonthList + "],\n" + prefix + prefix + "\"averages\":[" + monthlyAverageList + "],\n" +prefix + prefix + "\"volumes\":[" + monthlyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(200).entity(milkingRecordInformation).build();
    }
	
	@POST
	@Path("/milkingrecordofmonth")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedMonth(MilkingDetailBean searchBean){
		String milkDayList = "";
		String dailyVolList = "";
		String dailyAverageList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedMonth(new LocalDate(searchBean.getRecordDate()),true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			while (it.hasNext()) {
				milkRec = it.next();
				milkDayList += milkRec.getRecordDate().getDayOfMonth() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
				dailyAverageList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE) == null ? 0.0 : Math.round(((float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE))*10))/10 + ",";
			}
			if (dailyAverageList != null) {
				int commatoremove = dailyAverageList.lastIndexOf(",");
				dailyAverageList = dailyAverageList.substring(0,commatoremove);
			}
			if (milkDayList != null) {
				int commatoremove = milkDayList.lastIndexOf(",");
				milkDayList = milkDayList.substring(0,commatoremove);
			}
			if (dailyVolList != null) {
				int commatoremove = dailyVolList.lastIndexOf(",");
				dailyVolList = dailyVolList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" + "\n" + prefix + prefix + "\"days\":[" + milkDayList + "],\n"  + prefix + prefix + "\"averages\":[" + dailyAverageList + "],\n"  + prefix + prefix + "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(200).entity(milkingRecordInformation).build();
    }		

	
	@POST
	@Path("/milkingrecordofananimalforspecifiedmonthpair")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedAnimal(MilkingDetailBean searchBean){
		String milkDayList = "";
		String dailyVolList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	IMDLogger.log("retrieveMilkingRecordOfSpecifiedAnimal called", Util.INFO);

		MilkingDetailLoader loader = new MilkingDetailLoader();
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(searchBean.getOrgID(),
					searchBean.getAnimalTag(),searchBean.getRecordDate(),searchBean.getRecordDate().plusMonths(2).minusDays(1),true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			while (it.hasNext()) {
				milkRec = it.next();
				milkDayList += milkRec.getRecordDate().getDayOfMonth() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
			}
			if (milkDayList != null) {
				int commatoremove = milkDayList.lastIndexOf(",");
				milkDayList = milkDayList.substring(0,commatoremove);
			}
			if (dailyVolList != null) {
				int commatoremove = dailyVolList.lastIndexOf(",");
				dailyVolList = dailyVolList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" + "\n" + prefix + prefix + "\"days\":[" + milkDayList + "],\n"  + prefix + prefix +  "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(200).entity(milkingRecordInformation).build();
    }			
	
	@POST
	@Path("/milkingrecordofeachdayofyear")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfEachDayOfYear(MilkingDetailBean searchBean){
		String milkDayList = "";
		String dailyVolList = "";
		String dateList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			MilkingDetail[]  milkRecords = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(searchBean.getRecordDate());
			MilkingDetail milkRec = null;
			for (int i=0; i<milkRecords.length; i++) {
				milkRec = milkRecords[i];
				milkDayList += milkRec.getRecordDate().getDayOfYear() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
				dateList += "\"" + Util.getDateInSpecifiedFormart(milkRec.getRecordDate(),"dd-MMM") + "\",";
			}
			if (dateList != null) {
				int commatoremove = dateList.lastIndexOf(",");
				dateList = dateList.substring(0,commatoremove);
			}
			if (milkDayList != null) {
				int commatoremove = milkDayList.lastIndexOf(",");
				milkDayList = milkDayList.substring(0,commatoremove);
			}
			if (dailyVolList != null) {
				int commatoremove = dailyVolList.lastIndexOf(",");
				dailyVolList = dailyVolList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" + "\n" + prefix + prefix + "\"days\":[" + milkDayList + "],\n"  + prefix + prefix + "\"dates\":[" + dateList + "],\n"  + prefix + prefix + "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(200).entity(milkingRecordInformation).build();
    }		
		
	
	private String appendMilkingDetails(List<MilkingDetail> milkingRecords, MilkingDetailBean searchBean) throws IMDException {
		String milkingDetail = "";
		String prefix = "  ";
		if (milkingRecords.size() == 0) {
	    	MilkingDetail recordDetail = new MilkingDetail(searchBean.getOrgID(), searchBean.getAnimalTag(), 
	    			(short) 0, true, null, null, null, (short) 0);
	    	milkingDetail = "{\n" + recordDetail.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
		} else {
	    	Iterator<MilkingDetail> recordsIt = milkingRecords.iterator();
	    	while (recordsIt.hasNext()) {
	    		MilkingDetail recordDetail = recordsIt.next();
	    		milkingDetail += "{\n" + recordDetail.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
	    	}
		}    	
		return milkingDetail;
	}


	@POST
	@Path("/addfarmmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addFarmMilkingRecord(FarmMilkingDetailBean milkingEventRecord){
		String prefix = "   ";
    	milkingEventRecord.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(milkingEventRecord.toString(), Util.INFO);
    	try {
			if (milkingEventRecord.getMilkingEventNumber() <1)
			{
				// bad request
				return Response.status(404).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			}
//			if (milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().size()==0)
//			{
//				// bad request
//				return Response.status(404).entity("{ \"error\": true, \"message\":\"You did not specify any milking record\"}").build();
//			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		List<TagVolumeCommentTriplet> outcomeInfo = loader.addOrEditFarmMilkingEventRecord(milkingEventRecord);
    		
    		String outcomeString = "";
    		int count=0;
    		
    		Iterator<TagVolumeCommentTriplet> it = outcomeInfo.iterator();
    		while (it.hasNext()) {
    			count++;
    			if (count == outcomeInfo.size())
        			outcomeString += "{\n" + it.next().dtoToJson(prefix) + "\n}";
    			else
        			outcomeString += "{\n" + it.next().dtoToJson(prefix) + "\n},\n";
    		}
    		if (outcomeInfo == null || outcomeInfo.isEmpty())
        		outcomeString = "[]";
    		else
        		outcomeString = "["+ outcomeString + "]";
    			
        	IMDLogger.log(outcomeString, Util.INFO);
    		return Response.status(200).entity(outcomeString).build();

//    		responseCode = loader.insertMilkRecord(milkingRecord);
//    		if (responseCode == Util.ERROR_CODE.ALREADY_EXISTS)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
//    		else
//    			return Response.status(200).entity("{ \"error\": false, \"message\":\"" + responseCode + " milking records added" + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. " +  e.getMessage() + "\"}").build();
		}
    }	
	@POST
	@Path("/uploadfarmmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response uploadFarmMilkingEventRecord(InputDelimitedFileBean inputInformation) {
		String prefix = "   ";
    	String orgID = (String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID);
//    	String userID = (String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.USER_ID);
    	IMDLogger.log(inputInformation.toString(), Util.INFO);
    	try {
    		if (inputInformation == null || inputInformation.getInputDelimitedFileContents() == null) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"Unable to parse the information. The service seemed to have been called with invalid or missing parameter (inputDelimitedFileContents)\"}").build();    			
    		}
        	FarmMilkingDetailBean milkingEventRecord = Util.parseFarmMilkingDetailBean(inputInformation);
        	milkingEventRecord.setOrgID(orgID);
        	if (milkingEventRecord.getTemperatureInCentigrade() == null) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify a valid Temperature \"}").build();
			} else if (milkingEventRecord.getMilkingDateStr() == null) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify a valid milking date \"}").build();
			} else if (milkingEventRecord.getMilkingTimeStr() == null) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify a valid milking time \"}").build();
			} else if (milkingEventRecord.getMilkingEventNumber() <1) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			} else if (milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().isEmpty()) {
				return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify at least one milking record\"}").build();
			}
        	if (!inputInformation.getShouldAdd() ) {
				// only parse and show the results
				String parseResult = "";
				float totalVolume = 0;
				parseResult = "{\n\"date\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getMilkingDateStr()) + "\",\n" + 
						"\"time\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getMilkingTimeStr()) + "\",\n" +
						"\"event\":\"" + milkingEventRecord.getMilkingEventNumber() + "\",\n" +
						"\"temperature\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getTemperatureInCentigrade()) + "\",\n" +
						"\"humidity\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getHumidity()) + "\",\n" +
						"\"fat\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getFatValue()) + "\",\n" +
						"\"lr\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getLrValue()) + "\",\n" +
						"\"toxin\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getToxinValue()) + "\",\n" +
						"\"message\":\"All recrods have been successfully parsed\",\n" +
						"\"totalMilkRecords\":\"" + milkingEventRecord.getFarmMilkingEventRecords().size() + "\",\n";
				Iterator<TagVolumeCommentTriplet> it = milkingEventRecord.getFarmMilkingEventRecords().iterator();
				String milkRecords = "";
				int count = 1;
				String comma = ",";
				while (it.hasNext()) {
					TagVolumeCommentTriplet record = it.next();
					if (count == milkingEventRecord.getFarmMilkingEventRecords().size())
						comma ="";
					count++;
					milkRecords += "\n  {\n   \"tag\":\"" + record.getTag() + "\",\n" + 
							"   \"volume\":\"" + record.getVolume() + "\",\n" + 
							"   \"comments\":\"" + Util.substituteEmptyForNull(record.getComments()) + "\"\n  }" + comma;
					totalVolume += Float.parseFloat(record.getVolume());
				}
				parseResult += "\"totalVolume\":\"" + totalVolume + "\",\n"  +
						"\"milkingRecords\":[";
				
				parseResult += milkRecords + "]\n}"; 
				IMDLogger.log(parseResult, Util.INFO);
				return Response.status(200).entity(parseResult).build();
			} else {
	    		MilkingDetailLoader loader = new MilkingDetailLoader();
	    		List<TagVolumeCommentTriplet> outcomeInfo = loader.addOrEditFarmMilkingEventRecord(milkingEventRecord);
	    		
	    		String outcomeString = "";
	    		int count=0;
	    		
	    		Iterator<TagVolumeCommentTriplet> it = outcomeInfo.iterator();
	    		while (it.hasNext()) {
	    			count++;
	    			if (count == outcomeInfo.size())
	        			outcomeString += "{\n" + it.next().dtoToJson(prefix) + "\n}";
	    			else
	        			outcomeString += "{\n" + it.next().dtoToJson(prefix) + "\n},\n";
	    		}
	    		if (outcomeInfo == null || outcomeInfo.isEmpty())
	        		outcomeString = "[]";
	    		else
	        		outcomeString = "["+ outcomeString + "]";
	    			
	        	IMDLogger.log(outcomeString, Util.INFO);
	    		return Response.status(200).entity(outcomeString).build();
			}

//    		responseCode = loader.insertMilkRecord(milkingRecord);
//    		if (responseCode == Util.ERROR_CODE.ALREADY_EXISTS)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
//    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
//    		else
//    			return Response.status(200).entity("{ \"error\": false, \"message\":\"" + responseCode + " milking records added" + "\"}").build();
    	} catch (IMDException e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(e.getMessage())) + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. " +  e.getMessage() + "\"}").build();
		}
    }	
	
	@POST
	@Path("/addmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addCowMilkingRecord(MilkingDetailBean milkingRecord){
    	int responseCode = 0;
    	milkingRecord.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(milkingRecord.toString(), Util.INFO);
    	try {
			if (milkingRecord.getAnimalTag() == null || milkingRecord.getAnimalTag().isEmpty())
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
			if (milkingRecord.getMilkingEventNumber() <1)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			}
			if (!(milkingRecord.getMilkVolume() > 0))
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid milking volume\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		responseCode = loader.insertMilkRecord(milkingRecord);
    		if (responseCode == Util.ERROR_CODE.ALREADY_EXISTS)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
    		else
    			return Response.status(200).entity("{ \"error\": false, \"message\":\"" + responseCode + " record added" + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. " +  e.getMessage() + "\"}").build();
		}
    }	
	
	
	@POST
	@Path("/monthlymilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMonthlyMilkingRecord(MilkingDetailBean searchBean){
    	String milkingInformation = "";
    	List<MilkingDetail> dailyRecords = new ArrayList<MilkingDetail>(3);
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	LocalDate currentDate = null;
    	try {
			if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		AnimalLoader animalLoader = new AnimalLoader();
    		AnimalBean animalBean = new AnimalBean();
    		animalBean.setOrgID(searchBean.getOrgID());
    		animalBean.setAnimalTag(searchBean.getAnimalTag());    		
    		List<Animal> animals = animalLoader.getAnimalRawInfo(animalBean);
    		if (animals.size() != 1) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Unable to find the animal. foud " + animals.size() + " records\"}").build();    			    			
    		}
    		Animal animal = animals.get(0);
    		IMDLogger.log(animal.getAnimalType(), Util.INFO);
    		if (animal.getGender() != Util.GENDER.FEMALE) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information only applies to female animals\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.FEMALECALF)) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to female calves\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.HEIFER)) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to heifers\"}").build();    			
    		}    		
			List<MilkingDetail> animalValues = loader.retrieveMonthlyMilkingRecordsOfCow(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<MilkingDetail> recValuesIt = animalValues.iterator();
	    	while (recValuesIt.hasNext()) {
	    		MilkingDetail recValue = recValuesIt.next();
	    		if (currentDate == null) {
	    			currentDate = recValue.getRecordDate();
	    		} else if (!currentDate.isEqual(recValue.getRecordDate())) {
		    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + ",\n";
		    		dailyRecords.clear();
	    			currentDate = recValue.getRecordDate();
	    			IMDLogger.log(currentDate.toString(), Util.INFO);
	    		}
	    		dailyRecords.add(recValue);
//	    		milkingInformation += "{\n" + recValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + "\n";
	    	if (milkingInformation != null && !milkingInformation.trim().isEmpty() )
	    		milkingInformation = "[" + milkingInformation + "]";
	    	else
	    		milkingInformation = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
//    	IMDLogger.log(milkingInformation, Util.ERROR);
		return Response.status(200).entity(milkingInformation).build();
    }	
	
	private String consolidateDailyMilkingRecord(List<MilkingDetail> dailyRecords) {
		String json = "";
		String prefix = "   ";
		MilkingDetail recValue1 = null;
		MilkingDetail recValue2 = null;
		MilkingDetail recValue3 = null;
		LocalDate recDate = null;
		
		if (dailyRecords != null && !dailyRecords.isEmpty()) {
			
				for (int i=0; i < dailyRecords.size(); i++) {
					recDate = dailyRecords.get(i).getRecordDate();
					if (dailyRecords.get(i).getMilkingEventNumber() == 1) {
						recValue1 = dailyRecords.get(i);
					}
					else if (dailyRecords.get(i).getMilkingEventNumber() == 2) {
						recValue2 = dailyRecords.get(i);
					}
					else if (dailyRecords.get(i).getMilkingEventNumber() == 3) {
						recValue3 = dailyRecords.get(i);
					}
				}
	    		json = "{\n" + prefix + "\"milkingDate\":\""+ recDate + "\",\n";
	    		json += formatMilkingEventJson(recDate, prefix, recValue1,1,",");
	    		json += formatMilkingEventJson(recDate, prefix, recValue2,2,",");
	    		json += formatMilkingEventJson(recDate, prefix, recValue3,3,"}");
	    	}
		return json;
	}

	private String formatMilkingEventJson(LocalDate recDate, String prefix, MilkingDetail recValue, int postFix, String commaOrBracket) {
		String json = "";
//		IMDLogger.log("[" + recDate + "] milkVol" + postFix + " = " + (recValue == null ? "" : recValue.getMilkVolume()), Util.ERROR);

		json += prefix + "\"milkVol"+ postFix + "\":" + 
						(recValue == null || recValue.getMilkVolume() == null ? "\"\"" : recValue.getMilkVolume()) + ",\n";
		json += prefix + "\"event"+ postFix + "Time\":\"" + 
						(recValue == null || recValue.getRecordTime() == null ? "" : Util.getTimeInSQLFormart(recValue.getRecordTime())) + "\",\n";
		json += prefix + "\"event"+ postFix + "Temperature\":" +
						(recValue == null ? "\"\"" : recValue.getTemperatureInCentigrade())  + ",\n";
		json += prefix + "\"event"+ postFix + "Humidity\":" + 
						(recValue == null ? "\"\"" : recValue.getHumidity()) + ",\n";
		json += prefix + "\"event"+ postFix + "Comments\":\"" + 
						(recValue == null || recValue.getComments()== null ? "" : recValue.getComments()) + "\"" + commaOrBracket+ "\n";
		return json;
	}	
	
}





