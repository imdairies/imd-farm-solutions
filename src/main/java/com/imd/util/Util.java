package com.imd.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

public class Util {
	
	 public static final class PROPERTIES {
		public final static String JDBC_DRIVER = "JDBC_DRIVER";
		public final static String DB_URL = "DB_URL";
		public final static String USER = "USER";
		public final static String PASS = "PASS";
		public static final String APPLICATION_LOGGING_MODE = "APPLICATION_LOGGING_MODE";
		public static final String IMD_SERVICES_URL = "IMD_SERVICES_URL";
	}
	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;

	 public static final class GENDER {
		public static final char MALE = 'M';
		public static final char FEMALE = 'F';
		public static final char UNKNOWN = 'U';
	 }

	 public static final class ERROR_CODE {
			public static final int UNKNOWN_ERROR = -1000;
			public static final int ALREADY_EXISTS = -1001;
			public static final int DATA_LENGTH_ISSUE = -1002;
			public static final int SQL_SYNTAX_ERROR = -1003;
	 }
	 
	 
	 public static final class ANIMAL_STATUS {
		public static final String ACTIVE = "ACTIVE";
		public static final String DEAD = "DEAD";
		public static final String CULLED = "CULLED";
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
	
	public static String getDateInSQLFormart(DateTime dttm) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return fmt.print(dttm);
	}
}
