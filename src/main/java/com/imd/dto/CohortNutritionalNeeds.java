package com.imd.dto;

import org.joda.time.format.DateTimeFormatter;

import com.imd.util.Util;

public class CohortNutritionalNeeds extends IMDairiesDTO {
	private String nutritionalNeedsFeedCohortCD;
	private Float nutritionalNeedsStart;
	private Float nutritionalNeedsEnd;
	private Float nutritionalNeedsDryMatter;
	private Float nutritionalNeedsCrudeProtein;
	private Float nutritionalNeedsMetabloizableEnergy;
	public String getFeedCohortCD() {
		return nutritionalNeedsFeedCohortCD;
	}
	public void setFeedCohortCD(String feedCohortCD) {
		this.nutritionalNeedsFeedCohortCD = feedCohortCD;
	}
	public Float getStart() {
		return nutritionalNeedsStart;
	}
	public void setStart(Float start) {
		this.nutritionalNeedsStart = start;
	}
	public Float getEnd() {
		return nutritionalNeedsEnd;
	}
	public void setEnd(Float end) {
		this.nutritionalNeedsEnd = end;
	}
	public Float getDryMatter() {
		return nutritionalNeedsDryMatter;
	}
	public void setDryMatter(Float dryMatter) {
		this.nutritionalNeedsDryMatter = dryMatter;
	}
	public Float getMetabloizableEnergy() {
		return nutritionalNeedsMetabloizableEnergy;
	}
	public void setMetabloizableEnergy(Float metabloizableEnergy) {
		this.nutritionalNeedsMetabloizableEnergy = metabloizableEnergy;
	}
	public Float getCrudeProtein() {
		return nutritionalNeedsCrudeProtein;
	}
	public void setCrudeProtein(Float crudeProtein) {
		this.nutritionalNeedsCrudeProtein = crudeProtein;
	}
	public String stringify(String prefix) {
		return  prefix + fieldToJson("nutritionalNeedsFeedCohortCD", nutritionalNeedsFeedCohortCD) + ",\n" + 
				prefix + fieldToJson("nutritionalNeedsStart", this.nutritionalNeedsStart == null ? "" : this.nutritionalNeedsStart.toString()) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsEnd", this.nutritionalNeedsEnd == null ? "" : this.nutritionalNeedsEnd.toString()) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsDryMatter", this.nutritionalNeedsDryMatter == null ? "" : Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsDryMatter,1)) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsCrudeProtein", this.nutritionalNeedsCrudeProtein == null ? "" :  Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsCrudeProtein,1)) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsMetabloizableEnergy", this.nutritionalNeedsMetabloizableEnergy == null ? "" :  Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsMetabloizableEnergy,2)) + ",\n";
	}

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}

	public String dtoToJson(String prefix, boolean appendSuperJson)  {		
		if (appendSuperJson)
			return dtoToJson(prefix);
		else
			return stringify(prefix);
	}
	

	public String dtoToJson(String prefix, boolean appendSuperJson, DateTimeFormatter fmt)  {		
		if (appendSuperJson)
			return dtoToJson(prefix,fmt);
		else
			return stringify(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}
	public String tostring () {
		return dtoToJson("  ");
	}

	

}
