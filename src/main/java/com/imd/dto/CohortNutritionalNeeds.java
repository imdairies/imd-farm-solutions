package com.imd.dto;

public class CohortNutritionalNeeds extends IMDairiesDTO {
	private String feedCohortCD;
	private Float start;
	private Float end;
	private Float dryMatter;
	private Float crudeProtein;
	private Float metabloizableEnergy;
	public String getFeedCohortCD() {
		return feedCohortCD;
	}
	public void setFeedCohortCD(String feedCohortCD) {
		this.feedCohortCD = feedCohortCD;
	}
	public Float getStart() {
		return start;
	}
	public void setStart(Float start) {
		this.start = start;
	}
	public Float getEnd() {
		return end;
	}
	public void setEnd(Float end) {
		this.end = end;
	}
	public Float getDryMatter() {
		return dryMatter;
	}
	public void setDryMatter(Float dryMatter) {
		this.dryMatter = dryMatter;
	}
	public Float getMetabloizableEnergy() {
		return metabloizableEnergy;
	}
	public void setMetabloizableEnergy(Float metabloizableEnergy) {
		this.metabloizableEnergy = metabloizableEnergy;
	}
	public Float getCrudeProtein() {
		return crudeProtein;
	}
	public void setCrudeProtein(Float crudeProtein) {
		this.crudeProtein = crudeProtein;
	}
	

}
