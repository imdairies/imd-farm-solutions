package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Contact;
import com.imd.dto.Inventory;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class InventoryLoader {
	
		
	public int insertSemenInventory(Inventory inventory) throws SQLException {
		int recordAdded = -1;
		String qryString = "insert into INV_SEMEN (ORG_ID,"
				+ "ITEM_ID,"
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
	public int deleteSemenInventory(String orgID, String id, String sexed) {
		String qryString = "DELETE FROM INV_SEMEN where ORG_ID='" + orgID + 
				"' AND ITEM_ID = '" + id + "' AND SEXED = '" + sexed + "'";
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

	public int deleteSemenInventoryUsage(String orgID, String itemId, DateTime addedDTTM) {
		String qryString = "DELETE FROM INV_SEMEN_USAGE where ORG_ID= ?  AND ITEM_ID = ? AND INV_ADDED_DTTM = ?";
		
		int result = -1;
		PreparedStatement preparedStatement = null;
		IMDLogger.log(qryString, Util.INFO);
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, itemId);
			preparedStatement.setTimestamp(3, new Timestamp(addedDTTM.getMillis()));
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

	public int addSemenInventoryUsage(Inventory inventory) {
		int recordAdded = -1;
		String qryString = "insert into INV_SEMEN_USAGE (ORG_ID,"
				+ "ITEM_ID,"
				+ "SEXED,"
				+ "QUANTITY,"
				+ "INV_ADDED_DTTM,"
				+ "EVENT_ID,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, inventory.getOrgID());
			preparedStatement.setString(2, inventory.getItemSKU());
			preparedStatement.setString(3, inventory.getItemType());
			preparedStatement.setFloat(4,  inventory.getQuantity());
			preparedStatement.setString(5, (inventory.getInventoryAddDttmStr() == null ? null : Util.getDateInSQLFormart(inventory.getInventoryAddDttm())));
			preparedStatement.setString(6,  inventory.getAuxValue1());
			preparedStatement.setString(7, (inventory.getCreatedBy() == null ? null : inventory.getCreatedBy().getUserId()));
			preparedStatement.setString(8, (inventory.getCreatedDTTM() == null ? null :inventory.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(9,(inventory.getUpdatedBy() == null ? null : inventory.getUpdatedBy().getUserId()));
			preparedStatement.setString(10,(inventory.getUpdatedDTTM() == null ? null :inventory.getUpdatedDTTMSQLFormat()));
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
	
	public List<Sire> getSiresWithAvailableInventory(String orgID) {
		List<Sire> sireList = new ArrayList<Sire>();
		ResultSet rs;
		String qryString = "SELECT C.USE_COUNT, C.success_count, C.failure_count, C.tbd_count, A.remaining_qty as REMAINING_QTY, A.sexed, B.* " 
				+ " FROM imd.SEMEN_RMNG_QTY_VW A " 
				+ " LEFT OUTER JOIN  LV_SIRE B ON B.id=A.item_id " 
				+ " LEFT OUTER JOIN SIRE_USAGE_STATS_VW c ON A.item_id=C.CODE  WHERE A.item_id = B.id and A.org_id=? and remaining_qty > 0 "				
				+ " union "
				+ " select 0 as USE_COUNT,0 as success_count,0 as failure_count,0 as tbd_count, \"∞\" as REMAINING_QTY,'N' as SEXED, A.* from LV_SIRE A where semen_ind='N' "
				+ " and  A.ID not in (select animal_tag from animals where herd_leaving_dttm is not null)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	sireList.add(getSireFromSQLRecord(rs));
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log(ex.getMessage(),  Util.ERROR);
		} finally {
		    try {
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sireList;		
	}
	
	public Inventory getRemainingSemenInventory(String orgID, String itemSKU, String sexedInd) {
		Inventory inv = null;
		ResultSet rs;
		String qryString = "SELECT * FROM imd.SEMEN_RMNG_QTY_VW where ORG_ID=? AND ITEM_ID=? AND SEXED=?";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, itemSKU);
			preparedStatement.setString(3, sexedInd);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	inv = new Inventory();
				inv.setOrgID(rs.getString("ORG_ID"));
				inv.setItemSKU(rs.getString("ITEM_ID"));
				inv.setItemType(rs.getString("SEXED"));
				inv.setQuantity(rs.getFloat("ORDERED_QTY"));
				inv.setAuxValue1(rs.getString("CONSUMED_QTY"));
				inv.setAuxValue2(rs.getString("REMAINING_QTY"));
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			IMDLogger.log(ex.getMessage(),  Util.ERROR);
		} finally {
		    try {
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return inv;
	}
	private Sire getSireFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		Sire animalValue;
		String id = rs.getString("ID");
		String breed = rs.getString("BREED");
		String alias = rs.getString("ALIAS");
		String semenInd = rs.getString("SEMEN_IND");
		String sireSpecification = rs.getString("RECORD_URL");
		String sirePhoto = rs.getString("PHOTO_URL");
		String controller = rs.getString("CONTROLLER");
		String semenCompany = rs.getString("SEMEN_COMPANY");
		Float currentSexListPrice = rs.getFloat("CURRENT_SEX_LIST_PRICE_PKR");
		Float currentConventionalListPrice = rs.getFloat("CURRENT_CONV_LIST_PRICE_PKR");
		Float discountSexPercentage = rs.getFloat("DISCOUNT_SEX_PERCENTAGE");
		Float discountConventionalPercentage = rs.getFloat("DISCOUNT_CONV_PERCENTAGE");
		String remainingQuantity = rs.getString("REMAINING_QTY");
		String sexedInd = rs.getString("SEXED");
		Integer semenUsageCount = null;
		Integer semenSuccessCount = null;
		Integer semenFailureCount = null;
		Integer semenTbdCount = null;
		
		try {
			semenUsageCount = rs.getInt("USE_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("USE_COUNT column not found in the query", Util.WARNING);
		}
					
		try {
			semenSuccessCount = rs.getInt("SUCCESS_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("SUCCESS_COUNT column not found in the query", Util.WARNING);
		}

		try {
			semenFailureCount = rs.getInt("FAILURE_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("FAILURE_COUNT column not found in the query", Util.WARNING);
		}

		try {
			semenTbdCount = rs.getInt("TBD_COUNT");
		} catch (Exception ex) {
			IMDLogger.log("TBD_COUNT column not found in the query", Util.WARNING);
		}
		
		
		animalValue = new Sire("GBL", id,DateTime.now(),true,0d,"PKR");
		animalValue.setBreed(breed);
		animalValue.setAlias(alias);
		animalValue.setSemenInd(semenInd);
		animalValue.setSireSpecification(sireSpecification);
		animalValue.setSirePhoto(sirePhoto);
		animalValue.setController(controller);
		animalValue.setMarketedByCompany(new Contact(semenCompany));
		animalValue.setCurrentConventionalListPrice(currentConventionalListPrice);
		animalValue.setCurrentSexListPrice(currentSexListPrice);
		animalValue.setDiscountConventionalPercentage(discountConventionalPercentage);
		animalValue.setDiscountSexPercentage(discountSexPercentage);
		animalValue.setSemenUsageCount(semenUsageCount);
		animalValue.setSemenSuccessCount(semenSuccessCount);
		animalValue.setSemenFailureCount(semenFailureCount);
		animalValue.setSemenTbdCount(semenTbdCount);
		ArrayList<Note> rmngQty = new ArrayList<Note>();
		rmngQty.add(new Note(0,remainingQuantity));
		rmngQty.add(new Note(1,sexedInd));
		animalValue.setNotes(rmngQty);
		
		animalValue.setCreatedBy(new User(rs.getString("CREATED_BY")));
		animalValue.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM")));
		animalValue.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animalValue.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM")));
		return animalValue;
	}
	private Inventory getInventoryFromSQLRecord(ResultSet rs) throws SQLException, IMDException {
		Inventory inv = new Inventory();
		inv.setOrgID(rs.getString("ORG_ID"));
		inv.setItemSKU(rs.getString("ITEM_ID"));
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
