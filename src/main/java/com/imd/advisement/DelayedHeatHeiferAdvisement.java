package com.imd.advisement;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

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
 * 	All heifers who are older and haven't yet come in heat
 * @author kashif.manzoor
 *
 */
public class DelayedHeatHeiferAdvisement extends AdvisementRule {
	
	private static final int HEAT_FREQUENCY_THRESHOLD = 60;
	private static final String HEAT_FREQUENCY_THRESHOLD_MESSAGE = "The following animals came in heat more than two months ago. They should have come in heat again by now. Please get them checked by a vet";

	public DelayedHeatHeiferAdvisement(){
		setAdvisementID(Util.AdvisementRules.DELAYEDHEATHEIFER);
	}

	@Override
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve heifers who are not pregnant" + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveNonPregnantNonInseminatedHeifers(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
//						LocalDate startDate = new LocalDate(animal.getDateOfBirth().plusDays((int)ruleDto.getFirstThreshold()));
//						LocalDate endDate = LocalDate.now().plusDays(1);// Adding one will take care of the case when the animal came in heat today, so we want to include that event as well.
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.HEAT, Util.LifeCycleEvents.ABORTION,null,null);
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// No heat event found - indicates that the animal has never come in heat since its birth.
							int currentAgeInDays = getDaysBetween(DateTime.now(), animal.getDateOfBirth());
							String animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has never come in heat.";	
							String ruleNote = "";
							if (thirdThreshold > 0 && currentAgeInDays >= (thirdThreshold)) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
							} else if (secondThreshold > 0 && currentAgeInDays >= (secondThreshold)) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
							} else if (firstThreshold > 0 && currentAgeInDays >= (firstThreshold)) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
							}
							if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
								//animal.addLifecycleEvent(lifeEvents.get(0));
								ArrayList<Note> notesList = new ArrayList<Note>();
								notesList.add(new Note(1,ruleNote));
								notesList.add(new Note(2,animalNote));
								animal.setNotes(notesList);
								eligiblePopulation.add(animal);
							}
						} else {
							// Heat event found - indicates that the animal did come in heat, but since it has not been inseminated and neither is it pregnant
							// we need to flag this problem. A heifer that comes in heat should be inseminated as soon as its weight is 300kg+.
							int daysSinceHeat = getDaysBetween(DateTime.now(), lifeEvents.get(0).getEventTimeStamp());
							String animalNote = "This animal (" + animal.getAnimalTag() + ") came in heat " + daysSinceHeat + " days ago on " + lifeEvents.get(0).getEventTimeStampSQLFormat();
							String ruleNote = "";
							if (daysSinceHeat > HEAT_FREQUENCY_THRESHOLD) {
									ruleNote = HEAT_FREQUENCY_THRESHOLD_MESSAGE;
									animal.setThreshold3Violated(true);
							} 
							if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
								animal.addLifecycleEvent(lifeEvents.get(0));
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

	private int getDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		if (endTimeStamp == null || startTimeStamp == null) 
			return 0;
//		return (new Period(startTimeStamp, endTimeStamp, PeriodType.yearMonthDay()).getDays());
		return (new Period(startTimeStamp, endTimeStamp, PeriodType.days()).getDays());
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
