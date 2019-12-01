package com.imd.loader;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Message;
import com.imd.dto.User;
import com.imd.services.bean.MessageBean;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class MessageCatalogLoader {
	
	private static HashMap <String, Message> messageCache = new HashMap <String, Message>();
	
	public static Message getMessage(String orgId, String languageCD, String messageCD) {
		String qryString = " SELECT A.* FROM " +   
				" imd.MESSAGE_CATALOG A " +
				" WHERE A.ORG_ID=? AND A.LANG_CD=? AND MESSAGE_CD=? ";
		Message message = new Message(orgId, languageCD, messageCD);
		IMDLogger.log(message.toString(), Util.INFO);
		if (orgId == null || orgId.trim().isEmpty() || 
				languageCD == null || languageCD.trim().isEmpty() || 
				messageCD == null || messageCD.trim().isEmpty())
			return message;
		message = messageCache.get(orgId + "-" + languageCD + "-" + messageCD);
		if ( message == null) {
			message = new Message(orgId, languageCD, messageCD);
			PreparedStatement preparedStatement = null;
			ResultSet rs = null;
			try {
				Connection conn = DBManager.getDBConnection();
				preparedStatement = conn.prepareStatement(qryString);
				preparedStatement.setString(1, orgId);
				preparedStatement.setString(2, languageCD);
				preparedStatement.setString(3, messageCD);
				IMDLogger.log(preparedStatement.toString(), Util.INFO);
			    rs = preparedStatement.executeQuery();
			    while (rs.next()) {
			    	message = new Message(orgId,languageCD, rs.getString("MESSAGE_CD"));
			    	String messageText = rs.getString("MESSAGE_TEXT");
			    	if (messageText != null && !messageText.isEmpty())
			    		message.setMessageText(messageText);
			    	messageCache.put(orgId + "-" + languageCD + "-" + message.getMessageCD(),message);
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
	    return message;
	}
	public static Message getDynamicallyPopulatedMessage(String orgId, String languageCD, String messageCd, List<String> dynamicValues) {
		Message transformedMessage = getMessage( orgId,  languageCD,  messageCd) ;
		if (transformedMessage != null && transformedMessage.getMessageText() != null && 
				!transformedMessage.getMessageText().isEmpty() && 
				dynamicValues != null && !dynamicValues.isEmpty() && 
				transformedMessage.getMessageText().indexOf(Util.MessageCatalog.DYNAMIC_VALUE_PLACEHOLDER + "1") >= 0) {
			for (int i=0; i < dynamicValues.size(); i++) {
				transformedMessage.setMessageText(transformedMessage.getMessageText().replaceFirst(Util.MessageCatalog.DYNAMIC_VALUE_PLACEHOLDER + (i+1), dynamicValues.get(i)));
			}
		}
		return transformedMessage;			
	}
	public static Message getDynamicallyPopulatedMessage(String orgId, String languageCD, String messageCd, String firstDynamicValue) {
		List<String> dynamicValues = new ArrayList<String>();
		dynamicValues.add(firstDynamicValue);
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, dynamicValues);
	}
	public static Message getDynamicallyPopulatedMessage(String orgId, String languageCD, String messageCd, int firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
	public static Message getDynamicallyPopulatedMessage(String orgId, String languageCD, String messageCd, float firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
	public static Message getDynamicallyPopulatedMessage(String orgId, String languageCD, String messageCd, double firstDynamicValue) {
		return getDynamicallyPopulatedMessage( orgId,  languageCD,  messageCd, firstDynamicValue + "");
	}
	public List<Message> retrieveMessage(MessageBean messageBean) {
		String qryString = " SELECT * FROM " +   
				" imd.MESSAGE_CATALOG ";
		String whereClause = (messageBean.getOrgId() == null || messageBean.getOrgId().isEmpty() ? "" : " ORG_ID=? AND") +
				(messageBean.getLanguageCD() == null || messageBean.getLanguageCD().isEmpty() ? "" : " LANG_CD=? AND") +
				(messageBean.getMessageCD() == null || messageBean.getMessageCD().isEmpty() ? "" : " MESSAGE_CD=? ");
		if (!whereClause.isEmpty()) {
			whereClause = " WHERE " + whereClause;
			String queryConditions = (whereClause.lastIndexOf("AND") == whereClause.length()-3) ?  whereClause.substring(0,whereClause.length()-3) : whereClause;
			qryString += queryConditions;
		}
		qryString += " ORDER BY ORG_ID, LANG_CD, MESSAGE_CD";
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		int i = 1;
		List<Message> messages = new ArrayList<Message>();
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			if (messageBean.getOrgId() != null &&  !messageBean.getOrgId().isEmpty())
				preparedStatement.setString(i++, messageBean.getOrgId());
			if (messageBean.getLanguageCD() != null &&  !messageBean.getLanguageCD().isEmpty())
				preparedStatement.setString(i++, messageBean.getLanguageCD());
			if (messageBean.getMessageCD() != null &&  !messageBean.getMessageCD().isEmpty())
				preparedStatement.setString(i++, messageBean.getMessageCD());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	Message message = new Message(rs.getString("ORG_ID"),rs.getString("LANG_CD"),rs.getString("MESSAGE_CD"));
		    	message.setMessageText(rs.getString("MESSAGE_TEXT"));
		    	message.setCreatedBy(new User(rs.getString("CREATED_BY")));
				message.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
				message.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
				message.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		    	messages.add(message);
		    	messageCache.put(message.getOrgID() + "-" + message.getLanguageCD() + "-" + message.getMessageCD(),message);
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
	    return messages;
	}
	public int insertMessage(MessageBean messageBean) {
		int insertedRecord = -1;
		String qryString = "insert into imd.MESSAGE_CATALOG (ORG_ID,"
				+ "LANG_CD,"
				+ "MESSAGE_CD,"
				+ "MESSAGE_TEXT,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		int i = 1;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, messageBean.getOrgId());
			preparedStatement.setString(i++, messageBean.getLanguageCD());
			preparedStatement.setString(i++, messageBean.getMessageCD());
			preparedStatement.setString(i++, messageBean.getMessageText().toString());
			preparedStatement.setString(i++, messageBean.getUserId());
			preparedStatement.setString(i++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			preparedStatement.setString(i++,messageBean.getUserId());
			preparedStatement.setString(i++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));

			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    insertedRecord = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			insertedRecord = Util.ERROR_CODE.ALREADY_EXISTS;
			ex.printStackTrace();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			insertedRecord = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			insertedRecord = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			insertedRecord = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
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
	    return insertedRecord;		
	
	}
	public int updatedMessage(MessageBean messageBean) {
		int updatedRecord = -1;
		String qryString = "UPDATE imd.MESSAGE_CATALOG SET MESSAGE_TEXT=?, UPDATED_BY=?, UPDATED_DTTM=? WHERE "
				+ "ORG_ID=? AND LANG_CD=? AND MESSAGE_CD=?";
		
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		int i = 1;
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, messageBean.getMessageText().toString());
			preparedStatement.setString(i++,messageBean.getUserId());
			preparedStatement.setString(i++, Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			preparedStatement.setString(i++, messageBean.getOrgId());
			preparedStatement.setString(i++, messageBean.getLanguageCD());
			preparedStatement.setString(i++, messageBean.getMessageCD());

			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			updatedRecord = preparedStatement.executeUpdate();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			updatedRecord = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			updatedRecord = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			updatedRecord = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
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
	    return updatedRecord;		
	
	}	public int deleteMessage(MessageBean messageBean) {
		int deletedRecord = -1;
		String qryString = "DELETE FROM imd.MESSAGE_CATALOG WHERE ORG_ID=? AND "
				+ "LANG_CD=? AND "
				+ "MESSAGE_CD=?";
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		int i = 1;
		if (messageBean.getOrgId() == null || messageBean.getOrgId().isEmpty() || 
			messageBean.getLanguageCD() == null || messageBean.getLanguageCD().isEmpty() ||
			messageBean.getMessageCD() == null) {
			return deletedRecord;
		}
		try {
			Connection conn = DBManager.getDBConnection();
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(i++, messageBean.getOrgId());
			preparedStatement.setString(i++, messageBean.getLanguageCD());
			preparedStatement.setString(i++, messageBean.getMessageCD());

			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    deletedRecord = preparedStatement.executeUpdate();
		} catch (com.mysql.cj.jdbc.exceptions.MysqlDataTruncation ex) {
			deletedRecord = Util.ERROR_CODE.DATA_LENGTH_ISSUE;
			ex.printStackTrace();
		} catch (java.sql.SQLSyntaxErrorException ex) {
			deletedRecord = Util.ERROR_CODE.SQL_SYNTAX_ERROR;
			ex.printStackTrace();
		} catch (java.sql.SQLException ex) {
			deletedRecord = Util.ERROR_CODE.UNKNOWN_ERROR;
			ex.printStackTrace();
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
	    return deletedRecord;
	}

}





