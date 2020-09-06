package com.imd.services;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.imd.advisement.AdvisementRuleManager;
import com.imd.dto.Advisement;
import com.imd.dto.AnimalAdvisement;
import com.imd.dto.User;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AdvisementBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;;

@Path("/advisement")
public class AdvisementSrvc {

	
	
	@POST
	@Path("/retrieveanimaladvisement")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAnimalAdvisement(AdvisementBean advBean){
		String methodName = "retrieveAnimalAdvisement";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,advBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		IMDLogger.log(advBean.toString(), Util.INFO);

			
		AdvisementLoader loader = new AdvisementLoader();
		String animalTag = advBean.getAnimalTag();
		if (animalTag == null || animalTag.isEmpty())
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();
			
		List<Advisement> activeRules = loader.getAllActiveRules(orgID);
		AdvisementRuleManager advManager = new AdvisementRuleManager();

		List<AnimalAdvisement> advResults = advManager.executeAllRules(activeRules,
				advBean.isThreshold1Violated(),advBean.isThreshold2Violated(),advBean.isThreshold3Violated(), langCd);			

		String mapKey = "";
		HashMap<String, AnimalAdvisement> advJson = new HashMap<String, AnimalAdvisement>();
		Iterator<AnimalAdvisement> it = advResults.iterator();
		while (it.hasNext()) {
			AnimalAdvisement populationAnimal = it.next();
			if (populationAnimal.getAnimalTag().equalsIgnoreCase(animalTag)) {
				mapKey = populationAnimal.getAppliedAdvisementRule() + "-" + populationAnimal.getSeverityLevel();
				AnimalAdvisement mapEntry = advJson.get(mapKey);
				if (mapEntry == null) {
					mapEntry = new AnimalAdvisement(populationAnimal);
					advJson.put(mapKey, mapEntry);
//				} else {
//					mapEntry.setAnimalTag(mapEntry.getAnimalTag() + ", " + populationAnimal.getAnimalTag());
				}
			}
		}
		
		String returnJson = "";
		Iterator<String> mapKeysIt = advJson.keySet().iterator();
		int size = advJson.size();
		int count = 0;
		while (mapKeysIt.hasNext()) {
			String key = mapKeysIt.next();
			AnimalAdvisement value = advJson.get(key);
			returnJson += "{\n" + 
					"\"advisementRule\":\"" + value.getAppliedAdvisementRule() + "\"," +
					"\"severityThreshold\":\"" + value.getSeverityLevel() + "\"," +
					"\"ruleOutcomeMessage\":\"" + value.getAnimalSpecificMessage() + "\"," +
					"\"animalTags\":\"" + value.getAnimalTag() + "\"\n}"+ (++count == size ? "\n" : ",\n");
		}
		returnJson = "[\n" + returnJson + "]";
		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity(returnJson).build();
	}	
	
	
	@POST
	@Path("/retrievealladvisement")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAllAdvisement(AdvisementBean advBean){
		String methodName = "retrieveAllAdvisement";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,advBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		IMDLogger.log(advBean.toString(), Util.INFO);

		AdvisementLoader loader = new AdvisementLoader();
		List<Advisement> activeRules = loader.getAllActiveRules(orgID);
		AdvisementRuleManager advManager = new AdvisementRuleManager();

		List<AnimalAdvisement> advResults = advManager.executeAllRules(activeRules,
				advBean.isThreshold1Violated(),advBean.isThreshold2Violated(),advBean.isThreshold3Violated(), langCd);			

		String mapKey = "";
		HashMap<String, AnimalAdvisement> advJson = new HashMap<String, AnimalAdvisement>();
		Iterator<AnimalAdvisement> it = advResults.iterator();
		while (it.hasNext()) {
			AnimalAdvisement populationAnimal = it.next();
			mapKey = populationAnimal.getAppliedAdvisementRule() + "-" + populationAnimal.getSeverityLevel();
			AnimalAdvisement mapEntry = advJson.get(mapKey);
			if (mapEntry == null) {
				mapEntry = new AnimalAdvisement(populationAnimal);
				advJson.put(mapKey, mapEntry);
			} else {
				mapEntry.setAnimalTag(mapEntry.getAnimalTag() + ", " + populationAnimal.getAnimalTag());
			}
		}
		
		String returnJson = "";
		Iterator<String> mapKeysIt = advJson.keySet().iterator();
		int size = advJson.size();
		int count = 0;
		while (mapKeysIt.hasNext()) {
			String key = mapKeysIt.next();
			AnimalAdvisement value = advJson.get(key);
//			if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD1)) {
//				shortMessage = "The following animals need to be looked at for You should soon " + getActionName(value.getAppliedAdvisementRule()) + " the following animals: ";
//			} else if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD2)) {
//				shortMessage = "You must " + getActionName(value.getAppliedAdvisementRule()) + " the following animals immediately: ";
//			} else if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD3)) {
//				shortMessage = "Please " + getActionName(value.getAppliedAdvisementRule()) + " the following animals immediately, its over due: ";
//			}
			returnJson += "{\n" + 
					"\"advisementRule\":\"" + value.getAppliedAdvisementRule() + "\"," +
					"\"severityThreshold\":\"" + value.getSeverityLevel() + "\"," +
					"\"ruleOutcomeMessage\":\"" + value.getRuleOutcomeLongMessage() + "\"," +
					"\"animalTags\":\"" + value.getAnimalTag() + "\"\n}"+ (++count == size ? "\n" : ",\n");
		}
		returnJson = "[\n" + returnJson + "]";
		return Response.status(Util.HTTPCodes.BAD_REQUEST).entity(returnJson).build();
	}
}


