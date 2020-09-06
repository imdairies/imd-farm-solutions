package com.imd.dto;

import java.net.URL;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.imd.util.Util;

public class IMDairiesDTO {
	
	private User createdBy;
	private User updatedBy;
	private DateTime updatedDTTM;
	private DateTime createdDTTM;
	private String orgId;

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

	protected String getDateInSQLFormart(LocalDate dt) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		return fmt.print(dt);
	}
	
	protected String getDateInSQLFormart(DateTime dttm, DateTimeFormatter fmt) {
		return fmt.print(dttm);
	}
	public String toString() {
		return dtoToJson("   ");
	}
	public String dtoToJson(String prefix){
		String json = prefix + fieldToJson("createdBy", this.createdBy) + ",\n" + 
				prefix + fieldToJson("createdDTTM", this.getCreatedDTTMSQLFormat()) + ",\n" +
				prefix + fieldToJson("updatedBy", this.updatedBy) + ",\n" +
				prefix + fieldToJson("updatedDTTM", this.getUpdatedDTTMSQLFormat())+ ",\n" +
				prefix + fieldToJson("orgId", this.orgId);
		return json;

	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt){
		String json = prefix + fieldToJson("createdBy", this.createdBy) + ",\n" + 
				prefix + fieldToJson("createdDTTM", this.getCreatedDTTMSQLFormat(fmt)) + ",\n" +
				prefix + fieldToJson("updatedBy", this.updatedBy) + ",\n" +
				prefix + fieldToJson("updatedDTTM", this.getUpdatedDTTMSQLFormat(fmt)) + ",\n" +
				prefix + fieldToJson("orgId", this.orgId);
		return json;

	}
	
	public String fieldToJson(String fieldName, Period dateDifference) {
		String returnValue = "";
		returnValue += getSingularOrPluralPostfix(dateDifference.getYears(),"yr");
		returnValue += getSingularOrPluralPostfix(dateDifference.getMonths(),"mo");
		returnValue += getSingularOrPluralPostfix(dateDifference.getDays(),"day");
		returnValue += getSingularOrPluralPostfix(dateDifference.getHours(),"hr");
		returnValue += getSingularOrPluralPostfix(dateDifference.getMinutes(),"min");
		return ("\"" + fieldName + "\":\"" + returnValue +  "\"");	
	}

	private String getSingularOrPluralPostfix(int value, String singularPostfix) {
		if (value <= 0)
			return "";
		else if (value == 1)
			return value + " " + singularPostfix + " ";
		else 
			return value + " " + singularPostfix + "s ";
	}
	public String fieldToJson(String fieldName, LocalDate valueDt) {
		return ("\"" + fieldName + "\":" + (valueDt == null ? "\"\"" : "\"" + this.getDateInSQLFormart(valueDt) + "\""));
	}
	
	public String fieldToJson(String fieldName, LocalTime valueTm) {
		return ("\"" + fieldName + "\":" + (valueTm == null ? "\"\"" : "\"" + valueTm.getHourOfDay() + ":" + valueTm.getMinuteOfHour() + "\""));
	}

	public String fieldToJson(String fieldName, String[] nextLifecycleStageList) {
		String values = "";
		if (nextLifecycleStageList == null || nextLifecycleStageList.length == 0)
			return ("\"" + fieldName + "\":[]");
		else {
			for (int i=0; i < nextLifecycleStageList.length; i++) {
				if ((nextLifecycleStageList.length - i) == 1)
					values +=  "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(nextLifecycleStageList[i])) + "\"";
				else
					values +=  "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(nextLifecycleStageList[i])) + "\",";
			}
			values = "\"" + fieldName + "\":[" + values + "]";
		}
		return values;
	}

	public String fieldToJson(String fieldName, String strValue) {		
		return ("\"" + fieldName + "\":" + (strValue == null ? "\"\"" : "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(strValue)) + "\""));
	}
	public String fieldToJson(String fieldName, URL url) {		
		return ("\"" + fieldName + "\":" + (url == null ? "\"\"" : "\"" + new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(url.toString())) + "\""));
	}
	public String fieldToJson(String fieldName, DateTime valueDTTM) {
		return ("\"" + fieldName + "\":" + (valueDTTM == null ? "\"\"" : "\"" + this.getDateInSQLFormart(valueDTTM) + "\""));
	}
	public String fieldToJson(String fieldName, int intValue) {
		return ("\"" + fieldName + "\":" + Integer.toString(intValue));
	}
	public String fieldToJson(String fieldName, short shortValue) {
		return ("\"" + fieldName + "\":" + shortValue);
	}
	public String fieldToJson(String fieldName, Short shortValue) {
		return ("\"" + fieldName + "\":" + (shortValue == null ? "\"\"" : shortValue));
	}
	public String fieldToJson(String fieldName, Integer intValue) {
		return ("\"" + fieldName + "\":" + (intValue == null ? "\"\"" : intValue));
	}
	public String fieldToJson(String fieldName, char charValue) {
		return ("\"" + fieldName + "\":\"" + charValue + "\"");
	}
	public String fieldToJson(String fieldName, double dblValue) {
		return ("\"" + fieldName + "\":" + Double.toString(dblValue));
	}
	public String fieldToJson(String fieldName, Double dblValue) {
		return ("\"" + fieldName + "\":" + (dblValue == null ? "\"\"" : dblValue.doubleValue()));
	}
	public String fieldToJson(String fieldName, float flValue) {
		return ("\"" + fieldName + "\":" + flValue);
	}
	public String fieldToJson(String fieldName, Float flValue) {
		return ("\"" + fieldName + "\":" + (flValue == null ? "\"\"" : Util.formatTwoDecimalPlaces(flValue.floatValue())));
	}
	public String fieldToJson(String fieldName, Person personValue) {
		return ("\"" + fieldName + "\":" + (personValue == null ? "\"\"" : "\"" +  new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(personValue.getPersonID() == null ? "" : personValue.getPersonID()))  + "\""));
	}
	public String fieldToJson(String fieldName, User usrValue) {
		return ("\"" + fieldName + "\":" + (usrValue == null ? "\"\"" : "\"" +  new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(usrValue.getUserId() == null ? "" : usrValue.getUserId())) + "\""));
	}
	public String fieldToJson(String fieldName, boolean boolValue) {
		return ("\"" + fieldName + "\":" + (boolValue ? "true" : "false"));
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
}
