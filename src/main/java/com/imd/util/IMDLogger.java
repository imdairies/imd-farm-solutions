package com.imd.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class IMDLogger {
	public static int loggingMode = -1;
	
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_INFO_BLUE = "\033[1;34m";
	private static final String ANSI_WARN_YELLOW = "\033[1;33m";
	private static final String ANSI_ERROR_RED = "\033[1;31m";
//	private static final String ANSI_BLACK_DEBUG = "\u001B[30m";
//	private static final String ANSI_GREEN = "\u001B[32m";
//	private static final String ANSI_PURPLE = "\u001B[35m";
//	private static final String ANSI_CYAN = "\u001B[36m";
//	private static final String ANSI_WHITE = "\u001B[37m";	
	
	
	public static void log(String message, int messageSeverity) {
		DateTimeFormatter currentDTTMFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		String currentDTTMStr = currentDTTMFmt.print(DateTime.now(IMDProperties.getServerTimeZone()));

		if (loggingMode < 0)
			loadLoggingMode();
		
		if (messageSeverity >= loggingMode) {
			String messageColor = "";
			messageColor = "[" + 
				(messageSeverity == Util.INFO ? ANSI_INFO_BLUE + "INFO" : messageSeverity == Util.WARNING ? ANSI_WARN_YELLOW + "WARNING" : messageSeverity == Util.ERROR ? ANSI_ERROR_RED + "ERROR" : ANSI_ERROR_RED + "UNKNOWN")
				+  ANSI_RESET + "]";

			System.out.println(messageColor + currentDTTMStr + ": " + message);
		}		
	}
	public static void logFancy(String message, int messageColorCode) {
		DateTimeFormatter currentDTTMFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		String currentDTTMStr = currentDTTMFmt.print(DateTime.now(IMDProperties.getServerTimeZone()));
		String messageColor = "";
		if (messageColorCode == Util.INFO) {
			messageColor = ANSI_INFO_BLUE;
		} else if (messageColorCode == Util.WARNING) {
			messageColor = ANSI_WARN_YELLOW;
		} else if (messageColorCode == Util.ERROR) {
				messageColor = ANSI_ERROR_RED;
		}
		System.out.println(messageColor + currentDTTMStr + ": " + message + ANSI_RESET);
	}
	public static String getLoggingModeString() {
		if (loggingMode == Util.INFO) {
			return "INFO ("  + loggingMode + ")";
		} else if (loggingMode == Util.WARNING) {
			return "WARNING ("  + loggingMode + ")";
		} else if (loggingMode == Util.ERROR) {
			return "ERROR (" + loggingMode + ")";
		} else
			return "UNKNOWN MODE (" + loggingMode + ")";
	}
	public static void loadLoggingMode() {
		// we haven't yet loaded the logging mode value from the properties file.
		try {
			if (IMDProperties.getProperty(Util.PROPERTIES.APPLICATION_LOGGING_MODE) != null && !IMDProperties.getProperty(Util.PROPERTIES.APPLICATION_LOGGING_MODE).isEmpty())
				loggingMode = Integer.parseInt(IMDProperties.getProperty(Util.PROPERTIES.APPLICATION_LOGGING_MODE));
			else 
				loggingMode = Util.INFO;
		} catch (Exception ex) {
			ex.printStackTrace();
			loggingMode = Util.INFO;
		}
		System.out.println("========================================================\n"
				+ "LOGGING MODE SET TO: " + getLoggingModeString()+ 
				"\n========================================================\n");
	}
}


