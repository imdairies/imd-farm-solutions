package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Advisement;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class AdvisementLoader {
	
	public Advisement retrieveAdvisementRule(String orgId, String advisementID, boolean activeOnly) {
		String qryString = " SELECT  B.ORG_ID AS ORG_ID, " + 
				" A.ADVISEMENT_ID AS ADVISEMENT_ID, " +
				" B.ADVISEMENT_ID AS ORG_ADVISEMENT_ID, " + 
				" A.ENABLE_IND AS RULE_ENABLED, " + 
				" B.ENABLE_IND AS RULE_ENABLED_FOR_ORG, " + 
				" A.short_descr AS SHORT_DESCR, " + 
				" A.LONG_DESCR AS LONG_DESCR, " + 
				" B.THRESHOLD1 AS THRESHOLD1, " + 
				" B.THRESHOLD2 AS THRESHOLD2, " + 
				" B.THRESHOLD3 AS THRESHOLD3, " + 
				" B.THRESHOLD1_MSG AS THRESHOLD1_MSG, " + 
				" B.THRESHOLD2_MSG AS THRESHOLD2_MSG, " + 
				" B.THRESHOLD3_MSG AS THRESHOLD3_MSG, " + 
				
				" B.THRESHOLD1_MSG_CD AS THRESHOLD1_MSG_CD, " + 
				" B.THRESHOLD2_MSG_CD AS THRESHOLD2_MSG_CD, " + 
				" B.THRESHOLD3_MSG_CD AS THRESHOLD3_MSG_CD, " + 
				" B.AUX_INFO1 AS AUX_INFO1, " + 
				" B.AUX_INFO2 AS AUX_INFO2, " + 
				" B.AUX_INFO3 AS AUX_INFO3, " + 
				" B.AUX_INFO4 AS AUX_INFO4, " + 
				" B.AUX_INFO5 AS AUX_INFO5, " + 
				" B.EMAIL_IND AS EMAIL_IND, " + 
				" B.SMS_IND AS SMS_IND, " + 
				" B.WEB_IND AS WEB_IND, " + 
				" A.CREATED_BY AS TEMPLATE_CREATED_BY, " + 
				" A.CREATED_DTTM AS TEMPLATE_CREATED_DTTM, " + 
				" A.UPDATED_BY AS TEMPLATE_UPDATED_BY, " + 
				" A.UPDATED_DTTM AS TEMPLATE_UPDATED_DTTM, " + 
				" B.CREATED_BY AS CREATED_BY, " + 
				" B.CREATED_DTTM AS CREATED_DTTM, " + 
				" B.UPDATED_BY AS UPDATED_BY, " + 
				" B.UPDATED_DTTM AS UPDATED_DTTM " +			    
				" FROM " +   
				" imd.ADVISEMENT_RULE_TEMPLATE A, imd.ADVISEMENT_RULES B " + 
			    " where " +   
				" A.ADVISEMENT_ID = B.ADVISEMENT_ID AND B.ORG_ID=? AND A.ADVISEMENT_ID=? " + (activeOnly? " AND A.ENABLE_IND='Y' AND B.ENABLE_IND='Y' ": " ");
		
		Advisement advRule = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, advisementID);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	advRule = getAdvisementRuleFromSQLRecord(rs);
		    	IMDLogger.log(advRule.toString(), Util.INFO);
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
	    return advRule;
	}

	private Advisement getAdvisementRuleFromSQLRecord(ResultSet rs) throws IMDException, SQLException {
		Advisement advRule = new Advisement();
		advRule.setOrgID(rs.getString("ORG_ID"));
		advRule.setAdvisementID(rs.getString("ADVISEMENT_ID"));
		advRule.markEnabled(rs.getString("RULE_ENABLED").equalsIgnoreCase("Y") ? true : false);
		advRule.markEnabledForOrg(rs.getString("RULE_ENABLED_FOR_ORG").equalsIgnoreCase("Y") ? true : false);
		advRule.setShortDescription(rs.getString("SHORT_DESCR"));
		advRule.setLongDescription(rs.getString("LONG_DESCR"));
		advRule.setFirstThreshold(rs.getString("THRESHOLD1") == null ? -999f : rs.getFloat("THRESHOLD1"));
		advRule.setSecondThreshold(rs.getString("THRESHOLD2") == null ? -999f : rs.getFloat("THRESHOLD2"));
		advRule.setThirdThreshold(rs.getString("THRESHOLD3") == null ? -999f : rs.getFloat("THRESHOLD3"));
		advRule.setFirstThresholdMessageCode(rs.getString("THRESHOLD1_MSG_CD") == null ? 0 : rs.getInt("THRESHOLD1_MSG_CD"));
		advRule.setSecondThresholdMessageCode(rs.getString("THRESHOLD2_MSG_CD") == null ? 0 : rs.getInt("THRESHOLD2_MSG_CD"));
		advRule.setThirdThresholdMessageCode(rs.getString("THRESHOLD3_MSG_CD") == null ? 0 : rs.getInt("THRESHOLD3_MSG_CD"));
		advRule.setFirstThresholdMessage(rs.getString("THRESHOLD1_MSG"));
		advRule.setSecondThresholdMessage(rs.getString("THRESHOLD2_MSG"));
		advRule.setThirdThresholdMessage(rs.getString("THRESHOLD3_MSG"));
		advRule.setEmailInd(rs.getString("EMAIL_IND"));
		advRule.setSMSInd(rs.getString("SMS_IND"));
		advRule.setWebInd(rs.getString("WEB_IND"));
		advRule.setAuxInfo1(rs.getString("AUX_INFO1"));
		advRule.setAuxInfo2(rs.getString("AUX_INFO2"));
		advRule.setAuxInfo3(rs.getString("AUX_INFO3"));
		advRule.setAuxInfo4(rs.getString("AUX_INFO4"));
		advRule.setAuxInfo5(rs.getString("AUX_INFO5"));
		advRule.setCreatedBy(new User(rs.getString("CREATED_BY")));
		advRule.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		advRule.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		advRule.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		return advRule;
	}

	public List<Advisement> getAllActiveRules(String orgId) {
		String qryString = " SELECT  B.ORG_ID AS ORG_ID, " + 
				" A.ADVISEMENT_ID AS ADVISEMENT_ID, " +
				" B.ADVISEMENT_ID AS ORG_ADVISEMENT_ID, " + 
				" A.ENABLE_IND AS RULE_ENABLED, " + 
				" B.ENABLE_IND AS RULE_ENABLED_FOR_ORG, " + 
				" A.short_descr AS SHORT_DESCR, " + 
				" A.LONG_DESCR AS LONG_DESCR, " + 
				" B.THRESHOLD1 AS THRESHOLD1, " + 
				" B.THRESHOLD2 AS THRESHOLD2, " + 
				" B.THRESHOLD3 AS THRESHOLD3, " + 
				" B.THRESHOLD1_MSG AS THRESHOLD1_MSG, " + 
				" B.THRESHOLD2_MSG AS THRESHOLD2_MSG, " + 
				" B.THRESHOLD3_MSG AS THRESHOLD3_MSG, " + 
				
				" B.THRESHOLD1_MSG_CD AS THRESHOLD1_MSG_CD, " + 
				" B.THRESHOLD2_MSG_CD AS THRESHOLD2_MSG_CD, " + 
				" B.THRESHOLD3_MSG_CD AS THRESHOLD3_MSG_CD, " + 
				
				" B.AUX_INFO1 AS AUX_INFO1, " + 
				" B.AUX_INFO2 AS AUX_INFO2, " + 
				" B.AUX_INFO3 AS AUX_INFO3, " + 
				" B.AUX_INFO4 AS AUX_INFO4, " + 
				" B.AUX_INFO5 AS AUX_INFO5, " + 
				" B.EMAIL_IND AS EMAIL_IND, " + 
				" B.SMS_IND AS SMS_IND, " + 
				" B.WEB_IND AS WEB_IND, " + 
				" A.CREATED_BY AS TEMPLATE_CREATED_BY, " + 
				" A.CREATED_DTTM AS TEMPLATE_CREATED_DTTM, " + 
				" A.UPDATED_BY AS TEMPLATE_UPDATED_BY, " + 
				" A.UPDATED_DTTM AS TEMPLATE_UPDATED_DTTM, " + 
				" B.CREATED_BY AS CREATED_BY, " + 
				" B.CREATED_DTTM AS CREATED_DTTM, " + 
				" B.UPDATED_BY AS UPDATED_BY, " + 
				" B.UPDATED_DTTM AS UPDATED_DTTM " +			    
				" FROM " +   
				" imd.ADVISEMENT_RULE_TEMPLATE A, imd.ADVISEMENT_RULES B " + 
			    " where " +   
				" A.ADVISEMENT_ID = B.ADVISEMENT_ID AND B.ORG_ID=? AND A.ENABLE_IND='Y' and B.ENABLE_IND='Y'";
		
		List<Advisement> advRules = new ArrayList<Advisement>();
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	advRules.add(getAdvisementRuleFromSQLRecord(rs));
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
	    return advRules;
	}
	public List<Advisement> getSpecifiedActiveAdvisementRules(String orgId, String advisementID) {
		String qryString = " SELECT  B.ORG_ID AS ORG_ID, " + 
				" A.ADVISEMENT_ID AS ADVISEMENT_ID, " +
				" B.ADVISEMENT_ID AS ORG_ADVISEMENT_ID, " + 
				" A.ENABLE_IND AS RULE_ENABLED, " + 
				" B.ENABLE_IND AS RULE_ENABLED_FOR_ORG, " + 
				" A.short_descr AS SHORT_DESCR, " + 
				" A.LONG_DESCR AS LONG_DESCR, " + 
				" B.THRESHOLD1 AS THRESHOLD1, " + 
				" B.THRESHOLD2 AS THRESHOLD2, " + 
				" B.THRESHOLD3 AS THRESHOLD3, " + 
				" B.THRESHOLD1_MSG AS THRESHOLD1_MSG, " + 
				" B.THRESHOLD2_MSG AS THRESHOLD2_MSG, " + 
				" B.THRESHOLD3_MSG AS THRESHOLD3_MSG, " + 

				" B.THRESHOLD1_MSG_CD AS THRESHOLD1_MSG_CD, " + 
				" B.THRESHOLD2_MSG_CD AS THRESHOLD2_MSG_CD, " + 
				" B.THRESHOLD3_MSG_CD AS THRESHOLD3_MSG_CD, " + 
				
				
				" B.AUX_INFO1 AS AUX_INFO1, " + 
				" B.AUX_INFO2 AS AUX_INFO2, " + 
				" B.AUX_INFO3 AS AUX_INFO3, " + 
				" B.AUX_INFO4 AS AUX_INFO4, " + 
				" B.AUX_INFO5 AS AUX_INFO5, " + 
				" B.EMAIL_IND AS EMAIL_IND, " + 
				" B.SMS_IND AS SMS_IND, " + 
				" B.WEB_IND AS WEB_IND, " + 
				" A.CREATED_BY AS TEMPLATE_CREATED_BY, " + 
				" A.CREATED_DTTM AS TEMPLATE_CREATED_DTTM, " + 
				" A.UPDATED_BY AS TEMPLATE_UPDATED_BY, " + 
				" A.UPDATED_DTTM AS TEMPLATE_UPDATED_DTTM, " + 
				" B.CREATED_BY AS CREATED_BY, " + 
				" B.CREATED_DTTM AS CREATED_DTTM, " + 
				" B.UPDATED_BY AS UPDATED_BY, " + 
				" B.UPDATED_DTTM AS UPDATED_DTTM " +			    
				" FROM " +   
				" imd.ADVISEMENT_RULE_TEMPLATE A, imd.ADVISEMENT_RULES B " + 
			    " where " +   
				" A.ADVISEMENT_ID = B.ADVISEMENT_ID AND B.ORG_ID=? AND A.ADVISEMENT_ID = ? AND A.ENABLE_IND='Y' and B.ENABLE_IND='Y'";
		
		List<Advisement> advRules = new ArrayList<Advisement>();
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, advisementID);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	advRules.add(getAdvisementRuleFromSQLRecord(rs));
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
	    return advRules;
	}



}
