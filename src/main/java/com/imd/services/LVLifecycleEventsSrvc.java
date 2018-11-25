package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.imd.dto.LifeCycleEventCode;
import com.imd.loader.LVLifeCycleEventLoader;

@Path("/lv-lifecycle-event")
public class LVLifecycleEventsSrvc {

	/**
	 * Retrieves ALL the event codes
	 * @return
	 */
	@GET
	@Path("/all")
	@Produces(MediaType.TEXT_PLAIN)
    public String getActiveLifecycleEventsLookup() {
    	String lvEvents = "";
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
		 	List<LifeCycleEventCode> events = loader.retrieveAllLifeCycleEvents();	    	
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ") + "\n},\n";	    		
	    	}
	    	lvEvents = lvEvents.substring(0,lvEvents.lastIndexOf(",\n"));
//	    	System.out.println(lvEvents);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return lvEvents;
    }
	/**
	 * Retrieves ALL the event codes
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.TEXT_PLAIN)
    public String getAllActiveLifecycleEventsLookup() {
    	String lvEvents = "";
    	try {
			LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
		 	List<LifeCycleEventCode> events = loader.retrieveAllActiveLifeCycleEvents();	    	
	    	Iterator<LifeCycleEventCode> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifeCycleEventCode event = eventIt.next();
	    		lvEvents += "{\n" + event.dtoToJson("  ") + "\n},\n";	    		
	    	}
	    	lvEvents = lvEvents.substring(0,lvEvents.lastIndexOf(",\n"));
//	    	System.out.println(lvEvents);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return lvEvents;
    }	
	
	/**
	 * Retrieve a particular life cycle event given its event code. If the event does not exist then return
	 * No Record Found
	 * @param eventcode
	 * @return
	 */
	
	@GET
	@Path("{eventcode}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getLifecycleEvent(@PathParam("eventcode") String eventcode){
		LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
		LifeCycleEventCode eventCode = null;
		String event = "";
		try {
			eventCode = loader.retrieveLifeCycleEvent(eventcode);
			event = eventCode == null ? "No Record Found" : "{\n" + eventCode.dtoToJson("  ") +"\n}\n";
		} catch (Exception e) {
			e.printStackTrace();
			event = "Error: " + e.getMessage();
		}
		return event;
    }
}
