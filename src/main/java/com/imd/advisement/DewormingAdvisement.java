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
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving animals that should be dewormed: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
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
								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please deworm it immediately.";	
							} else if (ruleDto.getSecondThreshold() > 0 && daysSinceDewormed >= ruleDto.getSecondThreshold()) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please deworm it soon.";	
							} else if (ruleDto.getFirstThreshold() > 0 && daysSinceDewormed >= ruleDto.getFirstThreshold()) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote = animal.getAnimalTag() + " has not been dewormed in " + daysSinceDewormed + " days. Please deworm it soon.";	
							} 
						} else {
							// No deworming event found - the animal was never dewormed.
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = animal.getAnimalTag() + " has never been dewormed. Please deworm it immediately.";	
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

	@Override
	public HashMap<Animal, String> applyAdvisementRule(List<Animal> addressablePopulation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Animal, Integer> channelAdvisementRuleOutcome(List<Animal> addressablePopulation) {
		// TODO Auto-generated method stub
		return null;
	}

}
