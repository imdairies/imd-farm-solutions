package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.Util;

public class AnimalLoader {
	
	
	public int insertAnimal(Animal animal) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into ANIMALS (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "STATUS,"
				+ "DOB,"
				+ "DOB_ACCURACY_IND,"
				+ "GENDER,"
				+ "DAM_TAG,"
				+ "SIRE_TAG,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, (animal.getOrgID() == null ? null : animal.getOrgID()));
			preparedStatement.setString(2, (animal.getAnimalTag() == null ? null : animal.getAnimalTag()));
			preparedStatement.setString(3, (animal.getAnimalStatus() == null ? null : animal.getAnimalStatus()));
			preparedStatement.setString(4, (animal.getDateOfBirth() == null ? null : Util.getDateInSQLFormart(animal.getDateOfBirth())));
			preparedStatement.setString(5, (animal.isDateOfBirthEstimated() ? "N" : "Y"));
			preparedStatement.setString(6, (animal.getGender() == ' ' ? null : animal.getGender() + ""));
			preparedStatement.setString(7, (animal.getAnimalDam() == null ? null : animal.getAnimalDam().getAnimalTag()));		
			preparedStatement.setString(8, (animal.getAnimalSire() == null ? null : animal.getAnimalSire().getAnimalTag()));
			preparedStatement.setString(9, (animal.getCreatedBy() == null ? null : animal.getCreatedBy().getUserId()));
			preparedStatement.setString(10, (animal.getCreatedDTTM() == null ? null :animal.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(11,(animal.getUpdatedBy() == null ? null : animal.getUpdatedBy().getUserId()));
			preparedStatement.setString(12,(animal.getUpdatedDTTM() == null ? null :animal.getUpdatedDTTMSQLFormat()));
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
	
	public Animal retrieveAnimal(String orgID, String animalTag) {
		String qryString = "Select * from ANIMALS where ORG_ID='" + orgID + "' AND ANIMAL_TAG = '"+ animalTag + "'";
		Animal animal = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		    	animal = getAnimalFromSQLRecord(rs);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (rs != null && !rs.isClosed()) {
					rs.close();	
				}
				if (st != null && !st.isClosed()) {
					st.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	    return animal;
	}
	
	public List<Animal> retrieveActiveAnimals(String orgID) {
		return retrieveAnimals(orgID, true);
	}	

	public List<Animal> retrieveAllAnimals(String orgID) {
		return retrieveAnimals(orgID, false);
	}	
	
	private List<Animal> retrieveAnimals(String orgID, boolean retrieveActiveOnly) {
		ArrayList<Animal> allAnimals = new ArrayList<Animal>();		
		String qryString = "Select * from ANIMALS WHERE ORG_ID='" + orgID + "' " + (retrieveActiveOnly ? "  AND STATUS = '" + Util.ANIMAL_STATUS.ACTIVE + "' " : "") + " ORDER BY ANIMAL_TAG";
		Animal animal = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		        animal = getAnimalFromSQLRecord(rs);
		        allAnimals.add(animal);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (rs != null && !rs.isClosed()) {
					rs.close();	
				}
				if (st != null && !st.isClosed()) {
					st.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	    return allAnimals;
	}	
	private Animal getAnimalFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		String ind = rs.getString("DOB_ACCURACY_IND");
		DateTime dob = (rs.getTimestamp("DOB") != null ? new DateTime(rs.getTimestamp("DOB")) : null);
		String gender = rs.getString("GENDER");
		boolean isEstimated = (ind == null || ind.trim().isEmpty() || ind.trim().equalsIgnoreCase("N") ? true : false);

		Animal animal;
		if (gender != null && gender.trim().equalsIgnoreCase("F")) {
			animal = new Dam(rs.getString("ORG_ID"), rs.getString("ANIMAL_TAG"),dob,isEstimated,324000d,"PKR");
		} else {
			animal = new Sire(rs.getString("ORG_ID"), rs.getString("ANIMAL_TAG"),dob,isEstimated,324000d,"PKR");	
		}		 
		animal.setCreatedBy(new User(rs.getString("CREATED_BY")));
		animal.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		animal.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animal.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return animal;
	}
	
}
