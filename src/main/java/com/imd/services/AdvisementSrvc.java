package com.imd.services;


import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
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
	@Path("/retrievealladvisement")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAllAdvisement(AdvisementBean advBean){
		AdvisementLoader loader = new AdvisementLoader();
		IMDLogger.log("retrieveAllAdvisement Service Called with following input values", Util.INFO);
		IMDLogger.log(advBean.toString(), Util.INFO);
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		String orgId = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.ORG_ID);
		List<Advisement> activeRules = loader.getAllActiveRules(orgId);
		AdvisementRuleManager advManager = new AdvisementRuleManager();

		List<AnimalAdvisement> advResults = advManager.executeAllRules(activeRules,
				advBean.isThreshold1Violated(),advBean.isThreshold2Violated(),advBean.isThreshold3Violated());			

//		[
//		 	{
//		 		"advisementRule:" "DEHORN",
//		 		"severity:" "THRESHOLD1",
//		 		"animalTags:" "002, 003, 004, 005"
//		 	}
//		]
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
			String shortMessage = "";
			AnimalAdvisement value = advJson.get(key);
			if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD1)) {
				shortMessage = "You should soon " + getActionName(value.getAppliedAdvisementRule()) + " the following animals: ";
			} else if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD2)) {
				shortMessage = "You must " + getActionName(value.getAppliedAdvisementRule()) + " the following animals immediately: ";
			} else if (value.getSeverityLevel().equalsIgnoreCase(AdvisementRuleManager.SEVERITY_THRESHOLD3)) {
				shortMessage = "Please " + getActionName(value.getAppliedAdvisementRule()) + " the following animals immediately, its over due: ";
			}
			returnJson += "{\n" + 
					"\"advisementRule\":\"" + value.getAppliedAdvisementRule() + "\"," +
					"\"severityThreshold\":\"" + value.getSeverityLevel() + "\"," +
					"\"ruleOutcomeShortMessage\":\"" + shortMessage + "\"," +
					"\"ruleOutcomeLongMessage\":\"" + value.getRuleOutcomeLongMessage() + "\"," +
					"\"animalTags\":\"" + value.getAnimalTag() + "\"\n}"+ (++count == size ? "\n" : ",\n");
		}
		returnJson = "[\n" + returnJson + "]";
		return Response.status(400).entity(returnJson).build();
	}

	private String getActionName(String appliedAdvisementRule) {
		if (appliedAdvisementRule.equalsIgnoreCase(Util.AdvisementRules.DEHORN))
			return "dehorn";
		else if (appliedAdvisementRule.equalsIgnoreCase(Util.AdvisementRules.DRYCOW))
			return "dry";
		else if (appliedAdvisementRule.equalsIgnoreCase(Util.AdvisementRules.VACCINEFMD))
			return "vaccinate (FMD)";
		else if (appliedAdvisementRule.equalsIgnoreCase(Util.AdvisementRules.PREGNANCYTEST))
			return "check pregnancy of";
		else 
			return "UNKNOWN";
	}

}


