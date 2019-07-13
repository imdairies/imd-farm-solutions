package com.imd.services;



import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.controller.feed.FeedManager;
import com.imd.dto.Animal;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.loader.FeedLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

@Path("/feed")
public class FeedSrvc {
	
	@POST
	@Path("/determineanimalfeed")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response determineAnimalFeed(AnimalBean animalBean){
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		if (animalBean.getAnimalTag() == null || animalBean.getAnimalTag().isEmpty())
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();

    	try {
			FeedManager mgr = new FeedManager();
			FeedCohort feedCohortType = mgr.getAnimalFeedCohortType(orgID, animalBean.getAnimalTag());
			FeedPlan plan = mgr.getPersonalizedFeedPlan(feedCohortType, animalBean.getAnimalTag());
	    	responseJson = "[\n" + "{\n" + feedCohortType.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + (plan != null ? ",\n" + plan.dtoToJson("  ") : "")+  "\n}\n" + "]";
	    	IMDLogger.log(responseJson, Util.INFO);
			return Response.status(200).entity(responseJson).build(); 
		} catch (IMDException e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.determineAnimalFeed() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.determineAnimalFeed() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Following error was encountered in processing animal feed analysis: " + e.getMessage() + "\"}").build();
		}
	}
	
	@POST
	@Path("/farmactiveanimalfeedlisting")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveActiveAnimalFeedListing(AnimalBean animalBean){
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		String feedItems = "";

    	try {
			FeedManager manager = new FeedManager();
			List<Animal> herd = manager.getFeedCohortInformationForFarmActiveAnimals(orgID);
			if (herd == null || herd.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No active animal found in the herd for the farm "+ orgID + "\"}").build();
			}
			String prefix = " ";
			String feedItemsJson = "";
			String animalFeedInfoJson = "";
			FeedLoader loader = new FeedLoader();

			feedItemsJson = loader.retrieveDistinctFeedItemsInFeedPlan(orgID).dtoToJson(prefix + prefix+prefix, false);
			
			Iterator<Animal> it = herd.iterator();
			int count = 0;
			while (it.hasNext()) {
				count++;
				Animal animalValue = it.next();
//				responseJson += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
				animalFeedInfoJson += prefix + prefix + "\n{\n" + createFeedListingJson(orgID, animalValue,manager.getPersonalizedFeedPlan(animalValue.getFeedCohortInformation(), animalValue.getAnimalTag()),prefix+prefix+prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + prefix + prefix + "\n} " 
						+ (herd.size() == count ?  "" : ",");
			}
    		responseJson = "\n{\n" + prefix
    				+ "\"feedItems\":{" + feedItemsJson + prefix + "},\n" + prefix
    				+ "\"animalFeedInfo\":["  + animalFeedInfoJson + "]\n"
    				+ "}";
			
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.retrieveActiveAnimalFeedListing() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(responseJson, Util.INFO);
		return Response.status(200).entity(responseJson).build();
	}

	private String createFeedListingJson(String orgID, Animal animalValue, FeedPlan feedPlan, String prefix, DateTimeFormatter forPattern) throws IMDException {
		String responseJson = "";
		
		responseJson =  prefix + animalValue.fieldToJson("orgID", orgID) + ",\n" + 
				prefix + animalValue.fieldToJson("currentAge", animalValue.getCurrentAge()) + ",\n" + 
				prefix + animalValue.fieldToJson("weight", animalValue.getWeight()) + ",\n" + 
				prefix + animalValue.fieldToJson("animalTag", animalValue.getAnimalTag()) + ",\n" +
				(animalValue.getFeedCohortInformation() == null ? "" : animalValue.getFeedCohortInformation().dtoToJson(prefix,false)) +
				(animalValue.getAnimalNutritionalNeeds() == null ? "" : animalValue.getAnimalNutritionalNeeds().dtoToJson(prefix,false)) + 
				(feedPlan != null ? feedPlan.dtoToJson(prefix + prefix, false) : "");
		return responseJson;
	}
}
















