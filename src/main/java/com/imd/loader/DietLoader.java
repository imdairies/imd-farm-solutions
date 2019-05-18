package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.DietRequirement;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class DietLoader {
	
	
	public int deleteDietRequirement(DietRequirement dietReq) {
		String qryString = "DELETE FROM FEED_REQUIREMENT WHERE ORG_ID=? AND ANIMAL_TYPE=?";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, dietReq.getOrgID());
			preparedStatement.setString(2, dietReq.getApplicableAimalType().getEventCode());
			result = preparedStatement.executeUpdate();			
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
		return result;
	}
	
	public int insertDietRequirement(DietRequirement dietReq) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into  imd.FEED_REQUIREMENT (ORG_ID,"
				+ "ANIMAL_TYPE,"
				+ "START_TIME,"
				+ "END_TIME,"
				+ "DM,"
				+ "CP,"
				+ "ME,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1,  dietReq.getOrgID());
			preparedStatement.setString(2,  dietReq.getApplicableAimalType().getEventCode());
			preparedStatement.setInt(3,  dietReq.getStart());
			preparedStatement.setInt(4,  dietReq.getEnd());
			preparedStatement.setFloat(5,  dietReq.getDryMatter());
			preparedStatement.setFloat(6,  dietReq.getCrudeProtein());
			preparedStatement.setFloat(7,  dietReq.getMetabolizableEnergy());
			preparedStatement.setString(8, dietReq.getCreatedBy().getUserId());
			preparedStatement.setString(9, dietReq.getCreatedDTTMSQLFormat());
			preparedStatement.setString(10, dietReq.getUpdatedBy().getUserId());
			preparedStatement.setString(11, dietReq.getUpdatedDTTMSQLFormat());
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
	
	public List<DietRequirement> getDietRequirements(String orgId, List<LifeCycleEventCode> animalType) {
		String inClause = "";
		List<DietRequirement> dietReq = new ArrayList<DietRequirement>();
		
		String qryString = "SELECT * FROM imd.FEED_REQUIREMENT where ORG_ID=? " ;
		if (animalType != null && !animalType.isEmpty()) {
			inClause = " AND ANIMAL_TYPE IN (";
			int run = 1;
			Iterator<LifeCycleEventCode> it = animalType.iterator();
			while (it.hasNext()) {
				it.next();
				if (run == animalType.size())
					inClause += "?)";
				else 
					inClause += "?,";
				run++;
			}
		}
		qryString += inClause;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int index = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++,orgId);
			Iterator<LifeCycleEventCode> it = animalType.iterator();
			while (it.hasNext()) {
				preparedStatement.setString(index++,it.next().getEventCode());			
			}
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	dietReq.add(getDietRequirementFromSQLRecord(rs));
	    	}
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			dietReq = null;
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

	    return dietReq;
	}

	private DietRequirement getDietRequirementFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		DietRequirement diet = new DietRequirement();
		diet.setOrgID(rs.getString("ORG_ID"));
		diet.setApplicableAimalTypes(new LifeCycleEventCode(rs.getString("ANIMAL_TYPE"),"",""));
		diet.setStart(rs.getInt("START_TIME"));
		diet.setEnd(rs.getInt("END_TIME"));
		diet.setDryMatter(rs.getFloat("DM"));
		diet.setCrudeProtein(rs.getFloat("CP"));
		diet.setMetabolizableEnergy(rs.getFloat("ME"));
		diet.setCreatedBy(new User(rs.getString("CREATED_BY")));
		diet.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		diet.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		diet.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return diet;
	}
}
