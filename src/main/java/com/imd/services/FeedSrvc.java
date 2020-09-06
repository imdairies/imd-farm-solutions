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

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.controller.feed.FeedManager;
import com.imd.dto.Animal;
import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.FeedItemBean;
import com.imd.services.bean.FeedPlanBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

@Path("/feed")
public class FeedSrvc {

	
	private static final String ITEM_COST = "Cost Rs.";

	@POST
	@Path("/updatefeedplan")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateFeedPlan(FeedPlanBean feedPlanBean){

		IMDLogger.log("updateFeedPlan Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + ".updateFeedPlan",feedPlanBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + ".updateFeedPlan", Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		feedPlanBean.setOrgID(orgID);
    	IMDLogger.log(feedPlanBean.toString(), Util.INFO);
    	
    	String validationOutcome = validateInputValues(feedPlanBean);
    	if (validationOutcome != null) {
    		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\""+ validationOutcome + "\"}").build();
    	} else {
    		FeedLoader loader = new FeedLoader();
    		FeedPlan feedPlan = new FeedPlan(feedPlanBean, user);
    		int updatedRecord = 0;
    		try {
				 updatedRecord = loader.updateFeedPlan(feedPlan);
				 if (updatedRecord == Util.ERROR_CODE.DUPLICATE_ENTRY) {
		    		validationOutcome = " Error Code :" + updatedRecord + ". A duplicate entry was encountered while trying to update the feed plan for the " +feedPlanBean.getFeedCohortCD() + " cohort. You can not add the same feed item multiple times.";
		    		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\""+ validationOutcome + "\"}").build();
				 } else if (updatedRecord < 0) {
		    		validationOutcome = " Error Code :" + updatedRecord + ". An unknown error occurred while updating the feed plan for the cohort (" +feedPlanBean.getFeedCohortCD() + ") ";
		    		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\""+ validationOutcome + "\"}").build();
				 }
			} catch (Exception e) {
				e.printStackTrace();
	    		validationOutcome = " The following error occurred while updating the feed plan for the cohort (" +feedPlanBean.getFeedCohortCD() + ") :" + e.getMessage();
	    		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\""+ validationOutcome + "\"}").build();
			}    		
    		validationOutcome = feedPlanBean.getFeedPlanItems().size() + " feed items successfully added to the feed plan for the cohort:" + feedPlanBean.getFeedCohortCD();
    		return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\""+ validationOutcome + "\"}").build();
    	}
	}
	
	private String validateInputValues(FeedPlanBean feedPlanBean) {
		String validationResult = null;
		
		if (feedPlanBean == null)
			validationResult = "Invalid request. No valid feeditem was found for the plan.";
		else if (feedPlanBean.getFeedCohortCD() == null || feedPlanBean.getFeedCohortCD().isEmpty())
			validationResult = "Invalid request. You must specifiy the feed cohort for the feed plan.";
		else if (feedPlanBean.getOrgID() == null || feedPlanBean.getOrgID().isEmpty())
			validationResult = "Invalid request. You must specifiy the orgID.";
		else if (feedPlanBean.getFeedPlanItems() == null || feedPlanBean.getFeedPlanItems().isEmpty()) {
			IMDLogger.log("No Feed Item was found for the feedplan that is being updated. This tantamounts to deleting the feed plan for the cohort (" + feedPlanBean.getFeedCohortCD() +")", Util.WARNING);
			validationResult = null;
		} else {
			Iterator<FeedItemBean> it = feedPlanBean.getFeedPlanItems().iterator();
			int i=0;
			while (it.hasNext()) {
				i++;
				FeedItemBean item = it.next();
				if (item.getFulfillmentPct() == null || item.getFulfillmentPct() == 0) {
					validationResult = " You must specify a valid numeric value for Fulfillment for feed item # " + i;
					break;
				} else if (item.getFeedItemCD() == null || item.getFeedItemCD().isEmpty()) {
					validationResult = " You must specify a valid Feed Item value for feed item # " + i;
					break;
				} else if (item.getFulFillmentTypeCD() == null || item.getFulFillmentTypeCD().isEmpty()) {
					validationResult = " You must specify a valid Fulfillment Type value for feed item # " + i;
					break;
				}
			}
		}
		return validationResult;
	}

	@POST
	@Path("/retrievefeedplan")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFeedPlan(AnimalBean animalBean){
		String methodName = "retrieveFeedPlan";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		animalBean.setOrgId(orgID);
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String responseJson = "";
		String feedCohortCD = animalBean.getAnimalType();
		String prefix = "  ";
		boolean shouldTranslate = false;
		if ( feedCohortCD == null || feedCohortCD.isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid cohort type.\"}").build();
		}

    	try {
			FeedLoader loader = new FeedLoader();
			FeedPlan feedPlan = loader.retrieveFeedPlan(orgID, feedCohortCD);
    		if (langCd != null && !langCd.isEmpty() && 
    				!langCd.equals(Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD))) {
    			shouldTranslate = true;
    		}
			if (feedPlan == null || feedPlan.getFeedPlanItems().isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Could not find feed plan for the cohort: " + feedCohortCD + " .\"}").build();
			Iterator<FeedItem> it = feedPlan.getFeedPlanItems().iterator();
			while(it.hasNext()) {
				FeedItem item = it.next();
				if (shouldTranslate) {
					String shortDescrCd = item.getFeedItemLookupValue().getShortDescriptionMessageCd();
					String longDescrCd = item.getFeedItemLookupValue().getLongDescriptionMessageCd();
					if (MessageCatalogLoader.getMessage(orgID, langCd, shortDescrCd) != null)
						item.getFeedItemLookupValue().setShortDescription(MessageCatalogLoader.getMessage(orgID, langCd, shortDescrCd).getMessageText());
					if (MessageCatalogLoader.getMessage(orgID, langCd, longDescrCd) != null)
						item.getFeedItemLookupValue().setLongDescription(MessageCatalogLoader.getMessage(orgID, langCd, longDescrCd).getMessageText());
				}
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
		IMDLogger.log("determineAnimalFeed Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + ".determineAnimalFeed",animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + ".determineAnimalFeed", Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		animalBean.setOrgId(orgID);
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String responseJson = "";
		if (animalBean.getAnimalTag() == null || animalBean.getAnimalTag().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();

    	try {
			FeedManager mgr = new FeedManager();
			AnimalLoader anmlLoader = new AnimalLoader();
			List<Animal> animals = anmlLoader.getAnimalRawInfo(animalBean);
			if (animals == null || animals.isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Animal " + animalBean.getAnimalTag() + " not found\"}").build();
			Animal animalValue = animals.get(0);
			String prefix = "  ";
			animalValue.setMilkingAverage(mgr.getMilkAverage(animalValue.getOrgId(), animalValue.getAnimalTag(), LocalDate.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.MILK_AVERAGE_START_FROM_DAYS), FeedManager.MILK_AVERAGE_DAYS));
			animalValue.setAnimalNutritionalNeeds(new CohortNutritionalNeeds());
			FeedPlan plan = mgr.getPersonalizedFeedPlan(animalValue);
	    	responseJson =  "\n{\n" + animalValue + "\n" + prefix + "\"feedPlan\":" + "[{\n" +   (plan != null ? plan.dtoToJson("  ") : "")+  "\n}\n" + "]\n}";
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
		String methodName = "retrieveActiveAnimalFeedListing";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		animalBean.setOrgId(orgID);
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String responseJson = "";
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
			FeedPlan allFeedItems = getFeedListingHeaderRow(orgID, loader);
			
			Iterator<Animal> it = herd.iterator();
			int count = 0;

			while (it.hasNext()) {
				count++;
				Animal animalValue = it.next();
				try {
					FeedPlan rawPlan = manager.getPersonalizedFeedPlan(animalValue);
					List<FeedItem> formattedPlanItems = formatFeedPlanItems(allFeedItems.getFeedPlanItems(), rawPlan, intakeQuantity);
					rawPlan.setFeedPlanItems(formattedPlanItems); 
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
			Iterator<FeedItem> it1 = allFeedItems.getFeedPlanItems().iterator();
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
			IMDLogger.log("Exception in FeedSrvc." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An error occurred while analyzing the farm feed needs: " +  e.getClass().getName() + "-" + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(responseJson, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(responseJson).build();
	}

	@POST
	@Path("/specificanimalfeedlisting")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveSpecificAnimalFeedListing(AnimalBean animalBean){
		String methodName = "retrieveSpecificAnimalFeedListing";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		if (animalBean == null || animalBean.getAnimalTag() == null || animalBean.getAnimalTag().trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The service was not called with correct parameters \"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		animalBean.setOrgId(orgID);
    	IMDLogger.log(animalBean.toString(), Util.INFO);
		String responseJson = "";
		HashMap<String,Float> intakeQuantity = new HashMap<String, Float>();
		
    	try {
    		String[] tags = animalBean.getAnimalTag().split(",");
    		String tagInClause = "";
    		for (int i=0; i<tags.length; i++) {
    			tagInClause += (i==0 ? "'" : ",'") + tags[i].trim() + "'";
    		}
    		if (tagInClause.trim().isEmpty()) {
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The service was not called with correct parameters \"}").build();    			
    		}
    		tagInClause = "(" + tagInClause.trim() + ")";
			FeedManager manager = new FeedManager();
			
			List<Animal> herd = manager.getFeedCohortInformationForSpecifcAnimals(orgID, tagInClause);
			if (herd == null || herd.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active animal found in the herd for the farm "+ orgID + "\"}").build();
			}
			String prefix = " ";
			String feedItemsJson = "";
			String animalFeedInfoJson = "";
			FeedLoader loader = new FeedLoader();
			FeedPlan allFeedItems = getFeedListingHeaderRow(orgID, loader);
			
			Iterator<Animal> it = herd.iterator();
			int count = 0;
			while (it.hasNext()) {
				count++;
				Animal animalValue = it.next();
				try {
					FeedPlan rawPlan = manager.getPersonalizedFeedPlan(animalValue);
					List<FeedItem> formattedPlan = formatFeedPlanItems(allFeedItems.getFeedPlanItems(), rawPlan, intakeQuantity);
					rawPlan.setFeedPlanItems(formattedPlan); 
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
			Iterator<FeedItem> it1 = allFeedItems.getFeedPlanItems().iterator();
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
			IMDLogger.log("Exception in " +this.getClass().getName() + "." + methodName+ " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An error occurred while analyzing the specific animals' feed needs: " +  e.getClass().getName() + "-" + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(responseJson, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(responseJson).build();
	}

	private FeedPlan getFeedListingHeaderRow(String orgID, FeedLoader loader) throws IMDException {
		FeedPlan allFeedItems = loader.retrieveDistinctFeedItemsInFeedPlan(orgID);
		List<FeedItem> feedItemsPlusTotalCostItem = new ArrayList<FeedItem>();
		FeedItem totalCostDummyFeedItem = new FeedItem();
		totalCostDummyFeedItem.setOrgId(orgID);				
		LookupValues feedItemLV = new LookupValues(ITEM_COST,ITEM_COST, 
				ITEM_COST,ITEM_COST,"","");
		totalCostDummyFeedItem.setFeedItemLookupValue(feedItemLV);	
		totalCostDummyFeedItem.setUnits("");
		
		feedItemsPlusTotalCostItem.add(totalCostDummyFeedItem);
		feedItemsPlusTotalCostItem.addAll(allFeedItems.getFeedPlanItems());
		allFeedItems.setFeedPlanItems(feedItemsPlusTotalCostItem);
		return allFeedItems;
	}


	private List<FeedItem> formatFeedPlanItems(List<FeedItem> allFeedItems, FeedPlan rawPlan, HashMap<String,Float> intakeQuantity) {
		List<FeedItem> formattedItems = new ArrayList<FeedItem>();
		Iterator<FeedItem> allItemsit = allFeedItems.iterator();
		FeedItem formattedItem = null;
		while (allItemsit.hasNext()) {
			FeedItem item = allItemsit.next();
			String itemCode = item.getFeedItemLookupValue().getLookupValueCode();
			formattedItem = item;
			FeedItem dummyItemForShowingItemCost = new FeedItem();
			dummyItemForShowingItemCost.setOrgId(item.getOrgId());				
			LookupValues feedItemLV = new LookupValues(ITEM_COST,ITEM_COST, 
					ITEM_COST,ITEM_COST,"","");
			dummyItemForShowingItemCost.setFeedItemLookupValue(feedItemLV);	
			dummyItemForShowingItemCost.setUnits("Rs.");
			dummyItemForShowingItemCost.setDailyIntake(rawPlan.getPlanCost());
			rawPlan.getFeedPlanItems().add(dummyItemForShowingItemCost);
			Iterator<FeedItem> rawItemsIt = rawPlan.getFeedPlanItems().iterator();
			while (rawItemsIt.hasNext()) {
				FeedItem rawItem = rawItemsIt.next();
				String rawItemCode = rawItem.getFeedItemLookupValue().getLookupValueCode();
				if (itemCode.equalsIgnoreCase(rawItemCode)) {
					if (rawItem.getDailyIntake() != null) {
						if (intakeQuantity.get(rawItemCode) != null) {
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
		if (intakeQuantity.get(ITEM_COST) != null)
			intakeQuantity.put(ITEM_COST, Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(intakeQuantity.get(ITEM_COST),0)));
			
		return formattedItems;
	}

	private String createFeedListingJson(String orgID, Animal animalValue, FeedPlan feedPlan, String prefix, DateTimeFormatter dttmFormatter) throws IMDException {
		String responseJson = "";
		
		responseJson =  prefix + animalValue.fieldToJson("orgID", orgID) + ",\n" + 
				prefix + animalValue.fieldToJson("currentAge", animalValue.getCurrentAge()) + ",\n" + 
				prefix + animalValue.fieldToJson("weight", animalValue.getWeight()) + ",\n" + 
				prefix + animalValue.fieldToJson("milkingAverage", animalValue.getMilkingAverage()) + ",\n" + 
				prefix + animalValue.fieldToJson("animalTag", animalValue.getAnimalTag()) + ",\n" +
				(animalValue.getAnimalNutritionalNeeds() == null ? "" : animalValue.getAnimalNutritionalNeeds().dtoToJson(prefix,false)) + 
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




