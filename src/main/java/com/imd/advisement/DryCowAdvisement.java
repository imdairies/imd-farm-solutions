package com.imd.advisement;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
 * 	If a cow has been inseminated at least 6 months ago AND
 *  If the cow is confirmed pregnant AND
 *  If the cow is not already dry
 *  
 * If above conditions are met then the advisement will be triggered. This advisement will indicate to the user that the cow's should now be dried off.
 * @author kashif.manzoor
 *
 */
public class DryCowAdvisement extends AdvisementRule {
	
	public DryCowAdvisement(){
		setAdvisementID(Util.AdvisementRules.DRYCOW);
	}

	@Override
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		/**
		 * Following animals form the addressable population for this rule:
		 * Animal is a female.
		 * Animal is confirmed pregnant.
		 * It has been more than THRESHOLD1 days since the insemination.
		 */
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving cows that should be dried: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveNonDryPregnantAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						//LocalDate endDate = LocalDate.now().minusDays((int)ruleDto.getThirdThreshold());
						//LocalDate startDate = LocalDate.now().minusDays(PREGNANCY_DURATION_THRESHOLD);
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							IMDLogger.log("Insemination Date: " + lifeEvents.get(0).getEventTimeStamp(), Util.INFO);
							int daysSinceInseminated= getDaysBetween(DateTime.now(), lifeEvents.get(0).getEventTimeStamp());
							String ruleNote = "";
							String animalNote = "This cow was successfully inseminated " + daysSinceInseminated + " days ago.";							
							if (ruleDto.getThirdThreshold() > 0 && daysSinceInseminated >= ruleDto.getThirdThreshold()) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
							} else if (ruleDto.getSecondThreshold() > 0 && daysSinceInseminated >= ruleDto.getSecondThreshold()) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
							} else if (ruleDto.getFirstThreshold() > 0 && daysSinceInseminated >= ruleDto.getFirstThreshold()) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
							}
							if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
								// This cow has not yet been dried off
								animal.addLifecycleEvent(lifeEvents.get(0));
								ArrayList<Note> notesList = new ArrayList<Note>();
								notesList.add(new Note(1,ruleNote));
								notesList.add(new Note(2,animalNote));
								animal.setNotes(notesList);
								eligiblePopulation.add(animal);
							}
						} else {
							IMDLogger.log("This pregnant cow does not have an insemination event. The user should have added an insemination event !", Util.ERROR);
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
