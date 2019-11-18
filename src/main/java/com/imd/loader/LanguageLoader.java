package com.imd.loader;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class LanguageLoader {
	
	private static HashMap <String, String> messageCache = new HashMap <String, String>();
	
	public String retrieveMessage(String orgId, String languageCD, Integer messageCd) {
		String qryString = " SELECT A.MESSAGE_CD, MESSAGE_TEXT  FROM " +   
				" imd.LANG_MESSAGES A " +
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
}
