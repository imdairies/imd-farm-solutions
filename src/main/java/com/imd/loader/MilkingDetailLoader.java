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
import com.imd.services.bean.MilkingDetailBean;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class MilkingDetailLoader {
	
	
	public int insertMilkRecord(MilkingDetailBean milkingRecord) throws SQLException {
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
			preparedStatement.setString(15, LocalDate.now().toString());
			preparedStatement.setString(16, (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID));
			preparedStatement.setString(17, LocalDate.now().toString());
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
		String qryString = "SELECT * FROM imd.MILK_LOG " + 
				"where org_id=? " + 
				"and animal_tag=? ORDER BY MILK_DATE, SEQ_NBR DESC";
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		return readRecords(qryString, values);
	}

//	public List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(MilkingDetail searchBean) throws Exception {
//		return retrieveMonthlyMilkingRecordsOfCow(searchBean.getOrgID(),searchBean.getAnimalTag(), searchBean.getRecordDate().getMonthOfYear(),
//				searchBean.getRecordDate().getYear());
//	}
	public List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(MilkingDetailBean searchBean) throws Exception {
		return retrieveMonthlyMilkingRecordsOfCow(searchBean.getOrgID(),searchBean.getAnimalTag(), searchBean.getRecordDate().getMonthOfYear(),
				searchBean.getRecordDate().getYear());
	}
	
	private List<MilkingDetail> retrieveMonthlyMilkingRecordsOfCow(String orgID, String tagNbr, int monthOfYear, int year) throws Exception {
		String qryString = "SELECT * FROM imd.MILK_LOG " + 
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
	
	
	public List<MilkingDetail> retrieveSingleMilkingRecordsOfCow(MilkingDetail milkingSearchParam) throws Exception {
		String qryString = "SELECT * FROM imd.MILK_LOG " + 
				"where org_id=? " + 
				"and animal_tag=? and MILK_DATE=? and MILK_TIME=? ";
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		values.add(Util.getDateInSQLFormart(milkingSearchParam.getRecordDate()));
		values.add("" + milkingSearchParam.getMilkingEventNumber());
//		values.add(Util.getTimeInSQLFormart(milkingSearchParam.getRecordTime()));
		return readRecords(qryString, values);
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
		return milkDetail;
	}
}
