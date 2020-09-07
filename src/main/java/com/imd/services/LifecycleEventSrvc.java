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
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 * Root resource (exposed at "animalevent" path)
 */
@Path("animalevent")
public class LifecycleEventSrvc {
    
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnimalLifecycleEvent(LifeCycleEventBean animalEventBean){
		String methodName = "getAnimalLifecycleEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,animalEventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		animalEventBean.setOrgID(orgID);
		IMDLogger.log(animalEventBean.toString(), Util.ERROR);		
		
		LifeCycleEventsLoader animalEventsloader = new LifeCycleEventsLoader();
		AnimalLoader animalLoader = new AnimalLoader();
		String animalEvents = "";
		AnimalBean animalBean = new AnimalBean();
		
		String tagInClause = "";
		if (animalEventBean.getAnimalTag() != null && !animalEventBean.getAnimalTag().isEmpty()) {
			
			String[] tags = animalEventBean.getAnimalTag().split(",");
			for (int i=0; i<tags.length; i++) {
				tagInClause += (i==0 ? "'" : ",'") + tags[i].trim() + "'";
			}
			tagInClause = "(" + tagInClause + ")";
			animalBean.setAnimalTag(tagInClause);
		} else  {
			animalBean.setAnimalTag("");
		}
		animalBean.setAnimalTag(animalBean.getAnimalTag());
		animalBean.setOrgId(animalEventBean.getOrgID());
		try {
			
//			List<Animal> animalValues = animalLoader.retrieveMatchingAnimals(animalBean,false,null,null);
//			List<Animal> animalValues = animalLoader.retrieveMatchingAnimals(animalBean);
//			if (animalValues == null || animalValues.size() == 0)
//			{
//				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The animal " + Util.encodeJson(animalEventBean.getAnimalTag()) + " does not exist\"}").build();
//			}
			String eventTypeCD = animalEventBean.getEventCode();
			if (eventTypeCD == null || eventTypeCD.trim().isEmpty()|| eventTypeCD.trim().equalsIgnoreCase("%"))
				eventTypeCD = null; 
			List<Animal> animalsEvents = animalEventsloader.retrieveSpecificLifeCycleEventsForMultipleAnimals(
					orgID, tagInClause, null, null, eventTypeCD, null, null, null, null, null);
			if (animalsEvents == null || animalsEvents.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No life events found for specified animal(s)\"}").build();

			}
	    	Iterator<Animal> animalsEventIt = animalsEvents.iterator();
	    	while (animalsEventIt.hasNext()) {
	    		Animal animal = animalsEventIt.next();
	    		List<LifecycleEvent> animalEvent = animal.getLifeCycleEvents();
	    		if (animalEvent != null && !animalEvent.isEmpty()) {
	    			Iterator<LifecycleEvent> eventIt = animalEvent.iterator();
	    			while (eventIt.hasNext()) {
	    				LifecycleEvent event = eventIt.next();
			    		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
			    		String extendedComment = formatComent(event);
			    		animalEvents += "{\n" + event.dtoToJson("  ", fmt, animal.getDateOfBirth()) + ",\n  \"formattedComments\":\"" + Util.encodeJson(extendedComment) + "\"\n},\n";	    		
	    			}
	    		}
	    	}
	    	animalEvents = "[" + animalEvents.substring(0,animalEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(animalEvents, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity(animalEvents).build();
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
		String methodName = "retrieveOneEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgID(orgID);
		IMDLogger.log(eventBean.toString(), Util.INFO);				
		
		String animalEvent = "";
		String eventTransactionID = eventBean.getEventTransactionID();
				
		if (eventTransactionID == null || eventTransactionID.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event transaction id.\"}").build();
		}
		try {
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			LifecycleEvent event = loader.retrieveLifeCycleEvent(eventBean.getOrgID(),eventTransactionID);
			if (event == null)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The requested life event could not be found.\"}").build();
			}
			
//			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
			DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM d, yyyy HH:mm:ss");
	    	animalEvent = "[\n{" + event.dtoToJson("   ", fmt) + "}\n]";
	    	IMDLogger.log(animalEvent, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  Util.encodeJson(e.getMessage()) + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity(animalEvent).build();
	}  
	@POST
	@Path("/deleteoneevent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteOneEvent(LifeCycleEventBean eventBean){
		String methodName = "deleteOneEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgID(orgID);
		IMDLogger.log(eventBean.toString(), Util.INFO);				

		String eventTransactionID = eventBean.getEventTransactionID();
		int deleteCount = 0;
		
		if (eventTransactionID == null || eventTransactionID.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event transaction id.\"}").build();
		}
		try {
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			deleteCount = loader.deleteLifeCycleEvent(eventBean.getOrgID(),eventTransactionID);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  Util.encodeJson(e.getMessage()) + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + Util.encodeJson(deleteCount) + " life event was deleted. Please note that if at the time of creation of this event some related event was updated, then those updates were NOT reversed by the deletion. If you wish to reverse those updates, please do so manually.\"}").build();
	}
	
	@POST
	@Path("/addbatch")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addEventForMultipleAnimals(LifeCycleEventBean eventBean){
		String methodName = "addEventForMultipleAnimals";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgID(orgID);
		IMDLogger.log(eventBean.toString(), Util.INFO);				
		
		
		String animalTag = eventBean.getAnimalTag();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();		
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (animalTag == null || animalTag.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal Tag.\"}").build();
		}
		if (eventComments == null || eventComments.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide comments.\"}").build();
		}
		
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);		
		String animalTags[] = {""};
		AnimalBean animalBean = new AnimalBean();
		animalBean.setOrgId(orgID);
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
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"The event was applied to the following animals successfully: " + Util.encodeJson(successfulAnimals) + "\"}").build();
		else if (successfulAnimals.isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The event could NOT be applied to the following animals: " +  Util.encodeJson(unsuccessfulAnimals) + "\"}").build();
		else
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + Util.ERROR_POSTFIX + " The event was applied to the to the following animals: " +  Util.encodeJson(successfulAnimals) + ". But the event could NOT be applied to the following animals: " + Util.encodeJson(unsuccessfulAnimals) + "\"}").build();
	} 	
	
	private String addEventForSingleAnimal(LifeCycleEventBean eventBean, AnimalBean animalBean, String userID) {
		AnimalLoader animalLoader = new AnimalLoader();
		LifecycleEvent event;

		int result = -1;
		String additionalMessage = "";
		try {
			List<Animal> animals = animalLoader.retrieveMatchingAnimals(animalBean, false, null, null);
			if (animals == null || animals.isEmpty()) { 
				additionalMessage = Util.ERROR_POSTFIX + animalBean.getAnimalTag() + " does not exist";
			} else {
				event = new LifecycleEvent(eventBean, "yyyy-MM-dd HH:mm");
				LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
				event.setCreatedBy(new User(userID));
				event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				event.setUpdatedBy(new User(userID));
				event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
		String methodName = "addEvent";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgID(orgID);
		IMDLogger.log(eventBean.toString(), Util.INFO);				
		
		String animalTag = eventBean.getAnimalTag();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (animalTag == null || animalTag.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal Tag.\"}").build();
		}
		if (eventComments == null || eventComments.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide comments.\"}").build();
		}
		LifecycleEvent event;
		AnimalLoader animalLoader = new AnimalLoader();
		
		AnimalBean animalBean = new AnimalBean();
		animalBean.setAnimalTag(animalTag);
		animalBean.setOrgId(orgID);
		
		LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
		String validationResult = eventsLoader.performEventSpecificValidations(eventBean, animalBean);
		
		if (validationResult.isEmpty()) {
		
			int result = -1;
			String additionalMessage = "";
			try {
				List<Animal> animals = animalLoader.retrieveMatchingAnimals(animalBean, false, null, null);
				if (animals == null || animals.isEmpty() || animals.size() > 1) { 
					result = Util.ERROR_CODE.DOES_NOT_EXIST;
					// exactly one animal should exist for the specified animal tag.
				} else {
					Animal animal = animals.get(0);
					event = new LifecycleEvent(eventBean, "yyyy-MM-dd HH:mm");
					event.setCreatedBy(user);
					event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
					event.setUpdatedBy(user);
					event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully. The Transaction Id is: " + Util.encodeJson(result + additionalMessage) + "\"}").build();
			else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + Util.encodeJson(eventCode)  + "' already exists\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.DOES_NOT_EXIST)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified animal does not exit. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please specify a correct Animal Tag.\"}").build();
			else 
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during creation of the new lifecycle event\"}").build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + Util.encodeJson(validationResult) + ". The event '" + Util.encodeJson(eventCode) + "' could not be added.\"}").build();			
		}
	}

	private String performPostEventAdditionSteps(LifeCycleEventBean eventBean, LifecycleEvent event, Animal animal, User user) {
		String additionalMessage = performPostEventAdditionLifecycleStageUpdate(eventBean, animal, user);
		additionalMessage += performPostEventAdditionInventoryUpdate(eventBean, animal, user);
		additionalMessage += (new LifeCycleEventsLoader()).performPostEventAdditionEventsUpdates(event, animal, user);
		additionalMessage += insertCalf(event, animal, user, eventBean.getLoginToken());
		additionalMessage += performPostEventAdditionInfomationProcessing(event, animal, user);
		
		return additionalMessage;
	}
	
	
	private String insertCalf(LifecycleEvent event, Animal animal, User user, String authToken) {
		AnimalBean calfBean = new AnimalBean();
		try {
			calfBean.setLoginToken(authToken);
			if (!event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.PARTURATE) || event.getAuxField3Value() == null || event.getAuxField3Value().trim().isEmpty())
				return "";
			calfBean.setAnimalTag(event.getAuxField3Value().trim());
	
			String gender = event.getAuxField1Value();
			if (gender == null || !(gender.equalsIgnoreCase(Util.Gender.FEMALE) || gender.equalsIgnoreCase(Util.Gender.MALE)))
					return ". " + Util.ERROR_POSTFIX + "The gender of the calf is invalid (" + gender + "). The calf will not be automatically added. Please add the calf manually";
			if (gender.equalsIgnoreCase(Util.Gender.FEMALE)) {
				calfBean.setGender(Util.GENDER_CHAR.FEMALE);
				calfBean.setAnimalType(Util.AnimalTypes.FEMALECALF);
			} else { 
				calfBean.setGender(Util.GENDER_CHAR.MALE);
				calfBean.setAnimalType(Util.AnimalTypes.MALECALF);
			}
			
			if (event.getEventTimeStamp() == null)
				return ". " + Util.ERROR_POSTFIX + "The calf date of birth has not been specified. The calf will not be automatically added. Please add the calf manually";
			
			calfBean.setDateOfBirthStr(Util.getDateTimeInSpecifiedFormat(event.getEventTimeStamp(),"yyyy-MM-dd HH:mm"));
			calfBean.setBreed(animal.getBreed());
			calfBean.setDam(animal.getAnimalTag());
			calfBean.setOrgId(animal.getOrgId());
			calfBean.setDobAccuracyInd(Util.YES);
			calfBean.setHerdJoiningDttmStr(calfBean.getDateOfBirthStr());
	
			LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
			List<LifecycleEvent> events = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(), null, null, Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
			if (events == null || events.isEmpty())
				return ". " + Util.ERROR_POSTFIX + "No insemination or mating event found for this animal. "
						+ "This indicates that you have not added a corresponding mating/insemination event for the parturition. "
						+ "The calf will not be automatically added. Please add the calf manually";
			LifecycleEvent conceptionEvent = events.get(0);
			DateTime inseminationOrMatingTS = conceptionEvent.getEventTimeStamp();
			if (inseminationOrMatingTS == null)
				return ". " + Util.ERROR_POSTFIX + "An invalid insemination or mating event was found for this animal. "
				+ "Please view the latest insemination/mating event for this animal and fix the data discrepancy in that event. "
				+ "The calf will not be automatically added. Please add the calf manually";
			int daysBetweenCalvingAndInsemination = Util.getDaysBetween(event.getEventTimeStamp(), inseminationOrMatingTS);
			if (daysBetweenCalvingAndInsemination < (Util.LACTATION_DURATION - 20) ||  
				daysBetweenCalvingAndInsemination > (Util.LACTATION_DURATION + 20))
				return ". " + Util.ERROR_POSTFIX + "An invalid insemination or mating event was found for this animal. It seems that the insemination/mating event was " + daysBetweenCalvingAndInsemination + " days ago, which doesn't seem to be correct. "
				+ "Please view the latest insemination/mating event for this animal and fix the data discrepancy in that event. "
				+ "The calf will not be automatically added. Please add the calf manually";
	
			if (conceptionEvent.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.INSEMINATE)) {
				calfBean.setAiInd(Util.YES);
				calfBean.setSire(conceptionEvent.getAuxField1Value());
			} else {
				calfBean.setAiInd(Util.NO);
				calfBean.setSire(conceptionEvent.getAuxField1Value());
			}
			
			AnimalSrvc animalSrvc = new AnimalSrvc();
			calfBean.setOperatorAtBirth(event.getEventOperator() != null ? event.getEventOperator().getPersonID() : "");
			String calfAdditionResponse = animalSrvc.addAnimal(calfBean).getEntity().toString();
			if (calfAdditionResponse.indexOf("\"error\": false") >= 0)
				return "The calf with the tag# " + calfBean.getAnimalTag() + " has been successfully added to the herd";
			else 	
				return ". " + Util.ERROR_POSTFIX + "Calf could not be added to the herd. Please add the calf manually. The AnimalSrvc returned the following response: {" + calfAdditionResponse + "}";

		} catch (Exception ex) {
			ex.printStackTrace();
			return ". " + Util.ERROR_POSTFIX + "The calf could not be added automatically. Please add the calf manually using Add Animal feature (" + ex.getMessage() + ").";
		}
	}
	
	

	private String performPostEventAdditionInfomationProcessing(LifecycleEvent event, Animal animal, User user) {
		String message = "";
		if (event.getEventType().getEventCode().equals(Util.LifeCycleEvents.HEAT)) {
			message += ". The ideal insemination window of this animal is " + Util.getDateTimeInSpecifiedFormat(event.getEventTimeStamp().plusHours(12),"yyyy-MM-dd hh:mm a") +
					" - " + Util.getDateTimeInSpecifiedFormat(event.getEventTimeStamp().plusHours(18),"yyyy-MM-dd hh:mm a")
					+ " (i.e. 12-18 hours after the standing heat)";
		} else if (event.getEventType().getEventCode().equals(Util.LifeCycleEvents.WEIGHT)) {
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			List<LifecycleEvent> wtEvents = evtLdr.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(), Util.LifeCycleEvents.WEIGHT);
			message = "";
			if (wtEvents != null &&  wtEvents.size() >1) {
				Float lastWt = Float.parseFloat(wtEvents.get(1).getAuxField1Value());
				DateTime wtMeasuredTimestamp = wtEvents.get(1).getEventTimeStamp();
				float daysSinceLastMeasurement = Util.getDaysBetween(event.getEventTimeStamp(), wtMeasuredTimestamp);
				if (daysSinceLastMeasurement == 0)
					// case when we enter two weight events on the same day. Shouldn't happen but just in case it does, we 
					// save ourselves from divide by zero.
					daysSinceLastMeasurement = 1;
				Float rateOfWtIncrease = (Float.parseFloat(event.getAuxField1Value()) - lastWt) / daysSinceLastMeasurement;
				message = ". This animal's last weight as measured on " + Util.getDateTimeInSpecifiedFormat(wtMeasuredTimestamp,"yyyy-MM-dd hh:mm a") + " was " + 
				Util.formatToSpecifiedDecimalPlaces(lastWt, 1) + " Kgs. In the last " + daysSinceLastMeasurement + " days this animal grew at an average of " + rateOfWtIncrease + " Kgs. per day";
			}
		}
		return message;
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
				inventory.setOrgId(eventBean.getOrgID()); 
				inventory.setItemSKU(eventBean.getAuxField1Value()); // bull code
				inventory.setInventoryAddDttm(eventBean.getEventTimeStamp() == null ? null : DateTime.parse(eventBean.getEventTimeStamp(), DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm"))); // when was this item consumed
				inventory.setItemType(eventBean.getAuxField2Value() != null && eventBean.getAuxField2Value().trim().length() >= 1 ? eventBean.getAuxField2Value().charAt(0) + "" : eventBean.getAuxField2Value()); // sexed or not
				inventory.setQuantity(1.0f); // single usage per insemination
				inventory.setAuxValue1(eventBean.getEventTransactionID()); // FK to event table
				inventory.setCreatedBy(user);
				inventory.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				inventory.setUpdatedBy(user);
				inventory.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				int result = loader.addSemenInventoryUsage(inventory);
				if (result == 1)
					additionalMessage = ". Semen Inventory has been updated";
				else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
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
		animalDto.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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

		String methodName = "updateEvent";

		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,eventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		eventBean.setOrgID(orgID);
		IMDLogger.log(eventBean.toString(), Util.INFO);				
		
		String eventTransactionID = eventBean.getEventTransactionID();
		String eventComments = eventBean.getEventComments();
		String eventCode = eventBean.getEventCode();
		String animalTag = eventBean.getAnimalTag();
		IMDLogger.log("Update Event Called with following input values", Util.INFO);
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		if (eventCode == null || eventCode.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid event code.\"}").build();
		}
		if (animalTag == null || animalTag.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Transaction ID to update Tag.\"}").build();
		}
		if (eventComments == null || eventComments.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide comments.\"}").build();
		}
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			LifecycleEvent event = new LifecycleEvent(eventBean, "yyyy-MM-dd HH:mm");
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			event.setUpdatedBy(new User(userID));
			event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			result = loader.updateLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result > 0)
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Lifecycle event has been updated successfully.\"}").build();
		else if (result == 0)
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + Util.encodeJson(eventTransactionID) + "' does not exist.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Event  '" + Util.encodeJson(eventCode) + "' could not be added. Please reduce the field length and try again.\"}").build();
		else 
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during update of the lifecycle event\"}").build();
	}
	
	@POST
	@Path("/viewsirerecord")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSireInseminationRecord(LifeCycleEventBean animalEventBean) {
		String methodName = "getSireInseminationRecord";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,animalEventBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		animalEventBean.setOrgID(orgID);
		IMDLogger.log(animalEventBean.toString(), Util.INFO);		
		
		LifeCycleEventsLoader animalEventsloader = new LifeCycleEventsLoader();
		String animalEvents = "";
		AnimalBean animalBean = new AnimalBean();
		animalBean.setAnimalTag(animalEventBean.getAnimalTag());
		animalBean.setOrgId(animalEventBean.getOrgID());
		try {
			String eventTypeCD = animalEventBean.getEventCode();
			if (eventTypeCD == null || eventTypeCD.trim().isEmpty()|| eventTypeCD.trim().equalsIgnoreCase("%"))
				eventTypeCD = null;
			List<LifecycleEvent> events = animalEventsloader.retrieveSireInseminationRecord(animalBean.getOrgId(),animalEventBean.getAuxField1Value(),animalEventBean.getAuxField3Value());
			if (events == null || events.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No insemination/mating records found for the specified sire\"}").build();
			}
	    	Iterator<LifecycleEvent> eventIt = events.iterator();
	    	while (eventIt.hasNext()) {
	    		LifecycleEvent event = eventIt.next();
	    		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyyy h:mm a");
	    		String extendedComment = formatComent(event);
	    		animalEvents += "{\n" + event.dtoToJson("  ", fmt) + ",\n  \"formattedComments\":\"" + Util.encodeJson(extendedComment) + "\"\n},\n";	    		
	    	}
	    	animalEvents = "[" + animalEvents.substring(0,animalEvents.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(animalEvents, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		} 
		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity(animalEvents).build();
    }
    

	
	
}



