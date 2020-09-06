package com.imd.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.Util;
import com.imd.dto.PerformanceMilestone;

public class PerformanceMilestoneManager {
	private final static HashMap<String, MilestoneEvaluator> performanceEvaluators = new HashMap<String, MilestoneEvaluator>();

	public PerformanceMilestoneManager() {
		performanceEvaluators.put(Util.PerformanceMilestone.CALFWEIGHT, new CalfWeightMilestoneEvaluator());
		performanceEvaluators.put(Util.PerformanceMilestone.HEIFERWEIGHT, new HeiferWeightMilestoneEvaluator());
		performanceEvaluators.put(Util.PerformanceMilestone.FIRSTHEAT, new FirstHeatMilestoneEvaluator());
		performanceEvaluators.put(Util.PerformanceMilestone.CALVINGRATE, new CalvingRateMilestoneEvaluator());
		performanceEvaluators.put(Util.PerformanceMilestone.FIRSTLACTATIONPEAK, new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK, 1));
		performanceEvaluators.put(Util.PerformanceMilestone.SECONDLACTATIONPEAK, new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK, 2));
		performanceEvaluators.put(Util.PerformanceMilestone.THIRDLACTATIONPEAK, new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONPEAK, 3));
		performanceEvaluators.put(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION, new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION, 1));
		performanceEvaluators.put(Util.PerformanceMilestone.SECONDLACTATIONPRODUCTION, new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPRODUCTION, 2));
		performanceEvaluators.put(Util.PerformanceMilestone.THIRDLACTATIONPRODUCTION, new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONPRODUCTION, 3));
		performanceEvaluators.put(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY, new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY, 1));
		performanceEvaluators.put(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY, new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY, 2));
		performanceEvaluators.put(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY, new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY, 3));
	}
	public List<PerformanceMilestone> evaluateAllActivePerformanceMilestones(String orgID, String animalTag, String langCd) {
		List<PerformanceMilestone> milestoneEvaluationOutcome = new ArrayList<PerformanceMilestone> ();
		PerformanceMilestoneLoader milestoneLdr = new PerformanceMilestoneLoader();
		List<PerformanceMilestone> milestoneRules = milestoneLdr.retrieveActivePerformanceMilestones(orgID);
		if (milestoneRules == null || milestoneRules.isEmpty()) {
			return null;
		}

		Iterator<PerformanceMilestone> milestoneIt = milestoneRules.iterator();
		while (milestoneIt.hasNext()) {
			PerformanceMilestone milestoneRule = milestoneIt.next();
			MilestoneEvaluator evl = performanceEvaluators.get(milestoneRule.getMilestoneID());
			IMDLogger.log(milestoneRule.getMilestoneID(), Util.INFO);
			if (evl == null) {
				IMDLogger.log("The performance milestone " + milestoneRule.getMilestoneID() + " has been defined in the database but it does not have any implementation. We will ignore this rule", Util.WARNING);
			} else {
				try {
					PerformanceMilestone milestone = evl.evaluatePerformanceMilestone(milestoneRule, orgID, animalTag, langCd);
					if (milestone != null && milestone.getStarRating() != null && 
							milestone.getStarRating() >= Util.StarRating.COULD_NOT_COMPUTE_RATING)
						milestoneEvaluationOutcome.add(milestone);
					else {
						IMDLogger.log(milestone != null && milestone.getEvaluationResultMessage() != null ? milestone.getEvaluationResultMessage() : 
							" Could not detemine " + milestoneRule.getMilestoneID() + " for animal " + animalTag + 
							". Perhaps the animal does not exist or its data is incomplete or inconsistent.", Util.WARNING);
					}
				} catch (Exception ex) {
					IMDLogger.log("Could not evaluate performance milestone " + evl.getMilestoneID() + " for animal " + animalTag, Util.ERROR);
					ex.printStackTrace();
				}
			}
		}
//		IMDLogger.loggingMode = originalLoggingMode;
		return milestoneEvaluationOutcome;
	}
	
	public HashMap<String, Boolean> getAllBadges(List<PerformanceMilestone> milestoneEvaluationOutcome) {
		
		Iterator<PerformanceMilestone> it = milestoneEvaluationOutcome.iterator();
		boolean calfWeightBadge = false;
		boolean heiferWeightBadge = false;
		boolean firstHeatBadge = false;
		
		boolean firstLactationPeak = false;
		boolean secondLactationPeak = true;
		boolean thirdLactationPeak = true;

		boolean firstLactationProduction = false;
		boolean secondLactationProduction = true;
		boolean thirdLactationProduction = true;
		
		boolean calvingRateBadge = false;
		boolean firstLactationFertility = false;
		boolean secondLactationFertility = true;
		boolean thirdLactationFertility = true;

		while (it.hasNext()) {
			PerformanceMilestone milestone = it.next();
			if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.CALFWEIGHT)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.GROWTH_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					calfWeightBadge = true;
				} else {
					calfWeightBadge = false;
				}
					
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.HEIFERWEIGHT)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.GROWTH_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					heiferWeightBadge = true;
				} else {
					heiferWeightBadge = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.FIRSTHEAT)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.GROWTH_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					firstHeatBadge = true;
				} else {
					firstHeatBadge = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.FIRSTLACTATIONPEAK)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					firstLactationPeak = true;
				} else {
					firstLactationPeak = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.SECONDLACTATIONPEAK)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					secondLactationPeak = true;
				} else {
					secondLactationPeak = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.THIRDLACTATIONPEAK)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					thirdLactationPeak = true;
				} else {
					thirdLactationPeak = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					firstLactationProduction = true;
				} else {
					firstLactationProduction = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.SECONDLACTATIONPRODUCTION)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					secondLactationProduction = true;
				} else {
					secondLactationProduction = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.THIRDLACTATIONPRODUCTION)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.MILK_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					thirdLactationProduction = true;
				} else {
					thirdLactationProduction = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.FERTILITY_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					firstLactationFertility = true;
				} else {
					firstLactationFertility = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.FERTILITY_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					secondLactationFertility = true;
				} else {
					secondLactationFertility = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.FERTILITY_PLUS_BADGE_STAR_RATNG_THRESHOLD || milestone.getStarRating() == Util.StarRating.ANIMAL_NOT_ELIGIBLE) {
					thirdLactationFertility = true;
				} else {
					thirdLactationFertility = false;
				}
			} else if (milestone.getMilestoneID().equals(Util.PerformanceMilestone.CALVINGRATE)) {
				if (milestone.getStarRating() >= Util.PerformanceBadges.FERTILITY_PLUS_BADGE_STAR_RATNG_THRESHOLD) {
					calvingRateBadge = true;
				} else {
					calvingRateBadge = false;
				}
			}
		}
		
		HashMap<String, Boolean> badgeMap = new HashMap<String, Boolean>();
		
		IMDLogger.log("\n GROWTH_PLUS_BADGE \n calfWeightBadge = " + calfWeightBadge +
					  "\n heiferWeightBadge = " + heiferWeightBadge + 
					  "\n firstHeatBadge = " + firstHeatBadge, Util.INFO);
		
		badgeMap.put(Util.PerformanceBadges.GROWTH_PLUS_BADGE, 
				calfWeightBadge && heiferWeightBadge && firstHeatBadge);

		IMDLogger.log("\n MILK_PLUS_BADGE \n firstLactationPeak = " + firstLactationPeak +
			          "\n firstLactationProduction = " + firstLactationProduction + 
				      "\n secondLactationPeak = " + secondLactationPeak + 
				      "\n secondLactationProduction = " + secondLactationProduction + 
				      "\n thirdLactationPeak = " + thirdLactationPeak + 
				      "\n firstHeatthirdLactationProductionBadge = " + thirdLactationProduction, Util.INFO);
		
		badgeMap.put(Util.PerformanceBadges.MILK_PLUS_BADGE, 
				firstLactationPeak && firstLactationProduction 
				&& secondLactationPeak && secondLactationProduction
				&& thirdLactationPeak && thirdLactationProduction);

		IMDLogger.log("\n FERTILITY_PLUS_BADGE \n calvingRateBadge = " + calvingRateBadge +
				  "\n firstLactationFertility = " + firstLactationFertility + 
				  "\n secondLactationFertility = " + secondLactationFertility + 
				  "\n thirdLactationFertility = " + thirdLactationFertility, Util.INFO);
		
		badgeMap.put(Util.PerformanceBadges.FERTILITY_PLUS_BADGE,
				calvingRateBadge && firstLactationFertility 
				&& secondLactationFertility &&  thirdLactationFertility);
				
		return badgeMap;
	}
	
}



