package com.imd.dto;


public class DietRequirement extends IMDairiesDTO {
	private LifeCycleEventCode applicableAimalType;
	private int start;
	private int end;
	private Float dryMatter;
	private Float crudeProtein;
	private Float metabolizableEnergy;
	public LifeCycleEventCode getApplicableAimalType() {
		return applicableAimalType;
	}
	public void setApplicableAimalTypes(LifeCycleEventCode applicableAimalType) {
		this.applicableAimalType = applicableAimalType;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
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

}
