package com.imd.util;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Util {
	
	private static ConfigurationManager imdConfigurations;
	
	public static String COW_FRONT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/1.png";
	public static String COW_BACK_IMAGE_PHOTO_URL = "/assets/img/cow-photos/2.png";
	public static String COW_RIGHT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/3.png";
	public static String COW_LEFT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/4.png";

	
	 public static final class LookupValues {
		public final static String LCYCL = "LCYCL";
	}

	
	
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
	
	public static final DateTimeZone FARM_TIMEZONE = DateTimeZone.forID("Asia/Karachi");
	public static final DateTimeZone UTC_TIMEZONE = DateTimeZone.forID("UTC");
	public static final String VOL_UNIT = "LTR";

	public static final String COW_PHOTOS_URI_PREFIX = "/assets/img/cow-photos/";

	public static final String YES = "Yes";
	public static final String NO = "No";

	public static final class Breed  {
		public static final String HFCROSS = "HFCROSS";
		public static final String BROWNSWISS = "BROWNSWISS";
	}

	public static final class DataTypes {
		public static final String FLOAT = "FLOAT";
		public static final String TEXT = "TEXT";
		public static final String DATETIME = "DATETIME";
		public static final String YESNO = "YESNO";
		public static final String LV_SIRE = "LV:/sire";
	}

	
	public static final class MilkingDetailStatistics {
		public static final String DAILY_AVERAGE = "DAILY_AVERAGE";
		public static final String SEQ_NBR_MONTHLY_AVERAGE = "SEQ_NBR_MONTHLY_AVERAGE";
		public static final String YESTERDAY_SEQ_NBR_VOL = "YESTERDAY_SEQ_NBR_VOL";
	}

	public static final class LifeCycleEvents {
		public static final String INSEMINATE = "INSEMINATE";
		public static final String DEHORN = "DEHORN";
		public static final String FMDVACCINE = "FMDVACCINE";	
		public static final String MATING = "MATING";
		public static final String HEAT = "HEAT";
		public static final String PARTURATE = "PARTURATE";
		public static final String PREGTEST = "PREGTEST";
		public static final String ABORTION = "ABORTION";
		public static final String WEIGHT = "WEIGHT";
		public static final String WEANEDOFF = "WEANEDOFF";
		public static final String BIRTH = "BIRTH";
		public static final String VACCINE = "VACCINE";
	}

	public static final class AdvisementRules {
		public static final String DRYCOW = "DRYCOW";
		public static final String DEHORN = "DEHORN";
		public static final String VACCINEFMD = "VACCINEFMD";
		public static final String PREGNANCYTEST = "PREGNANCYTEST";
		public static final String HEATWARNING = "HEATWARNING";
		public static final String DELAYEDHEATHEIFER = "DELAYEDHEATHEIFER";
		public static final String DELAYEDHEATCOW = "DELAYEDHEATCOW";
		public static final String WEIGHTMEASUREMENT = "WEIGHTMEASUREMENT";
		public static final String HEIFERWEIGHT = "HEIFERWEIGHT";
		public static final String WEANOFF = "WEANOFF";
	}
	
	public static final class AnimalTypes {
		public static final String HEIFER = "HEIFER";
		public static final String FEMALECALF = "FEMALECALF";
		public static final String MALECALF = "MALECALF";
		public static final String HFRPREGN = "HFRPREGN";
		public static final String LACTATING = "LACTATING";
		public static final String LCTINSEMIN = "LCTINSEMIN";
		public static final String LCTPRGNT = "LCTPRGNT";
		public static final String HFRINSEMIN = "HFRINSEMIN";
		public static final String DRYINSEMIN = "DRYINSEMIN";
		public static final String DRYPRENG = "DRYPRENG";
	}
	

	 public static final class GENDER {
		public static final char MALE = 'M';
		public static final char FEMALE = 'F';
		public static final char UNKNOWN = 'U';
	 }

		public static final class  ConfigKeys {
			public static final String FARM_TIMEZONE = "FARM_TIMEZONE";
			public static final String VOL_UNIT = "VOL_UNIT";
			public static final String UTC_TIMEZONE = "UTC_TIMEZONE";
			public static final String ORG_ID = "ORG_ID";
			public static final String USER_ID = "USER_ID";
			public static final String USER_NAME = "USER_NAME";
			public static final String MILKING_FREQUENCY = "MILKING_FREQUENCY";
			public static final String FIRST_MILKING_TIME = "FIRST_MILKING_TIME";
			public static final String SECOND_MILKING_TIME = "SECOND_MILKING_TIME";
			public static final String THIRD_MILKING_TIME = "THIRD_MILKING_TIME";
		}

	 
	 
	 public static final class ERROR_CODE {
			public static final int UNKNOWN_ERROR = -1000;
			public static final int ALREADY_EXISTS = -1001;
			public static final int DATA_LENGTH_ISSUE = -1002;
			public static final int SQL_SYNTAX_ERROR = -1003;
			public static final int DOES_NOT_EXIST = -1004;
			public static final int PARTIAL_SUCCESS = -1005;
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
	public static String getDateInSQLFormart(LocalDate dt) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		return fmt.print(dt);
	}
	public static String getDateInSpecifiedFormart(DateTime dttm, String format) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.print(dttm);
	}
	
	public static String getTimeInSQLFormart(LocalTime tm) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		return fmt.print(tm);
	}
	public static ConfigurationManager getConfigurations() {
		if (imdConfigurations == null)
			imdConfigurations = new ConfigurationManager();
		return imdConfigurations;
	}
	public static String getDateInSpecifiedFormart(LocalDate dt, String format) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.print(dt);
	}
	
	public static int getDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		return (new Period(startTimeStamp, endTimeStamp, PeriodType.days()).getDays());
	}
	
	public static String getYearMonthDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		Period dateDifference = new Period(startTimeStamp, endTimeStamp, PeriodType.yearMonthDay());
		return (dateDifference.getYears() > 0 ?  dateDifference.getYears() + " yr(s) " : "") + (dateDifference.getMonths() > 0 ? dateDifference.getMonths() + " mo(s) " : "") + (dateDifference.getDays() > 0 ?  dateDifference.getDays() + " day(s) " : "");
	}
	
}

