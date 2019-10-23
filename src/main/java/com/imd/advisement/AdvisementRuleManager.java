package com.imd.advisement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.AnimalAdvisement;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class AdvisementRuleManager {
	public static final String SEVERITY_THRESHOLD1 = "THRESHOLD1";
	public static final String SEVERITY_THRESHOLD2 = "THRESHOLD2";
	public static final String SEVERITY_THRESHOLD3 = "THRESHOLD3";

	private HashMap<String, AdvisementRule> advisementRulesMap = new HashMap<String, AdvisementRule>();
	
	public AdvisementRuleManager() {
		advisementRulesMap.put(Util.AdvisementRules.DEHORN, new DehorningAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.DRYCOW, new DryCowAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.VACCINEFMD, new FMDVaccinationAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.PREGNANCYTEST, new PregnancyTestAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.HEATWARNING, new HeatWarningAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.DELAYEDHEATHEIFER, new DelayedHeatHeiferAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.DELAYEDHEATCOW, new DelayedHeatCowAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.WEIGHTMEASUREMENT, new WeightMeasurementAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.HEIFERWEIGHT, new HeiferWeightAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.WEANOFF, new WeanOffAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.DEWORM, new DewormingAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.CALVINGPREPFEED, new CalvingPrepFeedAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.CALFWEIGHT, new CalfWeightAdvisement());
		advisementRulesMap.put(Util.AdvisementRules.MASTITIS, new MastitisTestAdvisement());
	}

	public List<AnimalAdvisement> executeAllRules(List<Advisement> activeRules, boolean includeThreshold1, boolean includeThreshold2, boolean includeThreshold3) {
		List<AnimalAdvisement> advisementOutcomes = new ArrayList<AnimalAdvisement> ();
		if (activeRules != null && !activeRules.isEmpty()) {
			Iterator<Advisement> advRuleIt = activeRules.iterator();
			while (advRuleIt.hasNext()) {
				Advisement advConfig = advRuleIt.next();
				AdvisementRule advRule = advisementRulesMap.get(advConfig.getAdvisementID());
				if (advRule != null) {
					List <Animal> animalsInViolation = advRule.getAdvisementRuleAddressablePopulation(advConfig.getOrgId());
					if (animalsInViolation != null && !animalsInViolation.isEmpty()) {
						Iterator<Animal> it = animalsInViolation.iterator();
						while (it.hasNext()) {
							Animal animalInViolation = it.next();
							if (animalInViolation.isThreshold1Violated() && includeThreshold1) {
								advisementOutcomes.add(new AnimalAdvisement(animalInViolation.getAnimalTag(),SEVERITY_THRESHOLD1,animalInViolation.getNote(0).getNoteText(),animalInViolation.getNote(1) != null ? animalInViolation.getNote(1).getNoteText() : "",advConfig.getAdvisementID()));
							} else if (animalInViolation.isThreshold2Violated() && includeThreshold2) {
								advisementOutcomes.add(new AnimalAdvisement(animalInViolation.getAnimalTag(),SEVERITY_THRESHOLD2,animalInViolation.getNote(0).getNoteText(),animalInViolation.getNote(1) != null ? animalInViolation.getNote(1).getNoteText() : "",advConfig.getAdvisementID()));
							} else if (animalInViolation.isThreshold3Violated() && includeThreshold3) {
								advisementOutcomes.add(new AnimalAdvisement(animalInViolation.getAnimalTag(),SEVERITY_THRESHOLD3,animalInViolation.getNote(0).getNoteText(),animalInViolation.getNote(1) != null ? animalInViolation.getNote(1).getNoteText() : "",advConfig.getAdvisementID()));
							}
						}
					}
				} else {
					IMDLogger.log("The following active Advisement Rule exists in the database but it does not have any implementation: " + advConfig.getAdvisementID() , Util.WARNING);
				}
			}
		}
		return advisementOutcomes;
	}
}
