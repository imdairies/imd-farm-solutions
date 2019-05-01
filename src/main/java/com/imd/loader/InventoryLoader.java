package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.imd.dto.Animal;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.Inventory;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.SireBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class InventoryLoader {
	
		
	public int insertSemenInventory(Inventory inventory) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into INV_SEMEN (ORG_ID,"
				+ "ID,"
				+ "SEXED,"
				+ "ORDER_DTTM,"
				+ "PRICE,"
				+ "DISCOUNT,"
				+ "QUANTITY,"
				+ "RCVD_DTTM,"
				+ "INV_ADDED_DTTM,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, inventory.getOrgID());
			preparedStatement.setString(2, inventory.getItemSKU());
			preparedStatement.setString(3, inventory.getItemType());
			preparedStatement.setString(4,  Util.getDateInSQLFormart(inventory.getOrderDttm()));
			preparedStatement.setFloat(5, inventory.getPrice());
			preparedStatement.setFloat(6, inventory.getDiscount());
			preparedStatement.setFloat(7, inventory.getQuantity());
			preparedStatement.setString(8,  (inventory.getReceivedDttm() == null ? null : Util.getDateInSQLFormart(inventory.getReceivedDttm())));
			preparedStatement.setString(9,  (inventory.getInventoryAddDttmStr() == null ? null : Util.getDateInSQLFormart(inventory.getInventoryAddDttm())));
			preparedStatement.setString(10, (inventory.getCreatedBy() == null ? null : inventory.getCreatedBy().getUserId()));
			preparedStatement.setString(11, (inventory.getCreatedDTTM() == null ? null :inventory.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(12,(inventory.getUpdatedBy() == null ? null : inventory.getUpdatedBy().getUserId()));
			preparedStatement.setString(13,(inventory.getUpdatedDTTM() == null ? null :inventory.getUpdatedDTTMSQLFormat()));
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
	public int deleteSemenInventory(String orgID, String id, String sexed) {
		String qryString = "DELETE FROM INV_SEMEN where ORG_ID='" + orgID + 
				"' AND ID = '" + id + "' AND SEXED = '" + sexed + "'";
		int result = -1;
		Statement st = null;
		IMDLogger.log(qryString, Util.INFO);
		Connection conn = DBManager.getDBConnection();
		try {
			st = conn.createStatement();
			result = st.executeUpdate(qryString);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    try {
				if (st != null && !st.isClosed()) {
					st.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private Inventory getInventoryFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		Inventory inv = new Inventory();
		inv.setOrgID(rs.getString("ORG_ID"));
		inv.setItemSKU(rs.getString("ID"));
		inv.setItemType(rs.getString("SEXED"));
		inv.setPrice(rs.getFloat("PRICE"));
		inv.setDiscount(rs.getFloat("DISCOUNT"));
		inv.setQuantity(rs.getFloat("QUANTITY"));
		inv.setOrderDttm((new DateTime(rs.getTimestamp("ORDER_DTTM"))));
		inv.setReceivedDttm((new DateTime(rs.getTimestamp("RCVD_DTTM"))));
		inv.setInventoryAddDttm((new DateTime(rs.getTimestamp("INV_ADDED_DTTM"))));
		
		inv.setCreatedBy(new User(rs.getString("CREATED_BY")));
		inv.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		inv.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		inv.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return inv;
	}
}
