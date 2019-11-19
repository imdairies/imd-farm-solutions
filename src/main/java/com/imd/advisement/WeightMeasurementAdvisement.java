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
import com.imd.loader.LanguageLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 * This advisement class implements the following advise:
 * Trigger Condition: 
 * 	All animals younger than 6 months whose weight has not been measured in the last couple of weeks.
 * @author kashif.manzoor
 *
 */
public class WeightMeasurementAdvisement extends AdvisementRule {
	
	public WeightMeasurementAdvisement(){
		setAdvisementID(Util.AdvisementRules.WEIGHTMEASUREMENT);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve animals who are younger than " + Util.DefaultValues.YOUNG_ANIMAL_AGE_LIMIT + " days. " +  getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					LanguageLoader langLoader = new LanguageLoader();
					String localizedMessage  = langLoader.retrieveMessage(ruleDto.getOrgID(), languageCd, ruleDto.getFirstThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setFirstThresholdMessage(localizedMessage);
					localizedMessage  = langLoader.retrieveMessage(ruleDto.getOrgID(), languageCd, ruleDto.getSecondThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setSecondThresholdMessage(localizedMessage);
					localizedMessage  = langLoader.retrieveMessage(ruleDto.getOrgID(), languageCd, ruleDto.getThirdThresholdMessageCode());
					if (localizedMessage != null && !localizedMessage.isEmpty())
						ruleDto.setThirdThresholdMessage(localizedMessage);
				}
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveAnimalsBornOnOrAfterSpecifiedDate(orgId, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Util.DefaultValues.YOUNG_ANIMAL_AGE_LIMIT.intValue()));
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						LocalDate startDate = LocalDate.now(IMDProperties.getServerTimeZone()).minusDays(thirdThreshold);
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								startDate,
								null,
								Util.LifeCycleEvents.WEIGHT, null,null,null,null,null);
						int currentAgeInDays = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), animal.getDateOfBirth());
						String ruleNote = "";
						String animalNote = "";
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// No weight event found - indicates that the animal was not weighed in the last Threshold3 days.
							animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has not been weighed in the last " + ruleDto.getThirdThreshold() + " days. You should immediately weigh this animal.";	
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
						} else {
							int daysSinceLatestWeightEvent = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()),lifeEvents.get(0).getEventTimeStamp());
							if (daysSinceLatestWeightEvent > secondThreshold) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has not been weighed in the last " + ruleDto.getThirdThreshold() + " days. You should weigh this animal.";	
								
							} else if (daysSinceLatestWeightEvent > firstThreshold) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote = "This animal (" + animal.getAnimalTag() + ") is " + currentAgeInDays + " days old and has not been weighed in the last " + ruleDto.getThirdThreshold() + " days. You should plan to weigh this animal.";	
							} else {
								//the young animal was weighed recently
								IMDLogger.log("Animal " + animal.getAnimalTag() + " was weighed recently i.e. " + daysSinceLatestWeightEvent + " days ago. No Weight Measurement advisement necessary", Util.INFO);
							}
							animal.addLifecycleEvent(lifeEvents.get(0));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eligiblePopulation;
	}



}
