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
	private Float nutritionalNeedsTDN;
	
	
	public CohortNutritionalNeeds() {
		this.nutritionalNeedsDryMatter = 0f;
		this.nutritionalNeedsCrudeProtein = 0f;
		this.nutritionalNeedsMetabloizableEnergy = 0f;
		this.nutritionalNeedsTDN = 0f;
	}
	
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
				prefix + fieldToJson("nutritionalNeedsDryMatter", this.nutritionalNeedsDryMatter == null ? 0f : Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsDryMatter,4))) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsCrudeProtein", this.nutritionalNeedsCrudeProtein == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsCrudeProtein,3))) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsTDN", this.nutritionalNeedsTDN == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsTDN,3))) + ",\n" +
				prefix + fieldToJson("nutritionalNeedsMetabloizableEnergy", this.nutritionalNeedsMetabloizableEnergy == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsMetabloizableEnergy,3))) + ",\n";
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
	public Float getNutritionalNeedsTDN() {
		return nutritionalNeedsTDN;
	}
	public void setNutritionalNeedsTDN(Float nutritionalNeedsTDN) {
		this.nutritionalNeedsTDN = nutritionalNeedsTDN;
	}

	public String stringify(String prefix, String keyNamePrefix) {
		return  prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsFeedCohortCD", nutritionalNeedsFeedCohortCD) + ",\n" + 
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsStart", this.nutritionalNeedsStart == null ? "" : this.nutritionalNeedsStart.toString()) + ",\n" +
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsEnd", this.nutritionalNeedsEnd == null ? "" : this.nutritionalNeedsEnd.toString()) + ",\n" +
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsDryMatter", this.nutritionalNeedsDryMatter == null ? 0f : Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsDryMatter,4))) + ",\n" +
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsCrudeProtein", this.nutritionalNeedsCrudeProtein == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsCrudeProtein,3))) + ",\n" +
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsTDN", this.nutritionalNeedsTDN == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsTDN,3))) + ",\n" +
				prefix + fieldToJson(keyNamePrefix + "NutritionalNeedsMetabloizableEnergy", this.nutritionalNeedsMetabloizableEnergy == null ? 0f :  Float.parseFloat(Util.formatToSpecifiedDecimalPlaces(this.nutritionalNeedsMetabloizableEnergy,3))) + ",\n";
	}
}
