package com.imd.util;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.InputDelimitedFileBean;
import com.imd.services.bean.TagVolumeCommentTriplet;

public class Util {
	
	private static ConfigurationManager imdConfigurations;
	
	public static String COW_FRONT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/1.png";
	public static String COW_BACK_IMAGE_PHOTO_URL = "/assets/img/cow-photos/2.png";
	public static String COW_RIGHT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/3.png";
	public static String COW_LEFT_IMAGE_PHOTO_URL = "/assets/img/cow-photos/4.png";

	
	 public static final class LookupValues {
		public static final  String LCYCL = "LCYCL";
		public static final String FEED = "FEED";
		public static final String FEEDCOHORT = "FEEDCOHORT";
	}

	
	
	 public static final class PROPERTIES {
		public static final String JDBC_DRIVER = "JDBC_DRIVER";
		public static final  String DB_URL = "DB_URL";
		public static final String USER = "USER";
		public static final String PASS = "PASS";
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

	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String TBD = "TBD";
	
	public static final String FILE_NEW_LINE_SEPARATOR = "\n";
	public static final String FILE_RECORD_SEPARATOR = "\t";
	private static final String FILE_FIELD_ESCAPE_SEQUENCE = "\\";


	public static final String ERROR_POSTFIX = "ERROR: ";
	public static final String SUCCESS_POSTFIX = "SUCCESS: ";

	public static final double BIRTH_WEIGHT = 30d;
	public static final double DAILY_WEIGHT_GAIN_YEAR1 = 0.75d;
	public static final double DAILY_WEIGHT_GAIN_YEAR2 = 0.5d;
	public static final double DAILY_WEIGHT_GAIN_YEAR3 = 0.25d;
	public static final double DAILY_WEIGHT_GAIN_YEAR4 = 0.125d;
	public static final double DAILY_WEIGHT_GAIN_YEAR5 = 0.0625d;
	public static final double MAX_BODY_WEIGHT = 650d;

	public static final class FeedItems {
		public static final String ALFAHAY = "ALFAHAY";
		public static final String VANDA = "VANDA";
		public static final String WATER = "WATER";
		public static final String MILK = "MILK";
		public static final String CORNSILAGE = "CORNSILAGE";
		
	}

	public static final class NutritionalStats {
		public static final String DM_POSTFIX = "DM=";
		public static final String CP_POSTFIX = "CP=";
		public static final String ME_POSTFIX = "ME=";
		
	}


	public static final class FulfillmentType {
		public static final String ABSOLUTE = "ABSOLUTE";
		public static final String BODYWEIGHT = "BODYWEIGHT";
		public static final String FREEFLOW = "FREEFLOW";
		public static final String MILKPROD = "MILKPROD";
		public static final String BYDMREQPCT = "BYDMREQPCT";
		public static final Float NO_DM_MEASUREONVOLUME = new Float(0f);
	}

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
		public static final String CATEGORY_CD = "CATEGORY_CD";
	}

	
	public static final class MilkingDetailStatistics {
		public static final String DAILY_AVERAGE = "DAILY_AVERAGE";
		public static final String SEQ_NBR_MONTHLY_AVERAGE = "SEQ_NBR_MONTHLY_AVERAGE";
		public static final String YESTERDAY_SEQ_NBR_VOL = "YESTERDAY_SEQ_NBR_VOL";
		public static final String DAYS_IN_MILKING = "DAYS_IN_MILKING";
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
		public static final String DEWORM = "DEWORM";
		public static final String CULLED = "CULLED";
		public static final String DEATH = "DEATH";
		public static final String SOLD = "SOLD";
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
		public static final String DEWORM = "DEWORM";
	}
	
	
	public static final class AnimalTypes {
		public static final String HEIFER = "HEIFER";
		public static final String FEMALECALF = "FEMALECALF";
		public static final String MALECALF = "MALECALF";
		public static final String HFRPREGN = "HFRPREGN";
		public static final String LACTATING = "LACTATING";
		public static final String LCTPOSTPAR = "LCTPOSTPAR";
		public static final String LCTINSEMIN = "LCTINSEMIN";
		public static final String LCTPRGNT = "LCTPRGNT";
		public static final String HFRINSEMIN = "HFRINSEMIN";
		public static final String DRYINSEMIN = "DRYINSEMIN";
		public static final String DRYPREG = "DRYPREG";
		public static final String BULL = "BULL";
		public static final String HFRAWTHEAT = "HFRAWTHEAT";
	}
	
	public static final class FeedCohortType {
		public static final String BULL = "BULL";
		public static final String MALECALF = "MALECALF";
		public static final String HEIFER = "HEIFER";
		public static final String PREGHFR = "PREGHFR";
		public static final String HFRCLOSEUP = "HFRCLOSEUP";
		public static final String LCTPOSTPAR = "LCTPOSTPAR";
		public static final String LCTEARLY = "LCTEARLY";
		public static final String LCTMID = "LCTMID";
		public static final String LCTOLD = "LCTOLD";
		public static final String FMLWNDOFF = "FMLWNDOFF";
		public static final String FEMALECALF = "FEMALECALF";
		public static final String UNDETERMINED = "UNDETERMINED";
		public static final String NEARPRTRT = "NEARPRTRT";
		public static final String FARPRTRT = "FARPRTRT";
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
		public static final String INACTIVE = "INACTIVE";
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

	
	public static LocalTime parseLocalTime(String time, String format) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.parseLocalTime(time);
	}

	public static LocalTime parseLocalTimeHHmm(String time) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		return fmt.parseLocalTime(time);
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
	public static int getDaysBetween(LocalDate endDate, LocalDate startDate) {
		return (new Period(startDate, endDate, PeriodType.days()).getDays());
	}
	
	public static String getYearMonthDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		Period dateDifference = new Period(startTimeStamp, endTimeStamp, PeriodType.yearMonthDay());
		return (dateDifference.getYears() > 0 ?  dateDifference.getYears() + " yr(s) " : "") + (dateDifference.getMonths() > 0 ? dateDifference.getMonths() + " mo(s) " : "") + (dateDifference.getDays() > 0 ?  dateDifference.getDays() + " day(s) " : "");
	}
	public static String encodeJson(String rawString) {
		return new String(BufferRecyclers.getJsonStringEncoder().quoteAsString(rawString));
	}
	public static String encodeJson(int intValue) {
		return encodeJson(intValue + "");
	}
	public static String encodeJson(float floatValue) {
		return encodeJson(floatValue + "");
	}
	public static String formatTwoDecimalPlaces(float floatValue) {
		return formatToSpecifiedDecimalPlaces(floatValue,2);
	}
	public static String formatToSpecifiedDecimalPlaces(float floatValue, int decimalPlaces) {
		String pattern = "#.";
		for (int i=0;i<decimalPlaces; i++) {
			pattern += "#";
		}
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		return decimalFormat.format(floatValue);
	}

	public static String formatTwoDecimalPlaces(double floatValue) {
		return formatToSpecifiedDecimalPlaces(floatValue,2);
	}
	public static String formatToSpecifiedDecimalPlaces(double floatValue, int decimalPlaces) {
		String pattern = "#.";
		for (int i=0;i<decimalPlaces; i++) {
			pattern += "#";
		}
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		return decimalFormat.format(floatValue);
	}
	
	public static FarmMilkingDetailBean parseFarmMilkingDetailBean(InputDelimitedFileBean commaSeparatedRecords) throws IMDException {
		FarmMilkingDetailBean bean = new FarmMilkingDetailBean();
		String[] lines = commaSeparatedRecords.getInputDelimitedFileContents().split(FILE_NEW_LINE_SEPARATOR);
		String regex = "(?<!" + Pattern.quote(FILE_FIELD_ESCAPE_SEQUENCE) + ")" + Pattern.quote(FILE_RECORD_SEPARATOR);
		for (int i=0; i< lines.length; i++) {
			String line = lines[i];
			String[] records = line.split(regex);
			for (int k=0; k<records.length; k++) {
				String newValue = records[k];
				records[k] = newValue.replaceAll(FILE_FIELD_ESCAPE_SEQUENCE + FILE_FIELD_ESCAPE_SEQUENCE, "");
			}
			
			if (i == 0) {
				// Date, Time and Event numbers expected
				bean = parseTimeStampEventNum(bean, 1, line, records);
			} else if (i == 1) {
				// Temp, Humidity
				bean = parseTempHumidity(bean, 2, line, records);
			} else if (i == 2) {
				bean = parseFatLRToxin(bean,3,line,records);
			} else {
				// tag, milk_volume
				TagVolumeCommentTriplet milkRecordOfACow = parseCowMilkingEvent(bean,(i+1),line,records);
				if (bean.getFarmMilkingEventRecords() == null) {
					bean.setFarmMilkingEventRecords(new ArrayList<TagVolumeCommentTriplet>());					
				}
				bean.getFarmMilkingEventRecords().add(milkRecordOfACow);
			}	
		}
		return bean;
	}
	
	
	private static TagVolumeCommentTriplet parseCowMilkingEvent(FarmMilkingDetailBean bean, int lineNumber, String line, String[] records)
			throws IMDException {
		String exceptionMessage;
		String tag = "";
		Float volume = null;
		String comment = null;
		if (records == null || records.length < 2) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Cow Tag, Milk Volume, Comment (AAA" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "AAA) but found (" + line + ") which does not comply with the expected format.";
			throw new IMDException(exceptionMessage);
		}
		if (records[0].trim().isEmpty()) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Cow Tag (AAA" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "AAA) but found (" + line + ") which does not comply with the expected format. Cow Tag is missing";
			throw new IMDException(exceptionMessage);				
		} else {
			tag = records[0].trim();
		}
		if (records[1].trim().isEmpty()) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Milk Volume (AAA" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "AAA) but found (" + line + ") which does not comply with the expected format. Milking Volume is missing";
			throw new IMDException(exceptionMessage);				
		} else {
			try {
				volume = new Float(records[1].trim());
			} catch (Exception ex) {
				exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Milk Volume (AAA" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "AAA) but found (" + line + ") which does not comply with the expected format. Milking Volume is not properly formatted";
				throw new IMDException(exceptionMessage);								
			}
		}
		if (records.length >= 3) {
			comment = records[2].trim();
		}				
		TagVolumeCommentTriplet parsedRecord = new TagVolumeCommentTriplet();
		parsedRecord.setTag(tag);
		parsedRecord.setVolume(volume == null ? null : volume.toString());
		parsedRecord.setComments(comment);
		parsedRecord.setOutcome("");
		return parsedRecord;
	}		
	
	
	private static FarmMilkingDetailBean parseTimeStampEventNum(FarmMilkingDetailBean bean, int lineNumber, String line, String[] records)
			throws IMDException {
		String exceptionMessage;
		if (records.length != 3) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see milking date, milking time and event number (yyyy-mm-dd" + FILE_RECORD_SEPARATOR + "hh:mm" + FILE_RECORD_SEPARATOR + "n) but found (" + line + ") which does not comply with the expected format";
			throw new IMDException(exceptionMessage);
		}
		try {
			LocalDate milkingDate = new LocalDate(records[0].trim());
			bean.setMilkingDateStr(Util.getDateInSQLFormart(milkingDate));
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see milking date, milking time and event number (yyyy-mm-dd" + FILE_RECORD_SEPARATOR + "hh:mm" + FILE_RECORD_SEPARATOR + "n) but found (" + line + ") which does not comply with the expected format. Milking Date does not seem to be in correct format";
			throw new IMDException(exceptionMessage);
		}
		try {
			LocalTime milkingTime = parseLocalTimeHHmm(records[1].trim());
			bean.setMilkingTimeStr(Util.getTimeInSQLFormart(milkingTime));
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see milking date, milking time and event number (yyyy-mm-dd" + FILE_RECORD_SEPARATOR + "HH:mm" + FILE_RECORD_SEPARATOR + "n) but found (" + line + ") which does not comply with the expected format. Milking Time does not seem to be in correct format";
			throw new IMDException(exceptionMessage);
		}
		try {
			short milkingEventNumber = new Short(records[2].trim());
			bean.setMilkingEventNumber(milkingEventNumber);
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see milking date, milking time and event number (yyyy-mm-dd" + FILE_RECORD_SEPARATOR + "hh:mm" + FILE_RECORD_SEPARATOR + "n) but found (" + line + ") which does not comply with the expected format. Milking Event does not seem to be in correct format";
			throw new IMDException(exceptionMessage);
		}
		return bean;
	}
	
	private static FarmMilkingDetailBean parseTempHumidity(FarmMilkingDetailBean bean, int lineNumber, String line, String[] records)
			throws IMDException {
		String exceptionMessage;
		if (records.length < 1 || records.length > 2) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Temperature In Centigrade and humidity (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format.";
			throw new IMDException(exceptionMessage);
		}
		try {
			if (records[0].trim().isEmpty()) {
				exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Temperature In Centigrade and humidity (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Temperature CAN NOT be null";
				throw new IMDException(exceptionMessage);				
			} else {
				Float tempInC = new Float(records[0].trim());
				bean.setTemperatureInCentigrade(tempInC);
				
			}				
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Temperature In Centigrade and humidity (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Invalid Temperature found";
			throw new IMDException(exceptionMessage);
		}
		try {
			if (records.length < 2 || records[1].trim().isEmpty()) {
				bean.setHumidity(null);
			} else {
				Float humidity = new Float(records[1].trim());
				bean.setHumidity(humidity);
			}				
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Temperature In Centigrade and humidity (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Invalid Humidity found. Humidity is optional so you can specify an empty value; but you can't leave this out of your record.";
			throw new IMDException(exceptionMessage);
		}
		return bean;
	}	
	
	
	
	
	
	
	private static FarmMilkingDetailBean parseFatLRToxin(FarmMilkingDetailBean bean, int lineNumber, String line, String[] records)
			throws IMDException {
		String exceptionMessage;
		if (records.length > 3) {
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to see Fat, LR, Toxin (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format";
			throw new IMDException(exceptionMessage);
		}
		try {
			if (records == null || records.length < 1 || records[0].trim().isEmpty()) {
				bean.setFatValue(null);
			} else {
				Float fat = new Float(records[0].trim());
				bean.setFatValue(fat);
			}				
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to parse Fat% from the line (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Invalid Fat% found. Fat% is optional so you can specify an empty value; but you can't leave this out of your record.";
			throw new IMDException(exceptionMessage);
		}
		try {
			if (records == null || records.length < 2 || records[1].trim().isEmpty()) {
				bean.setLrValue(null);
			} else {
				Float lr = new Float(records[1].trim());
				bean.setLrValue(lr);
			}				
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to parse LR from the line (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Invalid LR found. LR is optional so you can specify an empty value; but you can't leave this out of your record.";
			throw new IMDException(exceptionMessage);
		}
		try {
			if (records == null || records.length < 3 || records[2].trim().isEmpty()) {
				bean.setToxinValue(null);
			} else {
				Float toxin = new Float(records[2].trim());
				bean.setToxinValue(toxin);
			}				
		} catch (Exception ex) {
			ex.printStackTrace();
			exceptionMessage = " Error processing line # " + (lineNumber) + ". Expected to parse Toxin from the line (ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff" + FILE_RECORD_SEPARATOR + "ff.ff) but found (" + line + ") which does not comply with the expected format. Invalid Aflatoxin found. Aflatoxin is optional so you can specify an empty value; but you can't leave this out of your record.";
			throw new IMDException(exceptionMessage);
		}

		return bean;
	}
	public static String substituteEmptyForNull(Object val) {
		return val == null ? "" : val.toString();
	}
	
	
}

