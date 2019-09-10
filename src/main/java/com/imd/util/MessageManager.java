package com.imd.util;

import java.util.HashMap;
import java.util.Properties;
import com.imd.util.IMDException;

public final class MessageManager {
	private static HashMap <String,Properties> messageMap = new HashMap<String,Properties>();
	
	public MessageManager() {
		// load all the messages from a configuration file.
	}
	
	/**
	 * This method loads all the messages for a particular language
	 * @param languageCode
	 */
	public static void loadMessages(String languageCode, Properties messages) {
		messageMap.put(languageCode, messages);		
	}
	public static String getMessage(String languageCode, String messageCode) throws IMDException{
		Object msgPropFile = messageMap.get(languageCode);
		if ( msgPropFile != null) {
			String textMsg = ((Properties) msgPropFile).getProperty(messageCode);
			if (textMsg == null) {
				throw new IMDException("Message file for the language ["+ languageCode + "] does exist but the requested message [" + messageCode + "] does not exist in the message file");			
			}
			else return textMsg;
		}
		else
			throw new IMDException ("Messages propertise do not exist for the language [" + languageCode + "]");
	}
}
