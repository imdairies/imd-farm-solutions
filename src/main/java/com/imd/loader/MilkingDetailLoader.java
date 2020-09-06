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

import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.User;
import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.TagVolumeCommentTriplet;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class MilkingDetailLoader {
	
	public static final Integer UNKNOWN_ERROR = -999;
	public static final Integer NO_MILK_RECORD_FOUND_AFTER_PARTURATION = -998;
	public static final Integer NO_PARTURATION_OR_ABORTION_EVENT_FOUND = -997;
	public static final Integer ANIMAL_DOES_NOT_EXIST = -996;
	public static final Integer ANIMAL_IS_NOT_LACTATING = -995;


	public int insertMilkRecord(MilkingDetail milkingRecord)  {
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
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, (milkingRecord.getOrgId() == null ? null : milkingRecord.getOrgId()));
			preparedStatement.setString(index++, (milkingRecord.getAnimalTag() == null ? null : milkingRecord.getAnimalTag()));
			preparedStatement.setString(index++, (milkingRecord.getRecordDate() == null ? null : Util.getDateInSQLFormat(milkingRecord.getRecordDate())));
			preparedStatement.setShort(index++, milkingRecord.getMilkingEventNumber());
			preparedStatement.setString(index++, (milkingRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingRecord.getRecordTime())));
			preparedStatement.setString(index++, milkingRecord.getMilkVolume() == null ? null : milkingRecord.getMilkVolume().toString());
			preparedStatement.setString(index++, Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT).toString());
			preparedStatement.setString(index++,  (milkingRecord.getLrValue() == null ? null : milkingRecord.getLrValue().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getFatValue() == null ? null : milkingRecord.getFatValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getToxinValue() == null ? null : milkingRecord.getToxinValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getTemperatureInCentigrade() == null ? null : milkingRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getHumidity() == null ? null : milkingRecord.getHumidity().toString()));
			preparedStatement.setString(index++, milkingRecord.getComments());
			preparedStatement.setString(index++, milkingRecord.getCreatedBy() == null ? null : milkingRecord.getCreatedBy().getUserId());
			preparedStatement.setString(index++, milkingRecord.getCreatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingRecord.getCreatedDTTMSQLFormat());
			preparedStatement.setString(index++, milkingRecord.getUpdatedBy() == null ? null : milkingRecord.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, milkingRecord.getUpdatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingRecord.getUpdatedDTTMSQLFormat());

			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			if (ex.getMessage().toUpperCase().contains("ORG_ANIMAL_ORG_FK")) {
				recordAdded = Util.ERROR_CODE.ANIMAL_DOES_NOT_EXIST;				
				ex.printStackTrace();
			} else if (ex.getMessage().toUpperCase().contains("DUPLICATE ENTRY")) {
				recordAdded = Util.ERROR_CODE.DUPLICATE_ENTRY;				
			} else {
				recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
				ex.printStackTrace();
			}
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
	
	public int insertFarmMilkRecord(MilkingDetail milkingRecord)  {
		int recordAdded = -1;
		
		String qryString = "insert into imd.FARM_MILK_LOG (ORG_ID,"
				+ "MILK_DATE,"
				+ "SEQ_NBR,"
				+ "MILK_TIME,"
				+ "LR,"
				+ "FAT,"
				+ "TOXIN,"
				+ "TEMP_C,"
				+ "HUMIDITY,"
				+ "PH,"
				+ "COMMENTS,"
				+ "FOR_CALVES_USE,"
				+ "FOR_FARM_USE,"
			    + "FOR_PERSONAL_USE,"
			    + "FOR_FAMILY_USE,"
			    + "FOR_OTHER_USE,"
			    + "FOR_WASTE_ADJ,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, (milkingRecord.getOrgId() == null ? null : milkingRecord.getOrgId()));
			preparedStatement.setString(index++, (milkingRecord.getRecordDate() == null ? null : Util.getDateInSQLFormat(milkingRecord.getRecordDate())));
			preparedStatement.setShort(index++, milkingRecord.getMilkingEventNumber());
			preparedStatement.setString(index++, (milkingRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingRecord.getRecordTime())));
			preparedStatement.setString(index++,  (milkingRecord.getLrValue() == null ? null : milkingRecord.getLrValue().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getFatValue() == null ? null : milkingRecord.getFatValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getToxinValue() == null ? null : milkingRecord.getToxinValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getTemperatureInCentigrade() == null ? null : milkingRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getHumidity() == null ? null : milkingRecord.getHumidity().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getPhValue() == null ? null : milkingRecord.getPhValue().toString()));
			preparedStatement.setString(index++, milkingRecord.getComments());
			preparedStatement.setString(index++,  (milkingRecord.getForCalvesUse() == null ? null : milkingRecord.getForCalvesUse().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getForFarmUse() == null ? null : milkingRecord.getForFarmUse().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getForPersonalUse() == null ? null : milkingRecord.getForPersonalUse().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getForFamilyUse() == null ? null : milkingRecord.getForFamilyUse().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getForOtherUse() == null ? null : milkingRecord.getForOtherUse().toString()));
			preparedStatement.setString(index++,  (milkingRecord.getForWasteAdj() == null ? null : milkingRecord.getForWasteAdj().toString()));
			preparedStatement.setString(index++, milkingRecord.getCreatedBy() == null ? null : milkingRecord.getCreatedBy().getUserId());
			preparedStatement.setString(index++, milkingRecord.getCreatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingRecord.getCreatedDTTMSQLFormat());
			preparedStatement.setString(index++, milkingRecord.getUpdatedBy() == null ? null : milkingRecord.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, milkingRecord.getUpdatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingRecord.getUpdatedDTTMSQLFormat());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			if (ex.getMessage().toUpperCase().contains("DUPLICATE ENTRY")) {
				recordAdded = Util.ERROR_CODE.DUPLICATE_ENTRY;				
			} else {
				recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
			}
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

	public int updateMilkRecord(MilkingDetail milkingRecord)  {
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
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, milkingRecord.getMilkVolume() == null ? null : milkingRecord.getMilkVolume().toString());
			preparedStatement.setString(index++, (milkingRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingRecord.getRecordTime())));
			preparedStatement.setString(index++, Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT).toString());
			preparedStatement.setString(index++, (milkingRecord.getMilkVolume() == null || milkingRecord.getLrValue() == null ? null : milkingRecord.getLrValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getMilkVolume() == null || milkingRecord.getFatValue() == null ? null : milkingRecord.getFatValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getMilkVolume() == null || milkingRecord.getToxinValue() == null ? null : milkingRecord.getToxinValue().toString()));
			preparedStatement.setString(index++, (milkingRecord.getMilkVolume() == null || milkingRecord.getTemperatureInCentigrade() == null ? null : milkingRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(index++, (milkingRecord.getMilkVolume() == null || milkingRecord.getHumidity() == null ? null : milkingRecord.getHumidity().toString()));
			preparedStatement.setString(index++,  milkingRecord.getComments());
			preparedStatement.setString(index++, milkingRecord.getUpdatedBy() == null ? null : milkingRecord.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, milkingRecord.getUpdatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingRecord.getUpdatedDTTMSQLFormat());
			
			preparedStatement.setString(index++, (milkingRecord.getOrgId() == null ? null : milkingRecord.getOrgId()));
			preparedStatement.setString(index++, (milkingRecord.getAnimalTag() == null ? null : milkingRecord.getAnimalTag()));
			preparedStatement.setString(index++, (milkingRecord.getRecordDate() == null ? null : Util.getDateInSQLFormat(milkingRecord.getRecordDate())));
			preparedStatement.setShort(index++, milkingRecord.getMilkingEventNumber());
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
	
	
	public int deleteFarmMilkingRecord(String orgId, LocalDate recordDate, Integer seqNbr) {
		String qryString = "";
		if (orgId == null || orgId.isEmpty() || recordDate == null)
			return 0;
		else if (seqNbr != null)
			qryString = "DELETE FROM imd.FARM_MILK_LOG where ORG_ID='" + orgId + "' AND MILK_DATE = '" + Util.getDateInSQLFormat(recordDate) + "' AND SEQ_NBR='" + seqNbr + "' ";
		else
			qryString = "DELETE FROM imd.FARM_MILK_LOG where ORG_ID='" + orgId + "' AND MILK_DATE = '" + Util.getDateInSQLFormat(recordDate) + "'";
		return performDeletion(qryString);
	}
	public int deleteMilkingRecordOfaDay(String orgId, String animalTag, LocalDate recordDate) {
		String qryString = "DELETE FROM imd.MILK_LOG where ORG_ID='" + orgId + "' AND ANIMAL_TAG = '" + animalTag + "' AND MILK_DATE='" + Util.getDateInSQLFormat(recordDate) + "'";
		return performDeletion(qryString);
	}
	public int deleteAllMilkingRecordOfanAnimal(String orgId, String animalTag) {
		if (orgId == null || orgId.isEmpty() || animalTag == null || animalTag.isEmpty())
			return 0;
		String qryString = "DELETE FROM imd.MILK_LOG where ORG_ID='" + orgId + "' AND ANIMAL_TAG = '" + animalTag + "'";
		return performDeletion(qryString);
	}
	public int deleteOneMilkingRecord(String orgId, String animalTag, LocalDate recordDate, int sequenceNbr) {
		String qryString = "DELETE FROM imd.MILK_LOG where ORG_ID='" + orgId + "' AND ANIMAL_TAG = '" + animalTag + 
				"' AND MILK_DATE='" + Util.getDateInSQLFormat(recordDate) + "' AND SEQ_NBR=" + sequenceNbr;
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
		values.add(milkingSearchParam.getOrgId());		
		values.add(milkingSearchParam.getAnimalTag());
		return readRecords(qryString, values);
	}

	public List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(MilkingDetailBean searchBean) throws Exception {
		return retrieveMonthlyMilkingRecordsOfCow(searchBean.getOrgID(),searchBean.getAnimalTag(), searchBean.getRecordDate().getMonthOfYear(),
				searchBean.getRecordDate().getYear());
	}
	
	private List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(String orgId, String tagNbr, int monthOfYear, int year) throws Exception {
		String qryString = "SELECT *, 0 AS AVERAGE_VOL FROM imd.MILK_LOG " + 
				"where org_id=? " + 
				"and animal_tag=? and MILK_DATE >= CAST(? AS DATE) and MILK_DATE < CAST(? AS DATE) ORDER BY MILK_DATE, SEQ_NBR ASC";
		LocalDate fromDate = new LocalDate(year, monthOfYear, 1);
		List<String> values = new ArrayList<String> ();
		values.add(orgId);
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
		IMDLogger.log(preparedStatement.toString(),Util.ERROR);		
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
			qryString = "SELECT A.*,B.AVERAGE_VOL AS AVERAGE_VOL FROM imd.MILK_LOG A, imd.milk_seq_avg_vw B " + 
					"where A.org_id=? " + 
					"and A.animal_tag=? and A.MILK_DATE=? and A.SEQ_NBR=? AND A.ORG_ID=B.ORG_ID AND A.ANIMAL_TAG=B.ANIMAL_TAG AND A.SEQ_NBR=B.SEQ_NBR AND B.MONTH = MONTH(A.MILK_DATE)";
		else
			qryString = "SELECT A.*, 0 AS AVERAGE_VOL FROM imd.MILK_LOG A " + 
					"where A.org_id=? " + 
					"and A.animal_tag=? and A.MILK_DATE=? and A.SEQ_NBR=? ";
		
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());
		values.add(milkingSearchParam.getAnimalTag());
		values.add(Util.getDateInSQLFormat(milkingSearchParam.getRecordDate()));
		values.add("" + milkingSearchParam.getMilkingEventNumber());
		return readRecords(qryString, values);
	}

	public MilkingDetail[] retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(LocalDate startDate) throws SQLException {
		LocalDate startOfYear = new LocalDate(startDate.getYear(), 1, 1);
		LocalDate endOfTheYear = new LocalDate(startDate.getYear(),12, 31);
		MilkingDetail[] dailyRecordofTheYear = new MilkingDetail[Util.getDaysBetween(endOfTheYear, startOfYear)+1];
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
				emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0.0f);
				emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0.0f);
				dailyRecordofTheYear[dayIndex-1] = emptyRecord;
				lastInsertedRecordDate = emptyRecord.getRecordDate();
			}
			dailyRecordofTheYear[dayIndex-1] = milkDetail;
			lastInsertedRecordDate = milkDetail.getRecordDate();
			dayIndex++;
		}
		for (; dayIndex <= endOfTheYear.getDayOfYear()  ;) {
			MilkingDetail emptyRecord = new MilkingDetail();
			if (lastInsertedRecordDate == null) {
				// case when the year does not have any milking record.
				lastInsertedRecordDate = startOfYear.minusDays(1);
			}
			lastInsertedRecordDate = lastInsertedRecordDate.plusDays(1);
			emptyRecord.setRecordDate(lastInsertedRecordDate);
			emptyRecord.setMilkVolume(0.0f);
			emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0.0f);
			emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0.0f);
			dailyRecordofTheYear[dayIndex-1] = emptyRecord;
			dayIndex++;
		}
		return dailyRecordofTheYear;
	}

	public MilkingDetail[] retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
		LocalDate rangeStartOfYear  = (startDate != null ? new LocalDate(startDate.getYear(), 1,   1) : null);
		LocalDate rangeEndOfTheMonth = (endDate   != null ? (new LocalDate(  endDate.getYear(), endDate.getMonthOfYear() + 1, 1)).minusDays(1) : null);
		List<MilkingDetail> dailyRecordforTheYear = retrieveFarmMilkVolumeForSpecifiedDateRange(rangeStartOfYear, rangeEndOfTheMonth, false);
		if (rangeStartOfYear == null) {
			rangeStartOfYear = dailyRecordforTheYear.get(0).getRecordDate(); 
		}
		if (rangeEndOfTheMonth == null) {
			rangeEndOfTheMonth = dailyRecordforTheYear.get(dailyRecordforTheYear.size()-1).getRecordDate();
		}
		
		MilkingDetail[] dailyRecordofTheYear = new MilkingDetail[Util.getDaysBetween(rangeEndOfTheMonth, rangeStartOfYear)+1];
		
		Iterator<MilkingDetail> it = dailyRecordforTheYear.iterator();
		int dayIndex = 0;
		LocalDate lastInsertedRecordDate = null;
		lastInsertedRecordDate = rangeStartOfYear.minusDays(1);
		while (it.hasNext()) {
			MilkingDetail milkDetail = it.next();
			int dayIndexInDateRange = Util.getDaysBetween(milkDetail.getRecordDate(), rangeStartOfYear);
			for (; dayIndexInDateRange > dayIndex; dayIndex++) {
//				int dateDiffInDays = dayIndexInDateRange - dayIndex;
				MilkingDetail emptyRecord = new MilkingDetail();
				emptyRecord.setRecordDate(lastInsertedRecordDate.plusDays(1));
				emptyRecord.setMilkVolume(0.0f);
				emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0.0f);
				emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0.0f);
				dailyRecordofTheYear[dayIndex] = emptyRecord;
				lastInsertedRecordDate = emptyRecord.getRecordDate();
			}
			dailyRecordofTheYear[dayIndex++] = milkDetail;
			lastInsertedRecordDate = milkDetail.getRecordDate();
		}
		for (; dayIndex < dailyRecordofTheYear.length  ;) {
			MilkingDetail emptyRecord = new MilkingDetail();
			if (lastInsertedRecordDate == null) {
				// case when the year does not have any milking record.
				lastInsertedRecordDate = rangeStartOfYear.minusDays(1);
			}
			lastInsertedRecordDate = lastInsertedRecordDate.plusDays(1);
			emptyRecord.setRecordDate(lastInsertedRecordDate);
			emptyRecord.setMilkVolume(0.0f);
			emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0.0f);
			emptyRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0.0f);
			dailyRecordofTheYear[dayIndex] = emptyRecord;
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
					noRecord.setMilkVolume(0f);
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
				noRecord.setMilkVolume(0f);
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
				+ " FROM imd.MILK_LOG ";
		
		if (startDate != null) {
			if (endDate != null) {
				qryString += " where MILK_DATE >= CAST(? AS DATE) AND MILK_DATE <= CAST(? AS  DATE) group by milk_date order by milk_date asc";
			} else {
				qryString += " where MILK_DATE >= CAST(? AS DATE) group by milk_date order by milk_date asc";
			}
		} else {
			if (endDate != null) {
				qryString += " where MILK_DATE <= CAST(? AS  DATE) group by milk_date order by milk_date asc";
			} else {
				qryString += " group by milk_date order by milk_date asc";
			}
		}
		
		ResultSet rs = null;
		int i = 1;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			if (startDate != null)
				preparedStatement.setString(i++,Util.getDateInSpecifiedFormart(startDate, "yyyy-MM-dd"));
			if (endDate != null)
				preparedStatement.setString(i++,Util.getDateInSpecifiedFormart(endDate, "yyyy-MM-dd"));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    LocalDate dateInProcess = startDate;
		    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");			
		    while (rs.next()) {
		    	if (dateInProcess == null) {
		    		// case when the calling function did not specify any start date.
		    		dateInProcess = dtf.parseLocalDate(rs.getString("MILK_DATE"));
		    		dateInProcess = new LocalDate(dateInProcess.getYear(), dateInProcess.getMonthOfYear(),1);
		    	}
				MilkingDetail milkDetail = new MilkingDetail();
				milkDetail.setRecordDate(new LocalDate(dtf.parseLocalDate(rs.getString("MILK_DATE"))));
				milkDetail.setMilkVolume(rs.getFloat("VOLUME"));
				milkDetail.setLrValue(rs.getFloat("AVG_LR"));
				milkDetail.setFatValue(rs.getFloat("AVG_FAT"));
				milkDetail.setTemperatureInCentigrade(rs.getFloat("AVG_TEMPERATURE"));
				milkDetail.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT,rs.getFloat("MILKED_ANIMALS"));
				milkDetail.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, (float)(milkDetail.getMilkVolume()/rs.getInt("MILKED_ANIMALS")));
				if (shouldIncludeMissingDays) {
					while (dateInProcess.isBefore(milkDetail.getRecordDate())) {
						MilkingDetail noRecord = new MilkingDetail();
						noRecord.setRecordDate(new LocalDate(dateInProcess.getYear(), dateInProcess.getMonthOfYear(), dateInProcess.getDayOfMonth()));
						noRecord.setMilkVolume(0f);
						noRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0f);
						noRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0f);
						allMatchingValues.add(noRecord);
						dateInProcess = dateInProcess.plusDays(1);
						IMDLogger.log(noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
					}
				}
				allMatchingValues.add(milkDetail);
				IMDLogger.log(milkDetail.getRecordDate().toString() + ": " + milkDetail.getMilkVolume(), Util.INFO);
				dateInProcess = dateInProcess.plusDays(1);
		    }
		    
			if (shouldIncludeMissingDays) {
				while (!dateInProcess.isAfter(endDate)) {
					MilkingDetail noRecord = new MilkingDetail();
					noRecord.setRecordDate(new LocalDate(dateInProcess.getYear(), dateInProcess.getMonthOfYear(), dateInProcess.getDayOfMonth()));
					noRecord.setMilkVolume(0f);
					noRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT, 0f);
					noRecord.getAdditionalStatistics().put(Util.MilkingDetailStatistics.DAILY_AVERAGE, 0f);
					allMatchingValues.add(noRecord);
					IMDLogger.log(noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
					dateInProcess = dateInProcess.plusDays(1);
			    }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	    return allMatchingValues;
	}
	
	
	/**
	 * Retrieves data from the startDate till endDate both dates inclusive. The data is grouped by day i.e. each record represents the consolidated milk volume for a given day.
	 * If a day does not have any milking information recorded in the DB then a 0 milking volume is reported for that day.
	 * @param orgId
	 * @param animalTag
	 * @param startDate if set to null the records will be brought from whenever the first milking was recorded for this animal
	 * @param endDate if set to null the records will be brought till whenever the last milking was recorded for this animal
	 * @param shouldIncludeMissingDays If set to true then even if there is no record for a day, the output will have a zero volume for that day. If set to false then the days with no record will not be included in the return array.
	 * @param isDescendingOrder 
	 * @return
	 * @throws SQLException 
	 */
	public List<MilkingDetail> retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(String orgId, String animalTag, LocalDate startDate, LocalDate endDate, boolean shouldIncludeMissingDays) throws SQLException{

		if (startDate == null) {
	    	AnimalLoader animalLdr = new AnimalLoader();
	    	try {
				List<Animal> animal = animalLdr.getAnimalRawInfo(orgId, animalTag);
				if (animal != null && !animal.isEmpty() && animal.get(0).getDateOfBirth() != null)
					startDate = new LocalDate(animal.get(0).getDateOfBirth(), IMDProperties.getServerTimeZone());
				else
					startDate = new LocalDate(1900,1,1);
			} catch (Exception e) {
				startDate = new LocalDate(1900,1,1);
				e.printStackTrace();
			}
	    }
	    
		if (endDate == null)
	    	endDate = LocalDate.now(IMDProperties.getServerTimeZone());
	    

		String qryString = "SELECT sum(vol) AS VOLUME, MILK_DATE " +
				" FROM imd.MILK_LOG where ORG_ID=? AND animal_tag=? " + 
				" AND MILK_DATE >= CAST(? AS DATE) " +
				" AND MILK_DATE <= CAST(? AS  DATE) "+
				" group by milk_date order by milk_date asc";
		ArrayList<MilkingDetail> allMatchingValues = new ArrayList<MilkingDetail>();
		ResultSet rs = null;
		int parameterIndex = 1;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(parameterIndex++,orgId);
			preparedStatement.setString(parameterIndex++,animalTag);
			preparedStatement.setString(parameterIndex++,Util.getDateInSpecifiedFormart(startDate, "yyyy-MM-dd"));
			preparedStatement.setString(parameterIndex++,Util.getDateInSpecifiedFormart(endDate, "yyyy-MM-dd"));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    
		    LocalDate dateInProcess = startDate;
		    while (rs.next()) {
				MilkingDetail milkDetail = new MilkingDetail();
				DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
				milkDetail.setAnimalTag(animalTag);
				milkDetail.setOrgId(orgId);
				milkDetail.setRecordDate(new LocalDate(dtf.parseLocalDate(rs.getString("MILK_DATE"))));
				milkDetail.setMilkVolume(rs.getFloat("VOLUME"));
				if (shouldIncludeMissingDays) {
					while (dateInProcess.isBefore(milkDetail.getRecordDate())) {
						MilkingDetail noRecord = new MilkingDetail();
						noRecord.setRecordDate(new LocalDate(dateInProcess.getYear(), dateInProcess.getMonthOfYear(), dateInProcess.getDayOfMonth()));
						noRecord.setMilkVolume(0f);
						allMatchingValues.add(noRecord);
						dateInProcess = dateInProcess.plusDays(1);
						IMDLogger.log("Adding empty record" + noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
					}
				}
				allMatchingValues.add(milkDetail);
				IMDLogger.log(milkDetail.getRecordDate().toString() + ": " + milkDetail.getMilkVolume(), Util.INFO);
				dateInProcess = dateInProcess.plusDays(1);
		    }
		    
		    //dateInProcess = endDate;
	
			if (shouldIncludeMissingDays) {
				while (!dateInProcess.isAfter(endDate)) {
					MilkingDetail noRecord = new MilkingDetail();
					noRecord.setRecordDate(new LocalDate(dateInProcess.getYear(), dateInProcess.getMonthOfYear(), dateInProcess.getDayOfMonth()));
					noRecord.setMilkVolume(0f);
					allMatchingValues.add(noRecord);
					IMDLogger.log("Adding trailing empty records" + noRecord.getRecordDate().toString() + ": " + noRecord.getMilkVolume(), Util.INFO);
					dateInProcess = dateInProcess.plusDays(1);
			    }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	    return allMatchingValues;
	}	
	
	private MilkingDetail getMilkingDetailFromSQLRecord(ResultSet rs) throws Exception {
		MilkingDetail milkDetail = new MilkingDetail();
		boolean isFarmMilkRecord = false;
		milkDetail.setOrgId(rs.getString("ORG_ID"));
		try {
			milkDetail.setAnimalTag(rs.getString("ANIMAL_TAG"));
		} catch (Exception ex) {
			// case when this query is run on imd.FARM_MILK_LOG table.
			milkDetail.setAnimalTag("N/A");
			isFarmMilkRecord = true;
		}
		Date milkingDate = rs.getDate("MILK_DATE");
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		LocalTime localTime = fmt.parseLocalTime(rs.getString("MILK_TIME"));
		milkDetail.setRecordDate(new LocalDate(milkingDate, IMDProperties.getServerTimeZone()));
		milkDetail.setRecordTime(new LocalTime(localTime));
		milkDetail.setMilkingEventNumber(rs.getShort("SEQ_NBR"));
		milkDetail.setMilkVolume(isFarmMilkRecord || rs.getString("VOL") == null || rs.getString("VOL").isEmpty() ? null : rs.getFloat("VOL"));
		milkDetail.setVolUnit(isFarmMilkRecord ? null : rs.getString("VOL_UNIT"));
		milkDetail.setLrValue(rs.getString("LR") == null || rs.getString("LR").isEmpty() ? null : rs.getFloat("LR"));
		milkDetail.setFatValue(rs.getString("FAT") == null || rs.getString("FAT").isEmpty() ? null : rs.getFloat("FAT"));
		milkDetail.setToxinValue(rs.getString("TOXIN") == null || rs.getString("TOXIN").isEmpty() ? null : rs.getFloat("TOXIN"));		
		milkDetail.setTemperatureInCentigrade(rs.getString("TEMP_C") == null || rs.getString("TEMP_C").isEmpty() ? null : rs.getFloat("TEMP_C"));
		milkDetail.setHumidity(rs.getString("HUMIDITY") == null || rs.getString("HUMIDITY").isEmpty() ? null : rs.getFloat("HUMIDITY"));
		milkDetail.setComments(rs.getString("COMMENTS"));

		// Farm Record Specific Values
		milkDetail.setPhValue(isFarmMilkRecord && rs.getString("PH") != null && !rs.getString("PH").isEmpty() ? rs.getFloat("PH") : null);
		milkDetail.setForCalvesUse(isFarmMilkRecord && rs.getString("FOR_CALVES_USE") != null && !rs.getString("FOR_CALVES_USE").isEmpty() ? rs.getFloat("FOR_CALVES_USE") : null);
		milkDetail.setForFarmUse(isFarmMilkRecord && rs.getString("FOR_FARM_USE") != null && !rs.getString("FOR_FARM_USE").isEmpty() ? rs.getFloat("FOR_FARM_USE") : null);
		milkDetail.setForPersonalUse(isFarmMilkRecord && rs.getString("FOR_PERSONAL_USE") != null && !rs.getString("FOR_PERSONAL_USE").isEmpty() ? rs.getFloat("FOR_PERSONAL_USE") : null);
		milkDetail.setForFamilyUse(isFarmMilkRecord && rs.getString("FOR_FAMILY_USE") != null && !rs.getString("FOR_FAMILY_USE").isEmpty() ? rs.getFloat("FOR_FAMILY_USE") : null);
		milkDetail.setForOtherUse(isFarmMilkRecord && rs.getString("FOR_OTHER_USE") != null && !rs.getString("FOR_OTHER_USE").isEmpty() ? rs.getFloat("FOR_OTHER_USE") : null);
		milkDetail.setForWasteAdj(isFarmMilkRecord && rs.getString("FOR_WASTE_ADJ") != null && !rs.getString("FOR_WASTE_ADJ").isEmpty() ? rs.getFloat("FOR_WASTE_ADJ") : null);
		//
		
		milkDetail.setCreatedBy(new User(rs.getString("CREATED_BY")));
		milkDetail.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		milkDetail.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		milkDetail.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		try {
			if (!isFarmMilkRecord)
				milkDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.SEQ_NBR_MONTHLY_AVERAGE, (rs.getString("AVERAGE_VOL") == null || rs.getString("AVERAGE_VOL").isEmpty() ? 0f :Float.parseFloat(rs.getString("AVERAGE_VOL"))));
		} catch (SQLException ex) {
			IMDLogger.log("The column AVERAGE_VOL does not exist in the resultset. This is probably because the query does not have a join with milk_seq_avg_vw view. This may not be a critical error and may be ignored, but its certainly worth investigating. [ANIMAL_TAG=" + milkDetail.getAnimalTag() +"]", Util.WARNING);
			ex.printStackTrace();
		}
			
		return milkDetail;
	}
	public List<TagVolumeCommentTriplet> addOrEditFarmAnimalsMilkingEventRecords(FarmMilkingDetailBean milkingEventRecord, 
			User createdBy, DateTime createTS, 
			User updatedBy, DateTime updatedTS, boolean donotOverwrite) {
		
		if (milkingEventRecord == null || milkingEventRecord.getFarmMilkingEventRecords() == null || milkingEventRecord.getFarmMilkingEventRecords().isEmpty())
			return null;
		
		Iterator<TagVolumeCommentTriplet> it = milkingEventRecord.getFarmMilkingEventRecords().iterator();
		List<TagVolumeCommentTriplet> responseList = new ArrayList<TagVolumeCommentTriplet> ();
		TagVolumeCommentTriplet cowMilkInfo = null;
		
		while (it.hasNext()) {
			try {
				cowMilkInfo = it.next();
				MilkingDetail cowMilkingDetail = new MilkingDetail();
				cowMilkingDetail.setOrgId(milkingEventRecord.getOrgID());
				cowMilkingDetail.setAnimalTag(cowMilkInfo.getTag());
				cowMilkingDetail.setRecordDate(milkingEventRecord.getRecordDate());
				cowMilkingDetail.setRecordTime(milkingEventRecord.getRecordTime());
				cowMilkingDetail.setMilkingEventNumber(milkingEventRecord.getMilkingEventNumber());
				cowMilkingDetail.setMilkVolume(cowMilkInfo.getVolume() == null || cowMilkInfo.getVolume().isEmpty() ? null : Float.parseFloat(cowMilkInfo.getVolume()));
				cowMilkingDetail.setLrValue(milkingEventRecord.getLrValue());
				cowMilkingDetail.setFatValue(milkingEventRecord.getFatValue());
				cowMilkingDetail.setToxinValue(milkingEventRecord.getToxinValue());
				cowMilkingDetail.setTemperatureInCentigrade(milkingEventRecord.getTemperatureInCentigrade());
				cowMilkingDetail.setHumidity(milkingEventRecord.getHumidity());
				cowMilkingDetail.setComments(cowMilkInfo.getComments());
				cowMilkingDetail.setCreatedBy(createdBy);
				cowMilkingDetail.setCreatedDTTM(createTS);
				cowMilkingDetail.setUpdatedBy(updatedBy);
				cowMilkingDetail.setUpdatedDTTM(updatedTS);
				
				
				int response =  Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
				if (cowMilkingDetail.getMilkVolume() != null)
					// case when a cow was set in lactation now and we update an older farm milking
					// record when this cow was not even lactating. So her previous milking vol will be
					// null and we should not try to add that record in back dates unless the user 
					// deliberately wants an addition which will be indicated by the fact that 
					// he mentions a non null milk volume for that animal.
					response = insertMilkRecord(cowMilkingDetail);
				if (response == Util.ERROR_CODE.DUPLICATE_ENTRY) {
					if (donotOverwrite) {
						// the record already exists and the instructions are not to overwrite an existing record.
						cowMilkInfo.setOutcome(Util.Outcome.NOT_OVERWRITTEN);
					} else {
						response = updateMilkRecord(cowMilkingDetail);
						if (response == 1)
							cowMilkInfo.setOutcome(Util.Outcome.EDIT);
						else if (response == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
							cowMilkInfo.setOutcome(Util.Outcome.ERROR);
						else if (response == Util.ERROR_CODE.UNKNOWN_ERROR)
							cowMilkInfo.setOutcome(Util.Outcome.ERROR);
						else if (response == 0) {
							// case when the cow tag is invalid i.e. FK violation
							cowMilkInfo.setOutcome(Util.Outcome.ERROR);
						} else 
							cowMilkInfo.setOutcome(Util.Outcome.ERROR);
					}
				} else if (response == Util.ERROR_CODE.ANIMAL_DOES_NOT_EXIST) {
					cowMilkInfo.setOutcome(Util.Outcome.ERROR);
				} else if (response == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION) {
					cowMilkInfo.setOutcome(Util.Outcome.ERROR);
				} else if (response == Util.ERROR_CODE.SQL_SYNTAX_ERROR) {
					cowMilkInfo.setOutcome(Util.Outcome.ERROR);
				} else if (response == Util.ERROR_CODE.UNKNOWN_ERROR) {
					cowMilkInfo.setOutcome(Util.Outcome.ERROR);
				} else {
					cowMilkInfo.setOutcome(Util.Outcome.OK);
				}
				responseList.add(cowMilkInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (cowMilkInfo != null && cowMilkInfo.getTag() != null && !cowMilkInfo.getTag().trim().isEmpty()) {
					cowMilkInfo.setOutcome(Util.Outcome.ERROR);
					responseList.add(cowMilkInfo);
				}
			}
		}
		return responseList;		
	}

	public Integer getDaysInMilkingOfCow(String orgId, String animalTag) {
		return getDaysInMilkingOfCow(orgId, animalTag, null);
	}

	public Integer getDaysInMilkingOfCow(String orgId, String animalTag, LocalDate toDateOfDaysInMilking) {
		Integer dim = null;
		DateTime dimWindowStart = null;
		AnimalLoader animalLoader = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		DateTime targetDateTime = toDateOfDaysInMilking == null ? DateTime.now(IMDProperties.getServerTimeZone()) : new DateTime(toDateOfDaysInMilking.getYear(),toDateOfDaysInMilking.getMonthOfYear(),toDateOfDaysInMilking.getDayOfMonth(),0,0, IMDProperties.getServerTimeZone());
		try {
			List<Animal> animals = animalLoader.getAnimalRawInfo(orgId, animalTag);
			if (animals == null || animals.isEmpty()) {
				IMDLogger.log("The animal " + animalTag + " does not exist. Can't calculate its Days in Milking", Util.ERROR);
				dim = ANIMAL_DOES_NOT_EXIST;
//			} else if (!animals.get(0).isLactating()) {
//				dim = ANIMAL_IS_NOT_LACTATING;
//				IMDLogger.log("The animal's type indicates that " + animalTag + " is not lactating. Days in Milking can only be calculated for lactating animals", Util.ERROR);
			} else {
				List<LifecycleEvent> events = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(orgId, animalTag, null, targetDateTime, Util.LifeCycleEvents.PARTURATE, Util.LifeCycleEvents.ABORTION, null,  null,  null, null);
				if (events == null || events.isEmpty()) {
					IMDLogger.log("Did not find any parturation or abortion event for the animal " + animalTag + "; Days in Milking won't be calculated for this animal", Util.ERROR);
					dim = NO_PARTURATION_OR_ABORTION_EVENT_FOUND;
				} else {
					dimWindowStart = events.get(0).getEventTimeStamp();
					dim = Util.getDaysBetween(targetDateTime, dimWindowStart);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			dim = UNKNOWN_ERROR;
		}
		return dim;
	}



	/**
	 * In general its not a good idea to take one day maximum production as the peak of the cow in that lactation. Therefore as a
	 * best practise we take the top Util.DefaultValues.MAX_LPD_AVERAGED_OVER_RECORD_COUNT daily production value and then take the
	 * average of the til.DefaultValues.MAX_LPD_AVERAGED_OVER_RECORD_COUNT days and use that as the maximum daily production value
	 * @param orgId
	 * @param animalTag
	 * @param milkDateStartTS
	 * @param milkDateEndTS
	 * @return
	 */
	public float getMaximumDailyProductionOfCow(String orgId, String animalTag, DateTime milkDateStartTS,
			DateTime milkDateEndTS) {
		
		String qryString = "SELECT " +
				" SUM(VOL) AS LPD, MILK_DATE " + 
				" FROM imd.MILK_LOG " + 
				" WHERE " + 
				" ORG_ID = ? AND ANIMAL_TAG = ? AND " + 
				" MILK_DATE >= ? " + (milkDateEndTS == null ? "" : " AND MILK_DATE <= ? ") + " GROUP BY MILK_DATE ORDER BY LPD DESC";
		
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int i = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++,orgId);
			preparedStatement.setString(i++,animalTag);
			preparedStatement.setString(i++,Util.getDateInSQLFormat(milkDateStartTS));
			if (milkDateEndTS != null)
				preparedStatement.setString(i++,Util.getDateInSQLFormat(milkDateEndTS));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    int averagedOver = 0;
		    float totalMilk = 0;
		    while (rs.next()) {
		    	if (averagedOver >= Util.DefaultValues.MAX_LPD_AVERAGED_OVER_RECORD_COUNT)
		    		break;
		    	else { 
		    		averagedOver++;
		    		totalMilk += rs.getFloat("LPD");
		    	}
		    }
		    return (averagedOver == 0 ? 0 : (float)((float)totalMilk / (float)averagedOver));
		} catch (SQLException e) {
			IMDLogger.log("Exception occurred while retrieving maximum daily production of the animal " + animalTag, Util.ERROR);
			e.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	public float getTotalProductionOfCow(String orgId, String animalTag, DateTime milkDateStartTS, DateTime milkDateEndTS) {
		
		String qryString = "SELECT " +
				" SUM(VOL) AS TOTAL_PRODUCTION " + 
				" FROM imd.MILK_LOG " + 
				" WHERE " + 
				" ORG_ID = ? AND ANIMAL_TAG = ? AND " + 
				" MILK_DATE >= ? AND MILK_DATE <= ? ";
		
		float totalMilk = 0;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int i = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++,orgId);
			preparedStatement.setString(i++,animalTag);
			preparedStatement.setString(i++,Util.getDateInSQLFormat(milkDateStartTS));
			preparedStatement.setString(i++,Util.getDateInSQLFormat(milkDateEndTS));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	totalMilk = rs.getFloat("TOTAL_PRODUCTION");
		    }
		} catch (SQLException e) {
			IMDLogger.log("Exception occurred while retrieving maximum daily production of the animal " + animalTag, Util.ERROR);
			e.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return totalMilk;
	}
	public float getDurationInMilking(String orgId, String animalTag, DateTime searchWindowStartTS, DateTime searchWindowEndTS, int durationType) {
		
		String qryString = "SELECT " +
				" MIN(MILK_DATE) as MILK_START, MAX(MILK_DATE) AS MILK_END " + 
				" FROM imd.MILK_LOG " + 
				" WHERE " + 
				" ORG_ID = ? AND ANIMAL_TAG = ? AND " + 
				" MILK_DATE >= ? AND MILK_DATE <= ? ";
		
		float duration = 0;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int i = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++,orgId);
			preparedStatement.setString(i++,animalTag);
			preparedStatement.setString(i++,Util.getDateInSQLFormat(searchWindowStartTS));
			preparedStatement.setString(i++,Util.getDateInSQLFormat(searchWindowEndTS));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	String firstDateOfMilking = rs.getString("MILK_START");
		    	String lastDateOfMilking = rs.getString("MILK_END");
		    	if (firstDateOfMilking != null && !firstDateOfMilking.isEmpty() && 
		    			lastDateOfMilking != null && !lastDateOfMilking.isEmpty()) {
		    		LocalDate startDate = new LocalDate(firstDateOfMilking, IMDProperties.getServerTimeZone());
		    		LocalDate endDate = new LocalDate(lastDateOfMilking, IMDProperties.getServerTimeZone());
		    		if (durationType == Util.DurationType.DAYS) {
		    			duration = Util.getDaysBetween(endDate, startDate)+1;
		    		} else if (durationType == Util.DurationType.MONTHS) {
		    			duration = (float)(((float)Util.getDaysBetween(endDate, startDate)+1)/30.5f);
		    		} else if (durationType == Util.DurationType.YEARS) {
		    			duration = (float)(((float)Util.getDaysBetween(endDate, startDate)+1)/365f);
		    		} else {
		    			duration = Util.getDaysBetween(endDate, startDate)+1;		    			
		    		}
		    	}
		    }
		} catch (SQLException e) {
			IMDLogger.log("Exception occurred while retrieving duration in milking for the animal " + animalTag, Util.ERROR);
			e.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return duration;
	}
	
	public MilkingDetail getLatestMilkingEvent(String orgId) {
		String qryString = "SELECT "
				+ " MILK_DATE, SEQ_NBR "
				+ " FROM imd.MILK_LOG WHERE ORG_ID=? "
				+ " GROUP BY MILK_DATE, SEQ_NBR "
				+ " HAVING MILK_DATE= (SELECT MAX(MILK_DATE) FROM imd.MILK_LOG) ORDER BY SEQ_NBR DESC";
				
		MilkingDetail milkRecord = new MilkingDetail();
		milkRecord.setRecordDate(LocalDate.now(IMDProperties.getServerTimeZone()));
		milkRecord.setMilkingEventNumber(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		LocalTime recordTime = fmt.parseLocalTime(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY_TIME);
		milkRecord.setRecordTime(recordTime);
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			int paramCount = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(paramCount++, orgId);
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
				Date milkingDate = rs.getDate("MILK_DATE");
				milkRecord.setRecordDate(new LocalDate(milkingDate, IMDProperties.getServerTimeZone()));
				milkRecord.setMilkingEventNumber(rs.getShort("SEQ_NBR"));
				
				if (milkRecord.getMilkingEventNumber() == Util.DefaultValues.THIRD_MILKING_EVENT_OF_THE_DAY) {
					recordTime = fmt.parseLocalTime(Util.DefaultValues.THIRD_MILKING_EVENT_OF_THE_DAY_TIME);
				} else if (milkRecord.getMilkingEventNumber() == Util.DefaultValues.SECOND_MILKING_EVENT_OF_THE_DAY) {
					recordTime = fmt.parseLocalTime(Util.DefaultValues.SECOND_MILKING_EVENT_OF_THE_DAY_TIME);
				} else {
					recordTime = fmt.parseLocalTime(Util.DefaultValues.FIRST_MILKING_EVENT_OF_THE_DAY_TIME);
				}
				milkRecord.setRecordTime(recordTime);
				break;
		    }
		} catch (SQLException e) {
			IMDLogger.log("Exception occurred while retrieving latest milking event", Util.ERROR);
			e.printStackTrace();
		} finally {
		    try {
		    	if (rs != null && !rs.isClosed()) {
		    		rs.close();
		    	}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return milkRecord;		
	}
 
	public String addOrUpdateFarmMilkingRecord(MilkingDetail farmMilkingDetail, boolean donotOverwrite) {
		if (farmMilkingDetail == null)
			return null;
		try {
			List<MilkingDetail> farmRecords =  retrieveFarmMilkRecord(farmMilkingDetail);
			if (farmRecords == null || farmRecords.isEmpty()) {
				int insertedRecordCount = insertFarmMilkRecord(farmMilkingDetail);
				if (insertedRecordCount != 1) {
					IMDLogger.log("Could not insert the following Farm Milking Record\n " + farmMilkingDetail.toString(), Util.ERROR);
					return Util.Outcome.ERROR;
				} else
					return Util.Outcome.ADDED;
			} else if (donotOverwrite){
				IMDLogger.log("The following Farm Milking Record already exists. It will NOT be overwritten\n" + farmRecords.get(0).toString(), Util.ERROR);
				return Util.Outcome.NOT_OVERWRITTEN;
			} else {
				// record already exists and we should update it.
				int updatedRecordCount = updateFarmMilkRecord(farmMilkingDetail);
				if (updatedRecordCount == 1)
					return Util.Outcome.EDIT;
				else {
					IMDLogger.log("The following Farm Milking Record already exists. We attempted to update it but ran into an error because of which the record could NOT be updated\n" + farmRecords.get(0).toString(), Util.ERROR);
					return Util.Outcome.ERROR;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return Util.Outcome.ERROR;
		}
	}
	
	private int updateFarmMilkRecord(MilkingDetail milkingEventRecord) {
		int recordAdded = -1;
		String qryString = "UPDATE imd.FARM_MILK_LOG SET "
				+ "MILK_TIME=? ,"
				+ "LR=? ,"
				+ "FAT=? ,"
				+ "TOXIN=? ,"
				+ "TEMP_C=? ,"
				+ "HUMIDITY=? ,"
				+ "PH=? ,"
				+ "COMMENTS=? ,"
				+ "FOR_CALVES_USE =? , "
				+ "FOR_FARM_USE =? ,"
				+ "FOR_PERSONAL_USE =? ,"
				+ "FOR_FAMILY_USE =? ,"
				+ "FOR_OTHER_USE =? ,"
				+ "FOR_WASTE_ADJ =? ,"
				+ "UPDATED_BY=? ,"
				+ "UPDATED_DTTM=? "
				+ " WHERE ORG_ID=? AND MILK_DATE=? AND SEQ_NBR=?";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, (milkingEventRecord.getRecordTime() == null ? null : Util.getTimeInSQLFormart(milkingEventRecord.getRecordTime())));
			preparedStatement.setString(index++,  (milkingEventRecord.getLrValue() == null ? null : milkingEventRecord.getLrValue().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getFatValue() == null ? null : milkingEventRecord.getFatValue().toString()));
			preparedStatement.setString(index++, (milkingEventRecord.getToxinValue() == null ? null : milkingEventRecord.getToxinValue().toString()));
			preparedStatement.setString(index++, (milkingEventRecord.getTemperatureInCentigrade() == null ? null : milkingEventRecord.getTemperatureInCentigrade().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getHumidity() == null ? null : milkingEventRecord.getHumidity().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getPhValue() == null ? null : milkingEventRecord.getPhValue().toString()));
			preparedStatement.setString(index++, milkingEventRecord.getComments());
			preparedStatement.setString(index++,  (milkingEventRecord.getForCalvesUse() == null ? null : milkingEventRecord.getForCalvesUse().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getForFarmUse() == null ? null : milkingEventRecord.getForFarmUse().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getForPersonalUse() == null ? null : milkingEventRecord.getForPersonalUse().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getForFamilyUse() == null ? null : milkingEventRecord.getForFamilyUse().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getForOtherUse() == null ? null : milkingEventRecord.getForOtherUse().toString()));
			preparedStatement.setString(index++,  (milkingEventRecord.getForWasteAdj() == null ? null : milkingEventRecord.getForWasteAdj().toString()));
			preparedStatement.setString(index++, milkingEventRecord.getUpdatedBy() == null ? null : milkingEventRecord.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, milkingEventRecord.getUpdatedDTTM() == null ? Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())) :milkingEventRecord.getUpdatedDTTMSQLFormat());
			preparedStatement.setString(index++, (milkingEventRecord.getOrgId() == null ? null : milkingEventRecord.getOrgId()));
			preparedStatement.setString(index++, (milkingEventRecord.getRecordDate() == null ? null : Util.getDateInSQLFormat(milkingEventRecord.getRecordDate())));
			preparedStatement.setShort(index++, milkingEventRecord.getMilkingEventNumber());
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

	public List<MilkingDetail> retrieveFarmMilkRecord(MilkingDetail milkingEventRecord) throws Exception {
		String qryString = "SELECT * FROM imd.FARM_MILK_LOG A ";
		String whereClause = "";
		List<String> values = new ArrayList<String> ();
		if (milkingEventRecord == null)
			return new ArrayList<MilkingDetail>();

		if (milkingEventRecord.getOrgId() != null && !milkingEventRecord.getOrgId().isEmpty()) {
			whereClause += " A.ORG_ID = ? ";
			values.add(milkingEventRecord.getOrgId());
		}

		if (milkingEventRecord.getRecordDate() != null) {
			whereClause += whereClause.isEmpty() ? "" : " AND " +" A.MILK_DATE = ? ";
			values.add(Util.getDateInSQLFormat(milkingEventRecord.getRecordDate()));
		}

		if (milkingEventRecord.getMilkingEventNumber() > 0) {
			whereClause += whereClause.isEmpty() ? "" : " AND " +" A.SEQ_NBR = ? ";
			values.add("" + milkingEventRecord.getMilkingEventNumber());
		}

		if (!whereClause.isEmpty())
			whereClause = " WHERE " + whereClause;
		qryString += whereClause;
		return readRecords(qryString, values);
	}

	public int deleteFarmMilkingRecordsWithNoChildRecords(String orgId) {
		String qryString = "DELETE FROM imd.FARM_MILK_LOG WHERE ORG_ID='" + orgId + "' AND MILK_DATE NOT IN (SELECT MILK_DATE FROM imd.MILK_LOG WHERE ORG_ID='" + orgId + "')";
		return performDeletion(qryString);
	}

	public List<MilkingDetail> retrieveFarmMilkRecord(MilkingDetailBean selectedDateSearchBean) throws Exception {
		MilkingDetail milkingDetail = new MilkingDetail(selectedDateSearchBean);
		return retrieveFarmMilkRecord(milkingDetail);
	}
}



