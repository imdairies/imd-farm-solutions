package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.format.DateTimeFormat;

import com.imd.dto.Animal;
import com.imd.dto.Sire;
import com.imd.loader.AnimalLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;;

@Path("/animals")
public class AnimalSrvc {

//	/**
//	 * Retrieves ALL the animals of all the farms
//	 * @return
//	 * @deprecated
//	 */
//	@GET
//	@Path("/all")
//	@Produces(MediaType.TEXT_PLAIN)
//    public String getAllAnimals() {
//    	String animalsJson = "";
//    	try {
//			AnimalLoader loader = new AnimalLoader();
//		 	List<Animal> animals = loader.retrieveAllAnimals((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
//		 	animalsJson = processAnimalRecords(animalsJson, animals);
//    	} catch (Exception ex) {
//    		ex.printStackTrace();
//    		System.out.println(ex.getMessage());
//    	}
//        return animalsJson;
//    }
	/**
	 * Retrieves ALL the active animals in a farm
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.TEXT_PLAIN)
    public String getAllActiveLifecycleEventsLookup() {
	   	String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	List<Animal> animals = loader.retrieveActiveAnimals((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
		 	animalsJson = processAnimalRecords(animalsJson, animals);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return animalsJson;
    }
	
	
	/**
	 * This API adds a new animal.
	 * Sample Use Case: Call this API to add a new animal.
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addLifecycleEvent(AnimalBean animalBean){
		String tag = animalBean.getAnimalTag();
		String alias  = animalBean.getAlias();
//		String longDescription  = eventBean.getEventLongDescription();
//		String isActive  = eventBean.getActiveIndicator();
		IMDLogger.log("Add Animal Called with following input values", Util.INFO);
		IMDLogger.log("animalTag : " + tag, Util.INFO);
		IMDLogger.log("alias : " + alias, Util.INFO);
//		IMDLogger.log("eventLongDescription : " + longDescription, Util.INFO);
//		IMDLogger.log("isActive : " + isActive, Util.INFO);
		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid Animal Tag.\"}").build();
		}
//		if (shortDescription == null || shortDescription.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Short Description.\"}").build();
//		}
//		if (longDescription == null || longDescription.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Long Description.\"}").build();
//		}
//		LifeCycleEventCode 	event = new LifeCycleEventCode(eventBean);
		String userID  = "KASHIF";
		int result = -1;
		try {
			AnimalLoader loader = new AnimalLoader();
//			event.setCreatedBy(new User(userID));
//			event.setCreatedDTTM(DateTime.now());
//			event.setUpdatedBy(new User(userID));
//			event.setUpdatedDTTM(DateTime.now());
//			result = loader.insertLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		if (result == 1)
//			return Response.status(200).entity("{ \"error\": false, \"message\":\"New Lifecycle event has been created successfully\"}").build();
//		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified Lifecycle Event '" + eventCode+ "' already exists\"}").build();
//		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Lifecycle Event '" + eventCode+ "' could not be added. Please reduce the field length and try again.\"}").build();
//		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Lifecycle Event '" + eventCode+ "' could not be added. Please submit a bug report.\"}").build();
//		else 
			return Response.status(400).entity("{ \"error\": true, \"message\":\"An unknown error occurred during animal addition\"}").build();

	}  	
	
	/**
	 * 
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/search")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchAnimals(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }

	/**
	 * 
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/lactatingcows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimals(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveLactatingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }	
	
	/**
	 * This API can be used to retrieve DAM or SIRE. The retrieved information can be used to populate the dam and sire dropdown
	 * on the add/update animal screens
	 * 
	 * @param luValuesBean
	 * @return
	 */
	@POST
	@Path("/retrieveaisire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAISires(AnimalBean searchBean){
		String sireValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Sire> sireValues = loader.retrieveAISire();
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();

			}
	    	Iterator<Sire> sireValueIt = sireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		Sire sireValue = sireValueIt.next();
	    		sireValueResult += "{\n" + sireValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (sireValueResult != null && !sireValueResult.trim().isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(200).entity(sireValueResult).build();
    }	
		
	
	private String processAnimalRecords(String animalsJson, List<Animal> animals) {
		if (animals == null || animals.isEmpty()) {
			animalsJson = "No Record Found";
		} else {
		
			Iterator<Animal> animalIt = animals.iterator();
			while (animalIt.hasNext()) {
				Animal animal = animalIt.next();
				animalsJson += "{\n" + animal.dtoToJson("  ") + "\n},\n";
			}
			animalsJson = animalsJson.substring(0,animalsJson.lastIndexOf(",\n"));
		}
//		System.out.println(animalsJson);
		return animalsJson;
	}		
}
