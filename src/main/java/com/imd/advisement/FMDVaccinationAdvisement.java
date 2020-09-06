package com.imd.advisement;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

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
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 * This advisement class implements the following advise:
 * Trigger Condition: 
 * 	If a cow's last FMD vaccination was more than 5 months ago
 *  
 * @author kashif.manzoor
 *
 */
public class FMDVaccinationAdvisement extends AdvisementRule {
	
	private static final String FOOTANDMOUTH = "FOOT&MOUTH";

	public FMDVaccinationAdvisement(){
		setAdvisementID(Util.AdvisementRules.VACCINEFMD);
	}

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving cows that have not been given FMD vaccination: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
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
				animalPopulation = animalLoader.retrieveActiveAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						DateTime startDate = DateTime.now(IMDProperties.getServerTimeZone()).minusDays((int)ruleDto.getThirdThreshold());
						DateTime endDate = DateTime.now(IMDProperties.getServerTimeZone()).plusDays(1);
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								startDate,
								endDate,
								Util.LifeCycleEvents.VACCINE, null,FOOTANDMOUTH,null,null,null);
						String ruleNote = "";
						String animalNote = ""; 						
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							IMDLogger.log("Latest Vaccination Date: " + lifeEvents.get(0).getEventTimeStamp(), Util.INFO);
							int daysSinceVaccinated= getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							animalNote = "This animal was given FMD vaccination " + daysSinceVaccinated + " days ago. ";
								if (ruleDto.getThirdThreshold() > 0 && daysSinceVaccinated >= ruleDto.getThirdThreshold()) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH3, daysSinceVaccinated) == null ? "":
										MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH3, daysSinceVaccinated).getMessageText();
//									animalNote += "This animal was administered FMD vaccination " + daysSinceVaccinated + " days ago. You should administer FMD vaccination as it is now over-due";
								} else if (ruleDto.getSecondThreshold() > 0 && daysSinceVaccinated >= ruleDto.getSecondThreshold()) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH2, daysSinceVaccinated) == null ? "":
										MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH2, daysSinceVaccinated).getMessageText();
//									animalNote += "This animal was administered FMD vaccination " + daysSinceVaccinated + " days ago. You should adminster FMD vaccination soon.";
								} else if (ruleDto.getFirstThreshold() > 0 && daysSinceVaccinated >= ruleDto.getFirstThreshold()) {
									ruleNote = ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH1, daysSinceVaccinated) == null ? "":
										MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH1, daysSinceVaccinated).getMessageText();
//									animalNote += "This animal was administered FMD vaccination " + daysSinceVaccinated + " days ago. You should plan to administer FMD vaccination with in a week or two.";
								}
						} else {
							// the cow was not vaccinated with in the last THRESHOLD3 days
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = MessageCatalogLoader.getMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH4) == null ? "":
								MessageCatalogLoader.getMessage(orgId, languageCd, Util.MessageCatalog.FMD_ADVISEMENT_TH4).getMessageText();
//							animalNote = "This animal's FMD vaccination is long over due. Please administer FMD vaccination immediately.";
						}
						if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
							if (lifeEvents != null && !lifeEvents.isEmpty())
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

	private int getDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		if (endTimeStamp == null || startTimeStamp == null) 
			return 0;
		return (new Period(startTimeStamp, endTimeStamp, PeriodType.days()).getDays());
	}



}
