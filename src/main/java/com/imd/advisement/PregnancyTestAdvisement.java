package com.imd.advisement;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Message;
import com.imd.dto.Note;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 * This advisement class implements the following advise:
 * Trigger Condition: 
 * 	If a cow has been inseminated long enough and its pregnancy has not yet been tested
 *  
 * @author kashif.manzoor
 *
 */
public class PregnancyTestAdvisement extends AdvisementRule {
	
	public PregnancyTestAdvisement(){
		setAdvisementID(Util.AdvisementRules.PREGNANCYTEST);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving cows whose pregnancy should be tested: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					Message localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getFirstThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setFirstThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getSecondThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setSecondThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getThirdThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setThirdThresholdMessage(localizedMessage.getMessageText());
				}
				if (languageCd == null)
					languageCd = Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD).toString();
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveInseminatedNonPregnantAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						DateTime startDate = DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Util.LACTATION_DURATION);
						DateTime endDate = DateTime.now(IMDProperties.getServerTimeZone()).plusDays(1);
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								startDate,
								endDate,
								Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							IMDLogger.log("Insemination Date: " + lifeEvents.get(0).getEventTimeStamp(), Util.INFO);
							int daysSinceInseminated= Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							String animalNote = "";
							String ruleNote = "";
							if (ruleDto.getThirdThreshold() > 0 && daysSinceInseminated >= ruleDto.getThirdThreshold()) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH3,daysSinceInseminated) == null ? "" : MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH3,daysSinceInseminated).getMessageText();
							} else if (ruleDto.getSecondThreshold() > 0 && daysSinceInseminated >= ruleDto.getSecondThreshold()) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH2,daysSinceInseminated) == null ? "" : MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH2,daysSinceInseminated).getMessageText();
							} else if (ruleDto.getFirstThreshold() > 0 && daysSinceInseminated >= ruleDto.getFirstThreshold()) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH1,daysSinceInseminated) == null ? "" : MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.PREGNANCY_TEST_ADVISEMENT_TH1,daysSinceInseminated).getMessageText();
							}
							if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
								animal.addLifecycleEvent(lifeEvents.get(0));
								ArrayList<Note> notesList = new ArrayList<Note>();
								notesList.add(new Note(1,ruleNote));
								notesList.add(new Note(2,animalNote));
								animal.setNotes(notesList);
								eligiblePopulation.add(animal);
							}
						} else {
							IMDLogger.log("This inseminated cow (" + animal.getAnimalTag() + ") does not have an insemination event in the last " + Util.LACTATION_DURATION + " days. This indicates that the user has either forgotten to add inseimnation event or has set the wrong current status (" + animal.getAnimalType() + ") of the animal", Util.ERROR);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eligiblePopulation;
	}


}
