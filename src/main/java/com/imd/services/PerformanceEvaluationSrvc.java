package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.imd.dto.PerformanceMilestone;
import com.imd.dto.User;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.performance.PerformanceMilestoneManager;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;;

@Path("/performance")
public class PerformanceEvaluationSrvc {

	
	
	@POST
	@Path("/evaluateanimalperformance")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response evaluateAnimalPerformance(AnimalBean animalBean){
		String returnJson = "";
		String prefix = " ";
		String methodName = "evaluateAnimalPerformance";
		try {
			IMDLogger.log(methodName + " Called ", Util.INFO);
			User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,animalBean.getLoginToken());
			if (user == null) {
				IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
						(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
						+ this.getClass().getName()  + "." + methodName, Util.WARNING);
				return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
			}
			String orgID = user.getOrgID();
			String langCd = user.getPreferredLanguage();
			IMDLogger.log(animalBean.toString(), Util.INFO);
				
			String animalTag = animalBean.getAnimalTag();
			if (animalTag == null || animalTag.isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();
			PerformanceMilestoneManager milestoneManager = new PerformanceMilestoneManager();
			List<PerformanceMilestone> evalResults = milestoneManager.evaluateAllActivePerformanceMilestones(orgID, animalTag, langCd);
			Iterator<PerformanceMilestone> evalResultsIt = evalResults.iterator();
			int recCount = 0;
			while (evalResultsIt.hasNext()) {
				recCount++;
				returnJson += "{\n" + evalResultsIt.next().dtoToJson(prefix) + "\n}" + (recCount == evalResults.size() ? "" : ",\n");
			}
			returnJson = "[\n" + returnJson + "\n]";
			IMDLogger.log(returnJson, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(returnJson).build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName +  " " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + e.getMessage() + "\"}").build();
		}
	}	
	
}


