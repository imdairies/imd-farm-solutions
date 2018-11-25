package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	 * Returns the updated date in "yyyy-MM-dd HH:mm:ss" format
	 * @return
	 */
	public String getUpdatedDTTMSQLFormat() {
		return getDateInSQLFormart(updatedDTTM);
	}
	
	protected String getDateInSQLFormart(DateTime dttm) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return fmt.print(dttm);
	}
	public String toString() {
		
		return "\nCREATED_BY=" + getCreatedBy().getUserId() + 
				"\nCREATED_DTTM=" + getCreatedDTTM()  +
				"\nUPDATED_BY=" + getUpdatedBy().getUserId() + 
				"\nUPDATED_DTTM=" + getUpdatedDTTM();
	}
	public String createUpdateString() {
		String updateString = "";
		if (getCreatedBy() != null && !getCreatedBy().getUserId().isEmpty())
			updateString += " CREATED_BY='" + getCreatedBy().getUserId() + "',";
		if (getUpdatedBy() != null && !getUpdatedBy().getUserId().isEmpty())
			updateString += " UPDATED_BY='" + getUpdatedBy().getUserId() + "',";
		if (getCreatedDTTM() != null)
			updateString += " CREATED_DTTM='" + getCreatedDTTMSQLFormat() + "',";
		if (getUpdatedDTTM() != null)
			updateString += " UPDATED_DTTM='" + getUpdatedDTTMSQLFormat() + "',";
		return updateString;
	}
	public String dtoToJson(String prefix){
		String json = prefix + fieldToJson("createdBy", this.createdBy) + ",\n" + 
				prefix + fieldToJson("createdDTTM", this.getCreatedDTTMSQLFormat()) + ",\n" +
				prefix + fieldToJson("createdBy", this.updatedBy) + ",\n" +
				prefix + fieldToJson("updatedDTTM", this.getUpdatedDTTMSQLFormat());
		return json;

	}
	protected String fieldToJson(String fieldName, String strValue) {		
		return ("\"" + fieldName + "\":" + (strValue == null ? "\"\"" : "\"" + strValue + "\""));
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
		return ("\"" + fieldName + "\":" + (personValue == null ? "\"\"" : "\"" + personValue.getPersonID() + "\""));
	}
	protected String fieldToJson(String fieldName, User usrValue) {
		return ("\"" + fieldName + "\":" + (usrValue == null ? "\"\"" : "\"" + usrValue.getUserId() + "\""));
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
