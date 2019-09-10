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
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 * This advisement class implements the following advise:
 * Trigger Condition: 
 * 	All insemindated cows whose pregnancy has not yet been confirmed and today falls in their monthly heat cycle
 * @author kashif.manzoor
 *
 */
public class HeatWarningAdvisement extends AdvisementRule {
	
	public HeatWarningAdvisement(){
		setAdvisementID(Util.AdvisementRules.HEATWARNING);
	}

	@Override
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving cows who are susceptible to coming in heat soon: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (ruleDto.getSecondThreshold() < 0 ? (int) 0 : (int)ruleDto.getSecondThreshold());
			int firstThreshold  = (ruleDto.getFirstThreshold() < 0 ? (int) 0 : (int)ruleDto.getFirstThreshold());

			if (ruleDto != null) {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveInseminatedNonPregnantAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						LocalDate startDate = LocalDate.now(IMDProperties.getServerTimeZone()).minusDays((int)ruleDto.getThirdThreshold()* 4);
						LocalDate endDate = LocalDate.now(IMDProperties.getServerTimeZone()).plusDays(1);// Adding one will take care of the case when insemination today, so we want to include that event as well.
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								startDate,
								endDate,
								Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
						if (ruleDto.getThirdThreshold() < 0) {
							// this rule has been mis-configured. It should have Th3 value greater than 0, ideally this value should be around 21.
							IMDLogger.log("The " + Util.AdvisementRules.HEATWARNING + " seems to be mis-configured. The THRESHOLD3 should have a value greater than 0, ideally this value should be around 21.", Util.ERROR);
						} else if (lifeEvents != null && !lifeEvents.isEmpty()) {
								
							IMDLogger.log("Insemination Date: " + lifeEvents.get(0).getEventTimeStamp(), Util.INFO);
							int daysSinceInseminated= getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							String animalNote = "This cow (" + animal.getAnimalTag() + ") was inseminated " + daysSinceInseminated + " days ago. ";	
							String ruleNote = "";
							int remainder = (daysSinceInseminated % thirdThreshold);
							if (daysSinceInseminated >= (thirdThreshold - firstThreshold)) {
								if (remainder == 0) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += "Keep a close eye on this animal. If it didn't conceive then it would be coming back in heat right about now.";
								} else if ( remainder >= (thirdThreshold - secondThreshold) || remainder <= secondThreshold) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += "Keep a close eye on this animal. If it didn't conceive then it would be coming back in heat with-in a day or two.";
								} else if ( remainder >= (thirdThreshold - firstThreshold) || remainder <= firstThreshold) {
									ruleNote = ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += "Keep a close eye on this animal. If it didn't conceive then it would be coming back in heat with-in 3-4 days.";
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
						} else {
							IMDLogger.log("This non-pregnant inseminated cow (" + animal.getAnimalTag() + ") does not have an insemination event in the last " + (ruleDto.getThirdThreshold()* 4) + " days. This indicates that the user has either forgotten to add inseimnation event or has set the wrong current status (" + animal.getAnimalType() + ") of the animal", Util.ERROR);
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
