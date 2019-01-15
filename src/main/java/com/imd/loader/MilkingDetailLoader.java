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
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class MilkingDetailLoader {
	
	
	public int insertMilkRecord(MilkingDetail milkingRecord) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into MILK_LOG (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "DATE,"
				+ "SEQ_NBR,"
				+ "TIME,"
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
			preparedStatement.setString(14, (milkingRecord.getCreatedBy() == null ? null : milkingRecord.getCreatedBy().getUserId()));
			preparedStatement.setString(15, (milkingRecord.getCreatedDTTM() == null ? null :milkingRecord.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(16,(milkingRecord.getUpdatedBy() == null ? null : milkingRecord.getUpdatedBy().getUserId()));
			preparedStatement.setString(17,(milkingRecord.getUpdatedDTTM() == null ? null :milkingRecord.getUpdatedDTTMSQLFormat()));
			recordAdded = preparedStatement.executeUpdate();
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
		String qryString = "DELETE FROM MILK_LOG where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + "' AND DATE='" + Util.getDateInSQLFormart(recordDate) + "'";
		return performDeletion(qryString);
	}
	public int deleteMilkingRecord(String orgID, String animalTag, LocalDate recordDate, int sequenceNbr) {
		String qryString = "DELETE FROM MILK_LOG where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + 
				"' AND DATE='" + Util.getDateInSQLFormart(recordDate) + "' AND SEQ_NBR=" + sequenceNbr;
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
				"and animal_tag=? ORDER BY DATE, SEQ_NBR DESC";
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		return readRecords(milkingSearchParam, qryString, values);
	}

	private ArrayList<MilkingDetail> readRecords(MilkingDetail milkingSearchParam, String qryString, List<String> values)
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
				"and animal_tag=? and DATE=? and TIME=? ";
		List<String> values = new ArrayList<String> ();
		values.add(milkingSearchParam.getOrgID());		
		values.add(milkingSearchParam.getAnimalTag());
		values.add(Util.getDateInSQLFormart(milkingSearchParam.getRecordDate()));
		values.add("" + milkingSearchParam.getMilkingEventNumber());
//		values.add(Util.getTimeInSQLFormart(milkingSearchParam.getRecordTime()));
		return readRecords(milkingSearchParam, qryString, values);
	}	
		
	
	private MilkingDetail getMilkingDetailFromSQLRecord(ResultSet rs) throws Exception {
		MilkingDetail milkDetail = new MilkingDetail();
		milkDetail.setOrgID(rs.getString("ORG_ID"));
		milkDetail.setAnimalTag(rs.getString("ANIMAL_TAG"));
		Date milkingDate = rs.getDate("DATE");
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		LocalTime localTime = fmt.parseLocalTime(rs.getString("TIME"));
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
/*
	public List<Sire> retrieveAISire()  throws Exception {
		ArrayList<Sire> allMatchingValues = new ArrayList<Sire>();
		String qryString = "Select * from LV_SIRE ORDER BY ALIAS";
		List<String> values = new ArrayList<String> ();
		Sire animalValue = null;
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
			String id = rs.getString("ID");
			String breed = rs.getString("BREED");
			String alias = rs.getString("ALIAS");
			String sireSpecification = rs.getString("RECORD_URL");
			String controller = rs.getString("CONTROLLER");
			String sirePhoto = rs.getString("PHOTO_URL");
			animalValue = new Sire("GBL", id,DateTime.now(),true,0d,"PKR");
			animalValue.setBreed(breed);
			animalValue.setAlias(alias);
			animalValue.setSireSpecification(sireSpecification);
			animalValue.setController(controller);
			animalValue.setSirePhoto(sirePhoto);
			animalValue.setCreatedBy(new User(rs.getString("CREATED_BY")));
			animalValue.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
			animalValue.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
			animalValue.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));	    	
	    	allMatchingValues.add(animalValue);
	    }
	    return allMatchingValues;
	}
	
	
	public List<Animal> retrieveDamOrSire(AnimalBean animalBean)  throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		String qryString = "Select a.*,s.RECORD_URL, s.ALIAS SIRE_ALIAS, s.ID, c.SHORT_DESCR as ANIMAL_TYPE " + 
				" from ANIMALS a " + 
				" LEFT OUTER JOIN LV_SIRE s ON a.SIRE_TAG=s.ID " + 
				" LEFT OUTER JOIN LOOKUP_VALUES c 	ON a.TYPE_CD=c.LOOKUP_CD " + 
				" WHERE ( a.ORG_ID=? AND a.GENDER=?)";
		
		List<String> values = new ArrayList<String> ();
		values.add(animalBean.getOrgID());
		values.add(animalBean.getGender()+"");
		IMDLogger.log(qryString,Util.INFO);
		Animal animalValue = null;
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
	    	animalValue = getAnimalFromSQLRecord(rs);
	    	allMatchingValues.add(animalValue);
	    }
	    return allMatchingValues;
	}

	public List<Animal> retrieveActiveLactatingAnimals(AnimalBean animalBean) throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		String qryString = "Select a.*,b.RECORD_URL, b.ALIAS SIRE_ALIAS, b.ID, c.SHORT_DESCR as ANIMAL_TYPE " + 
				"from ANIMALS a " + 
				"	LEFT OUTER JOIN LV_SIRE b " + 
				"	ON a.SIRE_TAG=b.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES c " + 
				"	ON a.TYPE_CD=c.LOOKUP_CD " + 
				" WHERE a.ORG_ID=? AND STATUS='ACTIVE' AND c.ADDITIONAL_FLD2='LACTATING=Y' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String> ();
		values.add(animalBean.getOrgID());		
		Animal animalValue = null;
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
	    	animalValue = getAnimalFromSQLRecord(rs);
	    	allMatchingValues.add(animalValue);
	    }
	    return allMatchingValues;
	}
	*/
}
