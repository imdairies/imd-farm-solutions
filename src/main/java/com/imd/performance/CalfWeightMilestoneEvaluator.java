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
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class CalfWeightMilestoneEvaluator extends MilestoneEvaluator {

	public CalfWeightMilestoneEvaluator() {
		this.setMilestoneID(Util.PerformanceMilestone.CALFWEIGHT);
	}
	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(String orgID, String animalTag, String languageCd) {
		return evaluatePerformanceMilestone(null,orgID,animalTag, languageCd);
	}
	private Float deduceWeightAtMilestone(LifecycleEvent weightEventBeforeMilestone, LifecycleEvent weightEventAfterMilestone, DateTime dob, int daysAtMilestone) {
		float y1 = Float.parseFloat(weightEventBeforeMilestone.getAuxField1Value());
		int x1 = Util.getDaysBetween(weightEventBeforeMilestone.getEventTimeStamp(),dob);
		float y2 = Float.parseFloat(weightEventAfterMilestone.getAuxField1Value());
		int x2 = Util.getDaysBetween(weightEventAfterMilestone.getEventTimeStamp(),dob);
		float deltaX = (float)(x2 - x1);
		float deltaY = y2 - y1;
		float m = deltaY / deltaX;
		
		float c = y1 - (m * x1);
		return ((m*daysAtMilestone) + c);
	}
	@Override
	public PerformanceMilestone evaluatePerformanceMilestone(PerformanceMilestone milestoneRule, String orgID,
			String animalTag, String languageCd) {
		LifeCycleEventsLoader wtEventLoader = new LifeCycleEventsLoader();
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
			values.add(milestoneRule.getAuxInfo1());
			milestoneRule.setShortDescription(MessageCatalogLoader.getDynamicallyPopulatedMessage(message, values));
			milestoneRule.setLongDescription(MessageCatalogLoader.getDynamicallyPopulatedMessage(milestoneRule.getLongDescription(), values));
		}
		
		
		couldnotBeEvaluatedMilestone.setStarRating(Util.StarRating.COULD_NOT_COMPUTE_RATING);
		if (milestoneRule == null) {
			List<PerformanceMilestone> milestones = loader.retrieveSpecificPerformanceMilestone(orgID,this.getMilestoneID());
			if (milestones == null || milestones.isEmpty()) {
				evaluationResultMessage = this.getMilestoneID() + " Performance Milestone not found or is not active. This milestone can not be evaluated for this animal.";
				IMDLogger.log(evaluationResultMessage, Util.WARNING);
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				return couldnotBeEvaluatedMilestone;
			} else if (milestones.size() != 1) {
				evaluationResultMessage = "Multiple records were found for the " + 
						this.getMilestoneID() + " Performance Milestone. We shall pick the most recently updated record and use that for our processing.";
				couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
				IMDLogger.log(evaluationResultMessage, Util.WARNING);			
			} else {
				milestoneRule = milestones.get(0);
			}
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
			}
		} catch (Exception ex) {
			evaluationResultMessage = "Exception occurred while retrieving the data for " + animalTag + ". This milestone can not be evaluated for this animal.";
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		Animal animal = animals.get(0);
		Float calfAgeInDaysAtMilestone = null;
		try {
			calfAgeInDaysAtMilestone = new Float(milestoneRule.getAuxInfo1());
			if (animal.getCurrentAgeInDays() < calfAgeInDaysAtMilestone) {
				// this milestone only applies to animals which are above a certain age.
				milestoneRule.setStarRating(Util.StarRating.ANIMAL_NOT_ELIGIBLE);
				milestoneRule.setEvaluationResultMessage(animalTag + " is not eligible for " + this.getMilestoneID() + " performance milestone, since it is younger than " + calfAgeInDaysAtMilestone);
				return milestoneRule;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			evaluationResultMessage = "A valid value for Calf Age at Milestone was not found. This indicates that the AUX_INFO1 has not been properly set for the Performance Milestone: " + milestoneRule.getMilestoneID() + 
					".  This milestone can not be evaluated for this animal.";
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		List<LifecycleEvent> twoWtEvents = wtEventLoader.retrieveTwoRelevantWeightEvents(animal, animal.getDateOfBirth().plusDays(calfAgeInDaysAtMilestone.intValue()));
		if (twoWtEvents == null || twoWtEvents.size() != 2) {
			evaluationResultMessage = "Two valid weight records of the animal " + animalTag + " were not found. We expected to find one weight record before " + 
				Util.getDateInSQLFormat(animal.getDateOfBirth().plusDays(calfAgeInDaysAtMilestone.intValue())) + " and one after this date. But we found " + 
							 (twoWtEvents == null ? "0": twoWtEvents.size() + ". This milestone can not be evaluated for this animal.");
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		
		Float weightOnMilestoneAge = deduceWeightAtMilestone(twoWtEvents.get(0), twoWtEvents.get(1),animal.getDateOfBirth(),calfAgeInDaysAtMilestone.intValue());
		if (weightOnMilestoneAge == null) {
			evaluationResultMessage = "Could not determine the weight of the animal " + animalTag + " at " + calfAgeInDaysAtMilestone + " days of age. This milestone can not be evaluated for this animal.";
			IMDLogger.log(evaluationResultMessage,Util.ERROR);
			couldnotBeEvaluatedMilestone.setEvaluationResultMessage(evaluationResultMessage);
			return couldnotBeEvaluatedMilestone;
		}
		
		Float fiveStarThreshold = milestoneRule.getFiveStarThreshold();
		Float fourStarThreshold = milestoneRule.getFourStarThreshold();
		Float threeStarThreshold = milestoneRule.getThreeStarThreshold();
		Float twoStarThreshold = milestoneRule.getTwoStarThreshold();
		Float oneStarThreshold = milestoneRule.getOneStarThreshold();
		
		if (fiveStarThreshold != null && weightOnMilestoneAge >= fiveStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FIVE_STAR);
		} else 	if (fourStarThreshold != null && weightOnMilestoneAge >= fourStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.FOUR_STAR);
		} else 	if (threeStarThreshold != null && weightOnMilestoneAge >= threeStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.THREE_STAR);
		} else 	if (twoStarThreshold != null && weightOnMilestoneAge >= twoStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.TWO_STAR);
		} else 	if (oneStarThreshold != null && weightOnMilestoneAge >= oneStarThreshold) {
			milestoneRule.setStarRating(Util.StarRating.ONE_STAR);
		} else {
			milestoneRule.setStarRating(Util.StarRating.NO_STAR);
		}
		milestoneRule.setEvaluationValue(Util.formatTwoDecimalPlaces(weightOnMilestoneAge));
//		milestoneRule.setEvaluationResultMessage("The " + this.getMilestoneID() + " performance milestone evaluation resuled in a rating of " + milestoneRule.getStarRating());
		milestoneRule.setEvaluationResultMessage("This animal weighed " + Util.formatTwoDecimalPlaces(weightOnMilestoneAge) + " Kgs. at " + calfAgeInDaysAtMilestone + " days of age");

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
			message = message.replaceFirst("%1", milestoneRule.getOneStarThreshold().toString());
			message = message.replaceFirst("%2", milestoneRule.getAuxInfo1());
			milestoneRule.setShortDescription(message);
		}
		
		IMDLogger.log(milestoneRule.toString(), Util.INFO);
		return milestoneRule;	
	}

}







