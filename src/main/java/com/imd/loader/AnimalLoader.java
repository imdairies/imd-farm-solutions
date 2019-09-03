package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.imd.dto.Animal;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.SireBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class AnimalLoader {
	
	
	public static final String LACTATING_INDICATOR = "LACTATING=Y";
	public static final String PREGNANT_INDICATOR = "PREGNANT=Y";
	public static final String HEIFER_INDICATOR = "HEIFER=Y";
	public static final String DRY_INDICATOR = "DRY=Y";
	public static final String INSEMINATED_INDICATOR = "INSEMINATED=Y";
	
	public int insertAnimal(Animal animal) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into ANIMALS (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "ALIAS,"
				+ "BREED,"
				+ "TYPE_CD,"
				+ "DOB,"
				+ "DOB_ACCURACY_IND,"
				+ "HERD_JOINING_DTTM,"
				+ "HERD_LEAVING_DTTM,"
				+ "GENDER,"
				+ "DAM_TAG,"
				+ "SIRE_TAG,"
				+ "AI_IND,"
				+ "FRONT_POSE,"
				+ "BACK_POSE,"
				+ "RIGHT_POSE,"
				+ "LEFT_POSE,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++,  (animal.getOrgID() == null ? null : animal.getOrgID()));
			preparedStatement.setString(index++,  (animal.getAnimalTag() == null ? null : animal.getAnimalTag()));
			preparedStatement.setString(index++,  (animal.getAlias() == null ? null : animal.getAlias()));
			preparedStatement.setString(index++,  (animal.getBreed() == null ? null : animal.getBreed()));
			preparedStatement.setString(index++,  (animal.getAnimalTypeCD() == null ? (animal.getAnimalType() == null ? null : animal.getAnimalType()) : animal.getAnimalTypeCD()));
			preparedStatement.setString(index++,  (animal.getDateOfBirth() == null ? null : Util.getDateInSQLFormart(animal.getDateOfBirth())));
			preparedStatement.setString(index++,  (animal.isDateOfBirthEstimated() ? "N" : "Y"));
			preparedStatement.setString(index++,  (animal.getHerdJoiningDate() == null ? null :Util.getDateInSQLFormart(animal.getHerdJoiningDate())));
			preparedStatement.setString(index++,  (animal.getHerdLeavingDate() == null ? null :Util.getDateInSQLFormart(animal.getHerdLeavingDate())));
			preparedStatement.setString(index++, (animal.getGender() == ' ' ? null : animal.getGender() + ""));
			preparedStatement.setString(index++, (animal.getAnimalDam() == null ? null : animal.getAnimalDam().getAnimalTag()));
			preparedStatement.setString(index++, (animal.getAnimalSire() == null ? null : animal.getAnimalSire().getAnimalTag()));
			preparedStatement.setString(index++, (animal.isBornThroughAI() ? "Y" : "N"));
			preparedStatement.setString(index++, (animal.getFrontSideImageURL() == null ? null : animal.getFrontSideImageURL().toString()));
			preparedStatement.setString(index++, (animal.getBackSideImageURL() == null ? null : animal.getBackSideImageURL().toString()));
			preparedStatement.setString(index++, (animal.getRightSideImageURL() == null ? null : animal.getRightSideImageURL().toString()));
			preparedStatement.setString(index++, (animal.getLeftSideImageURL() == null ? null : animal.getLeftSideImageURL().toString()));
			preparedStatement.setString(index++, (animal.getCreatedBy() == null ? null : animal.getCreatedBy().getUserId()));
			preparedStatement.setString(index++, (animal.getCreatedDTTM() == null ? null :animal.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(index++,(animal.getUpdatedBy() == null ? null : animal.getUpdatedBy().getUserId()));
			preparedStatement.setString(index++,(animal.getUpdatedDTTM() == null ? null :animal.getUpdatedDTTMSQLFormat()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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
	public int insertSire(SireBean sireBean, String createdByUser, DateTime createdDttm, String updatedByUser, DateTime updatedDttm) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into LV_SIRE ("
				+ "ID,"
				+ "BREED,"
				+ "ALIAS,"
				+ "SEMEN_IND,"
				+ "RECORD_URL,"
				+ "PHOTO_URL,"
				+ "CONTROLLER,"
				+ "SEMEN_COMPANY,"
				+ "CURRENT_SEX_LIST_PRICE_PKR,"
				+ "DISCOUNT_SEX_PERCENTAGE,"
				+ "CURRENT_CONV_LIST_PRICE_PKR,"
				+ "DISCOUNT_CONV_PERCENTAGE,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, sireBean.getAnimalTag());
			preparedStatement.setString(2, (sireBean.getBreed() == null ? null : sireBean.getBreed()));
			preparedStatement.setString(3, (sireBean.getAlias() == null ? null : sireBean.getAlias()));
			preparedStatement.setString(4, (sireBean.getSemenInd() == null ? null : sireBean.getSemenInd()));
			preparedStatement.setString(5, (sireBean.getRecordURL() == null ? null : sireBean.getRecordURL()));
			preparedStatement.setString(6, (sireBean.getPhotoURL() == null ? null : sireBean.getPhotoURL()));
			preparedStatement.setString(7, (sireBean.getController() == null ? null : sireBean.getController()));
			preparedStatement.setString(8, (sireBean.getSemenCompany() == null ? null : sireBean.getSemenCompany()));
			preparedStatement.setFloat(9, sireBean.getCurrentSexListPrice());
			preparedStatement.setFloat(10, sireBean.getDiscountSexPercentage());
			preparedStatement.setFloat(11, sireBean.getCurrentConventionalListPrice());
			preparedStatement.setFloat(12, sireBean.getDiscountConventionalPercentage());
			preparedStatement.setString(13, (createdByUser == null ? null : createdByUser));
			preparedStatement.setString(14, (createdDttm == null ? null : Util.getDateInSQLFormart(createdDttm)));
			preparedStatement.setString(15,(updatedByUser == null ? null : updatedByUser));
			preparedStatement.setString(16,(updatedDttm == null ? null : Util.getDateInSQLFormart(updatedDttm)));
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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
	public int deleteAnimal(String orgID, String animalTag) {
		String qryString = "DELETE FROM ANIMALS where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + "'";
		int result = -1;
		Statement st = null;
		Connection conn = DBManager.getDBConnection();
		try {
			st = conn.createStatement();
			IMDLogger.log(qryString, Util.INFO);
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
	public int deleteSire(String animalTag) {
		String qryString = "DELETE FROM LV_SIRE where ID = '" + animalTag + "'";
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
	
	public List<Animal> getAnimalRawInfo(String orgID, String animalTag) throws Exception {
		AnimalBean aBean = new AnimalBean();
		aBean.setOrgID(orgID);
		aBean.setAnimalTag(animalTag);
		return getAnimalRawInfo(aBean);
	}
	public List<Animal> getAnimalRawInfo(Animal animal) throws Exception {
		AnimalBean aBean = new AnimalBean();
		aBean.setOrgID(animal.getOrgID());
		aBean.setAnimalTag(animal.getAnimalTag());
		return getAnimalRawInfo(aBean);
	}
	
	public List<Animal> getAnimalRawInfo(AnimalBean animalBean) throws Exception {
		boolean isWildCardSearch = false;
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		String qryString =  "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR  " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.CATEGORY_CD=?)" + 
				" WHERE ( A.ORG_ID=? ";		
		
		List<String> values = new ArrayList<String> ();
		values.add(Util.LookupValues.LCYCL);
		values.add(animalBean.getOrgID());		
		if (animalBean.getAnimalTag() != null && !animalBean.getAnimalTag().trim().isEmpty()) {
			qryString +=  " AND A.ANIMAL_TAG " + (isWildCardSearch ?  " LIKE ? " : " = ?");
			values.add(animalBean.getAnimalTag());
			if (animalBean.getAnimalType() != null && !animalBean.getAnimalType().trim().isEmpty()) {
				qryString +=  " AND A.TYPE_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");				
				values.add(animalBean.getAnimalType());
			}
		} else if (animalBean.getAnimalType() != null && !animalBean.getAnimalType().trim().isEmpty()) {
			qryString +=  " AND A.TYPE_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");				
			values.add(animalBean.getAnimalType());
		}
		if (animalBean.getActiveOnly()) {
			qryString +=  " AND (A.HERD_JOINING_DTTM IS NOT NULL AND A.HERD_LEAVING_DTTM IS NULL))  ORDER BY A.ANIMAL_TAG";
		} else {
			qryString += ") ORDER BY A.ANIMAL_TAG";
		}
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
	
	public List<Animal> retrieveMatchingAnimals(AnimalBean animalBean) throws Exception {
		return retrieveMatchingAnimals(animalBean,true,null);
	}
	
	public List<Animal> retrieveMatchingAnimals(AnimalBean animalBean, boolean isWildCardSearch, String additionalQuery) throws Exception {		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();

		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.CATEGORY_CD=?)" +   
				" WHERE ( A.ORG_ID=? ";
		
		
		List<String> values = new ArrayList<String> ();
		values.add(Util.LookupValues.LCYCL);
		values.add(animalBean.getOrgID());		
		if (animalBean.getAnimalTag() != null && !animalBean.getAnimalTag().trim().isEmpty()) {
			qryString +=  " AND ANIMAL_TAG " + (isWildCardSearch ?  " LIKE ? " : " = ?");
			values.add(animalBean.getAnimalTag());
			if (animalBean.getAnimalType() != null && !animalBean.getAnimalType().trim().isEmpty()) {
				qryString +=  " AND TYPE_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");				
				values.add(animalBean.getAnimalType());
			}
		} else if (animalBean.getAnimalType() != null && !animalBean.getAnimalType().trim().isEmpty()) {
			qryString +=  " AND TYPE_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");				
			values.add(animalBean.getAnimalType());
		}
		if (animalBean.getGender() == 'M' || animalBean.getGender() == 'F') {
			qryString +=  " AND GENDER='" + animalBean.getGender() + "' " ;
			
		}
		if (additionalQuery != null && !additionalQuery.trim().isEmpty()) 
			qryString += " AND " + additionalQuery;
		if (animalBean.getActiveOnly()) {
			qryString +=  " AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL)) ORDER BY ANIMAL_TAG";
		} else {
			qryString += ") ORDER BY ANIMAL_TAG";
		}
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
	

	/**
	 * Retrieves animals that should be shown on the insemination dashboard. These will include unjointed heifers,
	 * inseminated heifers, pregnant hiefers and all adult cows - basically all active female animals older that 9 months.
	 * @param orgID
	 * @return
	 * @throws Exception
	 */
	public List<Animal> retrieveAdultFemaleCows(String orgID, int ageInDays) throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		
		
		String qryString = "Select A.*, \"\" as RECORD_URL, \"\" AS SIRE_ALIAS, \"\" as ID,  B.SHORT_DESCR as ANIMAL_TYPE, B.ADDITIONAL_FLD1 AS STATUS_INDICATOR, C.LACTATION_NBR from imd.ANIMALS A " +
		" left outer join imd.LOOKUP_VALUES B on A.TYPE_CD = B.LOOKUP_CD  AND CATEGORY_CD='" + Util.LookupValues.LCYCL+ "' " +
        " left outer join (SELECT e.ANIMAL_TAG, COUNT(*) AS LACTATION_NBR FROM imd.LIFECYCLE_EVENTS e WHERE  (e.EVENT_CD='PARTURATE' OR e.EVENT_CD='ABORTION') GROUP BY e.ANIMAL_TAG) C  " +
        " on C.animal_tag=A.animal_tag  " +
        " WHERE A.ORG_ID=? AND GENDER = 'F' and (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) and DOB <= ?" ;
		
//		String qryString = "Select A.*, \" \" as RECORD_URL, \" \"  SIRE_ALIAS, \" \" as ID, B.SHORT_DESCR as ANIMAL_TYPE, B.ADDITIONAL_FLD1 AS STATUS_INDICATOR from imd.ANIMALS a, "
//				+ " imd.LOOKUP_VALUES B WHERE "
//				+ " A.ORG_ID=? AND GENDER = 'F' and STATUS='ACTIVE' and DOB <= ? and A.TYPE_CD = B.LOOKUP_CD ORDER BY ANIMAL_TAG";
		
		Animal animalValue = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		preparedStatement = conn.prepareStatement(qryString);
		preparedStatement.setString(1,orgID);
		preparedStatement.setString(2,DateTime.now().minusDays(ageInDays).toString());		
		IMDLogger.log(preparedStatement.toString(), Util.INFO);
	    rs = preparedStatement.executeQuery();
	    while (rs.next()) {
	    	animalValue = getAnimalFromSQLRecord(rs);
	    	allMatchingValues.add(animalValue);
    	}
	    return allMatchingValues;
	}	
	
	
	public List<Animal> retrieveActiveDams(String orgID) throws Exception {
		AnimalBean animalBean = new AnimalBean();
		animalBean.setOrgID(orgID);
		animalBean.setActiveOnly(true);
		animalBean.setGender('F');
		String additionalQuery = " TYPE_CD NOT IN ('FEMALECALF', 'HEIFER') ";
		return retrieveMatchingAnimals(animalBean, false, additionalQuery);
	}	
	
	
	public List<Animal> retrieveActiveAnimals(String orgID) throws Exception {
		AnimalBean animalBean = new AnimalBean();
		animalBean.setOrgID(orgID);
		animalBean.setActiveOnly(true);
		return retrieveMatchingAnimals(animalBean);
	}	

	public List<Animal> retrieveAllAnimals(String orgID) throws Exception {
		AnimalBean animalBean = new AnimalBean();
		animalBean.setOrgID(orgID);
		animalBean.setActiveOnly(false);
		return retrieveMatchingAnimals(animalBean);
	}	
	
	private Animal getAnimalFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		String ind = rs.getString("DOB_ACCURACY_IND");
		DateTime dob = (rs.getTimestamp("DOB") != null ? new DateTime(rs.getTimestamp("DOB")) : null);
		DateTime herdJoiningDttm =  (rs.getTimestamp("HERD_JOINING_DTTM") != null ? new DateTime(rs.getTimestamp("HERD_JOINING_DTTM")) : null);
		DateTime herdLeavingDttm =  (rs.getTimestamp("HERD_LEAVING_DTTM") != null ? new DateTime(rs.getTimestamp("HERD_LEAVING_DTTM")) : null);
		String gender = rs.getString("GENDER");
		String typeCD = rs.getString("TYPE_CD");
		String typeDescr = rs.getString("ANIMAL_TYPE");
		String damTag = rs.getString("DAM_TAG");
		String sireTag = rs.getString("SIRE_TAG");
		String breed = rs.getString("BREED");
		String orgId = rs.getString("ORG_ID");
		String alias = rs.getString("ALIAS");
		String frontPoseImage = rs.getString("FRONT_POSE");
		String backPoseImage = rs.getString("BACK_POSE");
		String rightPoseImage = rs.getString("RIGHT_POSE");
		String leftPoseImage = rs.getString("LEFT_POSE");
		String aiInd = rs.getString("AI_IND");
		String statusIndicators = null;
		int lactationNbr = 0;
		
		try {
			statusIndicators = rs.getString("STATUS_INDICATOR");
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		try {
			lactationNbr = rs.getInt("LACTATION_NBR");
		} catch (Exception ex) {
			//ex.printStackTrace();
		}	
		boolean isEstimated = (ind == null || ind.trim().isEmpty() || ind.trim().equalsIgnoreCase("N") ? true : false);
		Animal animal;
		if (gender != null && gender.trim().equalsIgnoreCase("F")) {
			animal = new Dam(orgId, rs.getString("ANIMAL_TAG"),dob,isEstimated,0d,"PKR");
		} else {
			animal = new Sire(orgId, rs.getString("ANIMAL_TAG"),dob,isEstimated,0d,"PKR");
		}
		
		if (damTag != null) {
			animal.setAnimalDam(new Dam(damTag));
		}
		if (sireTag != null) {
			animal.setAnimalSire(new Sire(sireTag));
			animal.getAnimalSire().setSireSpecification(rs.getString("RECORD_URL"));
			animal.getAnimalSire().setAlias(rs.getString("SIRE_ALIAS"));
		}
		animal.setAlias(alias);
		animal.setAnimalType(typeDescr);
		animal.setBreed(breed);
		animal.setAnimalTypeCD(typeCD);
		animal.setStatusIndicators(statusIndicators);
		animal.setParturationCount(lactationNbr);
		animal.setFrontSideImageURL(frontPoseImage);
		animal.setBackSideImageURL(backPoseImage);
		animal.setRightSideImageURL(rightPoseImage);
		animal.setLeftSideImageURL(leftPoseImage);
		animal.setBornThroughAI((aiInd != null && aiInd.equalsIgnoreCase("Y")) ? true : false);
		animal.setHerdJoiningDate(herdJoiningDttm);
		animal.setHerdLeavingDate(herdLeavingDttm);
		animal.setCreatedBy(new User(rs.getString("CREATED_BY")));
		animal.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		animal.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animal.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return animal;
	}

	public List<Sire> retrieveAISire()  throws Exception {
		ArrayList<Sire> allMatchingValues = new ArrayList<Sire>();
		String qryString = "Select * from LV_SIRE A " + 
				"left outer join sire_usage_stats_vw B on A.id=B.code " + 
				"ORDER BY A.CONTROLLER,A.ALIAS";
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
			animalValue = getSireFromSQLRecord(rs);	    	
	    	allMatchingValues.add(animalValue);
	    }
	    return allMatchingValues;
	}
	
	
	public List<Animal> retrieveDamOrSire(AnimalBean animalBean)  throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		String qryString = "Select A.*,s.RECORD_URL, s.ALIAS SIRE_ALIAS, s.ID, C.SHORT_DESCR as ANIMAL_TYPE " + 
				" from ANIMALS A " + 
				" LEFT OUTER JOIN LV_SIRE s ON A.SIRE_TAG=s.ID " + 
				" LEFT OUTER JOIN LOOKUP_VALUES C ON (A.TYPE_CD=C.LOOKUP_CD AND C.CATEGORY_CD=?)" + 
				" WHERE ( A.ORG_ID=? AND A.GENDER=?)";
		
		List<String> values = new ArrayList<String> ();
		values.add( Util.LookupValues.LCYCL);
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

	public List<Animal> retrieveActiveLactatingAnimals(String orgID) throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + LACTATING_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String> ();
		values.add(orgID);		
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

	public List<Animal> retrieveActivePregnantAnimals(String orgID) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + PREGNANT_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgID);
		return retrieveAnimalTypes(values, qryString);
	}
	
	public List<Animal> retrieveActiveNonDryPregnantAnimals(String orgID) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR  " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + PREGNANT_INDICATOR + "%' AND C.ADDITIONAL_FLD1 NOT LIKE '%" + DRY_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgID);
		return retrieveAnimalTypes(values, qryString);
	}
	
	public List<Animal> retrieveActiveDryPregnantAnimals(String orgID) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR  " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + PREGNANT_INDICATOR + "%' AND C.ADDITIONAL_FLD1 LIKE '%" + DRY_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgID);
		return retrieveAnimalTypes(values, qryString);
	}

	private ArrayList<Animal> retrieveAnimalTypes(List<String> values, String qryString) throws SQLException, IMDException {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
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
	
	public List<Animal> retrieveActiveNonPregnantNonInseminatedLactatingOrDryCows(String orgId) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR  " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND (C.ADDITIONAL_FLD1 LIKE '%" + LACTATING_INDICATOR + "%' OR C.ADDITIONAL_FLD1 LIKE '%" + DRY_INDICATOR + "%') AND C.ADDITIONAL_FLD1 NOT LIKE '%" + PREGNANT_INDICATOR + "%' AND C.ADDITIONAL_FLD1 NOT LIKE '%" + INSEMINATED_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgId);
		return retrieveAnimalTypes(values, qryString);
	}
	
	public List<Animal> retrieveActiveNonPregnantNonInseminatedHeifers(String orgId) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " +
				"	ON A.SIRE_TAG=B.ID " +
				"	LEFT OUTER JOIN LOOKUP_VALUES C " +
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + HEIFER_INDICATOR + "%' AND C.ADDITIONAL_FLD1 NOT LIKE '%" + PREGNANT_INDICATOR + "%' AND C.ADDITIONAL_FLD1 NOT LIKE '%" + INSEMINATED_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgId);
		return retrieveAnimalTypes(values, qryString);
	}


	
	public List<Animal> retrieveActiveHeifers(String orgId) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " +
				" from ANIMALS A " +
				"	LEFT OUTER JOIN LV_SIRE B " +
				"	ON A.SIRE_TAG=B.ID " +
				"	LEFT OUTER JOIN LOOKUP_VALUES C " +
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + HEIFER_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgId);
		return retrieveAnimalTypes(values, qryString);
	}

	public List<Animal> retrieveActiveFemaleCalves(String orgID) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND A.TYPE_CD='" + Util.AnimalTypes.FEMALECALF + "' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgID);
		return retrieveAnimalTypes(values, qryString);
	}


	public List<Animal> retrieveCalves(String orgId) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND (A.TYPE_CD='" + Util.AnimalTypes.FEMALECALF +"' OR A.TYPE_CD='" + Util.AnimalTypes.MALECALF + "') ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgId);
		return retrieveAnimalTypes(values, qryString);
	}

	public List<Animal> retrieveActiveInseminatedNonPregnantAnimals(String orgId) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE, C.ADDITIONAL_FLD1 AS STATUS_INDICATOR " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd='" + Util.LookupValues.LCYCL + "') " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL) AND C.ADDITIONAL_FLD1 LIKE '%" + INSEMINATED_INDICATOR + "%' AND C.ADDITIONAL_FLD1 NOT LIKE '%" + PREGNANT_INDICATOR + "%' ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add(orgId);
		return retrieveAnimalTypes(values, qryString);
	}

	public int updateAnimal(Animal animalDto) {
		int recordAdded = -1;
		String qryString = "UPDATE ANIMALS SET " + 
				(animalDto.getAnimalTypeCD() == null ? "" : " TYPE_CD=?,") +
				(animalDto.getAlias() == null ? "" : " ALIAS=?,") + 
				" UPDATED_BY=?, UPDATED_DTTM=? WHERE ORG_ID=? AND ANIMAL_TAG=?";

		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			if (animalDto.getAnimalTypeCD() != null)
				preparedStatement.setString(index++, animalDto.getAnimalTypeCD());
			if (animalDto.getAlias() != null)
				preparedStatement.setString(index++, animalDto.getAlias());
			preparedStatement.setString(index++, animalDto.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, animalDto.getUpdatedDTTMSQLFormat());
			preparedStatement.setString(index++, animalDto.getOrgID());
			preparedStatement.setString(index++, animalDto.getAnimalTag());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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
	public int updateAnimalStatus(Animal animalDto) {
		int recordAdded = -1;
		String qryString = "UPDATE ANIMALS SET TYPE_CD=?, UPDATED_BY=?, UPDATED_DTTM=? WHERE ORG_ID=? AND ANIMAL_TAG=?";

		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, animalDto.getAnimalTypeCD());
			preparedStatement.setString(2, animalDto.getUpdatedBy().getUserId());
			preparedStatement.setString(3, animalDto.getUpdatedDTTMSQLFormat());
			preparedStatement.setString(4, animalDto.getOrgID());
			preparedStatement.setString(5, animalDto.getAnimalTag());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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

	public List<Animal> retrieveAnimalsYoungerThanSpecifiedDays(String orgId, LocalDate dobThreshold) throws Exception {
		String qryString = "Select A.*,B.RECORD_URL, B.ALIAS SIRE_ALIAS, B.ID, C.SHORT_DESCR as ANIMAL_TYPE " + 
				"from ANIMALS A " + 
				"	LEFT OUTER JOIN LV_SIRE B " + 
				"	ON A.SIRE_TAG=B.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES C " + 
				"	ON (A.TYPE_CD=C.LOOKUP_CD AND C.category_cd=?) " +
				" WHERE A.ORG_ID=? AND (HERD_JOINING_DTTM IS NOT NULL AND HERD_LEAVING_DTTM IS NULL)  AND DOB >= ? ORDER BY ANIMAL_TAG";
		List<String> values = new ArrayList<String>();
		values.add( Util.LookupValues.LCYCL);
		values.add(orgId);
		values.add(Util.getDateInSQLFormart(dobThreshold));	
		return retrieveAnimalTypes(values, qryString);
	}

	public Sire retrieveSire(String inseminationSireCode) {
		String qryString = "Select * from LV_SIRE A " + 
				"left outer join sire_usage_stats_vw B on A.id=B.code " + 
				" WHERE A.ID=? ORDER BY A.ALIAS ";
		Sire animalValue = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();		
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, inseminationSireCode);
			IMDLogger.log(preparedStatement.toString(),Util.INFO);		
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
				animalValue = getSireFromSQLRecord(rs);		
		    }
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
	    return animalValue;
    }
	public int getActiveHerdCountForDate(org.joda.time.LocalDate date) {
		String qryString = "SELECT count(*) AS HERD_COUNT FROM imd.ANIMALS "
				+ " where herd_joining_dttm <= ? and (herd_leaving_dttm is null or herd_leaving_dttm > ?)";
		ResultSet rs = null;
		int retValue = 0;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();		
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, Util.getDateInSQLFormart(date));
			preparedStatement.setString(2, Util.getDateInSQLFormart(date));
			IMDLogger.log(preparedStatement.toString(),Util.INFO);		
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
				retValue = rs.getInt(1);		
		    }
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
		return retValue;
	}
	
	public int getActiveHerdCountAtEndOfMonthYear(int month, int year) {
		LocalDate endOfMonthDate = new LocalDate(year, month, 1);
		endOfMonthDate = endOfMonthDate.plusMonths(1).minusDays(1);
		return getActiveHerdCountForDate(endOfMonthDate);
	}	
	
	
	private Sire getSireFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		Sire animalValue;
		String id = rs.getString("ID");
		String breed = rs.getString("BREED");
		String alias = rs.getString("ALIAS");
		String semenInd = rs.getString("SEMEN_IND"); 
		String sireSpecification = rs.getString("RECORD_URL");
		String sirePhoto = rs.getString("PHOTO_URL");
		String controller = rs.getString("CONTROLLER");
		String semenCompany = rs.getString("SEMEN_COMPANY");
		Float currentSexListPrice = rs.getFloat("CURRENT_SEX_LIST_PRICE_PKR");
		Float currentConventionalListPrice = rs.getFloat("CURRENT_CONV_LIST_PRICE_PKR");
		Float discountSexPercentage = rs.getFloat("DISCOUNT_SEX_PERCENTAGE");
		Float discountConventionalPercentage = rs.getFloat("DISCOUNT_CONV_PERCENTAGE");
		Integer semenUsageCount = null;
		Integer semenSuccessCount = null;
		Integer semenFailureCount = null;
		Integer semenTbdCount = null;
		
		try {
			semenUsageCount = rs.getInt("USE_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("USE_COUNT column not found in the query", Util.WARNING);
		}
					
		try {
			semenSuccessCount = rs.getInt("SUCCESS_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("SUCCESS_COUNT column not found in the query", Util.WARNING);
		}

		try {
			semenFailureCount = rs.getInt("FAILURE_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("FAILURE_COUNT column not found in the query", Util.WARNING);
		}

		try {
			semenTbdCount = rs.getInt("TBD_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("TBD_COUNT column not found in the query", Util.WARNING);
		}
							
		animalValue = new Sire("GBL", id,DateTime.now(),true,0d,"PKR");
		animalValue.setBreed(breed);
		animalValue.setAlias(alias);
		animalValue.setSemenInd(semenInd);
		animalValue.setSireSpecification(sireSpecification);
		animalValue.setSirePhoto(sirePhoto);
		animalValue.setController(controller);
		animalValue.setMarketedByCompany(new Contact(semenCompany));
		animalValue.setCurrentConventionalListPrice(currentConventionalListPrice);
		animalValue.setCurrentSexListPrice(currentSexListPrice);
		animalValue.setDiscountConventionalPercentage(discountConventionalPercentage);
		animalValue.setDiscountSexPercentage(discountSexPercentage);
		animalValue.setSemenUsageCount(semenUsageCount);
		animalValue.setSemenSuccessCount(semenSuccessCount);
		animalValue.setSemenFailureCount(semenFailureCount);
		animalValue.setSemenTbdCount(semenTbdCount);
		
		animalValue.setCreatedBy(new User(rs.getString("CREATED_BY")));
		animalValue.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		animalValue.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animalValue.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return animalValue;
	}
	public int updateAnimalHerdLeavingDTTM(String orgID, String animalTag, String timeStamp, User user) {
		int recordAdded = -1;
		String qryString = "UPDATE ANIMALS SET HERD_LEAVING_DTTM=?, UPDATED_BY=?, UPDATED_DTTM=? WHERE ORG_ID=? AND ANIMAL_TAG=?";

		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, timeStamp);
			preparedStatement.setString(index++, user.getUserId());
			preparedStatement.setString(index++, Util.getDateInSQLFormart(DateTime.now()));
			preparedStatement.setString(index++, orgID);
			preparedStatement.setString(index++, animalTag);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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
}
