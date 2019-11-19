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
import com.imd.loader.AdvisementLoader;
import com.imd.services.bean.AdvisementBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;;

@Path("/advisement")
public class AdvisementSrvc {

	
	
	@POST
	@Path("/retrieveanimaladvisement")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAnimalAdvisement(AdvisementBean advBean){
		AdvisementLoader loader = new AdvisementLoader();
		IMDLogger.log("retrieveAnimalAdvisement Service Called with following input values", Util.INFO);
		IMDLogger.log(advBean.toString(), Util.INFO);
		if (!Util.verifyAccess("/advisement/retrieveanimaladvisement",advBean.getLoginToken())) {
			IMDLogger.log("User does not have a valid access token", Util.WARNING);
			return Response.status(401).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
			
		String animalTag = advBean.getAnimalTag();
		if (animalTag == null || animalTag.isEmpty())
			return Response.status(200).entity("{ \"error\": true, \"message\":\"Please specify a valid animal tag.\"}").build();
			
		String orgId = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);
		String langCd = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.LANG_CD);
		List<Advisement> activeRules = loader.getAllActiveRules(orgId);
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
		return Response.status(400).entity(returnJson).build();
	}	
	
	
	@POST
	@Path("/retrievealladvisement")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAllAdvisement(AdvisementBean advBean){
		AdvisementLoader loader = new AdvisementLoader();
		IMDLogger.log("retrieveAllAdvisement Service Called with following input values", Util.INFO);
		IMDLogger.log(advBean.toString(), Util.INFO);
		if (!Util.verifyAccess("/advisement/retrievealladvisement",advBean.getLoginToken())) {
			IMDLogger.log("User does not have a valid access token", Util.WARNING);
			return Response.status(401).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
//		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		String orgId = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);
		String langCd = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.LANG_CD);
		List<Advisement> activeRules = loader.getAllActiveRules(orgId);
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
		return Response.status(400).entity(returnJson).build();
	}
}


