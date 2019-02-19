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
	
	private void loadUserSessionConfigurationsIfNeeded() {
		if (sessionConfigurations.isEmpty()) {
			sessionConfigurations.put(Util.ConfigKeys.ORG_ID, "IMD");
			sessionConfigurations.put(Util.ConfigKeys.USER_ID, "KASHIF");
			sessionConfigurations.put(Util.ConfigKeys.USER_NAME, "Kashif Manzoor");					
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
			orgConfigurations.put(Util.ConfigKeys.FARM_TIMEZONE, Util.FARM_TIMEZONE);
			orgConfigurations.put(Util.ConfigKeys.VOL_UNIT, Util.VOL_UNIT);
			orgConfigurations.put(Util.ConfigKeys.MILKING_FREQUENCY, new Short((short)3));
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(5,0,0));
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(13,0,0));
			orgConfigurations.put(Util.ConfigKeys.FIRST_MILKING_TIME, new LocalTime(21,0,0));
		}
	}
	public void reLoadOrganizationConfigurations() {
		if (orgConfigurations == null)
			orgConfigurations = new HashMap<String, Object> ();
		else
			orgConfigurations.clear();
		loadOrganizationConfigurationsIfNeeded();
	}

}
