package com.imd.advisement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale.LanguageRange;

import org.joda.time.DateTime;

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

public class CalfWeightAdvisement extends AdvisementRule {

	public CalfWeightAdvisement(){
		setAdvisementID(Util.AdvisementRules.CALFWEIGHT);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			String ageLimitInDays = ruleDto.getAuxInfo1();
			int calvesAgethreshold = Util.DefaultValues.CALFWEIGHT_ADVISEMENT_AGE;
			IMDLogger.log("Retrieving calves younger than " + ageLimitInDays + " days. Will apply " + getAdvisementID() + " Advisement on them", Util.INFO);
			if (ageLimitInDays != null && !ageLimitInDays.isEmpty()) {
				calvesAgethreshold = Integer.parseInt(ruleDto.getAuxInfo1());
			} else {
				IMDLogger.log("There is no age specified in AUX_INFO1 field for the " + this.getAdvisementID() + " Advisement Rule. Will use default value: " + calvesAgethreshold, Util.WARNING);
			}
			float thirdThreshold  =  ruleDto.getThirdThreshold();
			float secondThreshold = ruleDto.getSecondThreshold();
			float firstThreshold  = ruleDto.getFirstThreshold();
			DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusDays(calvesAgethreshold);

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
				animalPopulation = animalLoader.retrieveAnimalsBornOnOrAfterSpecifiedDate(orgId, dob);
				String animalNote = "";
				String ruleNote = "";
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						int currentAgeInDays = animal.getCurrentAgeInDays();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.WEIGHT, null,null,null,null,null);
						int ageThreshold = ruleDto.getAuxInfo2() != null && !ruleDto.getAuxInfo2().isEmpty() ? 
								Integer.parseInt(ruleDto.getAuxInfo2()) : Util.DefaultValues.CALFWEIGHT_ADVISEMENT_AGE;
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// No weight event found - indicates that the animal has never been weighed since its birth.
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = "This animal is " + currentAgeInDays + " days old and has never been weighed. Please weigh it immediately so that the system can perform its analysis on this animal. By not "
									+ "specifying its weight you are missing out on various useful analysis that the system could have performed on this animal.";
						} else if (lifeEvents.size() < 2) {
							if (animal.getCurrentAgeInDays() > ageThreshold /*Excludes those calves that were recently born and may not have two weight events.*/) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
								animalNote = "This animal is " + currentAgeInDays + " days old and has only been weighed " + lifeEvents.size() +  " time(s). "
										+ "Calf Weight Advisement can only be applied if an animal has been weighed at least twice in its lifetime";
								IMDLogger.log(animalNote, Util.WARNING); 
							} else {
								IMDLogger.log("The Animal " + animal.getAnimalTag() + " seems to be too young to have been weighed more than once. " + this.getAdvisementID() + " advisement will not be applied to this animal", Util.WARNING); 
							}
						} else {
							// at least two weight events found.
							float animalWeight1 = 0;
							float animalWeight2 = 0;
							String auxValue1 = "";
							String auxValue2 = "";
							
							try {
								auxValue1 = lifeEvents.get(0).getAuxField1Value();
								auxValue2 = lifeEvents.get(1).getAuxField1Value();
								animalWeight1 = Float.parseFloat(auxValue1);
								animalWeight2 = Float.parseFloat(auxValue2);
							} catch (Exception ex) {
								ex.printStackTrace();
								IMDLogger.log("Weight event for animal " + animal.getAnimalTag() + " was found but it seems that one or more of the the latest two weight values is/are invalid number (" + auxValue1 + ", "+ auxValue2 + "). We can't figure out " +
								this.getAdvisementID() + " unless valid weight values are found.", Util.ERROR);
							}
							int daysBetween = Util.getDaysBetween(lifeEvents.get(0).getEventTimeStamp(), lifeEvents.get(1).getEventTimeStamp());
							double weightIncrease = (double)(animalWeight1 - animalWeight2);
							double rateOfGrowth = daysBetween == 0 ? 0d : (double)((weightIncrease) / (double)daysBetween);
							animalNote = "This animal is " + currentAgeInDays + " days old and its last two measured weights are: " +
									+ animalWeight1 + " Kgs. as of " +  Util.getDateInSQLFormat(lifeEvents.get(0).getEventTimeStamp())  + " and "
									+ animalWeight2 + " Kgs. as of " +  Util.getDateInSQLFormat(lifeEvents.get(1).getEventTimeStamp()) + ". It grew at an average of " + Util.formatTwoDecimalPlaces(rateOfGrowth) + " Kgs/day in the last " + daysBetween + " days. ";
							
							if (rateOfGrowth <= thirdThreshold) {
								ruleNote = ruleDto.getThirdThresholdMessage();
								animal.setThreshold3Violated(true);
								animalNote += "This animal has shown signs of stunted growth in the last " + daysBetween +  " days. You must immediately tend to it.";
							} else if (rateOfGrowth <= secondThreshold) {
								ruleNote = ruleDto.getSecondThresholdMessage();
								animal.setThreshold2Violated(true);
								animalNote += "This animal should have grown more in the last " + daysBetween +  " days. You should improve its feed intake.";
							} else if (rateOfGrowth <= firstThreshold) {
								ruleNote = ruleDto.getFirstThresholdMessage();
								animal.setThreshold1Violated(true);
								animalNote += "This animal grew at a barely acceptable level in the last " + daysBetween +  " days. Please improve its feed intake.";
							}
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
