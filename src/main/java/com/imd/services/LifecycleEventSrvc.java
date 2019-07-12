package com.imd.services;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.dto.Animal;
import com.imd.dto.Inventory;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.InventoryLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.LookupValuesLoader;
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
	public Response getAnimalLifecycleEvent(LifeCycleEventBean animalEventBean){
		LifeCycleEventsLoader animalEventsloader = new LifeCycleEventsLoader();
		AnimalLoader animalLoader = new AnimalLoader();
		animalEventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
		String animalEvents = "";
		AnimalBean animalBean = new AnimalBean();
		animalBean.setAnimalTag(animalEventBean.getAnimalTag());
		animalBean.setOrgID(animalEventBean.getOrgID());
		try {
			List<Animal> animalValues = animalLoader.retrieveMatchingAnimals(animalBean,false,null);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"The animal " + Util.encodeJson(animalEventBean.getAnimalTag()) + " does not exist\"}").build();
			}
			String eventTypeCD = animalEventBean.getEventCode();
			if (eventTypeCD == null || eventTypeCD.trim().isEmpty()|| eventTypeCD.trim().equalsIgnoreCase("%"))
				eventTypeCD = null;
			List<LifecycleEvent> events = animalEventsloader.retrieveSpecificLifeCycleEventsForAnimal(animalBean.getOrgID(),animalValues.get(0).getAnimalTag(),eventTypeCD);
			if (events == null || events.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No life events found for specified animal\"}").build();

			}
	    	Iterator<LifecycleEvent> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifecycleEvent event = eventIt.next();
	    		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyyy h:mm a");
	    		String extendedComment = formatComent(event);
	    		animalEvents += "{\n" + event.dtoToJson("  ", fmt, animalValues.get(0).getDateOfBirth()) + ",\n  \"formattedComments\":\"" + Util.encodeJson(extendedComment) + "\"\n},\n";	    		
	    	}
	    	animalEvents = "[" + animalEvents.substring(0,animalEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(animalEvents, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(400).entity(animalEvents).build();
    }
    
	
	
	private String formatComent(LifecycleEvent event) {
		String comments = "";
		LifeCycleEventCode code = event.getEventType();
		String field1 = composeAuxFieldValue(code.getEventCode(),
				code.getField1Label(),
				code.getField1DataType(),
				code.getField1DataUnit(),
				event.getAuxField1Value());
		String field2 = composeAuxFieldValue(code.getEventCode(),
				code.getField2Label(),
				code.getField2DataType(),
				code.getField2DataUnit(),
				event.getAuxField2Value());
		String field3 = composeAuxFieldValue(code.getEventCode(),
				code.getField3Label(),
				code.getField3DataType(),
				code.getField3DataUnit(),
				event.getAuxField3Value());
		String field4 = composeAuxFieldValue(code.getEventCode(),
				code.getField4Label(),
				code.getField4DataType(),
				code.getField4DataUnit(),
				event.getAuxField4Value());

		comments = (field1.isEmpty() ? "" : field1 + " ") +
		 (field2.isEmpty() ? "" : field2 + " ") +
		 (field3.isEmpty() ? "" : field3 + " ") +
		 (field4.isEmpty() ? "" : field4 + " ");
		return (comments.isEmpty() ? "" : comments.trim());
	}

	private String composeAuxFieldValue(String eventCode, String fieldLabel, String fieldDataType, String fieldUnit, String fieldValue) {
		String comments = "";
		String value;
		LookupValuesLoader loader = new LookupValuesLoader();
		if (fieldLabel != null && !fieldLabel.isEmpty() && fieldValue != null && !fieldValue.isEmpty()) {
			if (fieldDataType.equals(Util.DataTypes.CATEGORY_CD) && fieldUnit != null && !fieldUnit.isEmpty()) {
				LookupValues lu = loader.retrieveLookupValue(fieldUnit, fieldValue);
				value = (lu == null ? "" : lu.getShortDescription());
			} else 
				value = fieldValue;
			comments = "[" + fieldLabel + ":" + value + "]";
		}
		return comments;
		
	}


	@POST
	@Path("/retrieveoneevent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveOneEvent(LifeCycleEventBean eventBean){
		String animalEvent = "";
		eventBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
		String eventTransactionID = eventBean.getEventTransactionID();
		
		IMDLogger.log("retrieveOneEvent Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventTransactionID == null || eventTransactionID.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid event transaction id.\"}").build();
		}
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  Util.encodeJson(e.getMessage()) + "\"}").build();
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  Util.encodeJson(e.getMessage()) + "\"}").build();
		}
		return Response.status(200).entity("{ \"error\": false, \"message\":\"" + Util.encodeJson(deleteCount) + " life event was deleted. Please note that if at the time of creation of this event some related event was updated, then those updates were NOT reversed by the deletion. If you wish to reverse those updates, please do so manually.\"}").build();
	}
	
	@POST
	@Path("/addbatch")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addEventForMultipleAnimals(LifeCycleEventBean eventBean){
		String orgId = (String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID);
		eventBean.setOrgID(orgId);
		String animalTag = eventBean.getAnimalTag();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		
		IMDLogger.log("Add Batch Event Called with following input values", Util.INFO);
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
		
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);		
		String animalTags[] = {""};
		AnimalBean animalBean = new AnimalBean();
		animalBean.setOrgID(orgId);
		if (animalTag.indexOf(",") <0)
			animalTags[0] = animalTag;
		else
			animalTags = eventBean.getAnimalTag().split(",");
		IMDLogger.log("Event is to be applied to " + animalTags.length + " animal(s)", Util.INFO);
		String individualMessage = "";
		String successfulAnimals = "";
		String unsuccessfulAnimals = "";
		
		for (int i=0; i<animalTags.length; i++) {
			animalBean.setAnimalTag(animalTags[i]);
			eventBean.setAnimalTag(animalBean.getAnimalTag());
			individualMessage = addEventForSingleAnimal(eventBean, animalBean, userID);
			if (individualMessage.indexOf(Util.ERROR_POSTFIX) == 0) {
				if (unsuccessfulAnimals.isEmpty())
					unsuccessfulAnimals +=  animalTags[i];
				else					
					unsuccessfulAnimals += ", " + animalTags[i];
			} else {
				if (successfulAnimals.isEmpty())
					successfulAnimals +=  animalTags[i];
				else					
					successfulAnimals += ", " + animalTags[i];
			}
		}
		IMDLogger.log("Successful Additions: " + successfulAnimals, Util.INFO);
		IMDLogger.log("Unsuccessful Additions: " + unsuccessfulAnimals, Util.INFO);
		if (unsuccessfulAnimals.isEmpty())
			return Response.status(200).entity("{ \"error\": false, \"message\":\"The event was applied to the following animals successfully: " + Util.encodeJson(successfulAnimals) + "\"}").build();
		else if (successfulAnimals.isEmpty())
			return Response.status(400).entity("{ \"error\": true, \"message\":\"The event could NOT be applied to the following animals: " +  Util.encodeJson(unsuccessfulAnimals) + "\"}").build();
		else
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + Util.ERROR_POSTFIX + " The event was applied to the to the following animals: " +  Util.encodeJson(successfulAnimals) + ". But the event could NOT be applied to the following animals: " + Util.encodeJson(unsuccessfulAnimals) + "\"}").build();
	} 	
	
	private String addEventForSingleAnimal(LifeCycleEventBean eventBean, AnimalBean animalBean, String userID) {
		AnimalLoader animalLoader = new AnimalLoader();
		LifecycleEvent event;

		int result = -1;
		String additionalMessage = "";
		try {
			List<Animal> animals = animalLoader.retrieveMatchingAnimals(animalBean, false, null);
			if (animals == null || animals.isEmpty()) { 
				additionalMessage = Util.ERROR_POSTFIX + animalBean.getAnimalTag() + " does not exist";
			} else {
				event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
				LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
				event.setCreatedBy(new User(userID));
				event.setCreatedDTTM(DateTime.now());
				event.setUpdatedBy(new User(userID));
				event.setUpdatedDTTM(DateTime.now());
				result = loader.insertLifeCycleEvent(event);
				if (result > 0)
					additionalMessage = Util.SUCCESS_POSTFIX + eventBean.getEventCode() + " successfully added for " + animalBean.getAnimalTag() + " [TRANS_ID:" + result + "]";
				else
					additionalMessage = Util.ERROR_POSTFIX + eventBean.getEventCode() + " could NOT be added successfully for " + animalBean.getAnimalTag();					
			}
		} catch (Exception e) {
			e.printStackTrace();
			additionalMessage = Util.ERROR_POSTFIX + " Unknown error occurred while adding event for a single animal." + Util.encodeJson(e.getMessage());
		}
		IMDLogger.log(additionalMessage, Util.INFO);
		return additionalMessage;
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
		
		LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
		String validationResult = eventsLoader.performEventSpecificValidations(eventBean, animalBean);
		
		if (validationResult.isEmpty()) {
		
			int result = -1;
			String additionalMessage = "";
			try {
				List<Animal> animals = animalLoader.retrieveMatchingAnimals(animalBean, false, null);
				if (animals == null || animals.isEmpty() || animals.size() > 1) { 
					result = Util.ERROR_CODE.DOES_NOT_EXIST;
					// exactly one animal should exist for the specified animal tag.
				} else {
					Animal animal = animals.get(0);
					event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
					User user = new User(userID);
					event.setCreatedBy(user);
					event.setCreatedDTTM(DateTime.now());
					event.setUpdatedBy(user);
					event.setUpdatedDTTM(DateTime.now());
					result = eventsLoader.insertLifeCycleEvent(event);
					event.setEventTransactionID(result);
					eventBean.setEventTransactionID("" + result);
					if (result > 0)
						additionalMessage = performPostEventAdditionSteps(eventBean, event, animal, user) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result > 0)
				return Response.status(200).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully. The Transaction Id is: " + Util.encodeJson(result + additionalMessage) + "\"}").build();
			else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + Util.encodeJson(eventCode)  + "' already exists\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.DOES_NOT_EXIST)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified animal does not exit. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please specify a correct Animal Tag.\"}").build();
			else 
				return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
		} else {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + Util.encodeJson(validationResult) + ". '" + Util.encodeJson(eventCode) + "' could not be added.\"}").build();			
		}
	}

	private String performPostEventAdditionSteps(LifeCycleEventBean eventBean, LifecycleEvent event, Animal animal, User user) {
		String additionalMessage = performPostEventAdditionLifecycleStageUpdate(eventBean, animal, user);
		additionalMessage += performPostEventAdditionInventoryUpdate(eventBean, animal, user);
		additionalMessage += (new LifeCycleEventsLoader()).performPostEventAdditionEventUpdate(event, animal, user);
		
		return additionalMessage;
	}

	private String performPostEventAdditionLifecycleStageUpdate(LifeCycleEventBean eventBean, Animal animal, User user) {
		String additionalMessage = "";
		if (eventBean.getNextLifeCycleStage() != null && !eventBean.getNextLifeCycleStage().isEmpty()) {
			LookupValuesLoader lookupLoader = new LookupValuesLoader();
			LookupValues nextStageValue = lookupLoader.retrieveLookupValue(Util.LookupValues.LCYCL, eventBean.getNextLifeCycleStage());
			if (nextStageValue == null) {
				IMDLogger.log("The lookup value " + eventBean.getNextLifeCycleStage() + " does not exist. The animal's next lifecycle stage can't be set", Util.ERROR);
				additionalMessage = ". The animal lifecycle stage could not be updated to " + eventBean.getNextLifeCycleStage() + ". You will have to manually update the stage";
			} else {
				animal.setAnimalTypeCD(nextStageValue.getLookupValueCode());
				animal.setAnimalType(nextStageValue.getShortDescription());
				IMDLogger.log("Attempting to set the Animal Lifecycle stage", Util.INFO);
				additionalMessage = ". " + updateAnimalLifecycleStage(animal, user);							
			}
		}
		return additionalMessage;
	}

	private String performPostEventAdditionInventoryUpdate(LifeCycleEventBean eventBean, Animal animal, User user) {
		String additionalMessage = "";
		if (eventBean.getShouldUpdateInventory() != null && eventBean.getShouldUpdateInventory().equalsIgnoreCase(Util.YES)) {
			Inventory inventory = new Inventory();
			InventoryLoader loader = new InventoryLoader();
			IMDLogger.log("Updating the inventory", Util.INFO);
			if (eventBean.getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.INSEMINATE)) {
				IMDLogger.log("Updating the Semen inventory", Util.INFO);
				inventory.setOrgID(eventBean.getOrgID()); 
				inventory.setItemSKU(eventBean.getAuxField1Value()); // bull code
				inventory.setInventoryAddDttm(eventBean.getEventTimeStamp() == null ? null : DateTime.parse(eventBean.getEventTimeStamp(), DateTimeFormat.forPattern( "MM/dd/yyyy, hh:mm:ss aa"))); // when was this item consumed
				inventory.setItemType(eventBean.getAuxField2Value() != null && eventBean.getAuxField2Value().trim().length() >= 1 ? eventBean.getAuxField2Value().charAt(0) + "" : eventBean.getAuxField2Value()); // sexed or not
				inventory.setQuantity(1.0f); // single usage per insemination
				inventory.setAuxValue1(eventBean.getEventTransactionID()); // FK to event table
				inventory.setCreatedBy(user);
				inventory.setCreatedDTTM(DateTime.now());
				inventory.setUpdatedBy(user);
				inventory.setUpdatedDTTM(DateTime.now());
				int result = loader.addSemenInventoryUsage(inventory);
				if (result == 1)
					additionalMessage = ". Semen Inventory has been updated";
				else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
					additionalMessage = ". " + Util.ERROR_POSTFIX + " Semen Inventory could not be updated since a similar entry already exists. Please review semen imnventory and update it manually";
				else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
					additionalMessage = ". " + Util.ERROR_POSTFIX + " Semen Inventory could not be updated since one or more data fields are longer than allowed length. Please submit a bug report";
				else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
					additionalMessage = ". " + Util.ERROR_POSTFIX + " Semen Inventory could not be updated because of an error in the application. Please submit a bug report";
				else 
					additionalMessage = ". " + Util.ERROR_POSTFIX + " An unknown error occurred during inventory update. Please update the semen inventory manually";
			}
		}
		return additionalMessage;
	}



	private String updateAnimalLifecycleStage(Animal animalDto, User user) {
		AnimalLoader loader = new AnimalLoader();
		animalDto.setUpdatedBy(user);
		animalDto.setUpdatedDTTM(DateTime.now());
		int result = loader.updateAnimalStatus(animalDto);
		if (result == 1)
			return "The animal lifecycle stage was automatically updated to " + animalDto.getAnimalType() + "(" + animalDto.getAnimalTypeCD() + ")";
		else
			return Util.ERROR_POSTFIX + "The animal status could NOT be automatically updated to " + animalDto.getAnimalType() + "(" + animalDto.getAnimalTypeCD() + ")" + ". Please manually set the animal status.";
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
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LifecycleEvent event = new LifecycleEvent(eventBean, "MM/dd/yyyy, hh:mm:ss aa");
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
			return Response.status(200).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + Util.encodeJson(eventTransactionID) + "' does not exist.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please reduce the field length and try again.\"}").build();
		else 
			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
	} 	
	
	
}
