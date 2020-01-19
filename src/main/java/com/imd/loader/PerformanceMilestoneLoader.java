package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.PerformanceMilestone;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class PerformanceMilestoneLoader {
	
	public List<PerformanceMilestone> retrieveActivePerformanceMilestones(String orgID) {
		return retrieveSpecificPerformanceMilestone(orgID, null);		
	}
	public List<PerformanceMilestone> retrieveSpecificPerformanceMilestone(String orgID, String milestoneID) {
		String qryString = " SELECT " + 
				" A.ORG_ID,  " + 
				" A.MILESTONE_ID, " + 
				" A.ENABLE_IND AS MILESTONE_ENABLED, " +  
				" B.ENABLE_IND AS TEMPLATE_ENABLED, " + 
				" B.SHORT_DESCR, " + 
				" B.SHORT_DESCR_CD, " + 
				" B.LONG_DESCR, " + 
				" B.LONG_DESCR_CD, " + 
				" A.ONE_STAR_TH, " + 
				" A.TWO_STAR_TH, " + 
				" A.THREE_STAR_TH, " + 
				" A.FOUR_STAR_TH, " + 
				" A.FIVE_STAR_TH, " + 
				" A.AUX_INFO1, " + 
				" A.AUX_INFO2, " + 
				" A.AUX_INFO3, " + 
				" A.AUX_INFO4, " + 
				" A.AUX_INFO5, " + 
				" A.CREATED_BY, " + 
				" A.CREATED_DTTM, " + 
				" A.UPDATED_BY, " + 
				" A.UPDATED_DTTM " + 
				" FROM imd.PERF_MILESTONES A,  " + 
				" imd.PERF_MILESTONE_TEMPLATE B  " + 
				" WHERE A.ORG_ID=? AND " +  (milestoneID != null ? " A.MILESTONE_ID=? AND " : "") +
				" A.MILESTONE_ID=B.MILESTONE_ID AND A.ENABLE_IND='Y' AND B.ENABLE_IND='Y' ORDER BY A.UPDATED_DTTM  DESC";
		List<PerformanceMilestone> perfMilestones = new ArrayList<PerformanceMilestone>();
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			if (milestoneID != null)
				preparedStatement.setString(2, milestoneID);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	perfMilestones.add(getMilestoneFromSQLRecord(rs));		    	
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log("Error occurred while retrieving active performance milestones", Util.ERROR);
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
	    return perfMilestones;		
	}
	private PerformanceMilestone getMilestoneFromSQLRecord(ResultSet rs) throws Exception {
		PerformanceMilestone milestone = new PerformanceMilestone();
		milestone.setOrgID(rs.getString("ORG_ID"));
		milestone.setMilestoneID(rs.getString("MILESTONE_ID"));
		milestone.setEnabled(rs.getString("TEMPLATE_ENABLED").equalsIgnoreCase("Y") ? true : false);
		milestone.setEnabledForOrg(rs.getString("MILESTONE_ENABLED").equalsIgnoreCase("Y") ? true : false);
		milestone.setShortDescription(rs.getString("SHORT_DESCR"));
		milestone.setLongDescription(rs.getString("LONG_DESCR"));
		milestone.setShortDescriptionCd(rs.getString("SHORT_DESCR_CD"));
		milestone.setLongDescriptionCd(rs.getString("LONG_DESCR_CD"));
		milestone.setOneStarThreshold(rs.getString("ONE_STAR_TH") == null ? null : rs.getFloat("ONE_STAR_TH"));
		milestone.setTwoStarThreshold(rs.getString("TWO_STAR_TH") == null ? null : rs.getFloat("TWO_STAR_TH"));
		milestone.setThreeStarThreshold(rs.getString("THREE_STAR_TH") == null ? null : rs.getFloat("THREE_STAR_TH"));
		milestone.setFourStarThreshold(rs.getString("FOUR_STAR_TH") == null ? null : rs.getFloat("FOUR_STAR_TH"));
		milestone.setFiveStarThreshold(rs.getString("FIVE_STAR_TH") == null ? null : rs.getFloat("FIVE_STAR_TH"));
		milestone.setAuxInfo1(rs.getString("AUX_INFO1"));
		milestone.setAuxInfo2(rs.getString("AUX_INFO2"));
		milestone.setAuxInfo3(rs.getString("AUX_INFO3"));
		milestone.setAuxInfo4(rs.getString("AUX_INFO4"));
		milestone.setAuxInfo5(rs.getString("AUX_INFO5"));
		milestone.setCreatedBy(new User(rs.getString("CREATED_BY")));
		milestone.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		milestone.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		milestone.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		return milestone;
	}
}



