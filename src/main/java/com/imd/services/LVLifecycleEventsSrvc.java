package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.imd.dto.Animal;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.InventoryLoader;
import com.imd.loader.LVLifeCycleEventLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventCodeBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

@Path("/lv-lifecycle-event")
public class LVLifecycleEventsSrvc {

	/**
	 * Retrieves ALL the event codes whether they are active or inactive
	 * Sample Use Case: Call this API to populate the list of all the lifecycle events e.g. on the Lifecycle code maintenance screen. 
	 * @return
	 */
	
	@GET
	@Path("/all")
	@Produces(MediaType.TEXT_PLAIN)
    public String getAllLifecycleEvents() {
    	String lvEvents = "";
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
		 	List<LifeCycleEventCode> events = loader.retrieveAllLifeCycleEvents();
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	lvEvents = "[" + lvEvents.substring(0,lvEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(lvEvents, Util.INFO);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
    	    	
        return lvEvents;
    }
	/**
	 * Retrieves ALL the active event codes.
	 * Sample Use Case: Call this API to populate the Lifecycle Event Drop down. 
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getAllActiveLifecycleEvents() {
    	String lvEvents = "";
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
			List<LifeCycleEventCode> events = loader.retrieveAllActiveLifeCycleEvents();
			if (events == null || events.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No lifecycle event code found\"}").build();
			}
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	lvEvents = "[" + lvEvents.substring(0,lvEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(lvEvents, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity(lvEvents).build(); 
    }	
	
	/**
	 * Retrieves a particular life cycle event given its event code.
	 * Sample Use Case: Call this API to retrieve a particular event code e.g. on the screen where you wish to show the life cycle events of an animal. 
	 * @param eventcode
	 * @return
	 */
	
	@GET
	@Path("{eventcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLifecycleEvent(@PathParam("eventcode") String eventcode){
		IMDLogger.log("Retrieving event code: " + eventcode, Util.INFO);
    	String lvEvents = "";
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
			List<LifeCycleEventCode> events = loader.retrieveLifeCycleEvent(eventcode);
			if (events == null || events.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No record found\"}").build();

			}
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	lvEvents = "[" + lvEvents.substring(0,lvEvents.lastIndexOf(",\n")) + "]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity(lvEvents).build(); 
	}
	
	/**
	 * Retrieve a particular life cycle event given its event code and/or short description. This API incorporates % wild card as well.
	 * Sample Use Case: Call this API to search for a particular event code when you don't know the exact event code e.g. on Event Code Maintenance screen 
	 * @param eventcode
	 * @return
	 */
	
	@POST
	@Path("/search")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchLifecycleEvent(LifeCycleEventCodeBean eventBean){
    	String lvEvents = "";
		String methodName = "searchLifecycleEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgId(orgId);
    	IMDLogger.log(eventBean.toString(), Util.INFO);
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
			List<LifeCycleEventCode> events = loader.retrieveMatchingLifeCycleEvents(eventBean);
			if (events == null || events.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (lvEvents == null || lvEvents.trim().isEmpty())
		    	lvEvents = "[ ]";
	    	else
	    		lvEvents = "[" + lvEvents.substring(0,lvEvents.lastIndexOf(",\n")) + "]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity(lvEvents).build();
    }	
	
	/**
	 * This API adds a new life cycle event.
	 * Sample Use Case: Call this API to add a new life cycle event e.g. on the Event Code Maintenance screen
	 * @param eventBean
	 * @return
	 */
	@POST
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addLifecycleEvent(LifeCycleEventCodeBean eventBean){
		String methodName = "addLifecycleEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgId(orgId);
    	IMDLogger.log(eventBean.toString(), Util.INFO);
    	
		String eventCode = eventBean.getEventCode();
		String shortDescription  = eventBean.getEventShortDescription();
		String longDescription  = eventBean.getEventLongDescription();
		
		if (eventCode == null || eventCode.trim().isEmpty()) {			
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (shortDescription == null || shortDescription.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Short Description.\"}").build();
		}
		if (longDescription == null || longDescription.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Long Description.\"}").build();
		}
		LifeCycleEventCode 	event = new LifeCycleEventCode(eventBean);
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
			event.setCreatedBy(new User(userID));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(new User(userID));
			event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.insertLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result == 1)
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully\"}").build();
		else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventCode+ "' already exists\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Lifecycle Event '" + eventCode+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Lifecycle Event '" + eventCode+ "' could not be added. Please submit a bug report.\"}").build();
		else 
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
	}
	/**
	 * This API adds a new life cycle event.
	 * If an empty field is passed in request JSON it will be updated to empty value; therefore if you do not wish to update a field
	 * then do not pass the field in the request JSON.
	 * Sample Use Case: Call this API to add a new life cycle event e.g. on the Event Code Maintenance screen
	 * @param eventBean
	 * @return
	 */
	@POST
	@Path("/update")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response editLifecycleEvent(LifeCycleEventCodeBean eventBean){
		
		String methodName = "editLifecycleEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgId(orgId);
    	IMDLogger.log(eventBean.toString(), Util.INFO);
		
		String eventCode = eventBean.getEventCode();
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		LifeCycleEventCode 	event = new LifeCycleEventCode(eventBean);
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
			event.setCreatedBy(new User(userID));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(new User(userID));
			event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.updateLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result == 1)
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"The Lifecycle event has been edited successfully\"}").build();
		else if (result == 0)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventCode+ "' may not exist\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Lifecycle Event '" + eventCode+ "' could not be updated. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the syntax of the update string. This indicates a bug that we may have left in the code. Lifecycle Event '" + eventCode+ "' could not be edited. Please submit a bug report with IMDLabs.\"}").build();
		else 
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An unknown error occurred while editing the lifecycle event\"}").build();
	}
	
	@POST
	@Path("/sire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAISires(AnimalBean searchBean){
		String sireValueResult = "";
		
		String methodName = "retrieveAISires";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Sire> sireValues = loader.retrieveAISire();
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();
			}
	    	Iterator<Sire> sireValueIt = sireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		Sire sireValue = sireValueIt.next();
	    		sireValueResult += "{\n" +  formJson(sireValue.getAnimalTag(), sireValue.getAlias(), "  ") + "\n},\n";
	    	}
	    	if (sireValueResult != null && !sireValueResult.trim().isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(sireValueResult).build();
    }
	
	
	@POST
	@Path("/availablesires")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAvailableSires(AnimalBean searchBean){
		
		String methodName = "retrieveAvailableSires";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
		String sireValueResult = "";
    	try {
    		InventoryLoader loader = new InventoryLoader();
			List<Sire> sireValues = loader.getSiresWithAvailableInventory(orgId);
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();
			}
	    	Iterator<Sire> sireValueIt = sireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		Sire sireValue = sireValueIt.next();
	    		String rmngQty = sireValue.getNote(0).getNoteText();
	    		String sexed = sireValue.getNote(1).getNoteText();
	    		String description = sireValue.getAlias() + " [" + rmngQty + "] " + (sexed.equalsIgnoreCase("Y") ? " ♀":"");
	    		sireValueResult += "{\n" +  formJson(sireValue.getAnimalTag(),description,"  ") + "\n},\n";
	    	}
	    	if (sireValueResult != null && !sireValueResult.trim().isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(sireValueResult).build();
    }	
	
	@POST
	@Path("/dam")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getActiveFemale(AnimalBean searchBean){
		String methodName = "getActiveFemale";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveDams(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active dam found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + formJson(animalValue.getAnimalTag(), animalValue.getAlias() == null ? "" : animalValue.getAlias(), "  ") + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	
	@POST
	@Path("/retrievemates")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getFemaleMates(AnimalBean searchBean){
		String methodName = "getFemaleMates";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	String animalValueResult = "";
    	searchBean.setOrgId((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveAnimals(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active dam found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + formJson(animalValue.getAnimalTag(), animalValue.getAnimalTag(),"  ") + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }	
	

	
	private String formJson(String field1, String field2, String prefix) {
		return prefix + "\"code\":\"" + field1 + "\",\n" + prefix + 
				"\"description\":\"" + field2 + "\"";
	}	
}
