package com.imd.services;

import java.util.ArrayList;
import java.util.HashMap;
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
	@Path("/retrievefeedplan")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFeedPlan(AnimalBean animalBean){
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		String feedCohortCD = animalBean.getAnimalType();
		String prefix = "  ";
		if ( feedCohortCD == null || feedCohortCD.isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid cohort type.\"}").build();

    	try {
			FeedLoader loader = new FeedLoader();
			FeedPlan feedPlan = loader.retrieveFeedPlan(orgID, feedCohortCD);
			if (feedPlan == null || feedPlan.getFeedPlan().isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Could not find feed plan for the cohort: " + feedCohortCD + " .\"}").build();
			Iterator<FeedItem> it = feedPlan.getFeedPlan().iterator();
			while(it.hasNext()) {
				FeedItem item = it.next();
				responseJson += "{\n" +  item.dtoToJson(prefix) + "\n}" + (it.hasNext() ? ",\n" : "\n");
			}	
	    	responseJson = "[" + responseJson + "]";
	    	IMDLogger.log(responseJson, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(responseJson).build(); 
		} catch (IMDException e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.retrieveFeedPlan() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.retrieveFeedPlan() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Following error was encountered in retrieving cohort feed plan: " + e.getMessage() + "\"}").build();
		}
	}	
	
	@POST
	@Path("/determineanimalfeed")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response determineAnimalFeed(AnimalBean animalBean){
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		if (animalBean.getAnimalTag() == null || animalBean.getAnimalTag().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();

    	try {
			FeedManager mgr = new FeedManager();
			FeedCohort feedCohortType = mgr.getAnimalFeedCohort(orgID, animalBean.getAnimalTag());
			FeedPlan plan = mgr.getPersonalizedFeedPlan(feedCohortType, animalBean.getAnimalTag());
	    	responseJson = "[\n" + "{\n" + feedCohortType.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + (plan != null ? ",\n" + plan.dtoToJson("  ") : "")+  "\n}\n" + "]";
	    	IMDLogger.log(responseJson, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(responseJson).build(); 
		} catch (IMDException e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.determineAnimalFeed() service method while processing " + animalBean.getAnimalTag() + ": " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.determineAnimalFeed() service method while processing " + animalBean.getAnimalTag() + ": " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Following error was encountered in processing animal feed analysis: " + e.getMessage() + "\"}").build();
		}
	}
	
	@POST
	@Path("/farmactiveanimalfeedlisting")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveActiveAnimalFeedListing(AnimalBean animalBean){
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
//		String feedItems = "";
		HashMap<String,Float> intakeQuantity = new HashMap<String, Float>();

    	try {
			FeedManager manager = new FeedManager();
			List<Animal> herd = manager.getFeedCohortInformationForFarmActiveAnimals(orgID);
			if (herd == null || herd.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active animal found in the herd for the farm "+ orgID + "\"}").build();
			}
			String prefix = " ";
			String feedItemsJson = "";
			String animalFeedInfoJson = "";
			FeedLoader loader = new FeedLoader();
			FeedPlan allFeedItems = loader.retrieveDistinctFeedItemsInFeedPlan(orgID);
			
			Iterator<Animal> it = herd.iterator();
			int count = 0;
			while (it.hasNext()) {
				count++;
				Animal animalValue = it.next();
				try {
					FeedPlan rawPlan = manager.getPersonalizedFeedPlan(animalValue);
					List<FeedItem> formattedPlan = formatFeedPlan(allFeedItems.getFeedPlan(), rawPlan, intakeQuantity);
					rawPlan.setFeedPlan(formattedPlan); 
					animalFeedInfoJson += prefix + prefix + "\n{\n" + createFeedListingJson(orgID, animalValue,rawPlan,prefix+prefix+prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + prefix + prefix + "\n} " 
							+ (herd.size() == count ?  "" : ",");
				} catch (Exception ex) {
					ex.printStackTrace();
					IMDLogger.log("An exception occurred while determining the Feed plan for tag#: " + animalValue.getAnimalTag(), Util.ERROR);
					String defaultValues = createFeedListingJson(orgID, animalValue,null,prefix+prefix+prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"));
					animalFeedInfoJson += prefix + prefix + "\n{\n" + defaultValues + addNullValues(prefix+prefix, animalValue) + prefix + prefix + "\n} " 
							+ (herd.size() == count ?  "" : ",");
				}
			}
			
			
			Iterator<FeedItem> it1 = allFeedItems.getFeedPlan().iterator();
			while (it1.hasNext()) {
				FeedItem item = it1.next();
				String code = item.getFeedItemLookupValue().getLookupValueCode();
				item.setDailyIntake(intakeQuantity.get(code));
			}
			
			
			feedItemsJson = allFeedItems.dtoToJson(prefix + prefix+prefix, false);
    		responseJson = "\n{\n" + prefix
    				+ "\"feedItems\":{" + feedItemsJson + prefix + "},\n" + prefix
    				+ "\"animalFeedInfo\":["  + animalFeedInfoJson + "]\n"
    				+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in FeedSrvc.retrieveActiveAnimalFeedListing() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An error occurred while analyzing the farm feed needs: " +  e.getClass().getName() + "-" + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(responseJson, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(responseJson).build();
	}


	private List<FeedItem> formatFeedPlan(List<FeedItem> allFeedItems, FeedPlan rawPlan, HashMap<String,Float> intakeQuantity) {
		List<FeedItem> formattedItems = new ArrayList<FeedItem>();
		Iterator<FeedItem> allItemsit = allFeedItems.iterator();
		FeedItem formattedItem = null;
		while (allItemsit.hasNext()) {
			FeedItem item = allItemsit.next();
			String itemCode = item.getFeedItemLookupValue().getLookupValueCode();
			formattedItem = item;
			Iterator<FeedItem> rawItemsIt = rawPlan.getFeedPlan().iterator();
			while (rawItemsIt.hasNext()) {
				FeedItem rawItem = rawItemsIt.next();
				String rawItemCode = rawItem.getFeedItemLookupValue().getLookupValueCode();
				if (itemCode.equalsIgnoreCase(rawItemCode)) {
					if (rawItem.getDailyIntake() != null) {
						if (intakeQuantity.get(rawItemCode) != null){
							intakeQuantity.put(rawItemCode, intakeQuantity.get(rawItemCode) + rawItem.getDailyIntake());
						} else {
							intakeQuantity.put(rawItemCode, rawItem.getDailyIntake());
						}
					}
					formattedItem = rawItem;
					break;
				}
			}
			formattedItems.add(formattedItem);
		}
		return formattedItems;
	}

	private String createFeedListingJson(String orgID, Animal animalValue, FeedPlan feedPlan, String prefix, DateTimeFormatter forPattern) throws IMDException {
		String responseJson = "";
		
		responseJson =  prefix + animalValue.fieldToJson("orgID", orgID) + ",\n" + 
				prefix + animalValue.fieldToJson("currentAge", animalValue.getCurrentAge()) + ",\n" + 
				prefix + animalValue.fieldToJson("weight", animalValue.getWeight()) + ",\n" + 
				prefix + animalValue.fieldToJson("animalTag", animalValue.getAnimalTag()) + ",\n" +
				(animalValue.getAnimalNutritionalNeeds() == null ? "" : animalValue.getAnimalNutritionalNeeds().dtoToJson(prefix,false)) + 
//				(animalValue.getFeedCohortInformation() == null ? "" : animalValue.getFeedCohortInformation().dtoToJson(prefix,false)) +
				prefix + animalValue.fieldToJson("feedCohortDeterminatationCriteria", animalValue.getFeedCohortInformation().getAnimalFeedCohortDeterminatationMessage()) + ",\n" +
				prefix + animalValue.fieldToJson("feedCohortTypeShortDescr", animalValue.getFeedCohortInformation().getFeedCohortLookupValue().getShortDescription()) + ",\n" +
				(feedPlan != null ? feedPlan.dtoToJson(prefix + prefix, false) : "");
		return responseJson;
	}

	private String addNullValues(String prefix, Animal animalValue) {
		return prefix + animalValue.fieldToJson("nutritionalNeedsFeedCohortCD","")  + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsStart", "") + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsEnd", "") + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsDryMatter", "") + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsCrudeProtein", "") + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsMetabloizableEnergy", "") + ",\n" + 
				prefix + animalValue.fieldToJson("nutritionalNeedsFeedCohortCD", "") + ",\n" + 
				prefix + animalValue.fieldToJson("planAnalysisComments", "Could not detemined the plan for this animal") + ",\n" +  
				"\"feedPlanItems\" :[]";
  	}
}
















