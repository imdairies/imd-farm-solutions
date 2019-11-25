package com.imd.advisement;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
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
 * 	Calves should be weaned off at three months of age.
 * @author kashif.manzoor
 *
 */
public class WeanOffAdvisement extends AdvisementRule {
	

	public WeanOffAdvisement(){
		setAdvisementID(Util.AdvisementRules.WEANOFF);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve animals who are younger than " + Util.YOUNG_ANIMAL_AGE_LIMIT + " days. " +  getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					MessageCatalogLoader langLoader = new MessageCatalogLoader();
					String localizedMessage  = langLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getFirstThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setFirstThresholdMessage(localizedMessage);
					localizedMessage  = langLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getSecondThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setSecondThresholdMessage(localizedMessage);
					localizedMessage  = langLoader.getMessage(ruleDto.getOrgID(), languageCd, ruleDto.getThirdThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setThirdThresholdMessage(localizedMessage);
				}
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveAnimalsBornOnOrAfterSpecifiedDate(orgId, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Util.YOUNG_ANIMAL_AGE_LIMIT));
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.WEANEDOFF, null,null,null,null,null);
						int currentAgeInDays = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), animal.getDateOfBirth());
						String ruleNote = "";
						String animalNote = "";
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// This calf doesn't have any weanoff event i.e. it is not weaned off yet.
							animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has not been weaned off. ";	
							if (currentAgeInDays >= thirdThreshold) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
								animalNote += "You should immediately wean it off. You are wasting money by not weaning it off.";
							} else 	if (currentAgeInDays >= secondThreshold) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote += "You should wean it off.";
							} else if (currentAgeInDays >= firstThreshold) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote += "You should plan its weaning off.";
							} else {
								//the young animal should not be weanedoff yet
								IMDLogger.log("Animal " + animal.getAnimalTag() + " is too young to be weaned off. Its just " +currentAgeInDays + " days old", Util.INFO);
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
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eligiblePopulation;
	}


}
