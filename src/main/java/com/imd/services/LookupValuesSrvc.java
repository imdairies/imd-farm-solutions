package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.loader.LookupValuesLoader;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

@Path("/lookupvalues")
public class LookupValuesSrvc {
	
	/**
	 * Retrieve a lookup values matching the search criteria based on the category code and lookup value code. This API incorporates % wild card as well.
	 * Sample Use Case: Call this API to retries lookup values belonging to a specific category or all lookup values e.g. on retrieve all animal breed lookup values. 
	 * @param LookupValuesBean
	 * @return
	 */
	
	@POST
	@Path("/search")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchLookupValues(LookupValuesBean luValuesBean){
    	String luValueResult = "";
    	IMDLogger.log(luValuesBean.toString(), Util.INFO);
    	try {
    		LookupValuesLoader loader = new LookupValuesLoader();
			List<LookupValues> luValues = loader.retrieveMatchingLookupValues(luValuesBean);
			if (luValues == null || luValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<LookupValues> luValuesIt = luValues.iterator();
	    	while (luValuesIt.hasNext()) {
	    		LookupValues luValue = luValuesIt.next();
	    		luValueResult += "{\n" + luValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (luValueResult != null && !luValueResult.trim().isEmpty() )
	    		luValueResult = "[" + luValueResult.substring(0,luValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		luValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(luValueResult, Util.INFO);
		return Response.status(200).entity(luValueResult).build();
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
			luValue.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			luValue.setUpdatedBy(new User(userID));
			luValue.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
			luValue.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			luValue.setUpdatedBy(new User(userID));
			luValue.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
