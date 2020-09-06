package com.imd.util;

import java.util.HashMap;

import org.joda.time.LocalTime;

public class ConfigurationManager {
	
	private static HashMap<String, Object> orgConfigurations = null;
	private static HashMap<String, Object> globalConfigurations = null;
	private static HashMap<String, Object> sessionConfigurations = null;

	
	public Object getOrganizationConfigurationValue(String key) {
		if (orgConfigurations == null) {
			orgConfigurations = new HashMap<String, Object> ();
			loadOrganizationConfigurationsIfNeeded();
		}
		return orgConfigurations.get(key);
	}
	public Object getGlobalConfigurationValue(String key) {
		if (globalConfigurations == null) {
			globalConfigurations = new HashMap<String, Object> ();
			loadGlobalConfigurationsIfNeeded();
		}
		return globalConfigurations.get(key);
	}
	
	public Object getSessionConfigurationValue(String key) {
		if (sessionConfigurations == null) {
			sessionConfigurations = new HashMap<String, Object> ();
			loadUserSessionConfigurationsIfNeeded();
		}
		return sessionConfigurations.get(key);
	}
	
	public Object setOrganizationConfigurationValue(String key, Object value) {
		if (orgConfigurations == null) {
			orgConfigurations = new HashMap<String, Object> ();
			loadOrganizationConfigurationsIfNeeded();
		}
		return orgConfigurations.put(key,value);
	}

	
	private void loadUserSessionConfigurationsIfNeeded() {
		if (sessionConfigurations.isEmpty()) {
			sessionConfigurations.put(Util.ConfigKeys.ORG_ID, "IMD");
			sessionConfigurations.put(Util.ConfigKeys.USER_ID, "KASHIF");
			sessionConfigurations.put(Util.ConfigKeys.USER_NAME, "Kashif Manzoor");
			sessionConfigurations.put(Util.ConfigKeys.LANG_CD, "URD");
		}
	}
	public void reLoadUserSessionConfigurations() {
		if (sessionConfigurations == null)
			sessionConfigurations = new HashMap<String, Object> ();
		else
			sessionConfigurations.clear();
		loadUserSessionConfigurationsIfNeeded();
	}

	
	
	private void loadGlobalConfigurationsIfNeeded() {
		if (globalConfigurations.isEmpty()) {
			globalConfigurations.put(Util.ConfigKeys.UTC_TIMEZONE, Util.UTC_TIMEZONE);
			globalConfigurations.put(Util.ConfigKeys.LANG_CD, Util.LanguageCode.ENG);
			globalConfigurations.put(Util.ConfigKeys.ORG_ID, "IMD");
		}
	}
	public void reLoadGlobalConfigurations() {
		if (globalConfigurations == null)
			globalConfigurations = new HashMap<String, Object> ();
		else
			globalConfigurations.clear();
		loadGlobalConfigurationsIfNeeded();
	}

	private void loadOrganizationConfigurationsIfNeeded() {
		if (orgConfigurations.isEmpty()) {
			orgConfigurations.put(Util.ConfigKeys.ORG_ID, "IMD");
			orgConfigurations.put(Util.ConfigKeys.LANG_CD, "URD");
			orgConfigurations.put(Util.ConfigKeys.FARM_TIMEZONE, Util.FARM_TIMEZONE);
			orgConfigurations.put(Util.ConfigKeys.VOL_UNIT, Util.VOL_UNIT);
			orgConfigurations.put(Util.ConfigKeys.MILKING_FREQUENCY, (short)3);
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(4,0,0));
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(12,0,0));
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(20,0,0));
			orgConfigurations.put(Util.ConfigKeys.TOKEN_EXPIRY_MINUTES, (int)30/*expires after 30 minutes of inactivity*/);
		}
	}
	public void reLoadOrganizationConfigurations() {
		if (orgConfigurations == null)
			orgConfigurations = new HashMap<String, Object> ();
		else
			orgConfigurations.clear();
		loadOrganizationConfigurationsIfNeeded();
	}
	public void setSessionConfigurationValue(String key, String value) {
		sessionConfigurations.put(key, value);
	}

}
