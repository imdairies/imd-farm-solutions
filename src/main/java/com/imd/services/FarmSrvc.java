package com.imd.services;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LookupValuesLoader;
import com.imd.services.bean.DurationBean;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

@Path("/farm")
public class FarmSrvc {
	
	@POST
	@Path("/herdsizehistory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getHerdSizeHistory(DurationBean duration){
    	IMDLogger.log(" getHerdSizeHistory Service called with the following input : " + duration.toString(), Util.INFO);
    	String outputJson = "";
    	String prefix = "  ";
		AnimalLoader loader = new AnimalLoader();
		String months = "";
		String herdCounts = "";

    	try {
    		if (duration.getStart() == null || duration.getStart().isEmpty()) {
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify Herd Size trend start duration\"}").build();
    		}
    		if (duration.getEnd() == null || duration.getEnd().isEmpty()) {
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify Herd Size trend end duration\"}").build();
    		}
    		if (duration.getSteps() == 0) {
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify step size. Step size indicates the number of months between two herd counts.\"}").build();
    		}
    		
    		LocalDate startDate = new LocalDate(duration.getStart());
    		LocalDate endDate = new LocalDate(duration.getEnd());
    		if (startDate.isAfter(endDate))
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"Start duration must be smaller than the end duration\"}").build();
    		LocalDate processingDate = startDate;
    		int count;
    		String comma = ",";
    		boolean endDateProcessed = false;
    		while (processingDate.compareTo(endDate)<=0) {
    			if (processingDate.getMonthOfYear() == endDate.getMonthOfYear() && 
    					processingDate.getYear() == endDate.getYear()) {
    				processingDate = endDate;
    				comma = "";
    				endDateProcessed = true;
    			}
    			count = loader.getActiveHerdCountForDate(processingDate);
    			months += "\"" +  Util.getDateInSpecifiedFormart(processingDate,"dd MMM yy") + "\"" + comma;
    			herdCounts +=  count + comma;
    			if (processingDate.isEqual(startDate)) {
    				processingDate = deduceNextProcessingDateAfterStartDate(duration.getSteps(), startDate);     				
    			} else {
    				processingDate = processingDate.plusMonths(duration.getSteps());
    			}
    		}
    		if (!endDateProcessed /*processingDate.isAfter(endDate)*/) {
    			count = loader.getActiveHerdCountForDate(endDate);
    			months += "\"" +  Util.getDateInSpecifiedFormart(endDate,"dd MMM yy") + "\"";
    			herdCounts +=  count;    			
    		}
    			
    		outputJson += "{" + "\n" + prefix + prefix + "\"months\":[" + months + "],\n" + prefix + prefix + "\"herdCounts\":[" + herdCounts + "]\n" + prefix + "}";
    		IMDLogger.log(outputJson, Util.INFO);    		
    	} catch (java.lang.IllegalArgumentException ex) {
			ex.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Invalid format of start and/or end duration. Allowed format is yyyy-MM-dd\"}").build();    			    		
    	} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(200).entity(outputJson).build();
    }

	/**
	 * Its more intuitive to have the herd counted first of every month. The very first herd count will be on the startDate and the very
	 * last count will be on the end date; but between these two dates the counts will be done on the first of the step month(s).
	 * @param duration
	 * @param startDate
	 * @return
	 */
	private LocalDate deduceNextProcessingDateAfterStartDate(int numberOfMonthsBetweenEachCount, LocalDate startDate) {
		LocalDate processingDate;
		processingDate = startDate.plusMonths(numberOfMonthsBetweenEachCount);
		int year = processingDate.getYear();
		int month = processingDate.getMonthOfYear();
		int dayofmonth = 1;
		processingDate = new LocalDate(year,month,dayofmonth);
		return processingDate;
	}	

	/**
	 * This API adds a new Lookup Value.
	 * Sample Use Case: Call this API to add a new lookup value.
	 * @param luValueBean
	 * @return
	 */
	@POST
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addLookupValues(LookupValuesBean luValueBean){
		String categoryCode = luValueBean.getCategoryCode();
		String lookupCode = luValueBean.getLookupValueCode();
		String shortDescription  = luValueBean.getShortDescription();
		String longDescription  = luValueBean.getLongDescription();
		IMDLogger.log("addLookupValues Called with following input values", Util.INFO);
		IMDLogger.log(luValueBean.toString() , Util.INFO);
		
		if (categoryCode == null || categoryCode.trim().isEmpty()) {			
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid category code.\"}").build();
		}
		if (lookupCode == null || lookupCode.trim().isEmpty()) {			
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid lookup code.\"}").build();
		}
		if (shortDescription == null || shortDescription.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Short Description.\"}").build();
		}
		if (longDescription == null || longDescription.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Long Description.\"}").build();
		}
		LookupValues luValue = new LookupValues(luValueBean);
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LookupValuesLoader loader = new LookupValuesLoader();
			luValue.setCreatedBy(new User(userID));
			luValue.setCreatedDTTM(DateTime.now());
			luValue.setUpdatedBy(new User(userID));
			luValue.setUpdatedDTTM(DateTime.now());
			result = loader.insertLookupValues(luValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result == 1)
			return Response.status(200).entity("{ \"error\": false, \"message\":\"New lookup value has been created successfully\"}").build();
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified catefory-lookup value '" + categoryCode + "-" + lookupCode + "' already exists\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Lookup '"  + categoryCode + "-" + lookupCode + "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Lookup '" + categoryCode + "-" + lookupCode +  "' could not be added. Please submit a bug report.\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lookup code\"}").build();
	}
	

	/**
	 * This API updates an existing Lookup Value.
	 * Sample Use Case: Call this API to update a lookup value.
	 * @param luValueBean
	 * @return
	 */
	@POST
	@Path("/update")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateLookupValues(LookupValuesBean luValueBean){
		String categoryCode = luValueBean.getCategoryCode();
		String lookupCode = luValueBean.getLookupValueCode();
		String shortDescription  = luValueBean.getShortDescription();
		String longDescription  = luValueBean.getLongDescription();
		IMDLogger.log("updateLookupValues Called with following input values", Util.INFO);
		IMDLogger.log(luValueBean.toString() , Util.INFO);
		
		if (categoryCode == null || categoryCode.trim().isEmpty()) {			
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid category code.\"}").build();
		}
		if (lookupCode == null || lookupCode.trim().isEmpty()) {			
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid lookup code.\"}").build();
		}
		if (shortDescription == null || shortDescription.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Short Description.\"}").build();
		}
		if (longDescription == null || longDescription.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Long Description.\"}").build();
		}
		LookupValues luValue = new LookupValues(luValueBean);
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LookupValuesLoader loader = new LookupValuesLoader();
			luValue.setCreatedBy(new User(userID));
			luValue.setCreatedDTTM(DateTime.now());
			luValue.setUpdatedBy(new User(userID));
			luValue.setUpdatedDTTM(DateTime.now());
			result = loader.updateLookupValues(luValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result == 1)
			return Response.status(200).entity("{ \"error\": false, \"message\":\"Lookup value has been updated successfully\"}").build();
		else if (result == 0)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Lookup value does not exist.\"}").build();
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified catefory-lookup value '" + categoryCode + "-" + lookupCode + "' already exists\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Lookup '"  + categoryCode + "-" + lookupCode + "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Lookup '" + categoryCode + "-" + lookupCode +  "' could not be added. Please submit a bug report.\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lookup code\"}").build();
	}  	
	
}
