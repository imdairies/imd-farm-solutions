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

public class FirstHeatMilestoneEvaluator extends MilestoneEvaluator {

	public FirstHeatMilestoneEvaluator() {
		this.setMilestoneID(Util.PerformanceMilestone.FIRSTHEAT);
	}
	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(String orgID, String animalTag, String languageCd) {
		return evaluatePerformanceMilestone(null,orgID,animalTag, languageCd);
	}

	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(PerformanceMilestone milestoneRule, String orgID,
			String animalTag, String languageCd) {
		LifeCycleEventsLoader heatEventLoader = new LifeCycleEventsLoader();
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
			dynamicValues.add(milestoneRule.getAuxInfo1());
			Message localizedMessage  = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, languageCd,
					milestoneRule.getShortDescriptionCd(), dynamicValues);
			if (localizedMessage != null && localizedMessage.getMessageText() != null)
				milestoneRule.setShortDescription(localizedMessage.getMessageText());
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
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				return couldnotBeEvaluatedMilestone;
			} else if (!animals.get(0).getGender().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "") || 
					animals.get(0).getCurrentAgeInDays() < milestoneRule.getOneStarThreshold()) {
				evaluationResultMessage = "The milestone " + this.getMilestoneID() + " is only applicable to Female Animals which are older than " + milestoneRule.getOneStarThreshold() + " days";
				IMDLogger.log(evaluationResultMessage,Util.INFO);
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				couldnotBeEvaluatedMilestone.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
				return couldnotBeEvaluatedMilestone;				
			}
		} catch (Exception ex) {
			evaluationResultMessage = "Exception occurred while retrieving the data for " + animalTag + ". This milestone can not be evaluated for this animal.";
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		Animal animal = animals.get(0);
		List<LifecycleEvent> heatEvents = heatEventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgId(), animal.getAnimalTag(),animal.getDateOfBirth(), 
				animal.getDateOfBirth().plusDays(milestoneRule.getOneStarThreshold().intValue()),Util.LifeCycleEvents.HEAT,null, null, null, null, null);
		if (heatEvents == null || heatEvents.isEmpty()) {
			evaluationResultMessage = "This animal did not come in heat by " +  milestoneRule.getOneStarThreshold() + " days of age.";
			milestoneRule.setEvaluationResultMessage(evaluationResultMessage);
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
			return milestoneRule;
		}
		int ageInDaysAtMilestoneEvent = Util.getDaysBetween(heatEvents.get(0).getEventTimeStamp(), animal.getDateOfBirth());
		String ageInYMDAtMilestoneEvents = Util.getYearMonthDaysBetween(heatEvents.get(0).getEventTimeStamp(), animal.getDateOfBirth());
		Float fiveStarThreshold = milestoneRule.getFiveStarThreshold();
		Float fourStarThreshold = milestoneRule.getFourStarThreshold();
		Float threeStarThreshold = milestoneRule.getThreeStarThreshold();
		Float twoStarThreshold = milestoneRule.getTwoStarThreshold();
		Float oneStarThreshold = milestoneRule.getOneStarThreshold();
		
		if (fiveStarThreshold != null && ageInDaysAtMilestoneEvent <= fiveStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FIVE_STAR);
		} else 	if (fourStarThreshold != null && ageInDaysAtMilestoneEvent <= fourStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FOUR_STAR);
		} else 	if (threeStarThreshold != null && ageInDaysAtMilestoneEvent <= threeStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.THREE_STAR);
		} else 	if (twoStarThreshold != null && ageInDaysAtMilestoneEvent <= twoStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.TWO_STAR);
		} else 	if (oneStarThreshold != null && ageInDaysAtMilestoneEvent <= oneStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.ONE_STAR);
		} else {
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
		}
		milestoneRule.setEvaluationValue(Util.formatTwoDecimalPlaces(ageInDaysAtMilestoneEvent));
		milestoneRule.setEvaluationResultMessage("This animal's earliest first heat was at an age of " + ageInYMDAtMilestoneEvents + ".");

		
		IMDLogger.log(milestoneRule.toString(), Util.INFO);
		return milestoneRule;	
	}

}







