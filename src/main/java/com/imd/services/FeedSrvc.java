package com.imd.services;



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
import com.imd.dto.FeedCohort;
import com.imd.dto.Inventory;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.InventoryLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.InventoryBean;
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
	    	responseJson = "[\n" + "{\n" + feedCohortType.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n}\n" + "]";
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
//		if (result == 1)
//			return Response.status(200).entity("{ \"error\": false, \"message\":\"Semen inventory has been added\"}").build();
//		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified semen record already exists\"}").build();
//		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Inventory could not be added. Please reduce the field length and try again.\"}").build();
//		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Inventory could not be updated. Please submit a bug report.\"}").build();
//		else 
//			return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during inventory update\"}").build();
	}
	
	@POST
	@Path("/retrievallsiresinventory")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAllSiresInventory(AnimalBean searchBean){
		String sireValueResult = "";
		String orgId = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);
    	IMDLogger.log(" /retrievallsiresinventory API called", Util.INFO);

    	try {
    		AnimalLoader loader = new AnimalLoader();
    		InventoryLoader invLoader = new InventoryLoader();
			List<Sire> sireValues = loader.retrieveAISire();
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();
			}
			Inventory invOutput = null;
			Iterator<Sire> sireValueIt = sireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		String invConv = ",\n   \"conventionalStock\":\"0\"";
	    		String invSexed = ",\n   \"sexedStock\":\"0\"";
	    		Sire sireValue = sireValueIt.next();
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(200).entity(sireValueResult).build();
    }		
	

}