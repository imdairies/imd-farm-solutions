package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatter;

import com.imd.util.Util;

public class AnimalPhoto extends IMDairiesDTO {
	
	private String animalTag;
	private String photoID;
	private String photoURI;
	private String comments;
	private DateTime photoTimeStamp;
	private DateTime dob;
	
	public AnimalPhoto(String orgID, String animalTag2) {
		this.setOrgId(orgID);
		this.animalTag = animalTag2;
	}
	public AnimalPhoto() {
	}

	public String getAnimalTag() {
		return animalTag;
	}
	
	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}

	public String getPhotoID() {
		return photoID;
	}

	public void setPhotoID(String photoID) {
		this.photoID = photoID;
	}

	public String getPhotoURI() {
		return photoURI;
	}

	public void setPhotoURI(String photoURI) {
		this.photoURI = photoURI;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	public DateTime getPhotoTimeStamp() {
		return photoTimeStamp;
	}
	public void setPhotoTimeStamp(DateTime photoTimeStamp) {
		this.photoTimeStamp = photoTimeStamp;
	}

	public String toString() {
		return stringify("   ", null);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix, fmt) + super.dtoToJson(prefix, fmt));
	}
	
	public String dtoToJson(String prefix)  {		
		return stringify(prefix, null) + super.dtoToJson(prefix);
	}
	
	public String stringify(String prefix, DateTimeFormatter fmt)  {
		String json = prefix + fieldToJson("animalTag", this.animalTag) + ",\n" +
				prefix + fieldToJson("dob",this.dob) + ",\n" +
				prefix + fieldToJson("photoID",this.photoID) + ",\n" +
				prefix + fieldToJson("photoURI", this.photoURI) + ",\n" + 
				prefix + fieldToJson("comments", this.comments) + ",\n" +
				prefix + fieldToJson("ageAtPhotoTimeStamp", this.getAgeAtPhotoDTTM()) + ",\n";
		if (fmt == null || this.photoTimeStamp == null) 
			json += prefix + fieldToJson("photoTimeStamp",this.photoTimeStamp) + ",\n";
		else 
			json += prefix + fieldToJson("photoTimeStamp",getDateInSQLFormart(this.photoTimeStamp, fmt)) + ",\n";
		return json;
	}
	public void setDob(DateTime animalDOB) {
		this.dob = animalDOB;
	}
	public Period getAgeAtPhotoDTTM() {
		if (this.dob == null || this.photoTimeStamp == null) {
			return null;
		} else {
			if (Util.getDaysBetween(this.photoTimeStamp,this.dob) <= 1) {
				return new Period(this.dob, this.photoTimeStamp, PeriodType.yearMonthDayTime());
			}
			else {
				return new Period(this.dob, this.photoTimeStamp, PeriodType.yearMonthDay());			
			}
		}
	
	}
	
}


