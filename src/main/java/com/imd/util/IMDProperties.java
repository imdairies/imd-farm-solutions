package com.imd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.joda.time.DateTimeZone;

public class IMDProperties {
	private static DateTimeZone timeZone = DateTimeZone.forID("Asia/Karachi");
	
	static Properties imdProperties;
	public static void loadProperties() {
		if (imdProperties == null) {
			imdProperties = new Properties();
			InputStream input = null;
			try {
				input = new FileInputStream("IMDConfig.properties");
				imdProperties.load(input);
				} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			String serverTimezone = getProperty(Util.PROPERTIES.SERVER_TIMEZONE);
			try {
				if (serverTimezone != null && DateTimeZone.forID("Asia/Karachi") != null)
					timeZone = DateTimeZone.forID(serverTimezone);
			} catch (Exception ex) {
				ex.printStackTrace();
				timeZone = DateTimeZone.forID("Asia/Karachi");
			}
		}
	}
	public static DateTimeZone getServerTimeZone() {
		return 	timeZone;
	}
	public static String getProperty(String key) {
		if (imdProperties == null)
			loadProperties();
		return imdProperties.getProperty(key);
	}
	
	public static void reloadProperties() {
		imdProperties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("IMDConfig.properties");
			imdProperties.load(input);
			} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	
}
