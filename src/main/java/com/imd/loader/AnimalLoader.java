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

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class AnimalLoader {
	
	
	public int insertAnimal(Animal animal) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into ANIMALS (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "ALIAS,"
				+ "STATUS,"
				+ "TYPE_CD,"
				+ "DOB,"
				+ "DOB_ACCURACY_IND,"
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
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, (animal.getOrgID() == null ? null : animal.getOrgID()));
			preparedStatement.setString(2, (animal.getAnimalTag() == null ? null : animal.getAnimalTag()));
			preparedStatement.setString(3, (animal.getAlias() == null ? null : animal.getAlias()));
			preparedStatement.setString(4, (animal.getAnimalStatus() == null ? null : animal.getAnimalStatus()));
			preparedStatement.setString(5, (animal.getAnimalType() == null ? null : animal.getAnimalType()));
			preparedStatement.setString(6, (animal.getDateOfBirth() == null ? null : Util.getDateInSQLFormart(animal.getDateOfBirth())));
			preparedStatement.setString(7, (animal.isDateOfBirthEstimated() ? "N" : "Y"));
			preparedStatement.setString(8, (animal.getGender() == ' ' ? null : animal.getGender() + ""));
			preparedStatement.setString(9, (animal.getAnimalDam() == null ? null : animal.getAnimalDam().getAnimalTag()));
			preparedStatement.setString(10, (animal.getAnimalSire() == null ? null : animal.getAnimalSire().getAnimalTag()));
			preparedStatement.setString(11, (animal.isBornThroughAI() ? "Y" : "N"));
			preparedStatement.setString(12, (animal.getFrontImageURL() == null ? null : animal.getFrontImageURL().toString()));
			preparedStatement.setString(13, (animal.getBacksideImageURL() == null ? null : animal.getBacksideImageURL().toString()));
			preparedStatement.setString(14, (animal.getLeftSideImageURL() == null ? null : animal.getLeftSideImageURL().toString()));
			preparedStatement.setString(15, (animal.getRightSideImageURL() == null ? null : animal.getRightSideImageURL().toString()));
			preparedStatement.setString(16, (animal.getCreatedBy() == null ? null : animal.getCreatedBy().getUserId()));
			preparedStatement.setString(17, (animal.getCreatedDTTM() == null ? null :animal.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(18,(animal.getUpdatedBy() == null ? null : animal.getUpdatedBy().getUserId()));
			preparedStatement.setString(19,(animal.getUpdatedDTTM() == null ? null :animal.getUpdatedDTTMSQLFormat()));
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
	
	public int deleteAnimal(String orgID, String animalTag) {
		String qryString = "DELETE FROM ANIMALS where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '" + animalTag + "'";
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
	public List<Animal> retrieveMatchingAnimals(AnimalBean animalBean) throws Exception {
		ArrayList<Animal> allMatchingValues = new ArrayList<Animal>();
		boolean isWildCardSearch = true;
		//String qryString = "Select a.*,s.RECORD_URL, s.ALIAS SIRE_ALIAS, s.ID from ANIMALS a LEFT OUTER JOIN LV_SIRE s ON a.SIRE_TAG=s.ID WHERE ( a.ORG_ID=? ";

		String qryString = "Select a.*,b.RECORD_URL, b.ALIAS SIRE_ALIAS, b.ID, c.SHORT_DESCR as ANIMAL_TYPE " + 
				"from ANIMALS a " + 
				"	LEFT OUTER JOIN LV_SIRE b " + 
				"	ON a.SIRE_TAG=b.ID " + 
				"	LEFT OUTER JOIN LOOKUP_VALUES c " + 
				"	ON a.TYPE_CD=c.LOOKUP_CD " + 
				" WHERE ( a.ORG_ID=? ";
		
		
		List<String> values = new ArrayList<String> ();
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
		if (animalBean.getActiveOnly()) {
			qryString +=  " AND STATUS='ACTIVE') ORDER BY ANIMAL_TAG";
		} else {
			qryString += ") ORDER BY ANIMAL_TAG";
		}
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
		String gender = rs.getString("GENDER");
		String type = rs.getString("ANIMAL_TYPE");
		String damTag = rs.getString("DAM_TAG");
		String sireTag = rs.getString("SIRE_TAG");
		String orgId = rs.getString("ORG_ID");
		String alias = rs.getString("ALIAS");
		String frontPoseImage = rs.getString("FRONT_POSE");
		String backPoseImage = rs.getString("BACK_POSE");
		String rightPoseImage = rs.getString("RIGHT_POSE");
		String leftPoseImage = rs.getString("LEFT_POSE");
		String aiInd = rs.getString("AI_IND");
				
				
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
		animal.setAnimalType(type);
		animal.setFrontImageURL(frontPoseImage);
		animal.setBacksideImageURL(backPoseImage);
		animal.setRightSideImageURL(rightPoseImage);
		animal.setLeftSideImageURL(leftPoseImage);
		animal.setBornThroughAI((aiInd != null && aiInd.equalsIgnoreCase("Y")) ? true : false);
		animal.setAnimalStatus(rs.getString("STATUS"));
		animal.setCreatedBy(new User(rs.getString("CREATED_BY")));
		animal.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		animal.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animal.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return animal;
	}

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
}
