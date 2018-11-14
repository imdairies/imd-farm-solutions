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


	public String getCreatedDTTMSQLFormat() {		
		return getDateInSQLFormart(createdDTTM);
	}

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
	public String dtoToJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		//Object to JSON in String
		String jsonInString = mapper.writeValueAsString(this);
		return jsonInString;
	}
}
