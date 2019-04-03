package com.imd.loader;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.dto.MilkingDetail;
import com.imd.dto.User;
import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.TagVolumeCommentTriplet;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class MilkingDetailLoader {
	
	
	public int insertMilkRecord(MilkingDetailBean milkingRecord)  {
		int recordAdded = -1;
		String qryString = "insert into MILK_LOG (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "MILK_DATE,"
				+ "SEQ_NBR,"
				+ "MILK_TIME,"
				+ "VOL,"
				+ "VOL_UNIT,"
				+ "LR,"
				+ "FAT,"
				+ "TOXIN,"
				+ "TEMP_C,"
				+ "HUMIDITY,"
				+ "COMMENTS,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, (milkingRecord.getOrgID() == null ? null : milkingRecord.getOrgID()));
			preparedStatement.setString(2, (milkingRecord.getAnimalTag() == null ? null : milkingRecord.getAnimalTag()));
			preparedStatement.setString(3, (milkingRecord.getRecordDate() == null ? null : Util.getDateInSQLFormart(milkingRecord.getRecordDate())));
			preparedStatement.setShort(4, milkingRecord.getMilkingEventNumber());
			preparedStatement.setString(5, (milkingRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingRecord.getRecordTime())));
			preparedStatement.setFloat(6, milkingRecord.getMilkVolume());
			preparedStatement.setString(7, Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT).toString());
			preparedStatement.setString(8,  (milkingRecord.getLrValue() == null ? null : milkingRecord.getLrValue().toString()));
			preparedStatement.setString(9,  (milkingRecord.getFatValue() == null ? null : milkingRecord.getFatValue().toString()));
			preparedStatement.setString(10, (milkingRecord.getToxinValue() == null ? null : milkingRecord.getToxinValue().toString()));
			preparedStatement.setString(11, (milkingRecord.getTemperatureInCentigrade() == null ? null : milkingRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(12,  (milkingRecord.getHumidity() == null ? null : milkingRecord.getHumidity().toString()));
			preparedStatement.setString(13, milkingRecord.getComments());
			preparedStatement.setString(14, (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID));
			preparedStatement.setString(15, Util.getDateInSQLFormart(DateTime.now()));
			preparedStatement.setString(16, (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID));
			preparedStatement.setString(17, Util.getDateInSQLFormart(DateTime.now()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			recordAdded = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			recordAdded = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return recordAdded;
	}
	
	

	public int updateMilkRecord(MilkingDetailBean milkingRecord)  {
		int recordAdded = -1;
		String qryString = "UPDATE MILK_LOG SET "
				+ "VOL=? ,"
				+ "MILK_TIME=? ,"
				+ "VOL_UNIT=? ,"
				+ "LR=? ,"
				+ "FAT=? ,"
				+ "TOXIN=? ,"
				+ "TEMP_C=? ,"
				+ "HUMIDITY=? ,"
				+ "COMMENTS=? ,"
				+ "UPDATED_BY=? ,"
				+ "UPDATED_DTTM=? WHERE ORG_ID=? AND ANIMAL_TAG=? AND MILK_DATE=? AND SEQ_NBR=?";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setFloat(1, milkingRecord.getMilkVolume());
			preparedStatement.setString(2, (milkingRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingRecord.getRecordTime())));
			preparedStatement.setString(3, Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT).toString());
			preparedStatement.setString(4,  (milkingRecord.getLrValue() == null ? null : milkingRecord.getLrValue().toString()));
			preparedStatement.setString(5,  (milkingRecord.getFatValue() == null ? null : milkingRecord.getFatValue().toString()));
			preparedStatement.setString(6, (milkingRecord.getToxinValue() == null ? null : milkingRecord.getToxinValue().toString()));
			preparedStatement.setString(7, (milkingRecord.getTemperatureInCentigrade() == null ? null : milkingRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(8,  (milkingRecord.getHumidity() == null ? null : milkingRecord.getHumidity().toString()));
			preparedStatement.setString(9, milkingRecord.getComments());
			preparedStatement.setString(10, (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID));
			preparedStatement.setString(11, Util.getDateInSQLFormart(DateTime.now()));
			preparedStatement.setString(12, (milkingRecord.getOrgID() == null ? null : milkingRecord.getOrgID()));
			preparedStatement.setString(13, (milkingRecord.getAnimalTag() == null ? null : milkingRecord.getAnimalTag()));
			preparedStatement.setString(14, (milkingRecord.getRecordDate() == null ? null : Util.getDateInSQLFormart(milkingRecord.getRecordDate())));
			preparedStatement.setShort(15, milkingRecord.getMilkingEventNumber());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			recordAdded = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			recordAdded = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return recordAdded;
	}	
	
	
	public int deleteMilkingRecordOfaDay(String orgID, String animalTag, LocalDate recordDate) {
		String qryString = "DELETE FROM MILK_LOG where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + "' AND MILK_DATE='" + Util.getDateInSQLFormart(recordDate) + "'";
		return performDeletion(qryString);
	}
	public int deleteOneMilkingRecord(String orgID, String animalTag, LocalDate recordDate, int sequenceNbr) {
		String qryString = "DELETE FROM MILK_LOG where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + 
				"' AND MILK_DATE='" + Util.getDateInSQLFormart(recordDate) + "' AND SEQ_NBR=" + sequenceNbr;
		return performDeletion(qryString);
	}
	private int performDeletion(String qryString) {
		int result = -1;
		Statement st = null;
		Connection conn = DBManager.getDBConnection();
		try {
			st = conn.createStatement();
			result = st.executeUpdate(qryString);			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (st != null && !st.isClosed()) {
					st.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public List<MilkingDetail> retrieveAllMilkingRecordsOfCow(MilkingDetail milkingSearchParam) throws Exception {
		String qryString = "SELECT A.*, 0 AS AVERAGE_VOL FROM imd.MILK_LOG A " + 
				"where A.org_id=? " + 
				"and A.animal_tag=? ORDER BY A.MILK_DATE, A.SEQ_NBR DESC";
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		return readRecords(qryString, values);
	}

	public List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(MilkingDetailBean searchBean) throws Exception {
		return retrieveMonthlyMilkingRecordsOfCow(searchBean.getOrgID(),searchBean.getAnimalTag(), searchBean.getRecordDate().getMonthOfYear(),
				searchBean.getRecordDate().getYear());
	}
	
	private List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(String orgID, String tagNbr, int monthOfYear, int year) throws Exception {
		String qryString = "SELECT *, 0 AS AVERAGE_VOL FROM imd.MILK_LOG " + 
				"where org_id=? " + 
				"and animal_tag=? and MILK_DATE >= CAST(? AS DATE) and MILK_DATE < CAST(? AS DATE) ORDER BY MILK_DATE, SEQ_NBR ASC";
		LocalDate fromDate = new LocalDate(year, monthOfYear, 1);
		List<String> values = new ArrayList<String> ();
		values.add(orgID);
		values.add(tagNbr);
		values.add(fromDate.toString());
		values.add(fromDate.plusMonths(1).toString());
		return readRecords(qryString, values);
	}

	private ArrayList<MilkingDetail> readRecords(String qryString, List<String> values)
			throws SQLException, Exception {
		ArrayList<MilkingDetail> allMatchingValues = new ArrayList<MilkingDetail>();		
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		preparedStatement = conn.prepareStatement(qryString);
		Iterator<String> it = values.iterator();
		int i=1;
		while (it.hasNext())
			preparedStatement.setString(i++,it.next());
		IMDLogger.log(preparedStatement.toString(),Util.INFO);		
	    rs = preparedStatement.executeQuery();
	    while (rs.next()) {
	    	MilkingDetail milkingRecord = getMilkingDetailFromSQLRecord(rs);
	    	allMatchingValues.add(milkingRecord);
	    }
		return allMatchingValues;
	}
	
	
	public List<MilkingDetail> retrieveSingleMilkingRecordsOfCow(MilkingDetailBean milkingSearchParam, boolean isMonthlyAverageRequired) throws Exception {
		String qryString = "";
		if (isMonthlyAverageRequired)
			qryString = "SELECT A.*,B.AVERAGE_VOL AS AVERAGE_VOL FROM imd.MILK_LOG A, imd.MILK_SEQ_AVG_VW B " + 
					"where A.org_id=? " + 
					"and A.animal_tag=? and A.MILK_DATE=? and A.SEQ_NBR=? AND A.ORG_ID=B.ORG_ID AND A.ANIMAL_TAG=B.ANIMAL_TAG AND A.SEQ_NBR=B.SEQ_NBR AND B.MONTH = MONTH(A.MILK_DATE)";
		else
			qryString = "SELECT A.*, 0 AS AVERAGE_VOL FROM imd.MILK_LOG A " + 
					"where A.org_id=? " + 
					"and A.animal_tag=? and A.MILK_DATE=? and A.SEQ_NBR=? ";
		
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		values.add(Util.getDateInSQLFormart(milkingSearchParam.getRecordDate()));
		values.add("" + milkingSearchParam.getMilkingEventNumber());
		return readRecords(qryString, values);
	}
	public MilkingDetail[] retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(LocalDate startDate) throws SQLException {
		LocalDate startOfYear = new LocalDate(startDate.getYear(), 1, 1);
		LocalDate endOfTheYear = new LocalDate(startDate.getYear(),12, 31);
		MilkingDetail[] dailyRecordofTheYear = new MilkingDetail[365];
		List<MilkingDetail> dailyRecordforTheYear = retrieveFarmMilkVolumeForSpecifiedDateRange(startOfYear, endOfTheYear, false);		
		Iterator<MilkingDetail> it = dailyRecordforTheYear.iterator();
		int dayIndex = 1;
		LocalDate lastInsertedRecordDate = null;
		while (it.hasNext()) {
			MilkingDetail milkDetail = it.next();
			int dayOfYear = milkDetail.getRecordDate().getDayOfYear();
			for (; dayOfYear > dayIndex; dayIndex++) {
				int dateDiffInDays = dayOfYear - dayIndex;
				MilkingDetail emptyRecord = new MilkingDetail();
				emptyRecord.setRecordDate(milkDetail.getRecordDate().minusDays(dateDiffInDays));
				emptyRecord.setMilkVolume(0.0f);
				dailyRecordofTheYear[dayIndex-1] = emptyRecord;
				lastInsertedRecordDate = emptyRecord.getRecordDate();
			}
			dailyRecordofTheYear[dayIndex-1] = milkDetail;
			lastInsertedRecordDate = milkDetail.getRecordDate();
			dayIndex++;
		}
		for (; dayIndex <= endOfTheYear.getDayOfYear()  ;) {
			MilkingDetail emptyRecord = new MilkingDetail();
			lastInsertedRecordDate = lastInsertedRecordDate.plusDays(1);
			emptyRecord.setRecordDate(lastInsertedRecordDate);
			emptyRecord.setMilkVolume(0.0f);
			dailyRecordofTheYear[dayIndex-1] = emptyRecord;
			dayIndex++;
		}
		return dailyRecordofTheYear;
	}

	
	public List<MilkingDetail> retrieveFarmMonthlyMilkVolumeForSpecifiedYear(LocalDate startDate, boolean shouldIncludeMissingMonths) throws SQLException {
		LocalDate startOfYear = new LocalDate(startDate.getYear(), 1, 1);
		LocalDate endOfTheYear = new LocalDate(startDate.getYear(),12, 31);
		List<MilkingDetail> monthlyRecordforTheYear = new ArrayList<MilkingDetail>();
		List<MilkingDetail> dailyRecordforTheYear = retrieveFarmMilkVolumeForSpecifiedDateRange(startOfYear, endOfTheYear, false);		
		Iterator<MilkingDetail> it = dailyRecordforTheYear.iterator();
		int processingRecordMonth = 1;
		MilkingDetail prevRecord = null;
		int totatRecordProcessedInGivenMonth = 0;
		while (it.hasNext()) {
			MilkingDetail milkDetail = it.next();
			if (prevRecord == null) {
				prevRecord = new MilkingDetail();
				prevRecord.setRecordDate(milkDetail.getRecordDate());
				prevRecord.setMilkVolume(milkDetail.getMilkVolume());
				prevRecord.setLrValue(milkDetail.getLrValue());
				prevRecord.setFatValue(milkDetail.getFatValue());
				prevRecord.setTemperatureInCentigrade(milkDetail.getTemperatureInCentigrade());
				prevRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, milkDetail.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE));
				totatRecordProcessedInGivenMonth++;
			} else if (prevRecord.getRecordDate().getMonthOfYear() == milkDetail.getRecordDate().getMonthOfYear()) {
				float totalVolume = prevRecord.getMilkVolume() + milkDetail.getMilkVolume();
				float averageLR = ((prevRecord.getLrValue()*totatRecordProcessedInGivenMonth) + milkDetail.getLrValue())/(totatRecordProcessedInGivenMonth+1);
				float averageFat = ((prevRecord.getFatValue()*totatRecordProcessedInGivenMonth) + milkDetail.getFatValue())/(totatRecordProcessedInGivenMonth+1);
				float averageTemp = ((prevRecord.getTemperatureInCentigrade()*totatRecordProcessedInGivenMonth) + milkDetail.getTemperatureInCentigrade())/(totatRecordProcessedInGivenMonth+1);
				float averagePerAnimal = ((prevRecord.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE)*totatRecordProcessedInGivenMonth) + milkDetail.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE))/(totatRecordProcessedInGivenMonth+1);
				prevRecord.setMilkVolume(totalVolume);
				prevRecord.setLrValue(averageLR);
				prevRecord.setFatValue(averageFat);
				prevRecord.setTemperatureInCentigrade(averageTemp);
				prevRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, averagePerAnimal);
				totatRecordProcessedInGivenMonth++;
			} else {
				monthlyRecordforTheYear.add(prevRecord);				
				prevRecord = new MilkingDetail();
				prevRecord.setRecordDate(milkDetail.getRecordDate());
				prevRecord.setMilkVolume(milkDetail.getMilkVolume());
				prevRecord.setLrValue(milkDetail.getLrValue());
				prevRecord.setFatValue(milkDetail.getFatValue());
				prevRecord.setTemperatureInCentigrade(milkDetail.getTemperatureInCentigrade());
				prevRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, milkDetail.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE));
				totatRecordProcessedInGivenMonth = 1;
			}
		}
		if (prevRecord!= null) {
			monthlyRecordforTheYear.add(prevRecord);
		}
		
		if (shouldIncludeMissingMonths) {
			List<MilkingDetail> twelveMonthsInformation = new ArrayList<MilkingDetail>();
			it = monthlyRecordforTheYear.iterator();
			while (it.hasNext()) {
				MilkingDetail milkDetail = it.next();
				while (processingRecordMonth < milkDetail.getRecordDate().getMonthOfYear()) {
					MilkingDetail noRecord = new MilkingDetail();
					noRecord.setRecordDate(new LocalDate(milkDetail.getRecordDate().getYear(), processingRecordMonth, 1));
					noRecord.setMilkVolume(0);
					noRecord.setLrValue(0f);
					noRecord.setFatValue(0f);
					noRecord.setTemperatureInCentigrade(0f);
					twelveMonthsInformation.add(noRecord);
					processingRecordMonth++;					
				}
				twelveMonthsInformation.add(milkDetail);
				processingRecordMonth++;			
			}
		    while (processingRecordMonth <= 12) {
				MilkingDetail noRecord = new MilkingDetail();
				noRecord.setRecordDate(new LocalDate(startDate.getYear(), processingRecordMonth, 1));
				noRecord.setMilkVolume(0);
				noRecord.setLrValue(0f);
				noRecord.setFatValue(0f);
				noRecord.setTemperatureInCentigrade(0f);
				twelveMonthsInformation.add(noRecord);
				processingRecordMonth++;					
		    }
			return twelveMonthsInformation;
		} else
			return monthlyRecordforTheYear;
	}

	public List<MilkingDetail> retrieveFarmMilkVolumeForSpecifiedMonth(LocalDate startDate, boolean shouldIncludeMissingDays) throws SQLException {
		LocalDate beginingOfStartDateMonth = new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), 1);
		LocalDate endOfTheMonth = beginingOfStartDateMonth.plusMonths(1).minusDays(1);
		return retrieveFarmMilkVolumeForSpecifiedDateRange(beginingOfStartDateMonth, endOfTheMonth, shouldIncludeMissingDays);
	}	
		
	/**
	 * Retrieves data from the startDate till endDate both dates inclusive. The data is grouped by day i.e. each record represents the consolidated milk volume for a given day.
	 * If a day does not have any milking information recorded in the DB then a 0 milking volume is reported for that day.
	 * @param startDate
	 * @param endDate
	 * @param shouldIncludeMissingDays If set to true then even if there is no record for a day, the output will have a zero volume for that day. If set to false then the days with no record will not be included in the return array.
	 * @return
	 * @throws SQLException 
	 */
	private List<MilkingDetail> retrieveFarmMilkVolumeForSpecifiedDateRange(LocalDate startDate, LocalDate endDate, boolean shouldIncludeMissingDays) throws SQLException{
		ArrayList<MilkingDetail> allMatchingValues = new ArrayList<MilkingDetail>();
		String qryString = "SELECT MILK_DATE,  COUNT(DISTINCT(ANIMAL_TAG)) AS MILKED_ANIMALS, "
				+ "SUM(VOL) AS VOLUME, " 
				+ "AVG(VOL)  AS AVG_VOLUME, "
				+ "AVG(LR) AS AVG_LR, "
				+ "AVG(FAT) AS AVG_FAT, "
				+ "AVG(TEMP_C) AS AVG_TEMPERATURE "
				+ " FROM imd.MILK_LOG where " + 
				"MILK_DATE >= CAST(? AS DATE) AND MILK_DATE <= CAST(? AS  DATE) group by milk_date order by milk_date asc";
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		preparedStatement = conn.prepareStatement(qryString);
		preparedStatement.setString(1,Util.getDateInSpecifiedFormart(startDate, "yyyy-MM-dd"));
		preparedStatement.setString(2,Util.getDateInSpecifiedFormart(endDate, "yyyy-MM-dd"));
		IMDLogger.log(preparedStatement.toString(),Util.INFO);
	    rs = preparedStatement.executeQuery();
	    int dayOfMonth = 1;
	    while (rs.next()) {
			MilkingDetail milkDetail = new MilkingDetail();
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");			
			milkDetail.setRecordDate(new LocalDate(dtf.parseLocalDate(rs.getString("MILK_DATE"))));
			milkDetail.setMilkVolume(rs.getFloat("VOLUME"));
			milkDetail.setLrValue(rs.getFloat("AVG_LR"));
			milkDetail.setFatValue(rs.getFloat("AVG_FAT"));
			milkDetail.setTemperatureInCentigrade(rs.getFloat("AVG_TEMPERATURE"));
			milkDetail.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, new Float(milkDetail.getMilkVolume()/rs.getInt("MILKED_ANIMALS")));
			if (shouldIncludeMissingDays) {
				while (dayOfMonth < milkDetail.getRecordDate().getDayOfMonth()) {
					MilkingDetail noRecord = new MilkingDetail();
					noRecord.setRecordDate(new LocalDate(milkDetail.getRecordDate().getYear(), milkDetail.getRecordDate().getMonthOfYear(), dayOfMonth));
					noRecord.setMilkVolume(0);
					noRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0f);
					allMatchingValues.add(noRecord);
					dayOfMonth++;
					IMDLogger.log(noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
				}
			}
			allMatchingValues.add(milkDetail);
			IMDLogger.log(milkDetail.getRecordDate().toString() + ": " + milkDetail.getMilkVolume(), Util.INFO);
			dayOfMonth++;
	    }
	    
		if (shouldIncludeMissingDays) {
		    int lastDateofMonth = endDate.getDayOfMonth();
		    while (dayOfMonth <= lastDateofMonth) {
				MilkingDetail noRecord = new MilkingDetail();
				noRecord.setRecordDate(new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), dayOfMonth));
				noRecord.setMilkVolume(0);
				allMatchingValues.add(noRecord);
				IMDLogger.log(noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
				dayOfMonth++;
		    }
		}
	    return allMatchingValues;
	}
	private MilkingDetail getMilkingDetailFromSQLRecord(ResultSet rs) throws Exception {
		MilkingDetail milkDetail = new MilkingDetail();
		milkDetail.setOrgID(rs.getString("ORG_ID"));
		milkDetail.setAnimalTag(rs.getString("ANIMAL_TAG"));
		Date milkingDate = rs.getDate("MILK_DATE");
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		LocalTime localTime = fmt.parseLocalTime(rs.getString("MILK_TIME"));
		milkDetail.setRecordDate(new LocalDate(milkingDate));
		milkDetail.setRecordTime(new LocalTime(localTime));
		milkDetail.setMilkingEventNumber(rs.getShort("SEQ_NBR"));
		milkDetail.setMilkVolume(rs.getFloat("VOL"));
		milkDetail.setVolUnit(rs.getString("VOL_UNIT"));
		milkDetail.setLrValue(rs.getFloat("LR"));
		milkDetail.setFatValue(rs.getFloat("FAT"));
		milkDetail.setToxinValue(rs.getFloat("TOXIN"));		
		milkDetail.setTemperatureInCentigrade(rs.getFloat("TEMP_C"));
		milkDetail.setHumidity(rs.getFloat("HUMIDITY"));
		milkDetail.setComments(rs.getString("COMMENTS"));
		milkDetail.setCreatedBy(new User(rs.getString("CREATED_BY")));
		milkDetail.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		milkDetail.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		milkDetail.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		try {
			milkDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.SEQ_NBR_MONTHLY_AVERAGE, Float.parseFloat(rs.getString("AVERAGE_VOL")));
		} catch (SQLException ex) {
			IMDLogger.log("The column AVERAGE_VOL does not exist in the resultset. This is probably because the query does not have a join with MILK_SEQ_AVG_VW view. This may not be a critical error and may be ignored, but its certainly worth investigating. [ANIMAL_TAG=" + milkDetail.getAnimalTag() +"]", Util.WARNING);
			ex.printStackTrace();
		}
			
		return milkDetail;
	}
	public List<TagVolumeCommentTriplet> addOrEditFarmMilkingEventRecord(FarmMilkingDetailBean milkingEventRecord) {
		
		if (milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().isEmpty())
			return null;
		
		Iterator<TagVolumeCommentTriplet> it = milkingEventRecord.getFarmMilkingEventRecords().iterator();
		List<TagVolumeCommentTriplet> responseList = new ArrayList<TagVolumeCommentTriplet> ();
		TagVolumeCommentTriplet cowMilkInfo = null;
		
		while (it.hasNext()) {
			try {
				cowMilkInfo = it.next();
				MilkingDetailBean cowMilkingDetail = new MilkingDetailBean();
				cowMilkingDetail.setOrgID(milkingEventRecord.getOrgID());
				cowMilkingDetail.setAnimalTag(cowMilkInfo.getTag());
				cowMilkingDetail.setRecordDate(milkingEventRecord.getRecordDate());
				cowMilkingDetail.setRecordTime(milkingEventRecord.getRecordTime());
				cowMilkingDetail.setMilkingEventNumber(milkingEventRecord.getMilkingEventNumber());
				cowMilkingDetail.setMilkVolume(Float.parseFloat(cowMilkInfo.getVolume()));
				cowMilkingDetail.setLrValue(milkingEventRecord.getLrValue());
				cowMilkingDetail.setFatValue(milkingEventRecord.getFatValue());
				cowMilkingDetail.setToxinValue(milkingEventRecord.getToxinValue());
				cowMilkingDetail.setTemperatureInCentigrade(milkingEventRecord.getTemperatureInCentigrade());
				cowMilkingDetail.setHumidity(milkingEventRecord.getHumidity());
				cowMilkingDetail.setComments(cowMilkInfo.getComments());
				int response = insertMilkRecord(cowMilkingDetail);
				if (response == Util.ERROR_CODE.ALREADY_EXISTS) {
					response = updateMilkRecord(cowMilkingDetail);
					if (response == 1)
						cowMilkInfo.setOutcome("EDIT");
					else if (response == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
						cowMilkInfo.setOutcome("ERROR");
					else if (response == Util.ERROR_CODE.UNKNOWN_ERROR)
						cowMilkInfo.setOutcome("ERROR");
					else 
						cowMilkInfo.setOutcome("ERROR");
				} else if (response == Util.ERROR_CODE.SQL_SYNTAX_ERROR) {
					cowMilkInfo.setOutcome("ERROR");
				} else if (response == Util.ERROR_CODE.UNKNOWN_ERROR) {
					cowMilkInfo.setOutcome("ERROR");
				} else {
					cowMilkInfo.setOutcome("OK");
				}
				responseList.add(cowMilkInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (cowMilkInfo != null && cowMilkInfo.getTag() != null && !cowMilkInfo.getTag().trim().isEmpty()) {
					cowMilkInfo.setOutcome("ERROR");
					responseList.add(cowMilkInfo);
				}
			}
		}
		return responseList;		
	}



}



