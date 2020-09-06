package com.imd.services;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.imd.dto.User;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.UserBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;



@Path("/user")
public class UserSrvc {

	@POST
	@Path("/login")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response loginUser(UserBean userBean){
    	IMDLogger.log(userBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		String userId = userBean.getUserId();
		String pwd = userBean.getLoginToken();
		userBean.setOrgId(orgID);
		if ( userId == null || userId.isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid user Id.\"}").build();
		userBean.setUserId(userId.toUpperCase());
		if ( pwd == null || pwd.isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid user password.\"}").build();

    	try {
			UserLoader loader = new UserLoader();
			User user = loader.authenticateUser(userBean.getOrgId(), userBean.getUserId(), loader.encryptPassword(userBean.getLoginToken()));
			if (user == null)
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Could not authenticate the user.\"}").build();
			
			setUserSessionValues(user);			
	    	responseJson = "{\n"
	    			+ "\"authToken\":\"" + user.getPassword() + "\"\n"
					+ "}";
	    	IMDLogger.log(responseJson, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(responseJson).build(); 
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in UserSrvc.loginUser() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}	

	private void setUserSessionValues(User user) {
		// TODO: set additional user profile settings in here. As of now the session is not keyed on user. We need
		// to modify the functionality and key the session on user id.
		Util.getConfigurations().setSessionConfigurationValue(Util.ConfigKeys.LANG_CD, user.getPreferredLanguage());		
	}

	@POST
	@Path("/logout")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response logoutUser(UserBean userBean){
    	IMDLogger.log(userBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String authToken = userBean.getLoginToken();
		userBean.setOrgId(orgID);
		UserLoader loader = new UserLoader();
		//TODO we should set a scheduler that runs every few minutes and inactivates expired tokens. As of now we only do it whenever 
		// a logout API request is made.
		loader.inactivateExpiredTokens(null);

		if ( authToken == null || authToken.isEmpty())
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"No auth token passed with the request. Logout can't be processed.\"}").build();

    	try {
			int returnValue = loader.logoutUser(authToken);
			if (returnValue == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"You have been logged out from the previous active session.\"}").build();
			else
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Auth Token inactivation not needed.\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in UserSrvc.logoutUser() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}	

 
	@POST
	@Path("/updateprofile")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateUserProfile(UserBean userBean){
		IMDLogger.log("updateUserProfile Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + ".updateUserProfile",userBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + ".updateUserProfile", Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		userBean.setOrgId(orgID);
		userBean.setUserId(user.getUserId());
		IMDLogger.log(userBean.toString(), Util.INFO);

    	try {
			UserLoader loader = new UserLoader();
			int updatedRecordsCount = loader.updateUserProfile(userBean);

			if (updatedRecordsCount == 1)
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"" + 
						MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.USER_PROFILE_UPDATED) + 
						"\"}").build();
			else
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":" + 
						MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.USER_PROFILE_NOT_UPDATED, updatedRecordsCount) + 
						"\"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in UserSrvc.updateUserProfile() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}
	
	
	

}
















