package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.imd.dto.LifeCycleEvent;
import com.imd.dto.Person;
import com.imd.util.DBManager;
import com.imd.util.IMDException;

public class LifeCycleEventLoader {
	
	public LifeCycleEventLoader() {
	}
	
	public int insertLifeCycleEvent(LifeCycleEvent event) throws SQLException {
		String qryString = "insert into LV_LIFECYCLE_EVENT (ORG_ID,EVENT_CD,ACTIVE_IND,SHORT_DESCR,LONG_DESCR,CREATED_BY,CREATED_DTTM,UPDATED_BY,UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?)";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, event.getOrgCode());
			preparedStatement.setString(2, event.getEventCode());
			preparedStatement.setString(3, (event.isActive() ? "A":"I"));		
			preparedStatement.setString(4, event.getEventShortDescription());
			preparedStatement.setString(5, event.getEventLongDescription());
			preparedStatement.setString(6, event.getCreatedBy().getUserID());
			preparedStatement.setString(7, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(8, event.getUpdatedBy().getUserID());
			preparedStatement.setString(9, event.getUpdatedDTTMSQLFormat());
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
	public LifeCycleEvent retrieveLifeCycleEvent(String organizationID, String eventCode) {
		String qryString = "Select * from LV_LIFECYCLE_EVENT where ORG_ID= '" + organizationID + "' AND EVENT_CD = '"+ eventCode + "'";
		LifeCycleEvent event = null;
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

	private LifeCycleEvent getLifeCycleEventFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		LifeCycleEvent event;
		event = new LifeCycleEvent(rs.getString("ORG_ID"),rs.getString("EVENT_CD"),rs.getString("SHORT_DESCR"),rs.getString("LONG_DESCR"));
		if (rs.getString("ACTIVE_IND") == "A") {
			event.markActive();
		} else
			event.markInActive();
		event.setCreatedBy(new Person(rs.getString("CREATED_BY"),"","",""));
		event.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		event.setUpdatedBy(new Person(rs.getString("UPDATED_BY"),"","",""));
		event.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return event;
	}
	public boolean doesLifecycleEventExist(LifeCycleEvent event) {
		return false;
	}
	public List<LifeCycleEvent> retrieveAllActiveLifeCycleEvents() {
		ArrayList<LifeCycleEvent> allActiveEvents = new ArrayList<LifeCycleEvent>();
		
		String qryString = "Select * from LV_LIFECYCLE_EVENT ORDER BY SHORT_DESCR";
		LifeCycleEvent event = null;
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
	public int deleteLifeCycleEvent(String organizationID, String eventCd) {
		String qryString = "DELETE FROM LV_LIFECYCLE_EVENT where ORG_ID= '" + organizationID + "' AND EVENT_CD = '"+ eventCd + "'";
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
	public boolean inactivateLifeCycleEvent(LifeCycleEvent event) {
		return false;
	}
	public boolean activateLifeCycleEvent(LifeCycleEvent event) {
		return false;
	}
	public boolean updateLifeCycleEvent(LifeCycleEvent event) {
		return false;
	}
}
