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

import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.services.bean.LifeCycleEventCodeBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class LVLifeCycleEventLoader {
	
	public LVLifeCycleEventLoader() {
	}
	
	public int insertLifeCycleEvent(LifeCycleEventCode event) {
		String qryString = "insert into LV_LIFECYCLE_EVENT (EVENT_CD,ACTIVE_IND,SHORT_DESCR,LONG_DESCR,"
				+ "FIELD1_LABEL,FIELD1_TYPE,FIELD1_UNIT,"
				+ "FIELD2_LABEL,FIELD2_TYPE,FIELD2_UNIT,"
				+ "FIELD3_LABEL,FIELD3_TYPE,FIELD3_UNIT,"
				+ "FIELD4_LABEL,FIELD4_TYPE,FIELD4_UNIT,"
				+ "NEXT_LCYCL,INV_UPDATE_LABEL,"
				+ "CREATED_BY,CREATED_DTTM,UPDATED_BY,UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int result = -1;
		// using prepared statement automatically takes care of the special characters.
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, event.getEventCode());
			preparedStatement.setString(2, (event.isActive() ? "Y":"N"));		
			preparedStatement.setString(3, event.getEventShortDescription());
			preparedStatement.setString(4, event.getEventLongDescription());
			preparedStatement.setString(5, event.getField1Label());
			preparedStatement.setString(6, event.getField1DataType());
			preparedStatement.setString(7, event.getField1DataUnit());
			preparedStatement.setString(8, event.getField2Label());
			preparedStatement.setString(9, event.getField2DataType());
			preparedStatement.setString(10, event.getField2DataUnit());
			preparedStatement.setString(11, event.getField3Label());
			preparedStatement.setString(12, event.getField3DataType());
			preparedStatement.setString(13, event.getField3DataUnit());
			preparedStatement.setString(14, event.getField4Label());
			preparedStatement.setString(15, event.getField4DataType());
			preparedStatement.setString(16, event.getField4DataUnit());
			preparedStatement.setString(17, event.getNextLifecycleStage());
			preparedStatement.setString(18, event.getInventoryUpdateLabel());
			preparedStatement.setString(19, event.getCreatedBy().getUserId());
			preparedStatement.setString(20, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(21, event.getUpdatedBy().getUserId());
			preparedStatement.setString(22, event.getUpdatedDTTMSQLFormat());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			result = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			result = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			result = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			result = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (Exception ex) {
			result = Util.ERROR_CODE.UNKNOWN_ERROR;
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
	public List<LifeCycleEventCode> retrieveLifeCycleEvent(String eventCode) {
		ArrayList<LifeCycleEventCode> allMatchingEvents = new ArrayList<LifeCycleEventCode>();
		String qryString = "Select * from LV_LIFECYCLE_EVENT where EVENT_CD = ? ORDER BY SHORT_DESCR";
		LifeCycleEventCode event = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, eventCode);
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		        event = getLifeCycleEventFromSQLRecord(rs);
		        allMatchingEvents.add(event);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (rs != null && !rs.isClosed()) {
					rs.close();	
				}
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	    return allMatchingEvents;
	}
	
	
	public List<LifeCycleEventCode> retrieveMatchingLifeCycleEvents( LifeCycleEventCodeBean eventBean) {
		ArrayList<LifeCycleEventCode> allMatchingEvents = new ArrayList<LifeCycleEventCode>();		
		String qryString = "Select * from LV_LIFECYCLE_EVENT ";
		
		if (!isNullOrEmpty(eventBean.getEventCode())) {
			qryString +=  " WHERE EVENT_CD  LIKE '" + eventBean.getEventCode() + "' ";
			if (!isNullOrEmpty(eventBean.getEventShortDescription())) {
				qryString +=  " AND SHORT_DESCR LIKE '" + eventBean.getEventShortDescription() + "' ORDER BY SHORT_DESCR";
			}
		} else if (!isNullOrEmpty(eventBean.getEventShortDescription())) {
			qryString +=  " WHERE SHORT_DESCR LIKE '" + eventBean.getEventShortDescription() + "' ORDER BY SHORT_DESCR";
		} else {
			qryString = "Select * from LV_LIFECYCLE_EVENT ORDER BY SHORT_DESCR" ;
		}
		IMDLogger.log(qryString,Util.INFO);
		LifeCycleEventCode event = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			st = conn.createStatement();
		    rs = st.executeQuery(qryString);
		    while (rs.next()) {
		        event = getLifeCycleEventFromSQLRecord(rs);
		        allMatchingEvents.add(event);
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
	    return allMatchingEvents;
	}

	private boolean isNullOrEmpty(String strValue) {
		return (strValue == null || strValue.trim().isEmpty());
	}		
	
	private LifeCycleEventCode getLifeCycleEventFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		LifeCycleEventCode event;
		event = new LifeCycleEventCode(rs.getString("EVENT_CD"),rs.getString("SHORT_DESCR"),rs.getString("LONG_DESCR"));
		if (rs.getString("ACTIVE_IND").equalsIgnoreCase("Y")) {
			event.markActive();
		} else
			event.markInActive();
		event.setField1Label(rs.getString("FIELD1_LABEL"));
		event.setField1DataType(rs.getString("FIELD1_TYPE"));
		event.setField1DataUnit(rs.getString("FIELD1_UNIT"));

		event.setField2Label(rs.getString("FIELD2_LABEL"));
		event.setField2DataType(rs.getString("FIELD2_TYPE"));
		event.setField2DataUnit(rs.getString("FIELD2_UNIT"));

		event.setField3Label(rs.getString("FIELD3_LABEL"));
		event.setField3DataType(rs.getString("FIELD3_TYPE"));
		event.setField3DataUnit(rs.getString("FIELD3_UNIT"));

		event.setField4Label(rs.getString("FIELD4_LABEL"));
		event.setField4DataType(rs.getString("FIELD4_TYPE"));
		event.setField4DataUnit(rs.getString("FIELD4_UNIT"));

		event.setNextLifecycleStage(rs.getString("NEXT_LCYCL"));
		event.setInventoryUpdateLabel(rs.getString("INV_UPDATE_LABEL"));
		
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
		return retrieveLifeCycleEvents(true);
		
	}	

	public List<LifeCycleEventCode> retrieveAllLifeCycleEvents() {
		return retrieveLifeCycleEvents(false);
		
	}
	private List<LifeCycleEventCode> retrieveLifeCycleEvents(boolean retrieveActiveOnly) {
		ArrayList<LifeCycleEventCode> allActiveEvents = new ArrayList<LifeCycleEventCode>();		
		String qryString = "Select * from LV_LIFECYCLE_EVENT " + (retrieveActiveOnly ? " WHERE ACTIVE_IND = 'Y' " : "") + " ORDER BY SHORT_DESCR";
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
	/**
	 * We should not use this method. This method is only for internal use. This Delete method does not have any corresponding API; so it can't be 
	 * called from the front end application. We should mark records "inactive" instead of deleting them. This method is only used during
	 * unit testing to clean up test records.
	 * @param eventCd
	 * @return
	 */
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
		PreparedStatement preparedStatement = null;
		
		List<String> updateString = new ArrayList<String>();
		valuestoBeUpdated = " ACTIVE_IND=? ";
		updateString.add((event.isActive() ? "Y": "N"));
		if (event.getEventShortDescription() != null && !event.getEventShortDescription().isEmpty()) {
			valuestoBeUpdated += ", SHORT_DESCR=?";
			updateString.add(event.getEventShortDescription());
		}
		if (event.getEventLongDescription() != null && !event.getEventLongDescription().isEmpty()) {			
			valuestoBeUpdated += ", LONG_DESCR=?";
			updateString.add(event.getEventLongDescription());
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
			qryString = qryString + "SET " + valuestoBeUpdated + " where EVENT_CD =?" ;
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
				preparedStatement.setString(i,event.getEventCode());
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
