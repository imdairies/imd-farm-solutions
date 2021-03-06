package com.imd.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.InputDelimitedFileBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.TagVolumeCommentTriplet;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;
@Path("/milkinginfo")
public class MilkingInformationSrvc {


	@POST
	@Path("/lactatingcowsmilkrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimalsMilkRecord(MilkingDetailBean selectedDateSearchBean) {
		//TODO: Improve the implementation of this service. It is inefficient and counter intuitive. 
		// TODO: The monthly average averages across the month in all of the lifetime of the cow instead of only for one month i.e.
		// Feb average will be for Febs of all the years, instead of only the Feb for a given year. This is not the right way to deduce monthly average.
		String methodName = "retrieveLactatingAnimalsMilkRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,selectedDateSearchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		selectedDateSearchBean.setOrgID(orgID);
		IMDLogger.log(selectedDateSearchBean.toString(), Util.INFO);

    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		MilkingDetailLoader milkingLoader = new MilkingDetailLoader();
    		List<Animal> animalValues = null;
    		if (selectedDateSearchBean.getRecordDate().isBefore(LocalDate.now(IMDProperties.getServerTimeZone()))) {
    			// a past date has been selected so we bring all animals who had a milk record for that date 
    			animalValues = loader.retrieveAnimalsMilkedAtSpecificMilkingEvent(selectedDateSearchBean.getOrgID(),selectedDateSearchBean.getRecordDate(),selectedDateSearchBean.getMilkingEventNumber());    			
    		} 
    		if (animalValues == null || animalValues.size() == 0) {
    			// case when the date is current or future OR when a past date does not have any milking entry. In such cases 
    			// we bring in all currently lactating animals. 
    			animalValues = loader.retrieveActiveLactatingAnimals(selectedDateSearchBean.getOrgID());
    		}
    		
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		selectedDateSearchBean.setAnimalTag(animalValue.getAnimalTag());
	    		MilkingDetailBean previousDateSearchBean = new MilkingDetailBean(selectedDateSearchBean);
	    		previousDateSearchBean.setRecordDate(selectedDateSearchBean.getRecordDate().minusDays(1));
	    		List <MilkingDetail> selectedDateMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(selectedDateSearchBean, true);
	    		List <MilkingDetail> prevDayMilkingInfo = null;
	    		if (selectedDateMilkingInfo == null || selectedDateMilkingInfo.size() == 0)
	    			prevDayMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(previousDateSearchBean, true);
	    		else
	    			prevDayMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(previousDateSearchBean, false);
	    		
	    		animalValueResult +=  appendMilkingDetails(selectedDateMilkingInfo,prevDayMilkingInfo,selectedDateSearchBean);
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
	    	IMDLogger.log(animalValueResult, Util.INFO);
    	} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc." + methodName + "() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\" One or more input parameters were invalid. Please review the input parameters, especially the timestamps and try again. The timestamps must be in yyyy-dd-mm hh:mm format." + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc." + methodName + "() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	private String appendMilkingDetails(List<MilkingDetail> selectedDaysMilkingRecords, List<MilkingDetail> previousDaysMilkingRecords, MilkingDetailBean searchBean) throws IMDException {
		String milkingDetail = "";
		String prefix = "  ";
		if (selectedDaysMilkingRecords == null || selectedDaysMilkingRecords.size() == 0) {
	    	MilkingDetail recordDetail = new MilkingDetail(searchBean.getOrgID(), searchBean.getAnimalTag(), 
	    			(short) 0, true, null, null, null, (short) 0);
	    	recordDetail.setAdditionalStatistics(getPreviousDaysVolumeForAnimal(searchBean.getAnimalTag(),previousDaysMilkingRecords));
	    	Float daysInMilking = getDaysInMilking(searchBean.getOrgID(), searchBean.getAnimalTag(),searchBean.getRecordDate());
			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.DAYS_IN_MILKING, daysInMilking < 0 ? null : daysInMilking);
	    	milkingDetail = "{\n" + recordDetail.dtoToJson(prefix/*,DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")*/) + "\n},\n";
		} else {
	    	Iterator<MilkingDetail> recordsIt = selectedDaysMilkingRecords.iterator();
	    	while (recordsIt.hasNext()) {
	    		MilkingDetail recordDetail = recordsIt.next();
	    		HashMap<String, Float> prevDayValues = getPreviousDaysVolumeForAnimal(recordDetail.getAnimalTag(),previousDaysMilkingRecords);
				recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.DAYS_IN_MILKING, getDaysInMilking(searchBean.getOrgID(), searchBean.getAnimalTag(),searchBean.getRecordDate()));
	    		if (prevDayValues != null) {
	    			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL, prevDayValues.get(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL));
	    		}
	    		milkingDetail += "{\n" + recordDetail.dtoToJson(prefix/*,*DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")*/) + "\n},\n";
	    		IMDLogger.log(milkingDetail, Util.INFO);
	    	}
		}
		return milkingDetail;
	}	
	private Float getDaysInMilking(String orgID, String animalTag, LocalDate toDateForDaysInMilking) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		return (float)(loader.getDaysInMilkingOfCow(orgID, animalTag, toDateForDaysInMilking));
	}
	private HashMap <String, Float> getPreviousDaysVolumeForAnimal(String animalTag, List<MilkingDetail> previousDaysMilkingRecords) {
		HashMap <String, Float> values = null;
    	Iterator<MilkingDetail> recordsIt = previousDaysMilkingRecords.iterator();
    	while (recordsIt.hasNext()) {
    		MilkingDetail recordDetail = recordsIt.next();
    		if (recordDetail.getAnimalTag().equalsIgnoreCase(animalTag)) {
    			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL, recordDetail.getMilkVolume());
    			values = recordDetail.getAdditionalStatistics();
    			break;
    		}
    	}
    	return values;
	}

	@POST
	@Path("/milkingrecordofyear")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedYear(MilkingDetailBean searchBean){
		String methodName = "retrieveMilkingRecordOfSpecifiedYear";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkMonthList = "";
		String monthlyVolList = "";
		String monthlyAverageList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
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
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }
	
	@POST
	@Path("/milkingrecordofmonth")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedMonth(MilkingDetailBean searchBean){

		String methodName = "retrieveMilkingRecordOfSpecifiedMonth";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkDayList = "";
		String dailyVolList = "";
		String dailyLactatingAnimalsList = "";
		String dailyAverageList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedMonth(new LocalDate(searchBean.getRecordDate()),true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			while (it.hasNext()) {
				milkRec = it.next();
				milkDayList += milkRec.getRecordDate().getDayOfMonth() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
//				dailyLactatingAnimalsList += milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT) + ",";
				dailyLactatingAnimalsList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT) == null ? 0.0 : milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT))+ ",";
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
			if (dailyLactatingAnimalsList != null) {
				int commatoremove = dailyLactatingAnimalsList.lastIndexOf(",");
				dailyLactatingAnimalsList = dailyLactatingAnimalsList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" +  "\n" + prefix + prefix + "\"max\":600, " + "\n" + prefix + prefix + "\"min\":100, " + "\n" + prefix + prefix + "\"days\":[" + milkDayList + "],\n"  + prefix + prefix + "\"averages\":[" + dailyAverageList + "],\n"  + prefix + prefix + "\"volumes\":[" + dailyVolList + "],\n" + prefix + prefix + "\"milkedAnimals\":[" + dailyLactatingAnimalsList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(" The following exception occurred while processing " + methodName + " request: " + e.getMessage())) + "}").build();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }		

	
	@POST
	@Path("/milkingrecordofananimalforspecifiedmonthpair")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfSpecifiedAnimal(MilkingDetailBean searchBean){

		String methodName = "retrieveMilkingRecordOfSpecifiedAnimal";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkDayList = "";
		String dailyVolList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	IMDLogger.log(methodName + " called", Util.INFO);

		MilkingDetailLoader loader = new MilkingDetailLoader();
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
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(" The following exception occurred while processing " + methodName + " request: " + e.getMessage())) + "}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }			
	
	@POST
	@Path("/milkingrecordofeachdayofyear")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMilkingRecordOfEachDayOfYear(MilkingDetailBean searchBean){

		String methodName = "retrieveMilkingRecordOfEachDayOfYear";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkDayList = "";
		String dailyVolList = "";
		String dateList = "";
		String milkingRecordInformation = "";
		String dailyLactatingAnimalsList = "";
		String dailyAverageList = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
			MilkingDetail[]  milkRecords = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(searchBean.getRecordDate());
			MilkingDetail milkRec = null;
			for (int i=0; i<milkRecords.length; i++) {
				milkRec = milkRecords[i];
				milkDayList += milkRec.getRecordDate().getDayOfYear() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
				dailyLactatingAnimalsList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT) == null ? 0.0 : milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT))+ ",";
				dailyAverageList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE) == null ? 0.0 : Math.round(((float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE))*10))/10 + ",";
				dateList += "\"" + Util.getDateInSpecifiedFormart(milkRec.getRecordDate(),"dd-MMM") + "\",";
			}
			if (dailyAverageList != null) {
				int commatoremove = dailyAverageList.lastIndexOf(",");
				dailyAverageList = dailyAverageList.substring(0,commatoremove);
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
			if (dailyLactatingAnimalsList != null) {
				int commatoremove = dailyLactatingAnimalsList.lastIndexOf(",");
				dailyLactatingAnimalsList = dailyLactatingAnimalsList.substring(0,commatoremove);
			}
			milkingRecordInformation += "[" + "\n" + prefix + "{" +  "\n" + prefix + prefix + "\"max\":600, " + "\n" + prefix + prefix + "\"min\":100, " + "\n" + prefix + prefix +  "\"days\":[" + milkDayList + "],\n"   + prefix + prefix + "\"averages\":[" + dailyAverageList + "],\n" + prefix + prefix +  "\"dates\":[" + dateList + "],\n"  + prefix + prefix  + "\"milkedAnimals\":[" + dailyLactatingAnimalsList + "],\n" + prefix + prefix + "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }		
		
	@POST
	@Path("/addfarmmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addFarmMilkingRecord(FarmMilkingDetailBean milkingEventRecord){

		String methodName = "addFarmMilkingRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,milkingEventRecord.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		milkingEventRecord.setOrgID(orgID);
		IMDLogger.log(milkingEventRecord.toString(), Util.INFO);

		String prefix = "   ";
    	IMDLogger.log(milkingEventRecord.toString(), Util.INFO);
    	try {
			if (milkingEventRecord.getMilkingEventNumber() <1)
			{
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			}
			
			if (milkingEventRecord.getTemperatureInCentigrade() == null)
			{
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid temperature\"}").build();
			}
			
			if (milkingEventRecord.getRecordDate() == null)
			{
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking timestamp\"}").build();
			}
			
			if (milkingEventRecord.getRecordTime() == null)
			{
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking timestamp\"}").build();
			}
			
			if (milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().isEmpty())
			{
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify at least one cow's milking information\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		MilkingDetail farmMilkingDetail = new MilkingDetail(milkingEventRecord);
    		farmMilkingDetail.setCreatedBy(user);
    		farmMilkingDetail.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
    		farmMilkingDetail.setUpdatedBy(user);
    		farmMilkingDetail.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
    		String farmMilkingEventAddOutcome = loader.addOrUpdateFarmMilkingRecord(farmMilkingDetail, false /*inputInformation.isDonotOverwrite()*/);
    		if (farmMilkingEventAddOutcome == null) {
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"One or more required parameters were missing in the request. Farm Milking Information could not be updated" + "\"}").build();
    		}
    		IMDLogger.log("Farm Milking Record was " + farmMilkingEventAddOutcome,Util.INFO);
    		List<TagVolumeCommentTriplet> outcomeInfo = loader.addOrEditFarmAnimalsMilkingEventRecords(milkingEventRecord,
    				user, DateTime.now(IMDProperties.getServerTimeZone()),
    				user, DateTime.now(IMDProperties.getServerTimeZone()),
    				false);
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
    		return Response.status(Util.HTTPCodes.OK).entity(outcomeString).build();

    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    }
	
	@POST
	@Path("/uploadfarmmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response uploadFarmMilkingEventRecord(InputDelimitedFileBean inputInformation) {

		String methodName = "uploadFarmMilkingEventRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inputInformation.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		IMDLogger.log(inputInformation.toString(), Util.INFO);

		String prefix = "   ";
    	IMDLogger.log(inputInformation.toString(), Util.INFO);
    	try {
    		if (inputInformation == null || inputInformation.getInputDelimitedFileContents() == null) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Unable to parse the information. The service seemed to have been called with invalid or missing parameter (inputDelimitedFileContents)\"}").build();    			
    		}
        	FarmMilkingDetailBean milkingEventRecord = Util.parseFarmMilkingDetailBean(inputInformation);
    		IMDLogger.log(milkingEventRecord.toString(), Util.INFO);
        	
        	milkingEventRecord.setOrgID(orgID);
        	if (milkingEventRecord.getTemperatureInCentigrade() == null) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid Temperature \"}").build();
			} else if (milkingEventRecord.getMilkingDateStr() == null) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking date \"}").build();
			} else if (milkingEventRecord.getMilkingTimeStr() == null) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking time \"}").build();
			} else if (milkingEventRecord.getMilkingEventNumber() <1) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			} else if (milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().isEmpty()) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify at least one milking record\"}").build();
			} else if (milkingEventRecord.getRecordDate().isAfter(LocalDate.now(IMDProperties.getServerTimeZone()))) {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You can not specify a future date \"}").build();				
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
						"\"ph\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getPhValue()) + "\",\n" +
						"\"forCalvesUse\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForCalvesUse()) + "\",\n" +
						"\"forFarmUse\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForFarmUse()) + "\",\n" +
						"\"forFamilyUse\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForFamilyUse()) + "\",\n" +
						"\"forPersonalUse\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForPersonalUse()) + "\",\n" +
						"\"forOtherUse\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForOtherUse()) + "\",\n" +
						"\"forWasteAdj\":\"" + Util.substituteEmptyForNull(milkingEventRecord.getForWasteAdj()) + "\",\n" +
						"\"message\":\"All recrods have been successfully parsed\",\n" +
						"\"totalMilkRecords\":\"" + milkingEventRecord.getFarmMilkingEventRecords().size() + "\",\n";
				Iterator<TagVolumeCommentTriplet> it = milkingEventRecord.getFarmMilkingEventRecords().iterator();
				String milkRecords = "";
				int count = 1;
				String cowTags = "";
				String comma = ",";
				while (it.hasNext()) {
					TagVolumeCommentTriplet record = it.next();
					if (count == milkingEventRecord.getFarmMilkingEventRecords().size())
						comma ="";
					count++;
					cowTags += " " + record.getTag() + comma;
					milkRecords += "\n  {\n   \"tag\":\"" + record.getTag() + "\",\n" + 
							"   \"volume\":\"" + record.getVolume() + "\",\n" + 
							"   \"comments\":\"" + Util.substituteEmptyForNull(record.getComments()) + "\"\n  }" + comma;
					totalVolume += Float.parseFloat(record.getVolume());
				}
				float notForSale = (milkingEventRecord.getForCalvesUse() == null ? 0f : milkingEventRecord.getForCalvesUse()) +
									(milkingEventRecord.getForFamilyUse()  == null ? 0f : + milkingEventRecord.getForFamilyUse()) +
									(milkingEventRecord.getForFarmUse()  == null ? 0f : + milkingEventRecord.getForFarmUse()) +
									(milkingEventRecord.getForOtherUse()  == null ? 0f : + milkingEventRecord.getForOtherUse()) +
									(milkingEventRecord.getForPersonalUse()  == null ? 0f : + milkingEventRecord.getForPersonalUse()) +
									(milkingEventRecord.getForWasteAdj() == null ? 0f : milkingEventRecord.getForWasteAdj());
				IMDLogger.log("Available for Sale: " + (totalVolume - notForSale), Util.INFO);
				parseResult +=
						"\"totalVolume\":\"" + totalVolume + "\",\n"  +
						"\"availableForSale\":\"" + (totalVolume - notForSale) + "\",\n"  +
						"\"parsedCowTags\":\"" + cowTags + "\",\n"  +
						"\"milkingRecords\":[";
				
				parseResult += milkRecords + "]\n}"; 
				IMDLogger.log(parseResult, Util.INFO);
				return Response.status(Util.HTTPCodes.OK).entity(parseResult).build();
			} else {
	    		MilkingDetailLoader loader = new MilkingDetailLoader();
	    		MilkingDetail farmMilkingDetail = new MilkingDetail(milkingEventRecord);
	    		farmMilkingDetail.setCreatedBy(user);
	    		farmMilkingDetail.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
	    		farmMilkingDetail.setUpdatedBy(farmMilkingDetail.getCreatedBy());
	    		farmMilkingDetail.setUpdatedDTTM(farmMilkingDetail.getCreatedDTTM());
	    		
	    		String farmMilkingEventAddOutcome = loader.addOrUpdateFarmMilkingRecord(farmMilkingDetail, false /*inputInformation.isDonotOverwrite()*/);
	    		if (farmMilkingEventAddOutcome == null) {
	    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"One or more required parameters were missing in the request. Farm Milking Information could not be updated" + "\"}").build();
	    		}
	    		IMDLogger.log("Farm Milking Record was " + farmMilkingEventAddOutcome,Util.INFO);
	    		List<TagVolumeCommentTriplet> outcomeInfo = loader.addOrEditFarmAnimalsMilkingEventRecords(milkingEventRecord, 
	    				user, DateTime.now(IMDProperties.getServerTimeZone()),
	    				user, DateTime.now(IMDProperties.getServerTimeZone()),inputInformation.isDonotOverwrite());
	    		if (outcomeInfo == null) {
	    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"One or more required parameters were missing in the request. Farm Milking Information could not be updated" + "\"}").build();
	    		}
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
	    		return Response.status(Util.HTTPCodes.OK).entity(outcomeString).build();
			}

    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + " " + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(e.getMessage())) + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getMessage() + "\"}").build();
		}
    }	
	
	@POST
	@Path("/addmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addCowMilkingRecord(MilkingDetailBean milkingRecord){
		
		String methodName = "addCowMilkingRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,milkingRecord.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		milkingRecord.setOrgID(orgID);
		IMDLogger.log(milkingRecord.toString(), Util.INFO);

    	int responseCode = 0;
    	milkingRecord.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(milkingRecord.toString(), Util.INFO);
    	try {
			if (milkingRecord.getAnimalTag() == null || milkingRecord.getAnimalTag().isEmpty())
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
			if (milkingRecord.getMilkingEventNumber() <1)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			}
			if (!(milkingRecord.getMilkVolume() > 0))
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid milking volume\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		MilkingDetail milkingDetail = new MilkingDetail(milkingRecord);
    		milkingDetail.setCreatedBy(user);
    		milkingDetail.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
    		milkingDetail.setUpdatedBy(milkingDetail.getCreatedBy());
    		milkingDetail.setUpdatedDTTM(milkingDetail.getCreatedDTTM());
    		
    		responseCode = loader.insertMilkRecord(milkingDetail);
    		if (responseCode == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION || responseCode ==Util.ERROR_CODE.DUPLICATE_ENTRY)
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
    		else
    			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + responseCode + " record added" + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    }	
	
	
	@POST
	@Path("/monthlymilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMonthlyMilkingRecord(MilkingDetailBean searchBean){
		
		String methodName = "retrieveMonthlyMilkingRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

    	String milkingInformation = "";
    	List<MilkingDetail> dailyRecords = new ArrayList<MilkingDetail>(3);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	LocalDate currentDate = null;
    	try {
			if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		AnimalLoader animalLoader = new AnimalLoader();
    		AnimalBean animalBean = new AnimalBean();
    		animalBean.setOrgId(searchBean.getOrgID());
    		animalBean.setAnimalTag(searchBean.getAnimalTag());    		
    		List<Animal> animals = animalLoader.getAnimalRawInfo(animalBean);
    		if (animals.size() != 1) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Unable to find the animal. foud " + animals.size() + " records\"}").build();    			    			
    		}
    		Animal animal = animals.get(0);
    		IMDLogger.log(animal.getAnimalType(), Util.INFO);
    		if (!animal.getGender().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE+"")) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information only applies to female animals\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.FEMALECALF)) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to female calves\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.HEIFER)) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to heifers\"}").build();    			
    		}    		
			List<MilkingDetail> animalValues = loader.retrieveMonthlyMilkingRecordsOfCow(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
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
	    	}
    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + "\n";
	    	if (milkingInformation != null && !milkingInformation.trim().isEmpty() )
	    		milkingInformation = "[" + milkingInformation + "]";
	    	else
	    		milkingInformation = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity(milkingInformation).build();
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

	
	@POST
	@Path("/completemilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveCompleteMilkingRecordOfFarm(MilkingDetailBean searchBean){

		String methodName = "retrieveCompleteMilkingRecordOfFarm";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkDayList = "";
		String dailyVolList = "";
		String dateList = "";
		String milkingRecordInformation = "";
		String dailyLactatingAnimalsList = "";
		String dailyAverageList = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	MilkingDetail milkRec = null;
    	try {
			MilkingDetail[]  milkRecords = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(null, null);
			for (int i=0; i<milkRecords.length; i++) {
				milkRec = milkRecords[i];
				milkDayList += milkRec.getRecordDate().getDayOfYear() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
				dailyLactatingAnimalsList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT) == null ? 0.0 : milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT))+ ",";
				dailyAverageList += (milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE) == null ? 0.0 : Math.round(((float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE))*10))/10 + ",";
				dateList += "\"" + Util.getDateInSpecifiedFormart(milkRec.getRecordDate(),"dd-MMM-YYYY") + "\",";
			}
			if (dailyAverageList != null) {
				int commatoremove = dailyAverageList.lastIndexOf(",");
				dailyAverageList = dailyAverageList.substring(0,commatoremove);
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
			if (dailyLactatingAnimalsList != null) {
				int commatoremove = dailyLactatingAnimalsList.lastIndexOf(",");
				dailyLactatingAnimalsList = dailyLactatingAnimalsList.substring(0,commatoremove);
			}
			String title = milkRecords != null && milkRecords.length > 0 ? 
					Util.getDateInSpecifiedFormart(milkRecords[0].getRecordDate(), "MMM YYYY")  + " - " + Util.getDateInSpecifiedFormart(milkRecords[milkRecords.length-1].getRecordDate(), "MMM YYYY") 
					: "";
			milkingRecordInformation += "[" + "\n" + prefix + "{" +  "\n" + prefix + prefix + "\"title\":\"" + title + "\"," + "\n" + prefix + prefix + "\"max\":600, " + "\n" + prefix + prefix + "\"min\":100, " + "\n" + prefix + prefix +  "\"days\":[" + milkDayList + "],\n"   + prefix + prefix + "\"averages\":[" + dailyAverageList + "],\n" + prefix + prefix +  "\"dates\":[" + dateList + "],\n"  + prefix + prefix  + "\"milkedAnimals\":[" + dailyLactatingAnimalsList + "],\n" + prefix + prefix + "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception occurred while processing milk information for the following: " +  milkRec, Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }		
		
	@POST
	@Path("/completemilkingrecordofanimal")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveCompleteMilkingRecordOfAnimal(MilkingDetailBean searchBean){

		String methodName = "retrieveCompleteMilkingRecordOfAnimal";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String milkDayList = "";
		String recordCount = "";
		String dailyVolList = "";
		String dateList = "";
		String milkingRecordInformation = "";
		String prefix = "   ";
    	
		MilkingDetailLoader loader = new MilkingDetailLoader();
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	MilkingDetail milkRec = null;
    	if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag whose milk information you wish to retrieve \"}").build();    		
    	}
    	AnimalLoader animalLdr = new AnimalLoader();
    	LocalDate startDate = null;
    	try {
			List<Animal> animal = animalLdr.getAnimalRawInfo(searchBean.getOrgID(), searchBean.getAnimalTag());
			if (animal != null && !animal.isEmpty() && animal.get(0).getDateOfBirth() != null) {
				if (searchBean.getDataStartReferenceEvent() != null &&
					!searchBean.getDataStartReferenceEvent().isEmpty() &&
					searchBean.getDataStartReferenceEvent().equalsIgnoreCase(Util.LifeCycleEvents.BIRTH)) {
					startDate = new LocalDate(animal.get(0).getDateOfBirth(), IMDProperties.getServerTimeZone());
				} else {
					LifeCycleEventsLoader lcycleLoader = new LifeCycleEventsLoader();
					List<LifecycleEvent> insemEvents = lcycleLoader.retrieveSpecificLifeCycleEventsForAnimal(searchBean.getOrgID(), searchBean.getAnimalTag(), 
							null, null, Util.LifeCycleEvents.ABORTION, Util.LifeCycleEvents.PARTURATE, null, null, 
							null, null);
					if (insemEvents == null || insemEvents.isEmpty())
						return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An animal that has ever lactated must have a calving or abortion event. Can not retrieve the milking information\"}").build();    		
					
					startDate = new LocalDate(insemEvents.get(insemEvents.size()-1).getEventTimeStamp(), IMDProperties.getServerTimeZone());
				}
			}
			else
				startDate = new LocalDate(1900,1,1);
		} catch (Exception e) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Could not retrieve the animal information.\"}").build();    		
		}
    	
    	try {
			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgID, searchBean.getAnimalTag(), 
					startDate, null, true);
			for (int i=0; i < milkRecords.size(); i++) {
				milkRec = milkRecords.get(i);
				milkDayList += milkRec.getRecordDate().getDayOfYear() + ",";
				dailyVolList += milkRec.getMilkVolume() + ",";
				recordCount += i + ",";
				dateList += "\"" + Util.getDateInSpecifiedFormart(milkRec.getRecordDate(),"dd-MMM-YYYY") + "\",";
			}
			if (!recordCount.isEmpty()) {
				int commatoremove = recordCount.lastIndexOf(",");
				recordCount = commatoremove < 0 ? recordCount : recordCount.substring(0,commatoremove);
			}
			if (dateList != null) {
				int commatoremove = dateList.lastIndexOf(",");
				dateList = commatoremove < 0 ? dateList : dateList.substring(0,commatoremove);
			}
			if (milkDayList != null) {
				int commatoremove = milkDayList.lastIndexOf(",");
				milkDayList = commatoremove < 0 ? milkDayList : milkDayList.substring(0,commatoremove);
			}
			if (dailyVolList != null) {
				int commatoremove = dailyVolList.lastIndexOf(",");
				dailyVolList = commatoremove < 0 ? dailyVolList : dailyVolList.substring(0,commatoremove);
			}
			String title = milkRecords != null && milkRecords.size() > 0 ? 
					Util.getDateInSpecifiedFormart(milkRecords.get(0).getRecordDate(), "MMM YYYY")  + " - " + Util.getDateInSpecifiedFormart(milkRecords.get(milkRecords.size()-1).getRecordDate(), "MMM YYYY") 
					: "";
			milkingRecordInformation += "[" + "\n" + prefix + "{" +  "\n" 
					+ prefix + prefix + "\"title\":\"" + title + "\"," + "\n" 
					+ prefix + prefix + "\"max\":600, " + "\n" 
					+ prefix + prefix + "\"min\":100, " + "\n" 
					+ prefix + prefix +  "\"count\":[" + recordCount + "],\n"   
					+ prefix + prefix +  "\"days\":[" + milkDayList + "],\n"   
					+ prefix + prefix +  "\"dates\":[" + dateList + "],\n"  
					+ prefix + prefix + "\"volumes\":[" + dailyVolList + "]\n" + prefix + "}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception occurred while processing milk information for the animal " + searchBean.getAnimalTag() + " for the following record: " +  milkRec, Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }		

	@POST
	@Path("/retrievenextmilkingevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveNextMilkingEvent(MilkingDetailBean searchBean){

		String methodName = "retrieveNextMilkingEvent";
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String milkingRecordInformation = "[]";
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		MilkingDetailLoader loader = new MilkingDetailLoader();
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	MilkingDetail lastValidMilkRec = new MilkingDetail();
    	try {
    		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
			lastValidMilkRec = loader.getLatestMilkingEvent(orgID);
			if (lastValidMilkRec.getRecordDate() == null || 
					lastValidMilkRec.getRecordTime() == null) {
				lastValidMilkRec.setRecordDate(LocalDate.now(IMDProperties.getServerTimeZone()));
				lastValidMilkRec.setMilkingEventNumber(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY);
				lastValidMilkRec.setRecordTime(fmt.parseLocalTime(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY_TIME));
			} else {
				if (lastValidMilkRec.getMilkingEventNumber() == Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY) {
					lastValidMilkRec.setMilkingEventNumber(Util.DefaultValues.SECOND_MILKING_EVENT_OF_THE_DAY);
					lastValidMilkRec.setRecordTime(fmt.parseLocalTime(Util.DefaultValues.SECOND_MILKING_EVENT_OF_THE_DAY_TIME));
				} else if (lastValidMilkRec.getMilkingEventNumber() == Util.DefaultValues.SECOND_MILKING_EVENT_OF_THE_DAY) {
					lastValidMilkRec.setMilkingEventNumber(Util.DefaultValues.THIRD_MILKING_EVENT_OF_THE_DAY);
					lastValidMilkRec.setRecordTime(fmt.parseLocalTime(Util.DefaultValues.THIRD_MILKING_EVENT_OF_THE_DAY_TIME));
				} else if (lastValidMilkRec.getMilkingEventNumber() == Util.DefaultValues.THIRD_MILKING_EVENT_OF_THE_DAY) {
					lastValidMilkRec.setMilkingEventNumber(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY);
					lastValidMilkRec.setRecordTime(fmt.parseLocalTime(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY_TIME));
					lastValidMilkRec.setRecordDate(lastValidMilkRec.getRecordDate().plusDays(1));
				} else {
					return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An Invalid Milking event number (" + lastValidMilkRec.getMilkingEventNumber() + ") was recorded the last time the milking record was saved on : " 
							+ Util.getDateInSQLFormat(lastValidMilkRec.getRecordDate()) + ". Please fix the milking event number for this date and try again. Error in : " + methodName + "\"}").build();					
				}
			}
			milkingRecordInformation = "[\n{\n" +  lastValidMilkRec.dtoToJson("   ",DateTimeFormat.forPattern("yyyy-MM-dd")) + "   \n}\n]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception occurred while retrieving last milk event information", Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingRecordInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingRecordInformation).build();
    }		

	@POST
	@Path("/farmmilkrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFarmMilkRecord(MilkingDetailBean selectedDateSearchBean) {
		String methodName = "retrieveFarmMilkRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,selectedDateSearchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		selectedDateSearchBean.setOrgID(orgID);
		IMDLogger.log(selectedDateSearchBean.toString(), Util.INFO);

    	String farmMilkingInformation = "";
    	try {
    		MilkingDetailLoader milkingLoader = new MilkingDetailLoader();
    		if (selectedDateSearchBean.getRecordDate() == null || selectedDateSearchBean.getMilkingEventNumber() <= 0) {
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"One or more required parameters were missing. You must specify both milkingDateStr (in yyyy-mm-dd format) and milkingEventNumber. \"}").build();
    		}
    		List<MilkingDetail> farmMilkingRecords = milkingLoader.retrieveFarmMilkRecord(selectedDateSearchBean);
    		if (farmMilkingRecords != null && farmMilkingRecords.size() > 1) {
    			return Response.status(Util.HTTPCodes.INTERNAL_SERVER_ERROR).entity("{ \"error\": true, \"message\":\"We expected to receive only ZERO OR ONE Farm Milk Record for the requested date and milking even number; instead we received " + farmMilkingRecords.size() + ". Please submit a bug report.\"}").build();
    		} else if (farmMilkingRecords.size() == 1) {
    			farmMilkingInformation = "[{\n" + farmMilkingRecords.get(0).dtoToJson("  ") + "}]";    			
    		} else {
    			farmMilkingInformation = "[]";
    		}
	    	IMDLogger.log(farmMilkingInformation, Util.INFO);
    	} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc." + methodName + "() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\" One or more input parameters were invalid. Please review the input parameters, especially the timestamps and try again. The timestamps must be in yyyy-dd-mm hh:mm format." + "\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc." + methodName + "() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following exception occurred while processing " + methodName + " request: " + e.getClass() + ' ' + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(farmMilkingInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(farmMilkingInformation).build();
    }	
	
//	@POST
//	@Path("/addmilkingrecord")
//	@Consumes (MediaType.APPLICATION_JSON)
//	public Response addCowMilkingRecord(MilkingDetailBean milkingRecord){
//		String methodName = "addCowMilkingRecord";
//		IMDLogger.log(methodName + " Called ", Util.INFO);
//		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,milkingRecord.getLoginToken(),/*renewToken*/ true);
//		if (user == null) {
//			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
//					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
//					+ this.getClass().getName() +  "." + methodName, Util.INFO);
//			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
//		}
//		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
//		milkingRecord.setOrgID(orgID);
//		IMDLogger.log(milkingRecord.toString(), Util.INFO);
//
//    	int responseCode = 0;
//    	try {
//			if (milkingRecord.getAnimalTag() == null || milkingRecord.getAnimalTag().isEmpty())
//			{
//				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
//			}
//			if (milkingRecord.getMilkingEventNumber() <1)
//			{
//				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
//			}
//			if (!(milkingRecord.getMilkVolume() > 0))
//			{
//				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid milking volume\"}").build();
//			}
//    		MilkingDetailLoader loader = new MilkingDetailLoader();
//    		MilkingDetail milkingDetail = new MilkingDetail(milkingRecord);
//    		responseCode = loader.insertMilkRecord(milkingDetail);
//    		if (responseCode == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
//    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
//    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
//    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
//    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
//    		else
//    			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + responseCode + " record added" + "\"}").build();
//    	} catch (Exception e) {
//			e.printStackTrace();
//			IMDLogger.log("Exception in AnimalSrvc.addCowMilkingRecord() service method: " + e.getMessage(),  Util.ERROR);
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. " +  e.getMessage() + "\"}").build();
//		}
//    }	
//	
//	
	
}





