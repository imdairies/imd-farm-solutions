package com.imd.services;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.imd.dto.User;
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
		IMDLogger.loggingMode = Util.INFO;
    	IMDLogger.log(userBean.toString(), Util.INFO);
		String orgID = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);    	
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);	
		String responseJson = "";
		String userId = userBean.getUserId();
		String pwd = userBean.getLoginToken();
		userBean.setOrgId(orgID);
		if ( userId == null || userId.isEmpty())
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Please specify a valid user Id.\"}").build();
		userBean.setUserId(userId.toUpperCase());
		if ( pwd == null || pwd.isEmpty())
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Please specify a valid user paqssword.\"}").build();

    	try {
			UserLoader loader = new UserLoader();
			User user = loader.authenticateUser(userBean.getOrgId(), userBean.getUserId(), userBean.getLoginToken());
			if (user == null)
				return Response.status(400).entity("{ \"error\": true, \"message\":\"Could not authenticate the user.\"}").build();
			Util.getConfigurations().setSessionConfigurationValue(Util.ConfigKeys.LANG_CD, user.getPreferredLanguage());
			
	    	responseJson = "{\n"
	    			+ "\"authToken\":\"" + user.getPassword() + "\"\n"
					+ "}";
	    	IMDLogger.log(responseJson, Util.INFO);
			return Response.status(200).entity(responseJson).build(); 
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in UserSrvc.loginUser() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}	
}
















