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
import org.joda.time.format.DateTimeFormatter;

import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

/**
 * Root resource (exposed at "animalevent" path)
 */
@Path("animalevent")
public class LifecycleEventSrvc {

    
	/**
	 * Retrieves ALL the events for a given Animal 
	 * @return
	 */
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnimalLifecycleEvent(AnimalBean animalBean){
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		String animalEvents = "";
		try {
			List<LifecycleEvent> events = loader.retrieveAllLifeCycleEventsForAnimal((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID),animalBean.getAnimalTag());
			if (events == null || events.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No life events found for specified animal\"}").build();

			}
	    	Iterator<LifecycleEvent> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifecycleEvent event = eventIt.next();
	    		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyyy h:mm a");
	    		animalEvents += "{\n" + event.dtoToJson("  ", fmt) + "\n},\n";	    		
	    	}
	    	animalEvents = "[" + animalEvents.substring(0,animalEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(animalEvents, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(400).entity(animalEvents).build();
    }
    
	
	
	@POST
	@Path("/retrieveoneevent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveOneEvent(LifeCycleEventBean eventBean){
		String animalEvent = "";
		eventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
//		String animalTag = eventBean.getAnimalTag();
		String eventTransactionID = eventBean.getEventTransactionID();
		
		IMDLogger.log("retrieveOneEvent Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventTransactionID == null || eventTransactionID.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid event transaction id.\"}").build();
		}
//		if (animalTag == null || animalTag.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide the correct Animal Tag.\"}").build();
//		}
		try {
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			LifecycleEvent event = loader.retrieveLifeCycleEvent(eventBean.getOrgID(),eventTransactionID);
			if (event == null)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"The requested life event could not be found.\"}").build();
			}
	    	animalEvent = "[\n{" + event.dtoToJson("   ") + "}\n]";
	    	IMDLogger.log(animalEvent, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(400).entity(animalEvent).build();
	}  
	@POST
	@Path("/deleteoneevent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteOneEvent(LifeCycleEventBean eventBean){
		eventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
		String eventTransactionID = eventBean.getEventTransactionID();
		int deleteCount = 0;
		IMDLogger.log("deleteOneEvent Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventTransactionID == null || eventTransactionID.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid event transaction id.\"}").build();
		}
		try {
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			deleteCount = loader.deleteLifeCycleEvent(eventBean.getOrgID(),eventTransactionID);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(200).entity("{ \"error\": false, \"message\":\"" + deleteCount + " life event(s) was deleted.\"}").build();
	}  	
	@POST
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addEvent(LifeCycleEventBean eventBean){
		String orgId = (String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID);
		eventBean.setOrgID(orgId);
		String animalTag = eventBean.getAnimalTag();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		
		IMDLogger.log("Add Event Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (animalTag == null || animalTag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal Tag.\"}").build();
		}
		if (eventComments == null || eventComments.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide comments.\"}").build();
		}
		LifecycleEvent event;
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		AnimalLoader animalLoader = new AnimalLoader();
		
		AnimalBean animalBean = new AnimalBean();
		animalBean.setAnimalTag(animalTag);
		animalBean.setOrgID(orgId);
		
		int result = -1;
		try {
			List<Animal> animals = animalLoader.retrieveMatchingAnimals(animalBean, false, null);
			if (animals == null || animals.isEmpty() || animals.size() > 1) { 
				result = Util.ERROR_CODE.DOES_NOT_EXIST;
				// exactly one animal should exist for the specified animal tag.
			} else {
				event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
				LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
				event.setCreatedBy(new User(userID));
				event.setCreatedDTTM(DateTime.now());
				event.setUpdatedBy(new User(userID));
				event.setUpdatedDTTM(DateTime.now());
				result = loader.insertLifeCycleEvent(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result > 0)
			return Response.status(200).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully. The Transaction Id is: " + result + "\"}").build();
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(200).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventCode + "' already exists\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + eventCode+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.DOES_NOT_EXIST)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified animal does not exit. Event  '" + eventCode+ "' could not be added. Please specify a correct Animal Tag.\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
	}     

	@POST
	@Path("/update")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateEvent(LifeCycleEventBean eventBean){
		eventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
		String eventTransactionID = eventBean.getEventTransactionID();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		String animalTag = eventBean.getAnimalTag();
		IMDLogger.log("Update Event Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (animalTag == null || animalTag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Transaction ID to update Tag.\"}").build();
		}
		if (eventComments == null || eventComments.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide comments.\"}").build();
		}
		LifecycleEvent event;
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			event.setUpdatedBy(new User(userID));
			event.setUpdatedDTTM(DateTime.now());
			result = loader.updateLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result > 0)
			return Response.status(200).entity("{ \"error\": false, \"message\":\"Lifecycle event has been updated successfully.\"}").build();
		else if (result == 0)
			return Response.status(200).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventTransactionID + "' does not exist.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + eventCode+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
	} 	
	
	
}
