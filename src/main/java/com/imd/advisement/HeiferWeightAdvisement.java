package com.imd.advisement;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Message;
import com.imd.dto.Note;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

/**
 * This advisement class implements the following advice:
 * Trigger Condition: 
 * 	All heifers who are older and haven't yet reached 300 Kgs (which is the weight after which Heifer can/should be inseminated.
 * @author kashif.manzoor
 *
 */
public class HeiferWeightAdvisement extends AdvisementRule {

	public HeiferWeightAdvisement(){
		setAdvisementID(Util.AdvisementRules.HEIFERWEIGHT);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieve heifers who haven't yet reached " + Util.DefaultValues.HEIFER_TARGET_WEIGHT + " Kgs of weight" + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			int thirdThreshold  =  (int) ruleDto.getThirdThreshold();
			int secondThreshold = (int)ruleDto.getSecondThreshold();
			int firstThreshold  = (int)ruleDto.getFirstThreshold();

			if (ruleDto != null) {
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					Message localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getFirstThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setFirstThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getSecondThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setSecondThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getMessage(ruleDto.getOrgId(), languageCd, ruleDto.getThirdThresholdMessageCode());
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setThirdThresholdMessage(localizedMessage.getMessageText());
				}
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveNonPregnantNonInseminatedHeifers(orgId);
				String animalNote = "";
				String ruleNote = "";
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						int currentAgeInDays = animal.getCurrentAgeInDays();
						if (currentAgeInDays < firstThreshold)
							// we only check for weight advisement for heifers that are older than Threshold 1.
							continue;
//						LocalDate startDate = new LocalDate(animal.getDateOfBirth().plusDays((int)ruleDto.getFirstThreshold()));
//						LocalDate endDate = LocalDate.now().plusDays(1);// Adding one will take care of the case when the animal came in heat today, so we want to include that event as well.
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.WEIGHT, null,null,null,null,null);
						if (lifeEvents == null || lifeEvents.isEmpty()) {
							// No weight event found - indicates that the animal has never been weighed since its birth.
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = "This animal is " + currentAgeInDays + " days old and has never been weighed. Please weigh it immediately so that the system can perform its analysis on this animal. By not "
									+ "specifying its weight you are missing out on various useful analysis that the system could have performed on this animal.";
						} else {
							// animal weight found.
							float animalWeight = 0;
							String auxValue = "";
							try {
								auxValue = lifeEvents.get(0).getAuxField1Value();
								animalWeight = Float.parseFloat(auxValue);
							} catch (Exception ex) {
								ex.printStackTrace();
								IMDLogger.log("Weight event for animal " + animal.getAnimalTag() + " was found but it seems that the latest weight value is an invalid number (" + auxValue + "). We can't figure out " +
								this.getAdvisementID() + " unless a valid weight value is found.", Util.ERROR);
							}
							animalNote = "This animal is " + currentAgeInDays + " days old and its weight, which was last measured on " + 
							lifeEvents.get(0).getEventTimeStampSQLFormat() + ", was " + animalWeight + 
									" Kgs. ";
							if (animalWeight < Util.DefaultValues.HEIFER_TARGET_WEIGHT) {
								if (currentAgeInDays >= thirdThreshold) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " This animal is severely underweight and needs your immediate attention. You must not inseminate it unless it is over " + Util.DefaultValues.HEIFER_TARGET_WEIGHT + " Kgs.";
								} else if (currentAgeInDays >= secondThreshold) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " This animal is moderately underweight and needs your attention. You must not inseminate it unless it is over " + Util.DefaultValues.HEIFER_TARGET_WEIGHT + " Kgs.";
								} else {
									ruleNote = ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " This animal is underweight and needs your attention. You must not inseminate it unless it is over " + Util.DefaultValues.HEIFER_TARGET_WEIGHT + " Kgs.";
								}
							}
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eligiblePopulation;
	}



}
