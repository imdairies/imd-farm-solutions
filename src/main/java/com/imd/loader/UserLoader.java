package com.imd.loader;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

import com.imd.dto.Animal;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class UserLoader {
	
	private static HashMap <String, User> userCache = new HashMap <String, User>();
	private static HashMap <String, User> sessionCache = new HashMap <String, User>();
	
	public User retrieveUser(String orgId, String userId) {
		String qryString = " SELECT  " +
				" A.ORG_ID," +
				" A.USER_ID," + 
				" A.ACTIVE_IND," +
				" A.PASSWORD," +
				" A.PERSON_ID," +
				" A.PREF_LANG," +
				" A.PREF_CURR," +
				" A.CREATED_BY," +
				" A.CREATED_DTTM," +
				" A.UPDATED_BY," +
				" A.UPDATED_DTTM " +
				" FROM imd.USERS A " +
				" WHERE A.ORG_ID=? AND A.USER_ID=? ";
		if (userCache.get(orgId + "-" + userId) == null) {		
			PreparedStatement preparedStatement = null;
			ResultSet rs = null;
			try {
				Connection conn = DBManager.getDBConnection();
				preparedStatement = conn.prepareStatement(qryString);
				preparedStatement.setString(1, orgId);
				preparedStatement.setString(2, userId);
				IMDLogger.log(preparedStatement.toString(), Util.INFO);
			    rs = preparedStatement.executeQuery();
			    while (rs.next()) {
			    	userCache.put(orgId + "-" + userId,loadUserFromSQL(rs));
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
	    return userCache.get(orgId + "-" + userId);
	}

	private User loadUserFromSQL(ResultSet rs) throws SQLException {
		User user = new User(rs.getString("USER_ID"));
		user.setOrgID(rs.getString("ORG_ID"));
		user.setPassword(rs.getString("PASSWORD"));
		user.setActive(rs.getString("ACTIVE_IND").equalsIgnoreCase(Util.Y));
		user.setPersonId(rs.getString("PERSON_ID"));
		user.setPreferredLanguage(rs.getString("PREF_LANG"));
		user.setPreferredCurrency(rs.getString("PREF_CURR"));
		user.setCreatedBy(new User(rs.getString("CREATED_BY")));
		user.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		user.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		user.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		return user;
	}

	public int deleteUser(String orgID, String userId) {
		String qryString = "DELETE FROM imd.USERS where ORG_ID= ? AND USER_ID = ?";
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, userId);
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

	public int insertUser(User newUser) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into imd.USERS (ORG_ID,"
				+ "USER_ID,"
				+ "ACTIVE_IND,"
				+ "PASSWORD,"
				+ "PERSON_ID,"
				+ "PREF_LANG,"
				+ "PREF_CURR,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++,  (newUser.getOrgID() == null ? null : newUser.getOrgID()));
			preparedStatement.setString(index++,  (newUser.getUserId() == null ? null : newUser.getUserId()));
			preparedStatement.setString(index++,  (newUser.isActive() ? Util.Y : Util.N));
			preparedStatement.setString(index++,  (newUser.getPassword() == null ? null : newUser.getPassword()));

			preparedStatement.setString(index++,  (newUser.getPersonId() == null ? null : newUser.getPersonId()));
			preparedStatement.setString(index++,  (newUser.getPreferredLanguage() == null ? null : newUser.getPreferredLanguage()));
			preparedStatement.setString(index++,  (newUser.getPreferredCurrency() == null ? null : newUser.getPreferredCurrency()));

			preparedStatement.setString(index++, (newUser.getCreatedBy() == null ? null : newUser.getCreatedBy().getUserId()));
			preparedStatement.setString(index++, (newUser.getCreatedDTTM() == null ? null :newUser.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(index++,(newUser.getUpdatedBy() == null ? null : newUser.getUpdatedBy().getUserId()));
			preparedStatement.setString(index++,(newUser.getUpdatedDTTM() == null ? null :newUser.getUpdatedDTTMSQLFormat()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
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

	public User authenticateUser(String orgId, String userId, String encryptedPassword) {
		User user = null;
		String qryString = " SELECT  " +
				" A.ORG_ID," +
				" A.USER_ID," + 
				" A.ACTIVE_IND," +
				" A.PASSWORD," +
				" A.PERSON_ID," +
				" A.PREF_LANG," +
				" A.PREF_CURR," +
				" A.CREATED_BY," +
				" A.CREATED_DTTM," +
				" A.UPDATED_BY," +
				" A.UPDATED_DTTM " +
				" FROM imd.USERS A " +
				" WHERE A.ORG_ID=? AND A.USER_ID=? AND A.PASSWORD=?";
		PreparedStatement preparedStatement = null;
		if (encryptedPassword != null && !encryptedPassword.isEmpty() && userId != null && !userId.isEmpty() && orgId != null && !orgId.isEmpty()) {
			ResultSet rs = null;
			try {
				Connection conn = DBManager.getDBConnection();
				preparedStatement = conn.prepareStatement(qryString);
				preparedStatement.setString(1, orgId);
				preparedStatement.setString(2, userId);
				preparedStatement.setString(3, encryptedPassword);
				IMDLogger.log(preparedStatement.toString(), Util.INFO);
			    rs = preparedStatement.executeQuery();
			    while (rs.next()) {
			    	user = loadUserFromSQL(rs);
				    user = loginUser(user);
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
		return user;
	}
	private User loginUser(User user) {
		
		int recordUpdated = -1;
		
		User returnUser = null;
		String insertQuery 	= "insert into imd.USER_LOGIN (ORG_ID,"
				+ "USER_ID,"
				+ "LOGIN_TOKEN,"
				+ "TOKEN_ISSUE_DTTM,"
				+ "TOKEN_EXPIRY_DTTM,"
				+ "EXPIRED_IND,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?)";
		String inactivateQuery = "UPDATE imd.USER_LOGIN SET EXPIRED_IND=?, UPDATED_BY=?, UPDATED_DTTM=? WHERE LOGIN_TOKEN IN ";
		String existingActiveSessions = "SELECT LOGIN_TOKEN from imd.USER_LOGIN WHERE ORG_ID=? AND USER_ID=? AND EXPIRED_IND <> ?";
		PreparedStatement existingActivePreparedStatement = null;
		PreparedStatement updatePreparedStatement = null;
		PreparedStatement insertPreparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		ResultSet rs = null;
		try {
			existingActivePreparedStatement = conn.prepareStatement(existingActiveSessions);
			existingActivePreparedStatement.setString(index++, user.getOrgID());
			existingActivePreparedStatement.setString(index++, user.getUserId());
			existingActivePreparedStatement.setString(index++, Util.Y);
			IMDLogger.log(existingActivePreparedStatement.toString(), Util.INFO);
			rs = existingActivePreparedStatement.executeQuery();
			String tokenList = "";
			while (rs.next()) {
				String token = rs.getString("LOGIN_TOKEN");
				tokenList = (!tokenList.isEmpty() ? "," : "") + "'" + token + "'";
				sessionCache.remove(token);
			}
			index = 1;
			if (!tokenList.isEmpty()) {
				inactivateQuery += "(" + tokenList + ")";
				updatePreparedStatement = conn.prepareStatement(inactivateQuery);
				updatePreparedStatement.setString(index++, Util.Y);
				updatePreparedStatement.setString(index++, user.getUserId());
				updatePreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
				IMDLogger.log(updatePreparedStatement.toString(), Util.INFO);
				recordUpdated = updatePreparedStatement.executeUpdate();
				if (recordUpdated > 0) {
					IMDLogger.log(recordUpdated + " open login session(s) found for the user " + user.getUserId() + "(" + user.getOrgID() + ")", Util.WARNING);
				}
			}
			index = 1;
			recordUpdated = -1;
			String token = UUID.randomUUID().toString();
			DateTime issueDttm = DateTime.now(IMDProperties.getServerTimeZone());
			DateTime expiryDttm = issueDttm.plusMinutes((Integer)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.TOKEN_EXPIRY_MINUTES));
			insertPreparedStatement = conn.prepareStatement(insertQuery);
			insertPreparedStatement.setString(index++, user.getOrgID());
			insertPreparedStatement.setString(index++, user.getUserId());
			insertPreparedStatement.setString(index++, token);
			insertPreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(issueDttm));
			insertPreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(expiryDttm));
			insertPreparedStatement.setString(index++, Util.N);
			insertPreparedStatement.setString(index++, user.getUserId());
			insertPreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(issueDttm));
			insertPreparedStatement.setString(index++, user.getUserId());
			insertPreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(issueDttm));
			IMDLogger.log(insertPreparedStatement.toString(), Util.INFO);
			recordUpdated = insertPreparedStatement.executeUpdate();
			if (recordUpdated != 1) {
				IMDLogger.log("Could not insert login session to the database for the user " + user.getUserId() + "(" + user.getOrgID() + ")", Util.ERROR);
			} else {
				returnUser = user;
				returnUser.setPassword(token);
				returnUser.setCreatedDTTM(expiryDttm);
				sessionCache.put(token, returnUser);
			}
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			recordUpdated = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			recordUpdated = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			recordUpdated = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (updatePreparedStatement != null && !updatePreparedStatement.isClosed()) {
					updatePreparedStatement.close();	
				}
				if (insertPreparedStatement != null && !insertPreparedStatement.isClosed()) {
					insertPreparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return returnUser;
	}

	public String encryptPassword(String plainTextPassword) {
		// This is a temporary implementation. The real implementation would leverage google or facebook for user authentication. We will
		// not store use password in this application for security reasons.
		String encryptedPassword = "";
		for (int i=0; i< plainTextPassword.length(); i++) {
			int encryptedValue = (int) plainTextPassword.charAt(i);
			encryptedPassword += "" + encryptedValue;
		}
		return encryptedPassword;
	}

	public int logoutUser(String loginToken) {
		String inactivateTokenQuery = "UPDATE imd.USER_LOGIN SET EXPIRED_IND='Y', UPDATED_BY=USER_ID, UPDATED_DTTM=? WHERE LOGIN_TOKEN=? AND EXPIRED_IND='N'";
		PreparedStatement updatePreparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		int recordUpdated =-1;
		try {
			updatePreparedStatement = conn.prepareStatement(inactivateTokenQuery);
			updatePreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			updatePreparedStatement.setString(index++, loginToken);
			IMDLogger.log(updatePreparedStatement.toString(), Util.INFO);
			recordUpdated = updatePreparedStatement.executeUpdate();
			sessionCache.remove(loginToken);
			if (recordUpdated == 0) {
				IMDLogger.log(loginToken + " not found in Login table or it was already inactivated. No inactivation necessary !", Util.WARNING);
			} else if (recordUpdated > 1) {
				
				IMDLogger.log("Total of " + recordUpdated + " records found for the auth token " + loginToken + " in Login table. Either someone is trying to hack the system or there is a bug in the software", Util.WARNING);
			} else {
				IMDLogger.log("Auth Token successfully inactivated", Util.INFO);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log("Exception occurred while inactivating Auth Token " + loginToken, Util.ERROR);
		} finally {
		    try {
				if (updatePreparedStatement != null && !updatePreparedStatement.isClosed()) {
					updatePreparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return recordUpdated;
	} 
	
	public int inactivateExpiredTokens(String orgID) {
		String inactivateTokenQuery = "UPDATE imd.USER_LOGIN SET EXPIRED_IND='Y', UPDATED_BY=USER_ID, UPDATED_DTTM=? WHERE "
				+ (orgID != null ? " ORG_ID=? AND " : "") + " TOKEN_EXPIRY_DTTM <= ? AND EXPIRED_IND='N'";
		PreparedStatement updatePreparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		int recordUpdated =-1;
		try {
			updatePreparedStatement = conn.prepareStatement(inactivateTokenQuery);
			updatePreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			if (orgID != null) 
				updatePreparedStatement.setString(index++, orgID);
			updatePreparedStatement.setString(index++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));

			IMDLogger.log(updatePreparedStatement.toString(), Util.INFO);
			recordUpdated = updatePreparedStatement.executeUpdate();
			if (recordUpdated > 0) {
				IMDLogger.log("Total of " + recordUpdated + " sessions were inactivated as their token expiry date had lapsed.", Util.INFO);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log("Exception occurred while inactivating expired Auth Tokens ", Util.ERROR);
		} finally {
		    try {
				if (updatePreparedStatement != null && !updatePreparedStatement.isClosed()) {
					updatePreparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return recordUpdated;
	}

	public User isUserAuthenticated(String authToken) {
		User user = sessionCache.get(authToken);
		if (user != null) {
			if (user.getCreatedDTTM().isBefore(DateTime.now(IMDProperties.getServerTimeZone()))) {
				// token has expired
				logoutUser(authToken);
				sessionCache.remove(authToken);
				user = null;
			}
		}
		return user;
	}
	public HashMap<String, User> getSessionCache(){
		return sessionCache;
	}

}









