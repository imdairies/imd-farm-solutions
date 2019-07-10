package com.imd.services.bean;

import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.util.BufferRecyclers;

public class TagVolumeCommentTriplet{
	private String tag;
	private String volume;
	private String comments;
	private String outcome;
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	
	public String toString() {
		return "\n{tag:" +  this.tag + "," + "volume:" + this.volume + "," + "comments:" +  this.comments + "," + "outcome:" +  this.outcome +"}";
		
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getOutcome() {
		return this.outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;		
	}

	private String stringify(String prefix, DateTimeFormatter fmt) {
		// this dto has no date field so we don't need dateformatter.
		return  stringify(prefix);
	}
	private String stringify(String prefix) {
		return  prefix + fieldToJson("tag", this.tag) + ",\n" + 
				prefix + fieldToJson("volume", this.volume) + ",\n" +
				prefix + fieldToJson("comments", this.comments) + ",\n" + 
				prefix + fieldToJson("outcome", this.outcome);
	}
	private String fieldToJson(String fieldName, String strValue) {
		return ("\"" + fieldName + "\":" + (strValue == null ? "\"\"" : "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(strValue)) + "\""));
	}
	public String dtoToJson(String prefix)  {		
		return stringify(prefix);// + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix, fmt));
		// + super.dtoToJson(prefix, fmt));
	}
}
