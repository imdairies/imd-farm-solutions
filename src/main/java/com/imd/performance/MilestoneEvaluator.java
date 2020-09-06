package com.imd.performance;


import com.imd.dto.PerformanceMilestone;


public abstract class MilestoneEvaluator {
	private String milestoneID;
	
	public abstract PerformanceMilestone evaluatePerformanceMilestone(String orgID, String animalTag, String languageCd);
	public abstract PerformanceMilestone evaluatePerformanceMilestone(PerformanceMilestone milestoneRule, String orgID, String animalTag, String languageCd);
	
	public void setMilestoneID(String milestoneID) {
		this.milestoneID = milestoneID;
	}
	
	public String getMilestoneID() {
		return this.milestoneID;
	}

}
