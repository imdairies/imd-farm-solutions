package com.imd.advisement;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

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
import com.imd.util.Util;

/**
 * This advisement class implements the following advise:
 * Trigger Condition: 
 * 	Calves should be weaned off at three months of age.
 * @author kashif.manzoor
 *
 */
public class WeanOffAdvisement extends AdvisementRule {
	
	private static final int YOUNG_ANIMAL_AGE_LIMIT = 180;

	public WeanOffAdvisement(){
		setAdvisementID(Util.AdvisementRules.WEANOFF);
	}

	@Override
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve animals who are younger than " + YOUNG_ANIMAL_AGE_LIMIT + " days. " +  getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveAnimalsYoungerThanSpecifiedDays(orgId, LocalDate.now().minusDays(YOUNG_ANIMAL_AGE_LIMIT));
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.WEANEDOFF, null,null,null);
						int currentAgeInDays = Util.getDaysBetween(DateTime.now(), animal.getDateOfBirth());
						String ruleNote = "";
						String animalNote = "";
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// This calf doesn't have any weanoff event i.e. it is not weaned off yet.
							animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has not been weaned off";	
							if (currentAgeInDays >= thirdThreshold) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
							} else 	if (currentAgeInDays >= secondThreshold) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
							} else if (currentAgeInDays >= firstThreshold) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
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
