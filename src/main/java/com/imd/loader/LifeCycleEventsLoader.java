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

import com.imd.dto.LifecycleEvent;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class LifeCycleEventsLoader {

	
	public LifeCycleEventsLoader() {
	}
	
	public int insertLifeCycleEvent(LifecycleEvent event) throws SQLException {
		String qryString = "insert into LIFECYCLE_EVENTS (ORG_ID,"
				+ "ANIMAL_TAG,"
				+ "EVENT_CD,"
				+ "EVENT_DTTM,"
				+ "OPERATOR,"
				+ "COMMENTS,"
				+ "AUX_FL1_VALUE,"
				+ "AUX_FL2_VALUE,"
				+ "AUX_FL3_VALUE,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		ResultSet result = null;
		int transactionID = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, event.getOrgID());
			preparedStatement.setString(2, event.getAnimalTag());
			preparedStatement.setString(3, event.getEventType().getEventCode());
			preparedStatement.setString(4, event.getEventTimeStampSQLFormat());
			preparedStatement.setString(5, event.getEventOperator().getPersonID());
			preparedStatement.setString(6, event.getEventNote());
			preparedStatement.setString(7, event.getAuxField1Value());
			preparedStatement.setString(8, event.getAuxField2Value());
			preparedStatement.setString(9, event.getAuxField3Value());
			preparedStatement.setString(10, event.getCreatedBy().getUserId());
			preparedStatement.setString(11, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(12, event.getUpdatedBy().getUserId());
			preparedStatement.setString(13, event.getUpdatedDTTMSQLFormat());
			preparedStatement.executeUpdate();
			result = preparedStatement.getGeneratedKeys();
			if (result.next()) 
				transactionID = result.getInt(1);
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			transactionID = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (Exception ex) {
			transactionID = Util.ERROR_CODE.UNKNOWN_ERROR;
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
		return transactionID;
	}
	public LifecycleEvent retrieveLifeCycleEvent(String orgId, int transID) {
		String qryString = "Select a.*, b.SHORT_DESCR as EVENT_SHORT_DESCR, c.SHORT_DESCR as OPERATOR_SHORT_DESCR " +
		  " from LIFECYCLE_EVENTS a  " +
		  "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT b  " +
		  "  ON	a.EVENT_CD = b.EVENT_CD  " +
		  "  LEFT OUTER JOIN LOOKUP_VALUES c " +
		  "  ON c.LOOKUP_CD=a.OPERATOR and  c.CATEGORY_CD='OPRTR' " +
	      " where a.ORG_ID='" + orgId + "' AND a.ID ="+ transID + "  ORDER BY a.EVENT_DTTM DESC"; 		
		
		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		        event = getLifeCycleEventFromSQLRecord(rs);
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
	    return event;
	}

	private LifecycleEvent getLifeCycleEventFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		LifecycleEvent event;
		event = new LifecycleEvent(rs.getString("ORG_ID"),rs.getInt("ID"),rs.getString("ANIMAL_TAG"), rs.getString("EVENT_CD"));
		event.getEventType().setEventShortDescription(rs.getString("EVENT_SHORT_DESCR"));
		event.setEventTimeStamp(new DateTime(rs.getTimestamp("EVENT_DTTM")));
		event.setEventNote(rs.getString("COMMENTS"));
		event.setEventOperator(new Person(rs.getString("OPERATOR_SHORT_DESCR"),"","",""));
		event.setAuxField1Value(rs.getString("AUX_FL1_VALUE"));
		event.setAuxField2Value(rs.getString("AUX_FL2_VALUE"));
		event.setAuxField3Value(rs.getString("AUX_FL3_VALUE"));
		event.setCreatedBy(new User(rs.getString("CREATED_BY")));
		event.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		event.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		event.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return event;
	}
	public List<LifecycleEvent> retrieveAllLifeCycleEventsForAnimal(String orgId, String tagNumber) {
		ArrayList<LifecycleEvent> allAnimalEvents = new ArrayList<LifecycleEvent>();
		String qryString = " Select a.*, b.SHORT_DESCR as EVENT_SHORT_DESCR, c.SHORT_DESCR as OPERATOR_SHORT_DESCR " +
		  " from LIFECYCLE_EVENTS a  " +
		  "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT b  " +
		  "  ON	a.EVENT_CD = b.EVENT_CD  " +
		  "  LEFT OUTER JOIN LOOKUP_VALUES c " +
		  "  ON c.LOOKUP_CD=a.OPERATOR and  c.CATEGORY_CD='OPRTR' " +
	      " where a.ORG_ID='" + orgId + "' AND a.ANIMAL_TAG ="+ tagNumber + " ORDER BY a.EVENT_DTTM DESC";		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		IMDLogger.log(qryString, Util.INFO);
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		        event = getLifeCycleEventFromSQLRecord(rs);
		        allAnimalEvents.add(event);
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
	    return allAnimalEvents;
	}
	public int deleteLifeCycleEvent(String orgID, int transId) {
		String qryString = "DELETE FROM LIFECYCLE_EVENTS WHERE ORG_ID='" + orgID + "' AND ID=" + transId;
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

	public int updateLifeCycleEvent(LifecycleEvent event) {
		
		String qryString = "UPDATE LIFECYCLE_EVENTS ";
		String valuestoBeUpdated = "";
		int updatedRecordCount = 0;
		PreparedStatement preparedStatement = null;

		List<String> updateString = new ArrayList<String>();
		valuestoBeUpdated = " EVENT_CD=? ";
		updateString.add(event.getEventType().getEventCode());
		if (event.getEventTimeStamp() != null) {
			valuestoBeUpdated += ", EVENT_DTTM=?";
			updateString.add(event.getEventTimeStampSQLFormat());
		}		
		if (event.getEventNote() != null && !event.getEventNote().isEmpty()) {
			valuestoBeUpdated += ", COMMENTS=? ";
			updateString.add(event.getEventNote());
		}
		if (event.getEventOperator() != null)	{
			valuestoBeUpdated += ", OPERATOR=? ";
			updateString.add(event.getEventOperator().getPersonID());
		}
		if (event.getAuxField1Value() != null) {
			valuestoBeUpdated += ", AUX_FL1_VALUE=? ";
			updateString.add(event.getAuxField1Value());
		}
		if (event.getAuxField2Value() != null) {
			valuestoBeUpdated += ", AUX_FL2_VALUE=? ";
			updateString.add(event.getAuxField2Value());
		}
		if (event.getAuxField3Value() != null) {
			valuestoBeUpdated += ", AUX_FL3_VALUE=? ";
			updateString.add(event.getAuxField3Value());
		}
		if (event.getUpdatedBy() != null && !event.getUpdatedBy().getUserId().isEmpty()) {
			valuestoBeUpdated += ", UPDATED_BY=?";
			updateString.add(event.getUpdatedBy().getUserId());
		}
		if (event.getUpdatedDTTM() != null) {
			valuestoBeUpdated += ", UPDATED_DTTM=?";
			updateString.add(event.getUpdatedDTTMSQLFormat());
		}

		IMDLogger.log(valuestoBeUpdated, Util.INFO);
		
		if (valuestoBeUpdated.isEmpty()) {
			updatedRecordCount = 0;
		} else {
			qryString = qryString + "SET " + valuestoBeUpdated + " where ORG_ID =? AND ID=?";
			Statement st = null;
			IMDLogger.log(qryString, Util.INFO);
			Connection conn = DBManager.getDBConnection();
			try {
				preparedStatement = conn.prepareStatement(qryString);
				Iterator<String> it = updateString.iterator();
				int i=1;
				while (it.hasNext()) {
					preparedStatement.setString(i++,it.next());
				}
				preparedStatement.setString(i++,event.getOrgID());
				preparedStatement.setInt(i,event.getEventTransactionID());

				updatedRecordCount = preparedStatement.executeUpdate();
			} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
				updatedRecordCount = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
				ex.printStackTrace();				
			} catch (java.sql.SQLSyntaxErrorException ex) {
				ex.printStackTrace();
				updatedRecordCount = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			} catch (Exception ex) {
				ex.printStackTrace();
				updatedRecordCount = Util.ERROR_CODE.UNKNOWN_ERROR;
			} finally {
			    try {
					if (st != null && !st.isClosed()) {
						st.close();	
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return updatedRecordCount;
	}

}
