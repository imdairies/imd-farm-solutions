package com.imd.loader;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class MessageCatalogLoader {
	
	private static HashMap <String, String> messageCache = new HashMap <String, String>();
	
	public static String getMessage(String orgId, String languageCD, Integer messageCd) {
		String qryString = " SELECT A.MESSAGE_CD, MESSAGE_TEXT  FROM " +   
				" imd.MESSAGE_CATALOG A " +
				" WHERE A.ORG_ID=? AND A.LANG_CD=? AND MESSAGE_CD=? ";
		
		if (messageCache.get(orgId + "-" + languageCD + "-" + messageCd) == null) {		
			PreparedStatement preparedStatement = null;
			ResultSet rs = null;
			try {
				Connection conn = DBManager.getDBConnection();
				preparedStatement = conn.prepareStatement(qryString);
				preparedStatement.setString(1, orgId);
				preparedStatement.setString(2, languageCD);
				preparedStatement.setInt(3, messageCd);
				IMDLogger.log(preparedStatement.toString(), Util.INFO);
			    rs = preparedStatement.executeQuery();
			    while (rs.next()) {
			    	messageCache.put(orgId + "-" + languageCD + "-" + rs.getString("MESSAGE_CD"),rs.getString("MESSAGE_TEXT"));
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
		} 
	    return messageCache.get(orgId + "-" + languageCD + "-" + messageCd);
	}
	public static String getDynamicallyPopulatedMessage(String orgId, String languageCD, Integer messageCd, List<String> dynamicValues) {
		String transformedMessage = getMessage( orgId,  languageCD,  messageCd) ;
		if (transformedMessage != null && !transformedMessage.isEmpty() && dynamicValues != null && !dynamicValues.isEmpty() && 
				transformedMessage.indexOf(Util.MessageCatalog.DYNAMIC_VALUE_PLACEHOLDER + "1") >= 0) {
			for (int i=1; i < dynamicValues.size(); i++) {
				transformedMessage = transformedMessage.replaceFirst(Util.MessageCatalog.DYNAMIC_VALUE_PLACEHOLDER + i, dynamicValues.get(i-1));
			}
			
		}
		return transformedMessage;			
	}
	public static String getDynamicallyPopulatedMessage(String orgId, String languageCD, Integer messageCd, String firstDynamicValue) {
		List<String> dynamicValues = new ArrayList<String>();
		dynamicValues.add(firstDynamicValue);
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, dynamicValues);
	}
	public static String getDynamicallyPopulatedMessage(String orgId, String languageCD, Integer messageCd, int firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
	public static String getDynamicallyPopulatedMessage(String orgId, String languageCD, Integer messageCd, float firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
	public static String getDynamicallyPopulatedMessage(String orgId, String languageCD, Integer messageCd, double firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
//	public void loadAllMessages(String orgID) {
//
//		String qryString = " SELECT A.ORG_ID, A.LANG_CD, A.MESSAGE_CD, MESSAGE_TEXT  FROM " +   
//				" imd.MESSAGE_CATALOG A " +
//				" WHERE A.ORG_ID=?";
//		
//			PreparedStatement preparedStatement = null;
//			ResultSet rs = null;
//			try {
//				Connection conn = DBManager.getDBConnection();
//				preparedStatement = conn.prepareStatement(qryString);
//				preparedStatement.setString(1, orgID);
//				IMDLogger.log(preparedStatement.toString(), Util.INFO);
//			    rs = preparedStatement.executeQuery();
//			    while (rs.next()) {
//			    	messageCache.put(orgID + "-" + rs.getString("LANG_CD") + "-" + rs.getString("MESSAGE_CD"),rs.getString("MESSAGE_TEXT"));
//			    }
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			} finally {
//			    try {
//					if (rs != null && !rs.isClosed()) {
//						rs.close();	
//					}
//					if (preparedStatement != null && !preparedStatement.isClosed()) {
//						preparedStatement.close();	
//					}
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//		}
}
