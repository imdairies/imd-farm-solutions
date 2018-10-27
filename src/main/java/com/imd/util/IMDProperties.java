package com.imd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IMDProperties {
	
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
		}
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
