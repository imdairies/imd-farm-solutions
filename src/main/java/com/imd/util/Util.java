package com.imd.util;


public class Util {
	
	 public static final class PROPERTIES {	
		public final static String JDBC_DRIVER = "JDBC_DRIVER";
		public final static String DB_URL = "DB_URL";
		public final static String USER = "USER";
		public final static String PASS = "PASS";
	}
	public static void throwExceptionIfNullOrEmpty(String stringToCheck, String parameterName) throws IMDException {
		if (stringToCheck == null)
				throw new IMDException ("A null value is not allowed for the parameter:" + parameterName);
		else if (stringToCheck.isEmpty())
			throw new IMDException ("An empty value is not allowed for the parameter:" + parameterName);
	}
	private Util() {
		
	}
	public static void throwExceptionIfNull(Object object, String parameterName) throws IMDException {
		if (object == null)
			throw new IMDException ("A null value is not allowed for the parameter:" + parameterName);
	}
	public static String removeEscapeCharacter(String escapeSequence, String originalValue) {
		return originalValue.replaceAll(escapeSequence, "");
	}
	
}
