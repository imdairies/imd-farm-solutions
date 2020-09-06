package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;


public class LookupValuesPhoto extends IMDairiesDTO {
	
	private String photoID;
	private String photoURI;
	private String comments;
	private DateTime photoTimeStamp;
	
	public LookupValuesPhoto(String photoID, String photoURI, String comments, DateTime photoTimeStamp) {
		super();
		this.photoID = photoID;
		this.photoURI = photoURI;
		this.comments = comments;
		this.photoTimeStamp = photoTimeStamp;
	}

	public LookupValuesPhoto() {
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
		String json = prefix + fieldToJson("photoID",this.photoID) + ",\n" +
				prefix + fieldToJson("photoURI", this.photoURI) + ",\n" + 
				prefix + fieldToJson("comments", this.comments) + ",\n";
		if (fmt == null || this.photoTimeStamp == null) 
			json += prefix + fieldToJson("photoTimeStamp",this.photoTimeStamp) + ",\n";
		else 
			json += prefix + fieldToJson("photoTimeStamp",getDateInSQLFormart(this.photoTimeStamp, fmt)) + ",\n";
		return json;
	}
}


