package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.imd.dto.Message;
import com.imd.dto.User;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.MessageBean;
import com.imd.services.bean.UserBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;



@Path("/messagecatalog")
public class MessageCatalogSrvc {

	@POST
	@Path("/search")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchMessageCatalog(MessageBean messageBean){
//		int originalLoggingMode = IMDLogger.loggingMode;
//		IMDLogger.loggingMode = Util.INFO;
		
		String methodName = "searchMessageCatalog";

		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,messageBean.getLoginToken());
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgID();
		String langCd = user.getPreferredLanguage();
		messageBean.setOrgId(orgID);
		messageBean.setUserId(user.getUserId());
		IMDLogger.log(messageBean.toString(), Util.INFO);
    	try {
    		MessageCatalogLoader loader = new MessageCatalogLoader();
			List<Message> messages = loader.retrieveMessage(messageBean);
			if (messages == null || messages.isEmpty()) {
				return Response.status(Util.HTTPCodes.OK).entity("[]").build(); 
			} else {
				Iterator<Message> it = messages.iterator();
				String responseJson = "";
				while (it.hasNext()) {
					Message message = it.next();
					responseJson += ",\n{" + message.dtoToJson("  ") + "}";
				}
				if (!responseJson.isEmpty())
					responseJson = responseJson.replaceFirst(",\n", "");
				responseJson = "[\n" + responseJson + "\n]";
				IMDLogger.log(responseJson, Util.INFO);
				return Response.status(Util.HTTPCodes.OK).entity(responseJson).build(); 
			}

		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in MessageCatalogSrvc.searchMessageCatalog() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}
	@POST
	@Path("/add")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addToMessageCatalog(MessageBean messageBean){
//		int originalLoggingMode = IMDLogger.loggingMode;
//		IMDLogger.loggingMode = Util.INFO;
		String methodName = "addToMessageCatalog";

		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,messageBean.getLoginToken());
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgID();
		String langCd = user.getPreferredLanguage();
		messageBean.setOrgId(orgID);
		messageBean.setUserId(user.getUserId());
		IMDLogger.log(messageBean.toString(), Util.INFO);
    	try {
    		if (messageBean.getOrgId() == null || messageBean.getOrgId().isEmpty() || 
    			messageBean.getLanguageCD() == null || messageBean.getLanguageCD().trim().isEmpty()	||
    			messageBean.getMessageCD() == null || messageBean.getMessageCD().trim().isEmpty() ||
    			messageBean.getMessageText() == null || messageBean.getMessageText().trim().isEmpty()) {
    			IMDLogger.log("One or more values are missing\n\n" + messageBean.toString() + "\n\n", Util.ERROR);
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.MISSING_VALUE).getMessageText();
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();    				
    		}

    		MessageCatalogLoader loader = new MessageCatalogLoader();
			int addResult = loader.insertMessage(messageBean);
			if (addResult == 1) {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.RECORD_SUCCESSFULLY_ADDED).getMessageText();
    			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + message + "\"}").build();    				
			} else if (addResult == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION) {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.ALREADY_EXISTS).getMessageText();
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();    				
			} else {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.GENERIC_ADDITION_FAILED_MESSAGE).getMessageText();
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();    				
			}

		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in MessageCatalogSrvc.addToMessageCatalog() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
//		} finally {
//			IMDLogger.loggingMode = originalLoggingMode;			
		}
	}	
	@POST
	@Path("/update")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateMessageCatalog(MessageBean messageBean){
		int originalLoggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;

		String methodName = "updateMessageCatalog";

		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,messageBean.getLoginToken());
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgID();
		String langCd = user.getPreferredLanguage();
		messageBean.setOrgId(orgID);
		messageBean.setUserId(user.getUserId());
		IMDLogger.log(messageBean.toString(), Util.INFO);
    	try {
    		if (messageBean.getOrgId() == null || messageBean.getOrgId().isEmpty() || 
    			messageBean.getLanguageCD() == null || messageBean.getLanguageCD().trim().isEmpty()	||
    			messageBean.getMessageCD() == null || messageBean.getMessageCD().trim().isEmpty() ||
    			messageBean.getMessageText() == null || messageBean.getMessageText().trim().isEmpty()) {
    			IMDLogger.log("One or more values are missing\n\n" + messageBean.toString() + "\n\n", Util.ERROR);
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.MISSING_VALUE).getMessageText();
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();    				
    		}

    		MessageCatalogLoader loader = new MessageCatalogLoader();
			int updateResult = loader.updatedMessage(messageBean);
			if (updateResult == 1) {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.RECORD_SUCCESSFULLY_UPDATED).getMessageText();
    			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + message + "\"}").build();    				
			} else {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.GENERIC_UPDATE_FAILED_MESSAGE).getMessageText();
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();    				
			}

		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in MessageCatalogSrvc.addToMessageCatalog() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;			
		}
    	
	}	
}
















