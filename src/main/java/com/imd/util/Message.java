package com.imd.util;

public class Message {
	private String messageCode;
	private String messageText;
	private String languageCode;
	
	public Message(String msgCode, String langCode) {
		this.messageCode = msgCode;
		this.languageCode = langCode;
		this.messageText = "";
	}
	
	public Message(String msgCode, String langCode, String message) {
		this.messageCode=msgCode;
		this.languageCode = langCode;
		this.messageText = message;
	}	
	
	public String getMessageCode() {
		return messageCode;
	}
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
}
