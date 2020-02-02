package com.imd.performance;


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Message;
import com.imd.dto.PerformanceMilestone;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class SpecifiedLactationPeakMilestoneEvaluator extends MilestoneEvaluator {

	private int lactationNumber;

	SpecifiedLactationPeakMilestoneEvaluator(String milestoneID, int lactationNumber) {
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
		List<LifecycleEvent> calvingEvents = parturitionEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgID(), animal.getAnimalTag(),
				animal.getDateOfBirth(), null,
				Util.LifeCycleEvents.PARTURATE,Util.LifeCycleEvents.ABORTION, null, null, null, null);
		if (calvingEvents == null || calvingEvents.isEmpty()) {
			evaluationResultMessage = this.getMilestoneID() + 
					" Milestone can only be applied if the animal has calved atleast " + this.lactationNumber + " time(s). This animal doesn't have any calving or abortion events in our records.";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
			return milestoneRule;
		}
		int totalLactations = calvingEvents.size();
		if (totalLactations < this.lactationNumber) {
			evaluationResultMessage = this.getMilestoneID() + 
					" Milestone can only be applied if the animal has calved atleast " + this.lactationNumber + " time(s). This animal only has " + totalLactations + " lactation/abortion events in our records.";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
			return milestoneRule;
		}
		DateTime measurementStartTS = calvingEvents.get(totalLactations-this.lactationNumber).getEventTimeStamp();
		DateTime measurementEndTS = null; 
		// DateTime.now(IMDProperties.getServerTimeZone());
		if (totalLactations >= (lactationNumber+1))
			measurementEndTS = calvingEvents.get(totalLactations-(lactationNumber+1)).getEventTimeStamp();
		
		MilkingDetailLoader milkLoader = new MilkingDetailLoader();
		float maxLpdSpecifiedLactation = milkLoader.getMaximumDailyProductionOfCow(animal.getOrgID(), animal.getAnimalTag(),measurementStartTS, measurementEndTS);
		
		if (maxLpdSpecifiedLactation == 0) {
			evaluationResultMessage = "The maximum daily production during lactation #" + this.lactationNumber + "   could not be deduced for this animal, probably because it doesn't have any milking record between " + 
					Util.getDateInSQLFormat(measurementStartTS) + " and " + Util.getDateInSQLFormat(measurementEndTS);
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.COULD_NOT_COMPUTE_RATING);
			return milestoneRule;
		}
			
		Float fiveStarThreshold = milestoneRule.getFiveStarThreshold();
		Float fourStarThreshold = milestoneRule.getFourStarThreshold();
		Float threeStarThreshold = milestoneRule.getThreeStarThreshold();
		Float twoStarThreshold = milestoneRule.getTwoStarThreshold();
		Float oneStarThreshold = milestoneRule.getOneStarThreshold();
		
		if (fiveStarThreshold != null && maxLpdSpecifiedLactation >= fiveStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FIVE_STAR);
		} else 	if (fourStarThreshold != null && maxLpdSpecifiedLactation >= fourStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FOUR_STAR);
		} else 	if (threeStarThreshold != null && maxLpdSpecifiedLactation >= threeStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.THREE_STAR);
		} else 	if (twoStarThreshold != null && maxLpdSpecifiedLactation >= twoStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.TWO_STAR);
		} else 	if (oneStarThreshold != null && maxLpdSpecifiedLactation >= oneStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.ONE_STAR);
		} else {
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
		}
		milestoneRule.setEvaluationValue(Util.formatTwoDecimalPlaces(maxLpdSpecifiedLactation));
		milestoneRule.setEvaluationResultMessage("This animal calved " + totalLactations + " time(s). It's lactation # " + this.lactationNumber + "  peak as averaged over its top " + Util.DefaultValues.MAX_LPD_AVERAGED_OVER_RECORD_COUNT + " maximum milking days is "  + 
				Util.formatToSpecifiedDecimalPlaces(maxLpdSpecifiedLactation,1) + " liters/day");
		IMDLogger.log(milestoneRule.toString(), Util.INFO);
		return milestoneRule;	
	}
}







