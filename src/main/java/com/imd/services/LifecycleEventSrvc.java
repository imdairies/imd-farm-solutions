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

import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

/**
 * Root resource (exposed at "lifecycle-event" path)
 */
@Path("lifecycle-event")
public class LifecycleEventSrvc {

    
	/**
	 * Retrieves ALL the events for a given Animal 
	 * @return
	 */
	
	@GET
	@Path("/all/{animalTag}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAnimalLifecycleEvent(@PathParam("animalTag") String animalTag){
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		String animalEvents = "";
		try {
			List<LifecycleEvent> events = loader.retrieveAllActiveLifeCycleEventsForAnimal("IMD",animalTag);
			if (events == null || events.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Either the animal does not exist or it does not have any life cycle events specified\"}").build();

			}
	    	Iterator<LifecycleEvent> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifecycleEvent event = eventIt.next();
	    		animalEvents += "{\n" + event.dtoToJson("  ") + "\n},\n";	    		
	    	}
	    	animalEvents = "[" + animalEvents.substring(0,animalEvents.lastIndexOf(",\n")) + "]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(400).entity(animalEvents).build();
    }
    
	@POST
	@Path("/addevent")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addCustomer(LifeCycleEventBean eventBean){
		String orgID = eventBean.getOrgID();
		String animalTag = eventBean.getAnimalTag();
		String auxField1Value = eventBean.getAuxField1Value();
		String auxField2Value = eventBean.getAuxField2Value();
		String auxField3Value = eventBean.getAuxField3Value();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		String eventTimeStamp = eventBean.getEventTimeStamp();
		String operatorID = eventBean.getOperatorID();
		
		IMDLogger.log("Add Event Called with following input values", Util.INFO);
		IMDLogger.log("orgID : " + orgID, Util.INFO);
		IMDLogger.log("eventCode : " + eventCode, Util.INFO);
		IMDLogger.log("animalTag : " + animalTag, Util.INFO);
		IMDLogger.log("auxField1Value : " + auxField1Value, Util.INFO);
		IMDLogger.log("auxField2Value : " + auxField2Value, Util.INFO);
		IMDLogger.log("auxField3Value : " + auxField3Value, Util.INFO);
		IMDLogger.log("eventTimeStamp : " + eventTimeStamp, Util.INFO);
		IMDLogger.log("operatorID : " + operatorID, Util.INFO);
		IMDLogger.log("eventComments : " + eventComments, Util.INFO);
		
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
		String userID  = "KASHIF";
		int result = -1;
		try {
			event = new LifecycleEvent(eventBean);
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			event.setCreatedBy(new User(userID));
			event.setCreatedDTTM(DateTime.now());
			event.setUpdatedBy(new User(userID));
			event.setUpdatedDTTM(DateTime.now());
			result = loader.insertLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result > 0)
			return Response.status(200).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully. The Transaction Id is: " + result + "\"}").build();
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(200).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventCode + "' already exists\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
	}     
    
}
