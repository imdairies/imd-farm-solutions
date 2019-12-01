package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class LookupValuesLoader {
	private static HashMap<String, LookupValues> loaderCache = new HashMap<String, LookupValues>();
	
	public LookupValuesLoader() {
	}
	
	public int insertLookupValues(LookupValues luValues) {
		String qryString = "insert into LOOKUP_VALUES (CATEGORY_CD,LOOKUP_CD,ACTIVE_IND,SHORT_DESCR,SHORT_DESCR_MSG_CD,LONG_DESCR,LONG_DESCR_MSG_CD,ADDITIONAL_FLD1,ADDITIONAL_FLD2,ADDITIONAL_FLD3,CREATED_BY,CREATED_DTTM,UPDATED_BY,UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int i=1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, luValues.getCategoryCode());
			preparedStatement.setString(i++, luValues.getLookupValueCode());
			preparedStatement.setString(i++, (luValues.isActive() ? "Y":"N"));		
			preparedStatement.setString(i++, luValues.getShortDescription());
			preparedStatement.setString(i++, luValues.getShortDescriptionMessageCd() == null ? null : luValues.getShortDescriptionMessageCd().toString());
			preparedStatement.setString(i++, luValues.getLongDescription());
			preparedStatement.setString(i++, luValues.getLongDescriptionMessageCd() == null ? null : luValues.getLongDescriptionMessageCd().toString());
			preparedStatement.setString(i++, luValues.getAdditionalField1());
			preparedStatement.setString(i++, luValues.getAdditionalField2());
			preparedStatement.setString(i++, luValues.getAdditionalField3());
			preparedStatement.setString(i++, luValues.getCreatedBy().getUserId());
			preparedStatement.setString(i++, luValues.getCreatedDTTMSQLFormat());
			preparedStatement.setString(i++, luValues.getUpdatedBy().getUserId());
			preparedStatement.setString(i++, luValues.getUpdatedDTTMSQLFormat());
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
	public List<LookupValues> retrieveLookupValues(LookupValuesBean searchBean) {
		return performSearch(searchBean.getCategoryCode(), searchBean.getLookupValueCode(), searchBean.getActiveIndicator(), false);
	}
	public  LookupValues retrieveLookupValue(String categoryCode, String lookupValueCode) {
		LookupValues luValue = loaderCache.get(categoryCode + "-" + lookupValueCode);
		if (luValue != null) {
			return luValue;
		} else {
			List<LookupValues> luValues = performSearch(categoryCode, lookupValueCode, "Y", false);
			if (luValues != null && luValues.size() >= 1) {
				loaderCache.put(luValues.get(0).getCategoryCode() + "-" + luValues.get(0).getLookupValueCode(), luValues.get(0));
				return luValues.get(0);
			}
			else 
				return null;
		}
	}
	public List<LookupValues> retrieveMatchingLookupValues(LookupValuesBean searchBean) {
		return performSearch(searchBean.getCategoryCode(), searchBean.getLookupValueCode(), searchBean.getActiveIndicator(), true);
	}
	private List<LookupValues> performSearch(String categoryCD, String lookupCD, String retrieveActiveOnlyInd, boolean isWildCardSearch) {
		ArrayList<LookupValues> allMatchingValues = new ArrayList<LookupValues>();
		String qryString = "Select * from LOOKUP_VALUES ";
		List<String> values = new ArrayList<String> ();
		
		if (categoryCD != null && !categoryCD.trim().isEmpty()) {
			qryString +=  " WHERE CATEGORY_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");
			values.add(categoryCD);
			if (lookupCD != null && !lookupCD.trim().isEmpty()) {
				qryString +=  " AND LOOKUP_CD " + (isWildCardSearch ?  " LIKE ? " : " = ?");				
				values.add(lookupCD);
				if (retrieveActiveOnlyInd != null && !retrieveActiveOnlyInd.trim().isEmpty()) {
					qryString +=  " AND ACTIVE_IND = ? ";				
					values.add(retrieveActiveOnlyInd);
				}				
			} else if (retrieveActiveOnlyInd != null && !retrieveActiveOnlyInd.trim().isEmpty()) {
					qryString +=  " AND ACTIVE_IND = ? ";				
					values.add(retrieveActiveOnlyInd);
			}
		} else if (lookupCD != null && !lookupCD.trim().isEmpty()) {
			qryString +=  " WHERE LOOKUP_CD " + (isWildCardSearch ?  " LIKE ? " : "= ?");
			values.add(lookupCD);
			if (retrieveActiveOnlyInd != null && !retrieveActiveOnlyInd.trim().isEmpty()) {
				qryString +=  " AND ACTIVE_IND = ? ";				
				values.add(retrieveActiveOnlyInd);
			}
		} else {
			qryString = "Select * from LOOKUP_VALUES " ; 
			if (retrieveActiveOnlyInd != null && !retrieveActiveOnlyInd.trim().isEmpty()) {
				qryString +=  " WHERE ACTIVE_IND = ? ";				
				values.add(retrieveActiveOnlyInd);
			}
		}
		qryString += " ORDER BY CATEGORY_CD,LOOKUP_CD" ;
		LookupValues luValue = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			Iterator<String> it = values.iterator();
			int i=1;
			while (it.hasNext())
				preparedStatement.setString(i++,it.next());
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	luValue = getDTOFromSQLRecord(rs);
		    	loaderCache.put(luValue.getCategoryCode() + "-" + luValue.getLookupValueCode(), luValue);
		    	allMatchingValues.add(luValue);
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
	    return allMatchingValues;
	}
	
	private LookupValues getDTOFromSQLRecord(ResultSet rs) throws Exception {
		LookupValues luValue;
		luValue = new LookupValues(rs.getString("CATEGORY_CD"),rs.getString("LOOKUP_CD"),rs.getString("SHORT_DESCR"),rs.getString("LONG_DESCR"));
		if (rs.getString("ACTIVE_IND").equalsIgnoreCase("Y")) {
			luValue.markActive();
		} else
			luValue.markInActive();
		luValue.setAdditionalField1(rs.getString("ADDITIONAL_FLD1"));
		luValue.setAdditionalField2(rs.getString("ADDITIONAL_FLD2"));
		luValue.setAdditionalField3(rs.getString("ADDITIONAL_FLD3"));
		luValue.setShortDescriptionMessageCd(rs.getString("SHORT_DESCR_MSG_CD"));
		luValue.setLongDescriptionMessageCd(rs.getString("LONG_DESCR_MSG_CD"));
		luValue.setCreatedBy(new User(rs.getString("CREATED_BY")));
		luValue.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		luValue.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		luValue.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		return luValue;
	}

	/**
	 * We should not use this method. This method is only for internal use. This Delete method does not have any corresponding API; so it can't be 
	 * called from the front end application. We should mark records "inactive" instead of deleting them. This method is only used during
	 * unit testing to clean up test records.
	 * @param eventCd
	 * @return
	 */
	public int deleteLookupValue( String categoryCD, String valueCD) {
		String qryString = "DELETE FROM LOOKUP_VALUES where CATEGORY_CD=? AND LOOKUP_CD = ? ";
		int result = -1;		
		IMDLogger.log(qryString,Util.INFO);
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, categoryCD);
			preparedStatement.setString(2, valueCD);
			result = preparedStatement.executeUpdate();
			loaderCache.remove(categoryCD + "-" + valueCD);
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

	
	public int updateLookupValues(LookupValues luValue) {
		String qryString = "UPDATE LOOKUP_VALUES ";
		String valuestoBeUpdated = "";
		int updatedRecordCount = 0;
		PreparedStatement preparedStatement = null;
		
		List<String> updateString = new ArrayList<String>();
		valuestoBeUpdated = " ACTIVE_IND=? ";
		updateString.add((luValue.isActive() ? "Y": "N"));
//		if (luValue.getAdditionalField1() != null && !luValue.getAdditionalField1().isEmpty()) {
			valuestoBeUpdated += ", ADDITIONAL_FLD1=?";
			updateString.add(luValue.getAdditionalField1());
//		}
//		if (luValue.getAdditionalField2() != null && !luValue.getAdditionalField2().isEmpty()) {
			valuestoBeUpdated += ", ADDITIONAL_FLD2=?";
			updateString.add(luValue.getAdditionalField2());
//		}
//		if (luValue.getAdditionalField3() != null && !luValue.getAdditionalField3().isEmpty()) {
			valuestoBeUpdated += ", ADDITIONAL_FLD3=?";
			updateString.add(luValue.getAdditionalField3());
//		}
		if (luValue.getShortDescription() != null && !luValue.getShortDescription().isEmpty()) {
			valuestoBeUpdated += ", SHORT_DESCR=?";
			updateString.add(luValue.getShortDescription());
		}
		if (luValue.getShortDescriptionMessageCd() != null) {
			valuestoBeUpdated += ", SHORT_DESCR_MSG_CD=?";
			updateString.add(luValue.getShortDescriptionMessageCd().toString());
		}
		if (luValue.getLongDescriptionMessageCd() != null) {
			valuestoBeUpdated += ", LONG_DESCR_MSG_CD=?";
			updateString.add(luValue.getLongDescriptionMessageCd().toString());
		}
		if (luValue.getLongDescription() != null && !luValue.getLongDescription().isEmpty()) {			
			valuestoBeUpdated += ", LONG_DESCR=?";
			updateString.add(luValue.getLongDescription());
		}
		if (luValue.getUpdatedBy() != null && !luValue.getUpdatedBy().getUserId().isEmpty()) {
			valuestoBeUpdated += ", UPDATED_BY=?";
			updateString.add(luValue.getUpdatedBy().getUserId());
		}
		if (luValue.getUpdatedDTTM() != null) {
			valuestoBeUpdated += ", UPDATED_DTTM=?";
			updateString.add(luValue.getUpdatedDTTMSQLFormat());
		}
		
		IMDLogger.log(valuestoBeUpdated, Util.INFO);
		
		if (valuestoBeUpdated.isEmpty()) {
			updatedRecordCount = 0;
		} else {
			qryString = qryString + "SET " + valuestoBeUpdated + " where CATEGORY_CD = ? AND LOOKUP_CD =? " ;
			PreparedStatement st = null;
			IMDLogger.log(qryString, Util.INFO);
			Connection conn = DBManager.getDBConnection();
			try {
				preparedStatement = conn.prepareStatement(qryString);
				Iterator<String> it = updateString.iterator();
				int i=1;
				while (it.hasNext()) {
					preparedStatement.setString(i++,it.next());
				}
				preparedStatement.setString(i++,luValue.getCategoryCode());
				preparedStatement.setString(i,luValue.getLookupValueCode());
				updatedRecordCount = preparedStatement.executeUpdate();
				loaderCache.remove(luValue.getCategoryCode() + "-" + luValue.getLookupValueCode());
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
