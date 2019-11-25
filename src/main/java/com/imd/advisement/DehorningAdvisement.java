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
 * 	If an animal has not yet been dehorned in the last xx days.
 *  
 * @author kashif.manzoor
 *
 */
public class DehorningAdvisement extends AdvisementRule {
	
	public DehorningAdvisement(){
		setAdvisementID(Util.AdvisementRules.DEHORN);
	}

	
	///////////

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving calves that should be dehorned: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				
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
				animalPopulation = animalLoader.retrieveCalves(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.DEHORN, null,null,null,null,null);
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// No dehorning event found - the calf was never dehorned.
							IMDLogger.log("Birth Date: " + animal.getDateOfBirth(), Util.INFO);
							int daysSinceBirth= Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), animal.getDateOfBirth());
							String ruleNote = "";
							String animalNote = animal.getAnimalTag() + " was born " + daysSinceBirth + " days ago and it hasn't been dehorned yet. Please de-horn the animal.";							
							if (ruleDto.getThirdThreshold() > 0 && daysSinceBirth >= ruleDto.getThirdThreshold()) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
							} else if (ruleDto.getSecondThreshold() > 0 && daysSinceBirth >= ruleDto.getSecondThreshold()) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
							} else if (ruleDto.getFirstThreshold() > 0 && daysSinceBirth >= ruleDto.getFirstThreshold()) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
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
