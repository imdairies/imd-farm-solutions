package com.imd.performance;


import java.util.ArrayList;
import java.util.List;


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

public class CalvingRateMilestoneEvaluator extends MilestoneEvaluator {

	public CalvingRateMilestoneEvaluator() {
		this.setMilestoneID(Util.PerformanceMilestone.CALVINGRATE);
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
			List<String> dynamicValues = new ArrayList<String>();
			dynamicValues.add(milestoneRule.getOneStarThreshold().toString());
			Message localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, languageCd,
					milestoneRule.getShortDescriptionCd(), dynamicValues);
			if (localizedMessage != null && localizedMessage.getMessageText() != null)
				milestoneRule.setShortDescription(localizedMessage.getMessageText());
			localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, languageCd,
					milestoneRule.getLongDescriptionCd(), dynamicValues);
			if (localizedMessage != null && localizedMessage.getMessageText() != null)
				milestoneRule.setLongDescription(localizedMessage.getMessageText());
		} else {
			String message = milestoneRule.getShortDescription();
			List<String> values = new ArrayList<String>();
			values.add(milestoneRule.getOneStarThreshold().toString());
			milestoneRule.setShortDescription(MessageCatalogLoader.getDynamicallyPopulatedMessage(message, values));
			milestoneRule.setLongDescription(MessageCatalogLoader.getDynamicallyPopulatedMessage(milestoneRule.getLongDescription(), values));
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
			} else if (!animals.get(0).getGender().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "") || 
					(milestoneRule.getAuxInfo1() != null && (animals.get(0).getCurrentAgeInDays() < Integer.parseInt(milestoneRule.getAuxInfo1())))) {
				evaluationResultMessage = "The milestone " + this.getMilestoneID() + " is only applicable to Female Animals which are older than " + 
					milestoneRule.getAuxInfo1() + " days. This animal's gender is '" + animals.get(0).getGender() + "' and " + 
						animals.get(0).getCurrentAgeInDays() + " days old";
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
		List<LifecycleEvent> calvingEvents = parturitionEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgID(), animal.getAnimalTag(),
				animal.getDateOfBirth(), null,
				Util.LifeCycleEvents.PARTURATE,null, null, null, null, null);
		if (calvingEvents == null || calvingEvents.size() < 2) {
			evaluationResultMessage = this.getMilestoneID() + 
					" Milestone can only be applied if the animal has calved atleast twice. This animal only has " +  calvingEvents.size()  + " calving event in our records.";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.COULD_NOT_COMPUTE_RATING);
			return milestoneRule;
		}
		int totalCalvings = calvingEvents.size();
		int daysBetweenFirstAndLastCalving = Util.getDaysBetween(calvingEvents.get(0).getEventTimeStamp(), calvingEvents.get(totalCalvings-1).getEventTimeStamp());
		float calvingRateInDays = daysBetweenFirstAndLastCalving / (totalCalvings -1);
		Float fiveStarThreshold = milestoneRule.getFiveStarThreshold();
		Float fourStarThreshold = milestoneRule.getFourStarThreshold();
		Float threeStarThreshold = milestoneRule.getThreeStarThreshold();
		Float twoStarThreshold = milestoneRule.getTwoStarThreshold();
		Float oneStarThreshold = milestoneRule.getOneStarThreshold();
		
		if (fiveStarThreshold != null && calvingRateInDays <= fiveStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FIVE_STAR);
		} else 	if (fourStarThreshold != null && calvingRateInDays <= fourStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FOUR_STAR);
		} else 	if (threeStarThreshold != null && calvingRateInDays <= threeStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.THREE_STAR);
		} else 	if (twoStarThreshold != null && calvingRateInDays <= twoStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.TWO_STAR);
		} else 	if (oneStarThreshold != null && calvingRateInDays <= oneStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.ONE_STAR);
		} else {
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
		}
		milestoneRule.setEvaluationValue(Util.formatTwoDecimalPlaces(calvingRateInDays));
		milestoneRule.setEvaluationResultMessage("This animal calved " + totalCalvings + " time(s). Total duration in days between the first and the last calving is: " +
		daysBetweenFirstAndLastCalving + ", the average days between two calvings is: " + calvingRateInDays + " day(s) or " + Util.formatToSpecifiedDecimalPlaces(calvingRateInDays/30.5,1) + " month(s)");
		IMDLogger.log(milestoneRule.toString(), Util.INFO);
		return milestoneRule;	
	}

}







