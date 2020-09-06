package com.imd.services;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.imd.controller.feed.FeedManager;
import com.imd.dto.FeedItem;
import com.imd.dto.FoodInventoryItem;
import com.imd.dto.FoodUsage;
import com.imd.dto.Inventory;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.InventoryLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.FoodInventoryItemBean;
import com.imd.services.bean.InventoryBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

@Path("/inventory")
public class InventorySrvc {
	
	@POST
	@Path("/addsemen")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addSemenInventory(InventoryBean inventoryBean){
		String methodName = "addSemenInventory";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
		    	String validityMessage = inventoryBean.validateValues();
		int result = -1;
		if (validityMessage.isEmpty()) {
	    	try {
				Inventory inventory = new Inventory(inventoryBean);
				inventory.setOrgId(orgId);
				inventory.setCreatedBy(user);
				inventory.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				inventory.setUpdatedBy(user);
				inventory.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
	    		InventoryLoader loader = new InventoryLoader();
				result  = loader.insertSemenInventory(inventory);
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in InventorySrvc.addSemenInventory() service method: " + e.getMessage(),  Util.ERROR);
			}
			if (result == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Semen inventory has been added\"}").build();
			else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified semen record already exists\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Inventory could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Inventory could not be updated. Please submit a bug report.\"}").build();
			else 
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during inventory update\"}").build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + validityMessage + "\"}").build();			
		}
	}
	
	@POST
	@Path("/retrievallsiresinventory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAllSiresInventory(AnimalBean searchBean){
		String methodName = "retrieveAllSiresInventory";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		String sireValueResult = "";

    	try {
    		AnimalLoader loader = new AnimalLoader();
    		InventoryLoader invLoader = new InventoryLoader();
			List<Sire> sireValues = loader.retrieveAISire();
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();
			}
			Inventory invOutput = null;
			Iterator<Sire> sireValueIt = sireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		Sire sireValue = sireValueIt.next();
	    		if (sireValue.getAnimalTag().equalsIgnoreCase("UNKNOWN"))
	    			continue;
	    		String invConv = ",\n   \"conventionalStock\":\"0\"";
	    		String invSexed = ",\n   \"sexedStock\":\"0\"";
				invOutput = invLoader.getRemainingSemenInventory(orgId,sireValue.getAnimalTag(),"Y");
				if (invOutput != null) {
					invSexed = ",\n   \"sexedStock\":\"" + invOutput.getAuxValue2() + "\"";
				}
				invOutput = invLoader.getRemainingSemenInventory(orgId, sireValue.getAnimalTag(), "N");
				if (invOutput != null) {
					invConv = ",\n   \"conventionalStock\":\"" + invOutput.getAuxValue2() + "\"";
				}
	    		sireValueResult += "{\n" + sireValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + invConv + invSexed + "\n},\n";
	    	}
	    	if (!sireValueResult.isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in InventorySrvc.retrievallsiresinventory() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(sireValueResult).build();
    }		

	@POST
	@Path("/addfooditeminventory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addFeedItemInventory(FoodInventoryItemBean inventoryBean){
		String methodName = "addfooditeminventory";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = "";
    	
    	if (inventoryBean.getCategoryCd() == null || inventoryBean.getCategoryCd().isEmpty())
    		validityMessage += " categoryCd ";
    	if (inventoryBean.getLookupCd() == null || inventoryBean.getLookupCd().isEmpty())
    		validityMessage += " lookupCd ";
    	if (inventoryBean.getNumberOfUnits() == null)
    		validityMessage += " numberOfUnits ";
    	if (inventoryBean.getPackingUnitCode() == null || inventoryBean.getPackingUnitCode().isEmpty())
    		validityMessage += " packingUnitCode ";
    	if (inventoryBean.getQuantityPerUnit() == null)
    		validityMessage += " quantityPerUnit ";
    	if (inventoryBean.getQuantityUnitCode() == null || inventoryBean.getQuantityUnitCode().isEmpty())
    		validityMessage += " quantityUnitCode ";
    	if (inventoryBean.getPricePerUnit() == null)
    		validityMessage += " pricePerUnit ";
    	if (inventoryBean.getCurrencyCode() == null || inventoryBean.getCurrencyCode().isEmpty())
    		validityMessage += " currencyCode ";
    	if (inventoryBean.getOrderFromDttmStr() == null || inventoryBean.getOrderFromDttmStr().isEmpty())
    		validityMessage += " orderFromDttmStr ";
    	
		int result = -1;
		if (validityMessage.isEmpty()) {
	    	try {
	    		FoodInventoryItem inventory = new FoodInventoryItem(inventoryBean);
	    		inventory.setOrgId(orgId);
				inventory.setCreatedBy(user);
				inventory.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				inventory.setUpdatedBy(user);
				inventory.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
	    		InventoryLoader loader = new InventoryLoader();
				result  = loader.addFoodInventory(inventory);
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			}
			if (result == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Food Item inventory has been added\"}").build();
			else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Food Item inventory record violates data integrity\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Food Item Inventory could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Food Item Inventory could not be updated. Please submit a bug report.\"}").build();
			else 
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during Food Item inventory update\"}").build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following field(s) were missing: " + validityMessage + "\"}").build();			
		}
	}

	@POST
	@Path("/retrievefooditeminventory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFoodItemInventory(FoodInventoryItemBean searchBean){
		String methodName = "retrieveFoodItemInventory";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgId);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		String invValueResult = "";

    	try {
    		FeedManager manager = new FeedManager();
    		HashMap<String, FeedItem> feedItemIntakeInfo = new HashMap<String, FeedItem>();
    		InventoryLoader invLoader = new InventoryLoader();
			List<FoodInventoryItem> itemValues = invLoader.retrieveFoodItemInventory(searchBean);
			FoodInventoryItem invOutput = null;
			Iterator<FoodInventoryItem> itemValuesIt = itemValues.iterator();
			if (!itemValues.isEmpty() && searchBean.isShouldShowReorderDays())
				feedItemIntakeInfo = manager.getFarmFeedItemsDailyIntake(searchBean.getOrgId());
	    	while (itemValuesIt.hasNext()) {
	    		invOutput = itemValuesIt.next();
	    		FeedItem intakeInfo = feedItemIntakeInfo.get(invOutput.getLookupCd());
				if (intakeInfo != null)
					IMDLogger.log(invOutput.getLookupCd() + " = " + intakeInfo.getDailyIntake(), Util.INFO);
	    		if (intakeInfo != null) {
	    			invOutput.setRemainingDays(invOutput.getRemainingQuantity()/intakeInfo.getDailyIntake());
	    		} else  {
	    			invOutput.setRemainingDays(null);
	    		}
	    		invValueResult += "{\n" + invOutput.dtoToJson("  ", DateTimeFormat.forPattern("MMMM d, yyyy HH:mm")) + "\n},\n";
	    	}
	    	if (!invValueResult.isEmpty() )
	    		invValueResult = "[" + invValueResult.substring(0,invValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		invValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(invValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(invValueResult).build();
    }		

	@POST
	@Path("/addfooditeminventoryusage")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addFeedItemInventoryUsage(FoodInventoryItemBean inventoryBean){
		String methodName = "addFeedItemInventoryUsage";
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = "";
    	
    	if (inventoryBean.getStockTrackingId() == null || inventoryBean.getStockTrackingId().isEmpty())
    		validityMessage += " stockTrackingId ";
    	if (inventoryBean.getConsumedFromDttmStr() == null || inventoryBean.getConsumedFromDttmStr().isEmpty())
    		validityMessage += " consumptionTimestamp ";
    	if (inventoryBean.getConsumptionQuantity() == null )
    		validityMessage += " consumptionQuantity ";

		int result = -1;
		if (validityMessage.isEmpty()) {
	    	try {
	    		inventoryBean.setConsumedFromDttmStr(inventoryBean.getConsumedFromDttmStr());
	    		FoodUsage usage = new FoodUsage(inventoryBean.getOrgId(), 
	    				inventoryBean.getStockTrackingId(), inventoryBean.getConsumedFromDttm(), 
	    				inventoryBean.getConsumptionQuantity(), 
	    				inventoryBean.getUsageCd(), inventoryBean.getComments(),
	    				DateTime.now(IMDProperties.getServerTimeZone()), user);
	    		InventoryLoader loader = new InventoryLoader();
	    		if (loader.retrieveRemainingQuantity(usage.getOrgId(), usage.getStockTrackingId()) == null || 
	    				Math.round(((loader.retrieveRemainingQuantity(usage.getOrgId(), usage.getStockTrackingId()).floatValue() - usage.getConsumptionQuantity().floatValue()) * 100)/100) < 0) {
	    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The usage exceedes the remaining stock " + usage.getConsumptionQuantity() + " > " + loader.retrieveRemainingQuantity(usage.getOrgId(), usage.getStockTrackingId()).floatValue() + "\"}").build();			
	    		}
				result  = loader.addFoodInventoryUsage(usage);
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			}
			if (result == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Food Item inventory usage has been added\"}").build();
			else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Food Item inventory usage record violates data integrity\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Food Item Inventory usage could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Food Item Inventory usage could not be updated. Please submit a bug report.\"}").build();
			else 
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An unknown error occurred during Food Item inventory usage update\"}").build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following field(s) were missing: " + validityMessage + "\"}").build();			
		}
	}

	
	@POST
	@Path("/editfooditeminventory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response editFoodItemInventory(FoodInventoryItemBean inventoryBean){
		String methodName = "editFoodItemInventory";
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = "";
    	
    	if (inventoryBean.getStockTrackingId() == null || inventoryBean.getStockTrackingId().isEmpty())
    		validityMessage += " stockTrackingId ";

		int result = -1;
		if (validityMessage.isEmpty()) {
	    	try {
	    		FoodInventoryItem inventory = new FoodInventoryItem(inventoryBean);
	    		inventory.setOrgId(orgId);
				inventory.setCreatedBy(user);
				inventory.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				inventory.setUpdatedBy(user);
				inventory.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
	    		InventoryLoader loader = new InventoryLoader();
				result  = loader.updateFoodInventoryItem(inventory);
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			}
			if (result == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Food Item inventory has been updated\"}").build();
			else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The specified Food Item inventory record violates data integrity\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Food Item Inventory could not be updated. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Food Item Inventory could not be updated. Please submit a bug report.\"}").build();
			else if (result == 0)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found for the update. Are you sure the following tracking id is valid: " + inventoryBean.getStockTrackingId() + "\"}").build();
			else 
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found for the update. Are you sure the following tracking id is valied: " + inventoryBean.getStockTrackingId() + "\"}").build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following field(s) were missing: " + validityMessage + "\"}").build();			
		}
	}
	
	@POST
	@Path("/retrieveitemusagedetails")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveItemUsageDetails(FoodInventoryItemBean inventoryBean){
		String methodName = "retrieveItemUsageDetails";
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = "";
    	
    	if (inventoryBean.getStockTrackingId() == null || inventoryBean.getStockTrackingId().isEmpty())
    		validityMessage += " stockTrackingId ";

    	String usageValueResult = "";
		if (validityMessage.isEmpty()) {
	    	try {
	    		FoodUsage usageOutput = null;
	    		InventoryLoader loader = new InventoryLoader();
				List<FoodUsage> usageList = loader.retrieveFeedInventoryUsage(inventoryBean.getOrgId(), inventoryBean.getStockTrackingId(), "A.USAGE_DTTM DESC");
				Iterator<FoodUsage> usageListIt = usageList.iterator();
				float remainingQuantity = 0f;
		    	while (usageListIt.hasNext()) {
		    		usageOutput = usageListIt.next();
		    		usageOutput.setRemainingQuantity(remainingQuantity);
		    		usageValueResult += "{\n" + usageOutput.dtoToJson("  ", DateTimeFormat.forPattern("MMMM d, yyyy HH:mm")) + "\n},\n";
		    		remainingQuantity += usageOutput.getConsumptionQuantity();
		    	}
		    	if (!usageValueResult.isEmpty() )
		    		usageValueResult = "[" + usageValueResult.substring(0,usageValueResult.lastIndexOf(",\n")) + "]";
		    	else
		    		usageValueResult = "[]";
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
			}
	    	IMDLogger.log(usageValueResult, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(usageValueResult).build();
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following field(s) were missing: " + validityMessage + "\"}").build();			
		}
	}	
	
	@POST
	@Path("/deleteitemusage")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response deleteItemUsage(FoodInventoryItemBean inventoryBean) {
		String methodName = "deleteItemUsage";
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,inventoryBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgId = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		inventoryBean.setOrgId(orgId);
		IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = "";
    	
    	if (inventoryBean.getOrgId() == null || inventoryBean.getOrgId().isEmpty())
    		validityMessage += " orgId ";
    	if (inventoryBean.getStockTrackingId() == null || inventoryBean.getStockTrackingId().isEmpty())
    		validityMessage += " stockTrackingId ";
    	if (inventoryBean.getConsumedFromDttmStr() == null || inventoryBean.getConsumedFromDttmStr().isEmpty())
    		validityMessage += " consumedFromDttmStr ";

		if (validityMessage.isEmpty()) {
	    	try {
	    		inventoryBean.setConsumedFromDttm(Util.parseDateTime(inventoryBean.getConsumedFromDttmStr(), "MMMM d, yyyy HH:mm"));
	    		InventoryLoader loader = new InventoryLoader();
				int deletedRecordCount = loader.deleteFoodInventoryUsage(inventoryBean.getOrgId(), 
						inventoryBean.getStockTrackingId(), inventoryBean.getConsumedFromDttm());
				if (deletedRecordCount > 0) {
					return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + deletedRecordCount + " usage record(s) successfully deleted\"}").build();								
				} else {
					return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The requested stock usage record could not be deleted. Error Code : " + deletedRecordCount + "\"}").build();								
				}
			} catch (Exception e) {
				e.printStackTrace();
				IMDLogger.log("Exception in " + this.getClass().getName()  + "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
			}
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The following field(s) were missing: " + validityMessage + "\"}").build();			
		}
	}	
}






