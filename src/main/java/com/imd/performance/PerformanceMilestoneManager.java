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
			if (evl == null) {
				IMDLogger.log("The performance milestone " + milestoneRule.getMilestoneID() + " has been defined in the database but it does not have any implementation. We will ignore this rule", Util.WARNING);
			} else {
				PerformanceMilestone milestone = evl.evaluatePerformanceMilestone(milestoneRule, orgID, animalTag, langCd);
				if (milestone.getStarRating() >= Util.StarRating.COULD_NOT_COMPUTE_RATING)
					milestoneEvaluationOutcome.add(milestone);
				else {
					IMDLogger.log(milestone.getEvaluationResultMessage(), Util.WARNING);
				}
			}
		}
		return milestoneEvaluationOutcome;
	}
}
