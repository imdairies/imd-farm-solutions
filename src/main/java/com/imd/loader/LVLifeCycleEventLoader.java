package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class LVLifeCycleEventLoader {
	
	public LVLifeCycleEventLoader() {
	}
	
	public int insertLifeCycleEvent(LifeCycleEventCode event) throws SQLException {
		String qryString = "insert into LV_LIFECYCLE_EVENT (EVENT_CD,ACTIVE_IND,SHORT_DESCR,LONG_DESCR,CREATED_BY,CREATED_DTTM,UPDATED_BY,UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?)";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, event.getEventCode());
			preparedStatement.setString(2, (event.isActive() ? "Y":"N"));		
			preparedStatement.setString(3, event.getEventShortDescription());
			preparedStatement.setString(4, event.getEventLongDescription());
			preparedStatement.setString(5, event.getCreatedBy().getUserId());
			preparedStatement.setString(6, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(7, event.getUpdatedBy().getUserId());
			preparedStatement.setString(8, event.getUpdatedDTTMSQLFormat());
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
	public LifeCycleEventCode retrieveLifeCycleEvent( String eventCode) {
		String qryString = "Select * from LV_LIFECYCLE_EVENT where EVENT_CD = '"+ eventCode + "'";
		LifeCycleEventCode event = null;
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

	private LifeCycleEventCode getLifeCycleEventFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		LifeCycleEventCode event;
		event = new LifeCycleEventCode(rs.getString("EVENT_CD"),rs.getString("SHORT_DESCR"),rs.getString("LONG_DESCR"));
		if (rs.getString("ACTIVE_IND") == "A") {
			event.markActive();
		} else
			event.markInActive();
		event.setCreatedBy(new User(rs.getString("CREATED_BY")));
		event.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		event.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		event.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return event;
	}
	public boolean doesLifecycleEventExist(LifeCycleEventCode event) {
		return false;
	}
	public List<LifeCycleEventCode> retrieveAllActiveLifeCycleEvents() {
		ArrayList<LifeCycleEventCode> allActiveEvents = new ArrayList<LifeCycleEventCode>();
		
		String qryString = "Select * from LV_LIFECYCLE_EVENT ORDER BY SHORT_DESCR";
		LifeCycleEventCode event = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		        event = getLifeCycleEventFromSQLRecord(rs);
		        allActiveEvents.add(event);
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
	    return allActiveEvents;
	}
	public int deleteLifeCycleEvent(String eventCd) {
		String qryString = "DELETE FROM LV_LIFECYCLE_EVENT where EVENT_CD = '"+ eventCd + "'";
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
	public boolean inactivateLifeCycleEvent(LifeCycleEventCode event) {
		return false;
	}
	public boolean activateLifeCycleEvent(LifeCycleEventCode event) {
		return false;
	}
	public int updateLifeCycleEvent(LifeCycleEventCode event) {
		String qryString = "UPDATE LV_LIFECYCLE_EVENT ";
		String valuestoBeUpdated = "";
		int updatedRecordCount = 0;
		
		valuestoBeUpdated = event.createUpdateString();
		
		if (valuestoBeUpdated.isEmpty()) {
			updatedRecordCount = 0;
		} else {
			qryString = qryString + "SET " + valuestoBeUpdated + " where EVENT_CD = '"+ event.getEventCode() + "'";
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
