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
import com.imd.util.Util;

public class AdvisementLoader {
	
	public Advisement retrieveAdvisementRule(String orgId, String advisementID, boolean activeOnly) {
		String qryString = " SELECT  b.ORG_ID AS ORG_ID, " + 
				" a.ADVISEMENT_ID AS ADVISEMENT_ID, " +
				" b.ADVISEMENT_ID AS ORG_ADVISEMENT_ID, " + 
				" a.ENABLE_IND AS RULE_ENABLED, " + 
				" b.ENABLE_IND AS RULE_ENABLED_FOR_ORG, " + 
				" a.short_descr AS SHORT_DESCR, " + 
				" A.LONG_DESCR AS LONG_DESCR, " + 
				" b.THRESHOLD1 AS THRESHOLD1, " + 
				" b.THRESHOLD2 AS THRESHOLD2, " + 
				" b.THRESHOLD3 AS THRESHOLD3, " + 
				" b.THRESHOLD1_MSG AS THRESHOLD1_MSG, " + 
				" b.THRESHOLD2_MSG AS THRESHOLD2_MSG, " + 
				" b.THRESHOLD3_MSG AS THRESHOLD3_MSG, " + 
				" b.EMAIL_IND AS EMAIL_IND, " + 
				" b.SMS_IND AS SMS_IND, " + 
				" b.WEB_IND AS WEB_IND, " + 
				" a.CREATED_BY AS TEMPLATE_CREATED_BY, " + 
				" a.CREATED_DTTM AS TEMPLATE_CREATED_DTTM, " + 
				" a.UPDATED_BY AS TEMPLATE_UPDATED_BY, " + 
				" a.UPDATED_DTTM AS TEMPLATE_UPDATED_DTTM, " + 
				" b.CREATED_BY AS CREATED_BY, " + 
				" b.CREATED_DTTM AS CREATED_DTTM, " + 
				" b.UPDATED_BY AS UPDATED_BY, " + 
				" b.UPDATED_DTTM AS UPDATED_DTTM " +			    
				" FROM " +   
				" imd.ADVISEMENT_RULE_TEMPLATE a, imd.ADVISEMENT_RULES b " + 
			    " where " +   
				" a.ADVISEMENT_ID = b.ADVISEMENT_ID AND b.ORG_ID=? AND A.ADVISEMENT_ID=? " + (activeOnly? " AND a.ENABLE_IND='Y' AND b.ENABLE_IND='Y' ": " ");
		
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
		advRule.setOrgId(rs.getString("ORG_ID"));
		advRule.setAdvisementID(rs.getString("ADVISEMENT_ID"));
		advRule.markEnabled(rs.getString("RULE_ENABLED").equalsIgnoreCase("Y") ? true : false);
		advRule.markEnabledForOrg(rs.getString("RULE_ENABLED_FOR_ORG").equalsIgnoreCase("Y") ? true : false);
		advRule.setShortDescription(rs.getString("SHORT_DESCR"));
		advRule.setLongDescription(rs.getString("LONG_DESCR"));
		advRule.setFirstThreshold(rs.getString("THRESHOLD1") == null ? -999f : rs.getFloat("THRESHOLD1"));
		advRule.setSecondThreshold(rs.getString("THRESHOLD2") == null ? -999f : rs.getFloat("THRESHOLD2"));
		advRule.setThirdThreshold(rs.getString("THRESHOLD3") == null ? -999f : rs.getFloat("THRESHOLD3"));
		advRule.setFirstThresholdMessage(rs.getString("THRESHOLD1_MSG"));
		advRule.setSecondThresholdMessage(rs.getString("THRESHOLD2_MSG"));
		advRule.setThirdThresholdMessage(rs.getString("THRESHOLD3_MSG"));
		advRule.setEmailInd(rs.getString("EMAIL_IND"));
		advRule.setSMSInd(rs.getString("SMS_IND"));
		advRule.setWebInd(rs.getString("WEB_IND"));
		advRule.setCreatedBy(new User(rs.getString("CREATED_BY")));
		advRule.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		advRule.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		advRule.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return advRule;
	}

	public List<Advisement> getAllActiveRules(String orgId) {
		String qryString = " SELECT  b.ORG_ID AS ORG_ID, " + 
				" a.ADVISEMENT_ID AS ADVISEMENT_ID, " +
				" b.ADVISEMENT_ID AS ORG_ADVISEMENT_ID, " + 
				" a.ENABLE_IND AS RULE_ENABLED, " + 
				" b.ENABLE_IND AS RULE_ENABLED_FOR_ORG, " + 
				" a.short_descr AS SHORT_DESCR, " + 
				" A.LONG_DESCR AS LONG_DESCR, " + 
				" b.THRESHOLD1 AS THRESHOLD1, " + 
				" b.THRESHOLD2 AS THRESHOLD2, " + 
				" b.THRESHOLD3 AS THRESHOLD3, " + 
				" b.THRESHOLD1_MSG AS THRESHOLD1_MSG, " + 
				" b.THRESHOLD2_MSG AS THRESHOLD2_MSG, " + 
				" b.THRESHOLD3_MSG AS THRESHOLD3_MSG, " + 
				" b.EMAIL_IND AS EMAIL_IND, " + 
				" b.SMS_IND AS SMS_IND, " + 
				" b.WEB_IND AS WEB_IND, " + 
				" a.CREATED_BY AS TEMPLATE_CREATED_BY, " + 
				" a.CREATED_DTTM AS TEMPLATE_CREATED_DTTM, " + 
				" a.UPDATED_BY AS TEMPLATE_UPDATED_BY, " + 
				" a.UPDATED_DTTM AS TEMPLATE_UPDATED_DTTM, " + 
				" b.CREATED_BY AS CREATED_BY, " + 
				" b.CREATED_DTTM AS CREATED_DTTM, " + 
				" b.UPDATED_BY AS UPDATED_BY, " + 
				" b.UPDATED_DTTM AS UPDATED_DTTM " +			    
				" FROM " +   
				" imd.ADVISEMENT_RULE_TEMPLATE a, imd.ADVISEMENT_RULES b " + 
			    " where " +   
				" a.ADVISEMENT_ID = b.ADVISEMENT_ID AND b.ORG_ID=? AND a.ENABLE_IND='Y' and b.ENABLE_IND='Y'";
		
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
}
