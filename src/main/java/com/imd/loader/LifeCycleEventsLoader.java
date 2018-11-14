package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
				+ "COMMENTS,"
				+ "OPERATOR,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?)";
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
			preparedStatement.setString(5, event.getEventNote());
			preparedStatement.setString(6, event.getEventOperator().getPersonID());
			preparedStatement.setString(7, event.getCreatedBy().getUserId());
			preparedStatement.setString(8, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(9, event.getUpdatedBy().getUserId());
			preparedStatement.setString(10, event.getUpdatedDTTMSQLFormat());
			preparedStatement.executeUpdate();
			result = preparedStatement.getGeneratedKeys();
			if (result.next()) 
				transactionID = result.getInt(1);
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
		return transactionID;
	}
	public LifecycleEvent retrieveLifeCycleEvent(String orgId, int transID) {
		String qryString = "Select * from LIFECYCLE_EVENTS where ID ="+ transID + " AND ORG_ID='" + orgId + "'";
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
		event.setEventTimeStamp(new DateTime(rs.getTimestamp("EVENT_DTTM")));
		event.setEventNote(rs.getString("COMMENTS"));
		event.setEventOperator(new Person(rs.getString("OPERATOR"),"","",""));
		event.setCreatedBy(new User(rs.getString("CREATED_BY")));
		event.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		event.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		event.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return event;
	}
	public List<LifecycleEvent> retrieveAllActiveLifeCycleEventsForAnimal(String orgId, String tagNumber) {
		ArrayList<LifecycleEvent> allAnimalEvents = new ArrayList<LifecycleEvent>();
		String qryString = "Select * from LIFECYCLE_EVENTS WHERE ORG_ID='" + orgId + "' AND ANIMAL_TAG='" + tagNumber + "'   ORDER BY EVENT_DTTM DESC";
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
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
		
		valuestoBeUpdated = event.createUpdateString();
		
		if (valuestoBeUpdated.isEmpty()) {
			updatedRecordCount = 0;
		} else {
			qryString = qryString + "SET " + valuestoBeUpdated + " where ORG_ID = '"+ event.getOrgID()+ "' AND ID="+event.getEventTransactionID();
			Statement st = null;
			IMDLogger.log(qryString, Util.INFO);
			Connection conn = DBManager.getDBConnection();
			try {
				st = conn.createStatement();
				updatedRecordCount = st.executeUpdate(qryString);			
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
		}
		return updatedRecordCount;
	}	
	
}
