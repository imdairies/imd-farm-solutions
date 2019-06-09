package com.imd.dto;

public class FeedItemNutritionalStats extends IMDairiesDTO {
	private String feedItemCD;
	private Float dryMatter;
	private Float crudeProtein;
	private Float metabolizableEnergy;
	private Float costPerUnit;
	public String getFeedItemCD() {
		return feedItemCD;
	}
	public void setFeedItemCD(String feedItemCD) {
		this.feedItemCD = feedItemCD;
	}
	public Float getDryMatter() {
		return dryMatter;
	}
	public void setDryMatter(Float dryMatter) {
		this.dryMatter = dryMatter;
	}
	public Float getCrudeProtein() {
		return crudeProtein;
	}
	public void setCrudeProtein(Float crudeProtein) {
		this.crudeProtein = crudeProtein;
	}
	public Float getMetabolizableEnergy() {
		return metabolizableEnergy;
	}
	public void setMetabolizableEnergy(Float metabolizableEnergy) {
		this.metabolizableEnergy = metabolizableEnergy;
	}
	public Float getCostPerUnit() {
		return costPerUnit;
	}
	public void setCostPerUnit(Float costPerUnit) {
		this.costPerUnit = costPerUnit;
	}

}
