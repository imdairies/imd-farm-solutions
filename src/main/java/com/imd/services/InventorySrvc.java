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

import com.imd.dto.Inventory;
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.loader.InventoryLoader;
import com.imd.loader.LookupValuesLoader;
import com.imd.services.bean.InventoryBean;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

@Path("/inventory")
public class InventorySrvc {
	
	@POST
	@Path("/addsemen")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchLookupValues(InventoryBean inventoryBean){
    	IMDLogger.log(inventoryBean.toString(), Util.INFO);
    	String validityMessage = inventoryBean.validateValues();
		int result = -1;
		if (validityMessage.isEmpty()) {
	    	try {
				Inventory inventory = new Inventory(inventoryBean);
				String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
				String orgId = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);
				inventory.setOrgID(orgId);
				inventory.setCreatedBy(new User(userID));
				inventory.setCreatedDTTM(DateTime.now());
				inventory.setUpdatedBy(new User(userID));
				inventory.setUpdatedDTTM(DateTime.now());
	    		InventoryLoader loader = new InventoryLoader();
				result  = loader.insertSemenInventory(inventory);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result == 1)
				return Response.status(200).entity("{ \"error\": false, \"message\":\"Semen inventory has been added\"}").build();
			else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"The specified semen record already exists\"}").build();
			else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Inventory could not be added. Please reduce the field length and try again.\"}").build();
			else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Inventory could not be updated. Please submit a bug report.\"}").build();
			else 
				return Response.status(200).entity("{ \"error\": true, \"message\":\"An unknown error occurred during inventory update\"}").build();
		} else {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + validityMessage + "\"}").build();			
		}
	}

}
