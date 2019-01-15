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

import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
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
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addEvent(LifeCycleEventBean eventBean){
		eventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
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
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
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
