package com.imd.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

	private static Connection dbConnection;
	
	public static Connection getDBConnection() {
		try 
		{
			if (dbConnection == null || dbConnection.isClosed()) {
				dbConnection = getConnection(dbConnection);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbConnection;
	}

	public static void cleanDBConnection() {
		try 
		{
			if (dbConnection != null && !dbConnection.isClosed()) {
				//dbConnection.commit();
				dbConnection.close();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private static Connection getConnection(Connection conn) throws ClassNotFoundException, SQLException {
        Class.forName(IMDProperties.getProperty(Util.PROPERTIES.JDBC_DRIVER));
        conn = DriverManager.getConnection(IMDProperties.getProperty(Util.PROPERTIES.DB_URL), 
        		IMDProperties.getProperty(Util.PROPERTIES.USER), IMDProperties.getProperty(Util.PROPERTIES.PASS));
		return conn;
	}

}
