package com.imd.dto;


import org.joda.time.format.DateTimeFormatter;

public class Message extends IMDairiesDTO{
	private String languageCD;
	private String messageCD;
	private String messageText;
	public Message(String orgId, String languageCD2, String messageCd2) {
		this.setOrgID(orgId);
		this.languageCD = languageCD2;
		this.messageCD = messageCd2;
		this.messageText = "The requested message does not exist in the catalog [" + 
				orgId + "-" + languageCD2 + "-" + messageCd2 + 
				"]";
	}
	public String getLanguageCD() {
		return languageCD;
	}
	public void setLanguageCD(String languageCD) {
		this.languageCD = languageCD;
	}
	public String getMessageCD() {
		return messageCD;
	}
	public void setMessageCD(String messageCD) {
		this.messageCD = messageCD;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String toString() {
		return stringify(" ");
	}
	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("languageCD", this.languageCD) + ",\n" +
				prefix + fieldToJson("messageCD", this.messageCD) + ",\n" +
				prefix + fieldToJson("messageText", this.messageText) + ",\n";
	}
	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}



}





