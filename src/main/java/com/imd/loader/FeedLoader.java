package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class FeedLoader {
	
	public int deleteFeedPlanItem(FeedItem feedItem, String startEndQuery, Float start, Float end) {
		String qryString = "DELETE FROM imd.FEED_PLAN WHERE ORG_ID=? AND FEED_ITEM = ? AND FEED_COHORT = ? " + (startEndQuery == null ? "" : startEndQuery);
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			int i= 1;
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, feedItem.getOrgID());
			preparedStatement.setString(i++, feedItem.getFeedItemLookupValue().getLookupValueCode());
			preparedStatement.setString(i++, feedItem.getFeedCohortCD().getLookupValueCode());
			if (start != null)
				preparedStatement.setFloat(i++, start);
			if (end != null)
				preparedStatement.setFloat(i++, end);
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
	public int deleteFeedPlanItem(FeedItem feedItem) {
		return deleteFeedPlanItem(feedItem, null, null, null);
	}
	public int deleteCohortNutritionalNeeds(CohortNutritionalNeeds dietReq) {
		String qryString = "DELETE FROM imd.FEED_COHORT_NUTRITIONAL_NEEDS WHERE ORG_ID=? AND FEED_COHORT=?";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, dietReq.getOrgID());
			preparedStatement.setString(2, dietReq.getFeedCohortCD());
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
	
	public int insertCohortNutritionalNeeds(CohortNutritionalNeeds dietReq) throws SQLException {
		
		// FeedPlan contains one or more FeedPlanItem. FeedPlanItem contains exactly one FeedNutritionalStats.
		int recordAdded = -1;
		String qryString = "insert into  imd.FEED_COHORT_NUTRITIONAL_NEEDS (ORG_ID,"
				+ "FEED_COHORT,"
				+ "START,"
				+ "END,"
				+ "DM,"
				+ "CP,"
				+ "ME,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1,  dietReq.getOrgID());
			preparedStatement.setString(2,  dietReq.getFeedCohortCD());
			preparedStatement.setFloat(3,  dietReq.getStart());
			preparedStatement.setFloat(4,  dietReq.getEnd());
			preparedStatement.setFloat(5,  dietReq.getDryMatter());
			preparedStatement.setFloat(6,  dietReq.getCrudeProtein());
			preparedStatement.setFloat(7,  dietReq.getMetabloizableEnergy());
			preparedStatement.setString(8, dietReq.getCreatedBy().getUserId());
			preparedStatement.setString(9, dietReq.getCreatedDTTMSQLFormat());
			preparedStatement.setString(10, dietReq.getUpdatedBy().getUserId());
			preparedStatement.setString(11, dietReq.getUpdatedDTTMSQLFormat());
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			recordAdded = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			recordAdded = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
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
		return recordAdded;
	}	
	public int insertFeedPlanItem(FeedItem feedItem) throws SQLException {
		
		int recordAdded = -1;
		String qryString = "insert into  imd.FEED_PLAN (ORG_ID,"
				+ "FEED_ITEM,"
				+ "FEED_COHORT,"
				+ "START,"
				+ "END,"
				+ "MIN_FULFILLMENT,"
				+ "FULFILLMENT_PCT,"
				+ "MAX_FULFILLMENT,"
				+ "FULFILLMENT_TYPE,"
				+ "UNITS,"
				+ "DAILY_FREQUENCY,"
				+ "COMMENTS,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int i = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++,  feedItem.getOrgID());
			preparedStatement.setString(i++,  feedItem.getFeedItemLookupValue().getLookupValueCode());
			preparedStatement.setString(i++,  feedItem.getFeedCohortCD().getLookupValueCode());
			preparedStatement.setFloat(i++,  feedItem.getStart());
			preparedStatement.setFloat(i++,  feedItem.getEnd());
			preparedStatement.setString(i++, formatFieldValue(feedItem.getMinimumFulfillment()));
			preparedStatement.setString(i++, formatFieldValue(feedItem.getFulfillmentPct()));
			preparedStatement.setString(i++, formatFieldValue(feedItem.getMaximumFulfillment()));
			preparedStatement.setString(i++,  feedItem.getFulFillmentTypeCD());
			preparedStatement.setString(i++, feedItem.getUnits());
			preparedStatement.setString(i++, formatFieldValue(feedItem.getDailyFrequency()));
			preparedStatement.setString(i++, feedItem.getComments());
			preparedStatement.setString(i++, feedItem.getCreatedBy().getUserId());
			preparedStatement.setString(i++, feedItem.getCreatedDTTMSQLFormat());
			preparedStatement.setString(i++, feedItem.getUpdatedBy().getUserId());
			preparedStatement.setString(i++, feedItem.getUpdatedDTTMSQLFormat());
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordAdded = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			recordAdded = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			recordAdded = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
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
		return recordAdded;
	}
	private String formatFieldValue(Object value) {
		return (value == null ? null : value.toString());
	}

	public List<CohortNutritionalNeeds> getCohortNutritionalNeeds(String orgId, List<FeedCohort> animalCohort) {
		String inClause = "";
		List<CohortNutritionalNeeds> dietReq = new ArrayList<CohortNutritionalNeeds>();
		
		String qryString = "SELECT * FROM imd.FEED_COHORT_NUTRITIONAL_NEEDS where ORG_ID=? " ;
		if (animalCohort != null && !animalCohort.isEmpty()) {
			inClause = " AND FEED_COHORT IN (";
			int run = 1;
			Iterator<FeedCohort> it = animalCohort.iterator();
			while (it.hasNext()) {
				it.next();
				if (run == animalCohort.size())
					inClause += "?)";
				else 
					inClause += "?,";
				run++;
			}
		}
		qryString += inClause;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int index = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++,orgId);
			Iterator<FeedCohort> it = animalCohort.iterator();
			while (it.hasNext()) {
				preparedStatement.setString(index++,it.next().getFeedCohortLookupValue().getLookupValueCode());			
			}
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	dietReq.add(getCohortNutritionalNeedsFromSQLRecord(rs));
	    	}
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			dietReq = null;
			ex.printStackTrace();
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

	    return dietReq;
	}

	private CohortNutritionalNeeds getCohortNutritionalNeedsFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		CohortNutritionalNeeds cohortNeeds = new CohortNutritionalNeeds();
		cohortNeeds.setOrgID(rs.getString("ORG_ID"));
		cohortNeeds.setFeedCohortCD(rs.getString("FEED_COHORT"));
		cohortNeeds.setStart(rs.getFloat("START"));
		cohortNeeds.setEnd(rs.getFloat("END"));
		cohortNeeds.setDryMatter(rs.getFloat("DM"));
		cohortNeeds.setCrudeProtein(rs.getFloat("CP"));
		cohortNeeds.setMetabloizableEnergy(rs.getFloat("ME"));
		cohortNeeds.setCreatedBy(new User(rs.getString("CREATED_BY")));
		cohortNeeds.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		cohortNeeds.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		cohortNeeds.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return cohortNeeds;
	}
	
	
	private FeedItem getFeedPlanItemFromSQLRecord(ResultSet rs) throws Exception {
		FeedItem feedItem = new FeedItem();
		feedItem.setOrgID(rs.getString("ORG_ID"));
		
		LookupValues feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,rs.getString("FEED_COHORT"), rs.getString("FEED_COHORT_SHORT_DESCR"),rs.getString("FEED_COHORT_LONG_DESCR"));
		feedItem.setFeedCohortCD(feedCohortLV);		
		
		LookupValues feedItemLV = new LookupValues(Util.LookupValues.FEED,rs.getString("FEED_ITEM"), rs.getString("ITEM_SHORT_DESCR"),rs.getString("ITEM_LONG_DESCR"));
		feedItem.setFeedItemLookupValue(feedItemLV);		
		feedItem.setStart(rs.getFloat("START"));
		feedItem.setEnd(rs.getFloat("END"));
		feedItem.setMinimumFulfillment(rs.getFloat("MIN_FULFILLMENT"));
		feedItem.setMaximumFulfillment(rs.getFloat("MAX_FULFILLMENT"));
		feedItem.setFulfillmentPct(rs.getFloat("FULFILLMENT_PCT"));
		feedItem.setFulFillmentTypeCD(rs.getString("FULFILLMENT_TYPE"));
		feedItem.setUnits(rs.getString("UNITS"));
		feedItem.setDailyFrequency(rs.getString("DAILY_FREQUENCY"));
		feedItem.setComments(rs.getString("COMMENTS"));
		feedItem.setCreatedBy(new User(rs.getString("CREATED_BY")));
		feedItem.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		feedItem.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		feedItem.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return feedItem;
	}	

	public List<CohortNutritionalNeeds> retrieveCohortNutritionalNeeds(FeedCohort cohort, Float start, Float end) {
		List<CohortNutritionalNeeds> dietReq = new ArrayList<CohortNutritionalNeeds>();
		String qryString = "SELECT * FROM imd.FEED_COHORT_NUTRITIONAL_NEEDS where ORG_ID = ? AND FEED_COHORT = ?  " + 
				(start == null ? "" : " AND START >= ? ") + 
				(end == null ? "" : " AND END <= ? ") + " ORDER BY FEED_COHORT, START, END ASC";
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			int index = 1;
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++,cohort.getOrgID());
			preparedStatement.setString(index++,cohort.getFeedCohortLookupValue().getLookupValueCode());
			if (start != null)
				preparedStatement.setFloat(index++,start);
			if (end != null)
				preparedStatement.setFloat(index++,end);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	dietReq.add(getCohortNutritionalNeedsFromSQLRecord(rs));
	    	}
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			dietReq = null;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			dietReq = null;
			ex.printStackTrace();
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
	    return dietReq;
	}
	public FeedItem retrieveFeedPlanItem(FeedItem feedItem, Float betweenStartAndEnd) throws IMDException {
		return retrieveFeedPlanItem(feedItem, betweenStartAndEnd, betweenStartAndEnd);
	}
	public FeedItem retrieveFeedPlanItem(FeedItem feedItem, Float gteStart, Float lteEnd) throws IMDException {
		String qryString = " SELECT a.*, " +
				" IFNULL(b.short_descr,a.FEED_ITEM) as ITEM_SHORT_DESCR, b.long_descr as ITEM_LONG_DESCR,b.additional_fld1 as ITEM_NUTRITIONAL_VALUES,  " +
				" IFNULL(c.short_descr,a.FEED_COHORT) AS FEED_COHORT_SHORT_DESCR, c.long_descr AS FEED_COHORT_LONG_DESCR,c.additional_fld1 AS FEED_COHORT_ADDITIONAL_FLD1 " +
				" from  " +
				" FEED_PLAN A " +
				" left  outer join LOOKUP_VALUES B on (b.lookup_cd=a.feed_item AND b.category_cd=? )  " +
				" left  outer join LOOKUP_VALUES C on (c.lookup_cd=a.FEED_COHORT AND c.category_cd=? ) " +
				" where A.ORG_ID=? "
				+ " AND A.FEED_ITEM = ? AND A.FEED_COHORT= ? AND A.START <= ? AND A.END >= ? ";
		
		FeedItem feedPlanItem = null;
		ResultSet rs = null;
		int i=1;
		int recCount=0;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++,Util.LookupValues.FEED);
			preparedStatement.setString(i++,Util.LookupValues.FEEDCOHORT);
			preparedStatement.setString(i++,feedItem.getOrgID());
			preparedStatement.setString(i++,feedItem.getFeedItemLookupValue().getLookupValueCode());
			preparedStatement.setString(i++,feedItem.getFeedCohortCD().getLookupValueCode());
			preparedStatement.setFloat(i++,gteStart);
			preparedStatement.setFloat(i++,lteEnd);
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	recCount++;
		    	feedPlanItem = getFeedPlanItemFromSQLRecord(rs);
		    }
		    if (recCount > 1)
		    	IMDLogger.log("Multiple Feed Plan Items were found that match the criteria, we expected to receive only ONE. Will pick the last result and discard all others", Util.ERROR);
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
	    return feedPlanItem;
	}

	public FeedPlan retrieveFeedPlan(String orgID, String feedCohortCD) throws IMDException {
		
		String qryString = " SELECT a.*, " +
				" IFNULL(b.short_descr,a.FEED_ITEM) as ITEM_SHORT_DESCR, b.long_descr as ITEM_LONG_DESCR,b.additional_fld1 as ITEM_NUTRITIONAL_VALUES,  " +
				" IFNULL(c.short_descr,a.FEED_COHORT) AS FEED_COHORT_SHORT_DESCR, c.long_descr AS FEED_COHORT_LONG_DESCR,c.additional_fld1 AS FEED_COHORT_ADDITIONAL_FLD1 " +
				" from  " +
				" FEED_PLAN A " +
				" left  outer join LOOKUP_VALUES B on (b.lookup_cd=a.feed_item AND b.category_cd=?   )  " +
				" left  outer join LOOKUP_VALUES C on (c.lookup_cd=a.FEED_COHORT and c.category_cd=? ) " +
				"  where "  + 
				" ORG_ID=? AND FEED_COHORT= ? ";

		
		FeedPlan feedPlan = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1,Util.LookupValues.FEED);
			preparedStatement.setString(2,Util.LookupValues.FEEDCOHORT);
			preparedStatement.setString(3,orgID);
			preparedStatement.setString(4,feedCohortCD);
			IMDLogger.log(preparedStatement.toString(),Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	FeedItem item = getFeedPlanItemFromSQLRecord(rs);
		    	if (feedPlan == null) {
		    		feedPlan = new FeedPlan();
				    feedPlan.setOrgID(item.getOrgID());
				    feedPlan.setFeedCohort(new FeedCohort(item.getOrgID(),item.getFeedCohortCD(), ""));
				    feedPlan.setFeedPlan(new ArrayList<FeedItem>());
		    	}
		    	feedPlan.getFeedPlan().add(item);
		    }
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
	    return feedPlan;
	}	
	
	
	
}





