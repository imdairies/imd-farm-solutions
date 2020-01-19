package com.imd.advisement;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.Message;
import com.imd.dto.Note;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 *  
 * @author kashif.manzoor
 *
 */
public class BrucellaVaccineAdvisement extends AdvisementRule {
	
	public BrucellaVaccineAdvisement(){
		setAdvisementID(Util.AdvisementRules.BRUCELLAVACCINE);
	}

	
	///////////

	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving female calves that should be applied Brucella Vaccine: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				List<String> dynamicValues = new ArrayList<String> ();
				dynamicValues.add(ruleDto.getAuxInfo1());
				dynamicValues.add(ruleDto.getAuxInfo2());
				
				if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
					Message localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(ruleDto.getOrgID(), languageCd, ruleDto.getFirstThresholdMessageCode(), dynamicValues);
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setFirstThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(ruleDto.getOrgID(), languageCd, ruleDto.getSecondThresholdMessageCode(), dynamicValues);
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setSecondThresholdMessage(localizedMessage.getMessageText());
					localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(ruleDto.getOrgID(), languageCd, ruleDto.getThirdThresholdMessageCode(), dynamicValues);
					if (localizedMessage != null && localizedMessage.getMessageText() != null)
						ruleDto.setThirdThresholdMessage(localizedMessage.getMessageText());
				}
				AnimalLoader animalLoader = new AnimalLoader();
				DateTime maxDob = DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Integer.parseInt(ruleDto.getAuxInfo1()));
				DateTime minDob = DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Integer.parseInt(ruleDto.getAuxInfo2()));
				animalPopulation = animalLoader.retrieveBrucellaCandidateAnimals(orgId, minDob, maxDob);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						int daysSinceBirth= Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), animal.getDateOfBirth());
						dynamicValues.clear();
						dynamicValues.add("" + daysSinceBirth);
						dynamicValues.add(ruleDto.getAuxInfo1());
						dynamicValues.add(ruleDto.getAuxInfo2());
						String ruleNote = "";
						String animalNote = "";
						if (ruleDto.getThirdThreshold() > 0 && daysSinceBirth >= ruleDto.getThirdThreshold()) {
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH3, dynamicValues) == null ? "":
								MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH3, dynamicValues).getMessageText();
						} else if (ruleDto.getSecondThreshold() > 0 && daysSinceBirth >= ruleDto.getSecondThreshold()) {
							ruleNote = ruleDto.getSecondThresholdMessage();
							animal.setThreshold2Violated(true);
							animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH2, dynamicValues) == null ? "":
								MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH2, dynamicValues).getMessageText();
						} else if (ruleDto.getFirstThreshold() > 0 && daysSinceBirth >= ruleDto.getFirstThreshold()) {
							ruleNote = ruleDto.getFirstThresholdMessage();
							animal.setThreshold1Violated(true);
							animalNote = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH1, dynamicValues) == null ? "":
								MessageCatalogLoader.getDynamicallyPopulatedMessage(orgId, languageCd, Util.MessageCatalog.BRUCELLAVACCINE_ADVISEMENT_TH1, dynamicValues).getMessageText();
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
			IMDLogger.log("Exception occurred while evaluating " + this.getAdvisementID() + ". BrucellaVaccination Advisement cannot be performed", Util.ERROR);
			e.printStackTrace();
		}
		return eligiblePopulation;
	}
}
