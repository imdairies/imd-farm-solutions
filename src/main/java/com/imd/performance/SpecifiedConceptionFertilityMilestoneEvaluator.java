package com.imd.performance;


import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Message;
import com.imd.dto.PerformanceMilestone;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class SpecifiedConceptionFertilityMilestoneEvaluator extends MilestoneEvaluator {

	private int lactationNumber;

	SpecifiedConceptionFertilityMilestoneEvaluator(String milestoneID, int lactationNumber) {
		this.lactationNumber = lactationNumber;
		this.setMilestoneID(milestoneID);		
	}
	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(String orgID, String animalTag, String languageCd) {
		return evaluatePerformanceMilestone(null,orgID,animalTag, languageCd);
	}

	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(PerformanceMilestone milestoneRule, String orgID,
			String animalTag, String languageCd) {
		LifeCycleEventsLoader parturitionEventLoader = new LifeCycleEventsLoader();
		AnimalLoader anmlLoader = new AnimalLoader();
		PerformanceMilestoneLoader loader = new PerformanceMilestoneLoader();
		String evaluationResultMessage = "An unknown error occurred while evaluating " + this.getMilestoneID() + " performance milestone.";
		PerformanceMilestone couldnotBeEvaluatedMilestone = milestoneRule;

		if (milestoneRule == null) {
			couldnotBeEvaluatedMilestone = new PerformanceMilestone();
			couldnotBeEvaluatedMilestone.setMilestoneID(this.getMilestoneID());
			List<PerformanceMilestone> milestones = loader.retrieveSpecificPerformanceMilestone(orgID, this.getMilestoneID());
			if (milestones == null || milestones.isEmpty()) {
				evaluationResultMessage = "Could not find " + this.getMilestoneID() + " in the database. The milestone can not be evaluated for the animal " + animalTag;
				IMDLogger.log(evaluationResultMessage,Util.ERROR);
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				return couldnotBeEvaluatedMilestone;				
			} else {
				milestoneRule = milestones.get(0);
				couldnotBeEvaluatedMilestone = milestoneRule;
				if (milestones.size() != 1) {
					evaluationResultMessage = "Found multiple entries for " + this.getMilestoneID() + " in the database. We shall pick one of these entries and perform the milestone evaluation for the animal " + animalTag;
					IMDLogger.log(evaluationResultMessage,Util.WARNING);
				}
			}
		}

		if (languageCd != null && !languageCd.equalsIgnoreCase(Util.LanguageCode.ENG)) {
			Message localizedMessage  = MessageCatalogLoader.getMessage(orgID, languageCd,milestoneRule.getShortDescriptionCd());
			if (localizedMessage != null && localizedMessage.getMessageText() != null)
				milestoneRule.setShortDescription(localizedMessage.getMessageText());
			localizedMessage  = MessageCatalogLoader.getMessage(orgID, languageCd,milestoneRule.getLongDescriptionCd());
			if (localizedMessage != null && localizedMessage.getMessageText() != null)
				milestoneRule.setLongDescription(localizedMessage.getMessageText());
		}

		milestoneRule.setAnimalTag(animalTag);
		List<Animal> animals = null;
		try {
			animals = anmlLoader.getAnimalRawInfo(orgID, animalTag);
			if (animals == null || animals.size() != 1) {
				evaluationResultMessage = "A valid record for the animal " + animalTag + " was not found. Expected to receive exactly one record for the animal but instead received: " + 
						 (animals == null ? "0": animals.size() + ".  This milestone can not be evaluated for this animal.");
				IMDLogger.log(evaluationResultMessage,Util.ERROR);
				couldnotBeEvaluatedMilestone.setStarRating(Util.StarRating.COULD_NOT_COMPUTE_RATING);
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				return couldnotBeEvaluatedMilestone;
			} else if (!animals.get(0).getGender().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "")) {
				evaluationResultMessage = "The milestone " + this.getMilestoneID() + " is only applicable to Female Animals. This animal's gender is '" + animals.get(0).getGender() + "' ";
				IMDLogger.log(evaluationResultMessage,Util.INFO);
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				couldnotBeEvaluatedMilestone.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
				return couldnotBeEvaluatedMilestone;
			}
		} catch (Exception ex) {
			evaluationResultMessage = "Exception occurred while retrieving the data for " + animalTag + ". " + this.getMilestoneID() + " milestone can not be evaluated for this animal.";
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		Animal animal = animals.get(0);
		boolean pregnantHeifer = false;
		List<LifecycleEvent> calvingOrAbortionEvents = parturitionEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(),
				animal.getDateOfBirth(), null,
				Util.LifeCycleEvents.PARTURATE,Util.LifeCycleEvents.ABORTION, null, null, null, null);
		if (calvingOrAbortionEvents == null || calvingOrAbortionEvents.isEmpty()) {
			// this could be a heifer which hasn't calved yet but has had inseminations done to it.
			List<LifecycleEvent> inseminationEventsForThisLactation = parturitionEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(),
					null, null, Util.LifeCycleEvents.INSEMINATE,Util.LifeCycleEvents.MATING, null, null, /*successfully inseminated*/ Util.YES, null);
			 if (inseminationEventsForThisLactation == null || inseminationEventsForThisLactation.isEmpty()) {
				 evaluationResultMessage = this.getMilestoneID() + 
						 " Milestone can only be applied if the animal has calved/aborted atleast " + this.lactationNumber + " time(s). This animal doesn't have any calving or abortion events in our records.";
				 milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
				 milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
				 return milestoneRule;
			 } else {
				 // case when its a heifer who has conceived but hasn't yet calved or aborted.
				 pregnantHeifer = true;
				 calvingOrAbortionEvents.add(inseminationEventsForThisLactation.get(0));
				 IMDLogger.log("This heifer hasn't yet calved but has been successfully inseminated" + animalTag, Util.INFO);
			 }
		}
		int totalCalvingsOrAbortions = calvingOrAbortionEvents.size();
		if (totalCalvingsOrAbortions < this.lactationNumber) {
			evaluationResultMessage = this.getMilestoneID() + 
					" Milestone can only be applied if the animal has calved/aborted atleast " + this.lactationNumber + " time(s). This animal only has " + totalCalvingsOrAbortions + " calving/abortion events in our records.";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
			return milestoneRule;
		}
		int index = totalCalvingsOrAbortions - this.lactationNumber;
		if (index < 0) {
			evaluationResultMessage = "Record for Calving # " + this.lactationNumber + " was not found. " + this.getMilestoneID() + 
					" Milestone can only be applied if the animal has calved/aborted atleast " + this.lactationNumber + " time(s).";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
			return milestoneRule;
		}
		LifecycleEvent calvingEvent = calvingOrAbortionEvents.get(index);
		DateTime measurementStartTS = animal.getDateOfBirth();
		DateTime measurementEndTS = calvingEvent.getEventTimeStamp(); 
		
		if (lactationNumber > 1 /*&& index >= 1*/)
			measurementStartTS = calvingOrAbortionEvents.get(index+1).getEventTimeStamp();
		
		List<LifecycleEvent> inseminationEventsForThisLactation = parturitionEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(),
				measurementStartTS, measurementEndTS,
				Util.LifeCycleEvents.INSEMINATE,Util.LifeCycleEvents.MATING, null, null, null, null);
		
		if (inseminationEventsForThisLactation == null || inseminationEventsForThisLactation.isEmpty()) {
			evaluationResultMessage = "The animal does not have any insemination/mating event(s) in our record. We expected to see at least 1 insemination/mating event between the dates of " + 
					Util.getDateInSQLFormat(measurementStartTS) + " and " + Util.getDateInSQLFormat(measurementEndTS) + " so that we can calculate the fertility performance of this animal in its lactation number " + this.lactationNumber;
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.COULD_NOT_COMPUTE_RATING);
			return milestoneRule;
		}
		
		int inseminationEventsCount = inseminationEventsForThisLactation.size();
		String sexedSemen = inseminationEventsForThisLactation.get(0).getAuxField2Value() == null  || inseminationEventsForThisLactation.get(0).getAuxField2Value().isEmpty() ? 
				"N" : inseminationEventsForThisLactation.get(0).getAuxField2Value().charAt(0) + "";
		String inseminatedInHarshWeather = (Util.isMonthFavourableForInsemination(inseminationEventsForThisLactation.get(0).getEventTimeStamp().getMonthOfYear()) ? "N" : "Y");

		String ratingKey = "[" + inseminationEventsCount + sexedSemen + inseminatedInHarshWeather + "]";

		String fiveStarThreshold = milestoneRule.getAuxInfo5();
		String fourStarThreshold = milestoneRule.getAuxInfo4();
		String threeStarThreshold = milestoneRule.getAuxInfo3();
		String twoStarThreshold = milestoneRule.getAuxInfo2();
		String oneStarThreshold = milestoneRule.getAuxInfo1();

		if (oneStarThreshold != null && oneStarThreshold.indexOf(ratingKey) >= 0) {
			milestoneRule.setStarRating(Util.StarRating.ONE_STAR);
		} else 	if (twoStarThreshold != null && twoStarThreshold.indexOf(ratingKey) >= 0) {
			milestoneRule.setStarRating(Util.StarRating.TWO_STAR);
		} else 	if (threeStarThreshold != null && threeStarThreshold.indexOf(ratingKey) >= 0) {
			milestoneRule.setStarRating(Util.StarRating.THREE_STAR);
		} else 	if (fourStarThreshold != null && fourStarThreshold.indexOf(ratingKey) >= 0) {
			milestoneRule.setStarRating(Util.StarRating.FOUR_STAR);
		} else if (fiveStarThreshold != null && fiveStarThreshold.indexOf(ratingKey) >= 0) {
			milestoneRule.setStarRating(Util.StarRating.FIVE_STAR);
		} else {
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
		}
		milestoneRule.setEvaluationValue(ratingKey);
		if (pregnantHeifer) {
			milestoneRule.setEvaluationResultMessage("This heifer conceived on attempt number " + inseminationEventsCount + ", the sire was " + 
					inseminationEventsForThisLactation.get(0).getAuxField1Value() + ", it was " +
					(sexedSemen != null && sexedSemen.equalsIgnoreCase(Util.Y) ? "sexed semen" : "normal semen" ) + 
					" and it conceived in " + (inseminatedInHarshWeather.equalsIgnoreCase(Util.N) ? "favourable weather" : "harsh weather") + " it hasn't yet calved");
		} else {
			milestoneRule.setEvaluationResultMessage("This animal conceived on attempt number " + inseminationEventsCount + ", the sire was " + 
					inseminationEventsForThisLactation.get(0).getAuxField1Value() + ", it was " +
					(sexedSemen != null && sexedSemen.equalsIgnoreCase(Util.Y) ? "sexed semen" : "normal semen" ) + 
					" and it conceived in " + (inseminatedInHarshWeather.equalsIgnoreCase(Util.N) ? "favourable weather" : "harsh weather"));
		}
		IMDLogger.log(milestoneRule.toString(), Util.INFO);
		return milestoneRule;
	}
}






