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
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;
import com.imd.util.Util.LifeCycleEvents;

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
				+ "AUX_FL4_VALUE,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
			preparedStatement.setString(5, (event.getEventOperator() == null ? null : event.getEventOperator().getPersonID()));
			preparedStatement.setString(6, event.getEventNote());
			preparedStatement.setString(7, event.getAuxField1Value());
			preparedStatement.setString(8, event.getAuxField2Value());
			preparedStatement.setString(9, event.getAuxField3Value());
			preparedStatement.setString(10, event.getAuxField4Value());
			preparedStatement.setString(11, event.getCreatedBy().getUserId());
			preparedStatement.setString(12, event.getCreatedDTTMSQLFormat());
			preparedStatement.setString(13, event.getUpdatedBy().getUserId());
			preparedStatement.setString(14, event.getUpdatedDTTMSQLFormat());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			preparedStatement.executeUpdate();
			result = preparedStatement.getGeneratedKeys();
			if (result.next()) 
				transactionID = result.getInt(1);
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			transactionID = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			transactionID = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
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
	
	public LifecycleEvent retrieveLifeCycleEvent(String orgId, String transID) {		
		try {
			return retrieveLifeCycleEvent(orgId, Integer.parseInt(transID));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	
	public LifecycleEvent retrieveLifeCycleEvent(String orgId, int transID) {
		String qryString = "Select A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, "
				+ "B.FIELD1_LABEL, B.FIELD1_TYPE, B.FIELD1_UNIT,"
				+ "B.FIELD2_LABEL, B.FIELD2_TYPE, B.FIELD2_UNIT,"
				+ "B.FIELD3_LABEL, B.FIELD3_TYPE, B.FIELD3_UNIT,"
				+ "B.FIELD4_LABEL, B.FIELD4_TYPE, B.FIELD4_UNIT,"
				+ "C.SHORT_DESCR as OPERATOR_SHORT_DESCR "
				+ " from LIFECYCLE_EVENTS A  "
				+ "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT B  "
				+ "  ON	A.EVENT_CD = B.EVENT_CD  "
				+ "  LEFT OUTER JOIN LOOKUP_VALUES C "
				+ "  ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='OPRTR' "
				+ " where A.ORG_ID=? AND A.ID =?  ORDER BY A.EVENT_DTTM DESC";
		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = null;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, Integer.toString(transID));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
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
		event = new LifecycleEvent(rs.getString("ORG_ID"),rs.getInt("ID"),rs.getString("ANIMAL_TAG"), rs.getString("EVENT_CD"),
				new User(rs.getString("CREATED_BY")),new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()),
				new User(rs.getString("UPDATED_BY")),new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone())
				);
		event.getEventType().setEventShortDescription(rs.getString("EVENT_SHORT_DESCR"));
		event.setEventTimeStamp(new DateTime(rs.getTimestamp("EVENT_DTTM"),IMDProperties.getServerTimeZone()));
		event.setEventNote(rs.getString("COMMENTS"));
		event.setEventOperator(new Person(rs.getString("OPERATOR"),rs.getString("OPERATOR_SHORT_DESCR"),"",""));
		event.setAuxField1Value(rs.getString("AUX_FL1_VALUE"));
		event.setAuxField2Value(rs.getString("AUX_FL2_VALUE"));
		event.setAuxField3Value(rs.getString("AUX_FL3_VALUE"));
		event.setAuxField4Value(rs.getString("AUX_FL4_VALUE"));
		event.setEventType(getLifeCycleEventCodeFromSQLRecord(rs));
		return event;
	}
	
	
	private LifeCycleEventCode getLifeCycleEventCodeFromSQLRecord(ResultSet rs) {
		String eventCd = setValueIfAvailable(rs, "EVENT_CD");
		String eventShortDescription = setValueIfAvailable(rs, "EVENT_SHORT_DESCR");
		String eventLongDescription = setValueIfAvailable(rs, "EVENT_LONG_DESCR");
		
		String field1Label = setValueIfAvailable(rs, "FIELD1_LABEL");
		String field1DataType = setValueIfAvailable(rs, "FIELD1_TYPE");
		String field1DataUnit = setValueIfAvailable(rs, "FIELD1_UNIT");
		
		String field2Label = setValueIfAvailable(rs, "FIELD2_LABEL");
		String field2DataType = setValueIfAvailable(rs, "FIELD2_TYPE");
		String field2DataUnit = setValueIfAvailable(rs, "FIELD2_UNIT");
		
		String field3Label = setValueIfAvailable(rs, "FIELD3_LABEL");
		String field3DataType = setValueIfAvailable(rs, "FIELD3_TYPE");
		String field3DataUnit = setValueIfAvailable(rs, "FIELD3_UNIT");
		
		String field4Label = setValueIfAvailable(rs, "FIELD4_LABEL");
		String field4DataType = setValueIfAvailable(rs, "FIELD4_TYPE");
		String field4DataUnit = setValueIfAvailable(rs, "FIELD4_UNIT");
		LifeCycleEventCode eventCode = null;
		try {
			eventCode = new LifeCycleEventCode(eventCd, eventShortDescription, eventLongDescription);
			eventCode.setField1Label(field1Label);
			eventCode.setField1DataType(field1DataType);
			eventCode.setField1DataUnit(field1DataUnit);
	
			eventCode.setField2Label(field2Label);
			eventCode.setField2DataType(field2DataType);
			eventCode.setField2DataUnit(field2DataUnit);		

			eventCode.setField3Label(field3Label);
			eventCode.setField3DataType(field3DataType);
			eventCode.setField3DataUnit(field3DataUnit);		

			eventCode.setField4Label(field4Label);
			eventCode.setField4DataType(field4DataType);
			eventCode.setField4DataUnit(field4DataUnit);		
			
			
		} catch (IMDException ex) {
			ex.printStackTrace();
		}
		return eventCode;
	}

	private String setValueIfAvailable(ResultSet rs, String columnName) {
		String valueOrEmpty = "";
		try {
			valueOrEmpty = rs.getString(columnName);
			
		} catch (Exception ex) {
			;//IMDLogger.log("Column " + columnName + " does not exist in the ResultSet", Util.ERROR);
		}
		return valueOrEmpty;
	}
	
	public List<LifecycleEvent> retrieveSpecificLifeCycleEventsForAnimal(String orgId, String animalTag, String eventCD) {
		return retrieveSpecificLifeCycleEventsForAnimal(orgId,animalTag,null,null,eventCD, null, null, null, null, null);
	}

	public List<LifecycleEvent> retrieveSpecificLifeCycleEventsForAnimal(String orgId, String tagNumber, DateTime fromDate, DateTime toDate, String eventTypeCD1, String eventTypeCD2, String auxField1Value, String auxField2Value, String auxField3Value, String auxField4Value) {
		ArrayList<LifecycleEvent> allAnimalEvents = new ArrayList<LifecycleEvent>();
		String qryString = " Select A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, C.SHORT_DESCR as OPERATOR_SHORT_DESCR, "
							+ " B.FIELD1_LABEL, B.FIELD1_TYPE, B.FIELD1_UNIT, "
							+ " B.FIELD2_LABEL, B.FIELD2_TYPE, B.FIELD2_UNIT, "
							+ " B.FIELD3_LABEL, B.FIELD3_TYPE, B.FIELD3_UNIT, "
							+ " B.FIELD4_LABEL, B.FIELD4_TYPE, B.FIELD4_UNIT  " 
							+ " from LIFECYCLE_EVENTS A  "
							+ "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT B  " 
							+ "  ON	A.EVENT_CD = B.EVENT_CD  " 
							+ "  LEFT OUTER JOIN LOOKUP_VALUES C "
							+ "  ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='OPRTR' "
							+ " where A.ORG_ID=?  AND A.ANIMAL_TAG =?  ";
		String event1Str = "";
		String event2Str = "";
		if (eventTypeCD1 != null && !eventTypeCD1.trim().isEmpty()) {
			event1Str = " A.EVENT_CD=? ";
		}
		if (eventTypeCD2 != null && !eventTypeCD2.trim().isEmpty()) {
			event2Str = " A.EVENT_CD=? ";
		}
	    
		if (event1Str.isEmpty() && event2Str.isEmpty()) {
			;
		} else if (event1Str.isEmpty() || event2Str.isEmpty()) {
			qryString += " AND " + event1Str + event2Str;
		} else if (!event1Str.isEmpty() && !event2Str.isEmpty()) {
			qryString += " AND (" + event1Str +  " OR " + event2Str + ") ";
		}
		qryString += (auxField1Value != null ? " AND AUX_FL1_VALUE=? " : "") + 
				(auxField2Value != null ? " AND AUX_FL2_VALUE=? "  :  "") + 
				(auxField3Value != null ? " AND AUX_FL3_VALUE=? " : "") + 
				(auxField4Value != null ? " AND AUX_FL4_VALUE=? " : "") + 
				(fromDate != null ? " AND A.EVENT_DTTM >=? " : "" ) + (toDate != null ? " AND A.EVENT_DTTM <=? " : "" ) +  " ORDER BY A.EVENT_DTTM DESC";		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		int index = 1;
		try {
			PreparedStatement preparedStatement = null;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, orgId);
			preparedStatement.setString(index++, tagNumber);
			if (eventTypeCD1 != null && !eventTypeCD1.trim().isEmpty())
				preparedStatement.setString(index++, eventTypeCD1);
			if (eventTypeCD2 != null && !eventTypeCD2.trim().isEmpty())
				preparedStatement.setString(index++, eventTypeCD2);
			if (auxField1Value != null)
				preparedStatement.setString(index++, auxField1Value);
			if (auxField2Value != null)
				preparedStatement.setString(index++, auxField2Value);
			if (auxField3Value != null)
				preparedStatement.setString(index++, auxField3Value);
			if (auxField4Value != null)
				preparedStatement.setString(index++, auxField4Value);
			if (fromDate != null) 
				preparedStatement.setString(index++, fromDate.toString());
			if (toDate != null) 
				preparedStatement.setString(index++, toDate.toString());
				
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
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
	
	public List<LifecycleEvent> retrieveSpecificLifeCycleEvents(String orgId, DateTime fromDate, DateTime toDate, String eventTypeCD1, String eventTypeCD2, String auxField1Value, String auxField2Value, String auxField3Value, String auxField4Value) {
		ArrayList<LifecycleEvent> allAnimalEvents = new ArrayList<LifecycleEvent>();
		String qryString = " Select A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, C.SHORT_DESCR as OPERATOR_SHORT_DESCR, "
							+ " B.FIELD1_LABEL, B.FIELD1_TYPE, B.FIELD1_UNIT, "
							+ " B.FIELD2_LABEL, B.FIELD2_TYPE, B.FIELD2_UNIT, "
							+ " B.FIELD3_LABEL, B.FIELD3_TYPE, B.FIELD3_UNIT, "
							+ " B.FIELD4_LABEL, B.FIELD4_TYPE, B.FIELD4_UNIT  " 
							+ " from LIFECYCLE_EVENTS A  "
							+ "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT B  " 
							+ "  ON	A.EVENT_CD = B.EVENT_CD  " 
							+ "  LEFT OUTER JOIN LOOKUP_VALUES C "
							+ "  ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='OPRTR' "
							+ " where A.ORG_ID=?  ";
		String event1Str = "";
		String event2Str = "";
		if (eventTypeCD1 != null && !eventTypeCD1.trim().isEmpty()) {
			event1Str = " A.EVENT_CD=? ";
		}
		if (eventTypeCD2 != null && !eventTypeCD2.trim().isEmpty()) {
			event2Str = " A.EVENT_CD=? ";
		}
	    
		if (event1Str.isEmpty() && event2Str.isEmpty()) {
			;
		} else if (event1Str.isEmpty() || event2Str.isEmpty()) {
			qryString += " AND " + event1Str + event2Str;
		} else if (!event1Str.isEmpty() && !event2Str.isEmpty()) {
			qryString += " AND (" + event1Str +  " OR " + event2Str + ") ";
		}
		qryString += (auxField1Value != null ? " AND AUX_FL1_VALUE=? " : "") + 
				(auxField2Value != null ? " AND AUX_FL2_VALUE=? "  :  "") + 
				(auxField3Value != null ? " AND AUX_FL3_VALUE=? " : "") + 
				(auxField4Value != null ? " AND AUX_FL4_VALUE=? " : "") + 
				(fromDate != null ? " AND A.EVENT_DTTM >=? " : "" ) + (toDate != null ? " AND A.EVENT_DTTM <=? " : "" ) +  " ORDER BY A.EVENT_DTTM DESC";		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		int index = 1;
		try {
			PreparedStatement preparedStatement = null;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, orgId);
			if (eventTypeCD1 != null && !eventTypeCD1.trim().isEmpty())
				preparedStatement.setString(index++, eventTypeCD1);
			if (eventTypeCD2 != null && !eventTypeCD2.trim().isEmpty())
				preparedStatement.setString(index++, eventTypeCD2);
			if (auxField1Value != null)
				preparedStatement.setString(index++, auxField1Value);
			if (auxField2Value != null)
				preparedStatement.setString(index++, auxField2Value);
			if (auxField3Value != null)
				preparedStatement.setString(index++, auxField3Value);
			if (auxField4Value != null)
				preparedStatement.setString(index++, auxField4Value);
			if (fromDate != null) 
				preparedStatement.setString(index++, Util.getDateTimeInSQLFormat(fromDate));
			if (toDate != null) 
				preparedStatement.setString(index++, Util.getDateTimeInSQLFormat(toDate));
				
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
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
		
	public List<LifecycleEvent> retrieveAllLifeCycleEventsForAnimal(String orgId, String tagNumber) {
		ArrayList<LifecycleEvent> allAnimalEvents = new ArrayList<LifecycleEvent>();
		String qryString = " Select A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, C.SHORT_DESCR as OPERATOR_SHORT_DESCR " +
		  " from LIFECYCLE_EVENTS A  " +
		  "	LEFT OUTER JOIN LV_LIFECYCLE_EVENT B  " +
		  "  ON	A.EVENT_CD = B.EVENT_CD  " +
		  "  LEFT OUTER JOIN LOOKUP_VALUES C " +
		  "  ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='OPRTR' " +
		  " where A.ORG_ID=?  AND A.ANIMAL_TAG =?  ORDER BY A.EVENT_DTTM DESC";		
		LifecycleEvent event = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = null;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, tagNumber);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
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
		String qryString = "DELETE FROM LIFECYCLE_EVENTS WHERE ORG_ID=? AND ID=?";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setInt(2, transId);
			
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
		if (event.getAuxField4Value() != null) {
			valuestoBeUpdated += ", AUX_FL4_VALUE=? ";
			updateString.add(event.getAuxField4Value());
		}
		if (event.getUpdatedBy() != null && !event.getUpdatedBy().getUserId().isEmpty()) {
			valuestoBeUpdated += ", UPDATED_BY=?";
			updateString.add(event.getUpdatedBy().getUserId());
		}
		if (event.getUpdatedDTTM() == null) {
			event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		}
		valuestoBeUpdated += ", UPDATED_DTTM=?";
		updateString.add(event.getUpdatedDTTMSQLFormat());

		
		if (valuestoBeUpdated.isEmpty()) {
			updatedRecordCount = 0;
		} else {
			qryString = qryString + "SET " + valuestoBeUpdated + " where ORG_ID =? AND ID=?";
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
				IMDLogger.log(preparedStatement.toString(), Util.INFO);

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
					if (preparedStatement != null && !preparedStatement.isClosed()) {
						preparedStatement.close();	
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return updatedRecordCount;
	}

	public int deleteLifeCycleEvent(String orgID, String eventTransactionID) {
		try {
			return deleteLifeCycleEvent(orgID, Integer.parseInt(eventTransactionID));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public int deleteAnimalLifecycleEvents(String orgID, String animalTag) {
		
		String qryString = "DELETE FROM LIFECYCLE_EVENTS WHERE ORG_ID=? AND ANIMAL_TAG=?";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, animalTag);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			
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

	public int determineInseminationAttemptCountInCurrentLactation(String orgID, String animalTag) {
		String qryString = "select animal_tag,count(*) AS INSEMINATION_ATTEMPTS_COUNT from LIFECYCLE_EVENTS A "
				+ "where A.ORG_ID = ? AND A.animal_tag=? and (event_cd=? OR event_cd=?) and "
				+ "event_dttm >= (select max(EVENT_DTTM) from LIFECYCLE_EVENTS where org_id=A.org_id and animal_tag=A.animal_tag and (event_cd=? OR event_cd=?  OR event_cd=?) )";
		int inseminationAttemptsCount = 0;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		ResultSet rs = null;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, animalTag);
			preparedStatement.setString(3, Util.LifeCycleEvents.INSEMINATE);
			preparedStatement.setString(4, Util.LifeCycleEvents.MATING);
			preparedStatement.setString(5, Util.LifeCycleEvents.PARTURATE);
			preparedStatement.setString(6, Util.LifeCycleEvents.ABORTION);
			preparedStatement.setString(7, Util.LifeCycleEvents.BIRTH);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	inseminationAttemptsCount = rs.getInt("INSEMINATION_ATTEMPTS_COUNT");
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log("Could not detemine insemination attempt for the animal [" + orgID + "] " + animalTag, Util.ERROR);
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
	    return inseminationAttemptsCount;
    }

	public String performEventSpecificValidations(LifeCycleEventBean eventBean, AnimalBean animalBean) {
		String validationMessage = "";
		AnimalLoader animalLoader = new AnimalLoader();
		if (eventBean.getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.MATING)) {
			try {
				Animal animal = animalLoader.getAnimalRawInfo(animalBean).get(0);
				AnimalBean mateBean = new AnimalBean();
				mateBean.setAnimalTag(eventBean.getAuxField1Value());
				mateBean.setOrgID(animal.getOrgID());
				List<Animal> matchingMates = animalLoader.getAnimalRawInfo(mateBean);
				if (matchingMates != null && !matchingMates.isEmpty()) {
					if (matchingMates.get(0).getGender() == animal.getGender()) {
						validationMessage = "Animal can not be of the same gender as the mate";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				validationMessage = "An exception occurred while trying to apply event validations. " + e.getMessage();
			}
		} else if (eventBean.getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.PARTURATE) && eventBean.getAuxField3Value() != null && !eventBean.getAuxField3Value().trim().isEmpty()) {
			try {
				List<Animal> animals = animalLoader.getAnimalRawInfo(animalBean.getOrgID(),eventBean.getAuxField3Value().trim());
				if (animals != null && !animals.isEmpty())
					validationMessage = "You have assigned tag# " + eventBean.getAuxField3Value().trim() + " to the new born calf. This tag# is already in use. Please assign another tag#. If you do not wish to add the calf to the herd at this moment, please remove the tag# and try again";
					
			} catch (Exception e) {
				e.printStackTrace();
				validationMessage = "An exception occurred while trying to apply " + Util.LifeCycleEvents.PARTURATE + " event validations. " + e.getMessage();
			}
		}
		return validationMessage;
	}
	public String performPostEventAdditionEventsUpdates(LifecycleEvent event, Animal animal, User user) {
		String additionalMessage = "";
		IMDLogger.log("Checking if this event addition warrants additional record updates ..." + event, Util.INFO);
		
		additionalMessage = updateLastInseminationOutcome(event, user, additionalMessage);
		additionalMessage += updateHerdLeavingDttm(event, animal, user);
		IMDLogger.log(additionalMessage, Util.INFO);			

		if (additionalMessage == null || additionalMessage.isEmpty())
				IMDLogger.log("No additional cascaded updates required.", Util.INFO);
		return additionalMessage;
	}

	
	
	
	private String updateHerdLeavingDttm(LifecycleEvent event, Animal animal, User user) {
		String outcome = "";
		try {
			if (event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.CULLED) || 
					event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.SOLD) ||
					event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.DEATH)) {
				AnimalLoader loader = new AnimalLoader();
				int recordUpdated = loader.updateAnimalHerdLeavingDTTM(animal.getOrgID(), animal.getAnimalTag(), event.getEventTimeStampSQLFormat(), user);
				if (recordUpdated == 1)
					outcome = ". The animal's herd leaving date has been set to : " + event.getEventTimeStampSQLFormat();
				else
					outcome = ". " + Util.ERROR_POSTFIX + "The animal's herd leaving date could NOT be set to : " + event.getEventTimeStamp() + ". Please set the date manually else this animal will continue to be considered as active.";
			} 
		}
		catch (Exception ex) {
			ex.printStackTrace();
			outcome = ". " + Util.ERROR_POSTFIX + "The animal's herd leaving date could NOT be set to : " + event.getEventTimeStamp() + ". Please set the date manually else this animal will continue to be considered as active (" + ex.getMessage() + ").";
		}
		return outcome;
	}

	private String updateLastInseminationOutcome(LifecycleEvent sourceEvent, User user, String additionalMessage) {
		String outcome = "";
		DateTime latestParturationOrAbortionTS = null;
		try {
			if (shouldUpdateInseminationResults(sourceEvent)) {
				IMDLogger.log("Updating the last insemination results", Util.INFO);
				
				List<LifecycleEvent> parturationAbortionEvents = retrieveSpecificLifeCycleEventsForAnimal(sourceEvent.getOrgID(), sourceEvent.getAnimalTag(), null, null, Util.LifeCycleEvents.PARTURATE, Util.LifeCycleEvents.ABORTION, null,null,null,null);
				if (parturationAbortionEvents != null && parturationAbortionEvents.size() >0)
					// the latest insemination should be after the latest parturation else we may end up updating an insmemination
					// from past lactation
					latestParturationOrAbortionTS = parturationAbortionEvents.get(0).getEventTimeStamp();

				List<LifecycleEvent> inseminationEvents = retrieveSpecificLifeCycleEventsForAnimal(sourceEvent.getOrgID(), sourceEvent.getAnimalTag(), null, null, Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING, null,null,null,null);
				outcome = ". We could not update the last insemination event outcome because no past Insemination or Mating event exists for this animal. This either indicates data entry problem or it could be that this is the first time this animal has come into heat. "
						+ "If this is the first time this animal has come into heat then you can ignore this warning; else "
						+ "make sure you have added an insemination or mating event for this animal and manually set the \"Insemination Successful?\" to \"NO\"";
				if (inseminationEvents != null && !inseminationEvents.isEmpty()) {
					Iterator<LifecycleEvent> it = inseminationEvents.iterator();
					String pregnancyTestResult = determineInseminationOutcomeValue(sourceEvent);
					while (it.hasNext()) {
						LifecycleEvent affectedEvent = it.next();
						if (latestParturationOrAbortionTS == null || latestParturationOrAbortionTS.isBefore(affectedEvent.getEventTimeStamp())) {
//						if (evt.getEventType().getEventCode().equals(Util.LifeCycleEvents.INSEMINATE) || evt.getEventType().getEventCode().equals(Util.LifeCycleEvents.MATING)) {
							affectedEvent.setUpdatedBy(user);
							if (affectedEvent.getEventType().getEventCode().equals(Util.LifeCycleEvents.MATING))
								affectedEvent.setAuxField2Value(pregnancyTestResult);
							else 
								affectedEvent.setAuxField3Value(pregnancyTestResult);
							int updateCount = updateLifeCycleEvent(affectedEvent);
							if (updateCount > 0)
								outcome = ". The outcome of the latest insemination/mating event (" + affectedEvent.getEventTransactionID() + ") was updated successfully";
							else 
								outcome = ". " + Util.ERROR_POSTFIX + "The outcome of the latest insemination/mating event (" + affectedEvent.getEventTransactionID() + ") could NOT be updated successfully. Please update this event manually.";
							break;									
						} else {
							outcome = ". We could not find any insemination or mating event for this animal since its latest parturition/abortion; therefore we can not update last insemination outcome";						
						}
					}
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			outcome = ". " + Util.ERROR_POSTFIX + "The outcome of the latest insemination/mating event could NOT be updated successfully. Please update this event manually (" + ex.getMessage() + ").";			
		}
		return outcome;
	}

	private String determineInseminationOutcomeValue(LifecycleEvent event) {
		String retValue = "";
		if (event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.HEAT))
			// if A cow comes in heat then this is A sign that previous insemination was unsuccessful.
			retValue = Util.NO.toUpperCase();
		else if (event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.PREGTEST))
			retValue = event.getAuxField1Value();
		return retValue;
	}

	private boolean shouldUpdateInseminationResults(LifecycleEvent event) {
		return 	(
					(event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.PREGTEST) && 
					event.getAuxField2Value() != null && 
					event.getAuxField2Value().equalsIgnoreCase(Util.YES)) 
					|| 
					(event.getEventType().getEventCode().equalsIgnoreCase(Util.LifeCycleEvents.HEAT) && 
					event.getAuxField1Value() != null && 
					event.getAuxField1Value().equalsIgnoreCase(Util.YES))
				);
	}

	public List<LifecycleEvent> getBirthsInSpecificDateRange(String orgID, DateTime startDate, DateTime endDate) {
		return retrieveSpecificLifeCycleEvents(orgID, startDate, endDate, Util.LifeCycleEvents.BIRTH,
				null, 
				null, null, null, null);
	}
	public List<LifecycleEvent> getInseminationsOrMatingInSpecificDateRange(String orgID, DateTime startDate, DateTime endDate) {
		return retrieveSpecificLifeCycleEvents(orgID, startDate, endDate, Util.LifeCycleEvents.INSEMINATE,
				Util.LifeCycleEvents.INSEMINATE, 
				null, null, null, null);
	}

	public List<LifecycleEvent> getAbortionsInSpecificDateRange(String orgID, DateTime startDate, DateTime endDate) {
		return retrieveSpecificLifeCycleEvents(orgID, startDate, endDate, Util.LifeCycleEvents.ABORTION,
				null, 
				null, null, null, null);
	}

	public List<LifecycleEvent> retrieveSireInseminationRecord(String orgID, String auxField1Value,
			String auxField3Value) {
		return retrieveSpecificLifeCycleEvents(orgID, null, null, 
				Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING, 
				auxField1Value, null, auxField3Value, null);
	}

	public List<LifecycleEvent> retrieveTwoRelevantWeightEvents(Animal animal, DateTime plusDays) {
		String qryString = "SELECT A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, C.SHORT_DESCR as OPERATOR_SHORT_DESCR " +
				   " FROM LIFECYCLE_EVENTS A  " +
				   " LEFT OUTER JOIN LV_LIFECYCLE_EVENT B " +
				   " 	ON	A.EVENT_CD = B.EVENT_CD  " +
				   " LEFT OUTER JOIN LOOKUP_VALUES C " +
				   " 	ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='OPRTR' " +
				   "     WHERE  A.ORG_ID=? AND A.ANIMAL_TAG=? AND A.EVENT_CD='" + Util.LifeCycleEvents.WEIGHT + "' AND " +
				   " A.EVENT_DTTM = (SELECT MAX(EVENT_DTTM) FROM imd.LIFECYCLE_EVENTS WHERE ANIMAL_TAG=A.ANIMAL_TAG AND ORG_ID=A.ORG_ID AND EVENT_CD=A.EVENT_CD AND EVENT_DTTM <= ?) " +
				   "  UNION " +
				   " SELECT A.*, B.SHORT_DESCR as EVENT_SHORT_DESCR, C.SHORT_DESCR as OPERATOR_SHORT_DESCR  " +
				   " FROM LIFECYCLE_EVENTS A " +
				   " LEFT OUTER JOIN LV_LIFECYCLE_EVENT B " +
				   " 	ON	A.EVENT_CD = B.EVENT_CD  " +
				   " LEFT OUTER JOIN LOOKUP_VALUES C " +
				   " 	ON C.LOOKUP_CD=A.OPERATOR and  C.CATEGORY_CD='" +  Util.LifeCycleEvents.WEIGHT + "' " +
				   "      WHERE A.ORG_ID=? AND A.ANIMAL_TAG=? AND A.EVENT_CD='WEIGHT' AND " +
				   "  A.EVENT_DTTM = (SELECT MIN(EVENT_DTTM) FROM imd.LIFECYCLE_EVENTS WHERE ANIMAL_TAG=A.ANIMAL_TAG AND ORG_ID=A.ORG_ID AND EVENT_CD=A.EVENT_CD AND EVENT_DTTM > ?)";
		List<LifecycleEvent> wtEvents = new ArrayList<LifecycleEvent>();
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		ResultSet rs = null;
		int i=1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, animal.getOrgID());
			preparedStatement.setString(i++, animal.getAnimalTag());
			preparedStatement.setString(i++, Util.getDateInSQLFormat(plusDays));
			preparedStatement.setString(i++, animal.getOrgID());
			preparedStatement.setString(i++, animal.getAnimalTag());
			preparedStatement.setString(i, Util.getDateInSQLFormat(plusDays));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	wtEvents.add(this.getLifeCycleEventFromSQLRecord(rs));
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log("Could not retrieve two relevant weight events for the animal [" + animal.getOrgID() +"-" + animal.getAnimalTag() + "]", Util.ERROR);
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
	    return wtEvents;
	}
}


