package com.imd.advisement;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
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
 * 	If a new born calf has not yet been dewormed
 *  
 * @author kashif.manzoor
 *
 */
public class DewormingAdvisement extends AdvisementRule {
	
	public DewormingAdvisement(){
		setAdvisementID(Util.AdvisementRules.DEWORM);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving animals that should be dewormed: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					Message localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getFirstThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setFirstThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getSecondThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setSecondThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getThirdThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setThirdThresholdMessage(localizedMessage.getMessageText());
				}
				if (languageCd == null)
					languageCd = Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD).toString();
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.DEWORM, null,null,null,null,null);
						String ruleNote = "";
						String animalNote = "";
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							int daysSinceDewormed= Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							if (ruleDto.getThirdThreshold() > 0 && daysSinceDewormed >= ruleDto.getThirdThreshold()) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH3, daysSinceDewormed) == null ? "":
									MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH3, daysSinceDewormed).getMessageText();
//								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please deworm it immediately.";	
							} else if (ruleDto.getSecondThreshold() > 0 && daysSinceDewormed >= ruleDto.getSecondThreshold()) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH2, daysSinceDewormed) == null ? "" :
									MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH2, daysSinceDewormed).getMessageText();
//								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please deworm it soon.";	
							} else if (ruleDto.getFirstThreshold() > 0 && daysSinceDewormed >= ruleDto.getFirstThreshold()) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH1, daysSinceDewormed) == null ? "" :
									MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH1, daysSinceDewormed).getMessageText();
//								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please plan to deworm it soon.";	
							} 
						} else {
							// No deworming event found - the animal was never dewormed.
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = MessageCatalogLoader.getMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH4) == null ? "" :
								MessageCatalogLoader.getMessage(orgId, languageCd, Util.MessageCatalog.DEWORMING_ADVISEMENT_TH4).getMessageText();	
						}
						if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
							ArrayList<Note> notesList = new ArrayList<Note>();
							notesList.add(new Note(1,ruleNote));
							notesList.add(new Note(2,animalNote));
							animal.setNotes(notesList);
							eligiblePopulation.add(animal);
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
