package com.imd.dto;

import java.net.URL;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.util.BufferRecyclers;

public class IMDairiesDTO {
	
	private User createdBy;
	private User updatedBy;
	private DateTime updatedDTTM;
	private DateTime createdDTTM;
	private String orgID;

	public User getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(User user) {
		this.createdBy = user;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(User user) {
		this.updatedBy = user;
	}

	public DateTime getCreatedDTTM() {
		return createdDTTM;
	}
	public void setCreatedDTTM(DateTime createdDateTime) {
		this.createdDTTM = createdDateTime;
	}

	public DateTime getUpdatedDTTM() {
		return updatedDTTM;
	}
	public void setUpdatedDTTM(DateTime updatedDateTime) {
		this.updatedDTTM = updatedDateTime;
	}


	/**
	 * Returns the created date in "yyyy-MM-dd HH:mm:ss" format
	 * @return
	 */

	public String getCreatedDTTMSQLFormat() {		
		return getDateInSQLFormart(createdDTTM);
	}
	/**
	 * Returns the created date in specified format
	 * @return
	 */

	public String getCreatedDTTMSQLFormat(DateTimeFormatter fmt) {		
		return getDateInSQLFormart(createdDTTM, fmt);
	}

	/**
	 * Returns the updated date in "yyyy-MM-dd HH:mm:ss" format
	 * @return
	 */
	public String getUpdatedDTTMSQLFormat() {
		return getDateInSQLFormart(updatedDTTM);
	}
	/**
	 * Returns the updated date in specified format
	 * @return
	 */
	public String getUpdatedDTTMSQLFormat(DateTimeFormatter fmt) {
		return getDateInSQLFormart(updatedDTTM, fmt);
	}
	
	protected String getDateInSQLFormart(DateTime dttm) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return fmt.print(dttm);
	}
	protected String getDateInSQLFormart(DateTime dttm, DateTimeFormatter fmt) {
		return fmt.print(dttm);
	}
	public String toString() {
		
		return "\nCREATED_BY=" + (this.createdBy == null ? "null" : getCreatedBy().getUserId()) + 
				"\nCREATED_DTTM=" + (this.getCreatedDTTM() == null ? "null" : this.getCreatedDTTMSQLFormat()) +
				"\nUPDATED_BY=" + (this.updatedBy == null ? "null" : this.updatedBy.getUserId()) + 
				"\nUPDATED_DTTM=" + (this.getUpdatedDTTM() == null ? "null" : this.getUpdatedDTTMSQLFormat());
	}
	public String dtoToJson(String prefix){
		String json = prefix + fieldToJson("createdBy", this.createdBy) + ",\n" + 
				prefix + fieldToJson("createdDTTM", this.getCreatedDTTMSQLFormat()) + ",\n" +
				prefix + fieldToJson("updatedBy", this.updatedBy) + ",\n" +
				prefix + fieldToJson("updatedDTTM", this.getUpdatedDTTMSQLFormat());
		return json;

	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt){
		String json = prefix + fieldToJson("createdBy", this.createdBy) + ",\n" + 
				prefix + fieldToJson("createdDTTM", this.getCreatedDTTMSQLFormat(fmt)) + ",\n" +
				prefix + fieldToJson("updatedBy", this.updatedBy) + ",\n" +
				prefix + fieldToJson("updatedDTTM", this.getUpdatedDTTMSQLFormat(fmt));
		return json;

	}
	
	protected String fieldToJson(String fieldName, Period dateDifference) {
		return ("\"" + fieldName + "\":\"" + (dateDifference == null ? "\"\"" : 
				(dateDifference.getYears() > 0 ?  dateDifference.getYears() + " yr(s) " : "") +
				(dateDifference.getMonths() > 0 ? dateDifference.getMonths() + " mo(s) " : "") +
				(dateDifference.getDays() > 0 ?  dateDifference.getDays() + " day(s) " : "")) + "\"");

	
	}
	
	protected String fieldToJson(String fieldName, String strValue) {		
		return ("\"" + fieldName + "\":" + (strValue == null ? "\"\"" : "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(strValue)) + "\""));
	}
	protected String fieldToJson(String fieldName, URL url) {		
		return ("\"" + fieldName + "\":" + (url == null ? "\"\"" : "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(url.toString())) + "\""));
	}
	protected String fieldToJson(String fieldName, DateTime valueDTTM) {
		return ("\"" + fieldName + "\":" + (valueDTTM == null ? "\"\"" : "\"" + this.getDateInSQLFormart(valueDTTM) + "\""));
	}
	protected String fieldToJson(String fieldName, int intValue) {
		return ("\"" + fieldName + "\":" + Integer.toString(intValue));
	}
	protected String fieldToJson(String fieldName, char charValue) {
		return ("\"" + fieldName + "\":\"" + charValue + "\"");
	}
	protected String fieldToJson(String fieldName, double dblValue) {
		return ("\"" + fieldName + "\":" + Double.toString(dblValue));
	}
	protected String fieldToJson(String fieldName, Person personValue) {
		return ("\"" + fieldName + "\":" + (personValue == null ? "\"\"" : "\"" +  new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(personValue.getPersonID() == null ? "" : personValue.getPersonID()))  + "\""));
	}
	protected String fieldToJson(String fieldName, User usrValue) {
		return ("\"" + fieldName + "\":" + (usrValue == null ? "\"\"" : "\"" +  new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(usrValue.getUserId() == null ? "" : usrValue.getUserId())) + "\""));
	}
	protected String fieldToJson(String fieldName, boolean boolValue) {
		return ("\"" + fieldName + "\":" + (boolValue ? "true" : "false"));
	}
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}
}
