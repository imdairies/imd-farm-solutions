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
 * 	All cows that have parturited a few months ago but hasn't come in heat yet.
 * @author kashif.manzoor
 *
 */
public class DelayedHeatCowAdvisement extends AdvisementRule {
	

	public DelayedHeatCowAdvisement(){
		setAdvisementID(Util.AdvisementRules.DELAYEDHEATCOW);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve heifers who are not pregnant" + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			String ruleNote = "";
			String animalNote = "";
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveNonPregnantNonInseminatedLactatingOrDryCows(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
//						LocalDate startDate = new LocalDate(animal.getDateOfBirth().plusDays((int)ruleDto.getFirstThreshold()));
//						LocalDate endDate = LocalDate.now(IMDProperties.getServerTimeZone()).plusDays(1);// Adding one will take care of the case when the animal came in heat today, so we want to include that event as well.
						List<LifecycleEvent> parturitionEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.PARTURATE, Util.LifeCycleEvents.ABORTION,null,null,null,null);
						if (parturitionEvents == null || parturitionEvents.isEmpty()) {
							IMDLogger.log("Animal (" + animal.getAnimalTag() + ") is a lactating cow which is neither inseminated nor pregnant. "
									+ "It must have had a parturition or abortion event in the past - we did not find any. "
									+ "Either the animal's Type is incorrectly set or a parturition/abortion event has not been entered. Please fix the erroneous data ", Util.ERROR);
						} else {
							List<LifecycleEvent> heatEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
									orgId,animal.getAnimalTag(),
									new LocalDate(parturitionEvents.get(0).getEventTimeStamp()),
									LocalDate.now(IMDProperties.getServerTimeZone()).plusDays(1),/*this ensures that we accommodate today's events*/
									Util.LifeCycleEvents.HEAT, null,null,null,null,null);
							if (heatEvents == null || heatEvents.isEmpty()) {
								// This animal never came into heat since its last parturition.
								int daysSinceParturition = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), parturitionEvents.get(0).getEventTimeStamp());
								animalNote = "This animal (" + animal.getAnimalTag() + ") parturated " + daysSinceParturition + " days ago and hasn't come into heat since then. The delayed heat could be because of nutritional deficiency or it could be uterus problem.";	
								if (thirdThreshold > 0 && daysSinceParturition >= (thirdThreshold)) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
								} else if (secondThreshold > 0 && daysSinceParturition >= (secondThreshold)) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
								} else if (firstThreshold > 0 && daysSinceParturition >= (firstThreshold)) {
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
								int daysSinceParturition = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), parturitionEvents.get(0).getEventTimeStamp());
								int daysSinceLastHeatAfterParturition = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), heatEvents.get(0).getEventTimeStamp());
								animalNote = "This animal (" + animal.getAnimalTag() + ") parturated " + daysSinceParturition + " days ago and its latest heat was " + daysSinceLastHeatAfterParturition + " days ago.";	
								animalNote += " It seems that after its last heat, the animal was not inseminated (if it was then the insemination event was not added to the record). The animal should now have come in heat again but it didn't. Please consult the vet";
								if (thirdThreshold > 0 && daysSinceLastHeatAfterParturition >= (thirdThreshold)) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
								} else if (secondThreshold > 0 && daysSinceLastHeatAfterParturition >= (secondThreshold)) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
								} else if (firstThreshold > 0 && daysSinceLastHeatAfterParturition >= (firstThreshold)) {
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
