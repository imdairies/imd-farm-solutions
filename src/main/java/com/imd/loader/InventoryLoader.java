package com.imd.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.Contact;
import com.imd.dto.FoodInventoryItem;
import com.imd.dto.FoodUsage;
import com.imd.dto.Inventory;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.FoodInventoryItemBean;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
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
			preparedStatement.setString(1, inventory.getOrgId());
			preparedStatement.setString(2, inventory.getItemSKU());
			preparedStatement.setString(3, inventory.getItemType());
			preparedStatement.setString(4,  Util.getDateTimeInSQLFormat(inventory.getOrderDttm()));
			preparedStatement.setFloat(5, inventory.getPrice());
			preparedStatement.setFloat(6, inventory.getDiscount());
			preparedStatement.setFloat(7, inventory.getQuantity());
			preparedStatement.setString(8,  (inventory.getReceivedDttm() == null ? null : Util.getDateTimeInSQLFormat(inventory.getReceivedDttm())));
			preparedStatement.setString(9,  (inventory.getInventoryAddDttmStr() == null ? null : Util.getDateTimeInSQLFormat(inventory.getInventoryAddDttm())));
			preparedStatement.setString(10, (inventory.getCreatedBy() == null ? null : inventory.getCreatedBy().getUserId()));
			preparedStatement.setString(11, (inventory.getCreatedDTTM() == null ? null :inventory.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(12,(inventory.getUpdatedBy() == null ? null : inventory.getUpdatedBy().getUserId()));
			preparedStatement.setString(13,(inventory.getUpdatedDTTM() == null ? null :inventory.getUpdatedDTTMSQLFormat()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
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
	public int deleteSemenInventory(String orgId, String id, String sexed) {
		String qryString = "DELETE FROM INV_SEMEN where ORG_ID='" + orgId + 
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
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgID);
			preparedStatement.setString(2, itemId);
			preparedStatement.setString(3, Util.getDateTimeInSQLFormat(addedDTTM));
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
	
	public int deleteFoodInventoryUsage(FoodUsage usg) {
		return this.deleteFoodInventoryUsage(usg.getOrgId(), usg.getStockTrackingId(), usg.getConsumptionTimestamp());
	}
	public int deleteFoodInventoryUsage(String orgId, String trackingId) {
		return deleteFoodInventoryUsage(orgId, trackingId, null);
	}

	public int deleteFoodInventoryUsage(String orgId, String trackingId, DateTime usageTimestamp) {
		String qryString = "DELETE FROM imd.INV_FOOD_USAGE where ORG_ID=? AND STOCK_TRACKING_ID=?";
		if (usageTimestamp != null)
			qryString += " AND USAGE_DTTM=? ";
		
		int result = -1;
		int parameterIndex = 0;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(++parameterIndex, orgId);
			preparedStatement.setString(++parameterIndex, trackingId);
			if (usageTimestamp != null)
				preparedStatement.setString(++parameterIndex, Util.getDateTimeInSQLFormat(usageTimestamp));
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
	
	public Float retrieveRemainingQuantity(String orgId, String stockTrackingId) {
		FoodInventoryItemBean searchBean = new FoodInventoryItemBean();
		searchBean.setOrgId(orgId);
		searchBean.setStockTrackingId(stockTrackingId);
		List <FoodInventoryItem> items  = retrieveFoodItemInventory(searchBean);
		if (items != null && !items.isEmpty())
			return items.get(0).getRemainingQuantity();
		else 
			return null;
	}

	public int addFoodInventoryUsage(FoodUsage usage) {
		int recordAdded = -1;
		int parameterIndex = 0;
		
		String qryString = "insert into imd.INV_FOOD_USAGE (ORG_ID,"
				+ "STOCK_TRACKING_ID,"
				+ "USAGE_QTY,"
				+ "USAGE_DTTM,"
				+ "USAGE_CD,"
				+ "COMMENTS,"
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(++parameterIndex, usage.getOrgId());
			preparedStatement.setString(++parameterIndex, usage.getStockTrackingId());
			preparedStatement.setString(++parameterIndex, usage.getConsumptionQuantity() == null ? null : usage.getConsumptionQuantity().toString());
			preparedStatement.setString(++parameterIndex, Util.getDateTimeInSQLFormat(usage.getConsumptionTimestamp()));
			preparedStatement.setString(++parameterIndex, usage.getUsageCd());
			preparedStatement.setString(++parameterIndex, usage.getComments());
			preparedStatement.setString(++parameterIndex, (usage.getCreatedBy() == null ? null : usage.getCreatedBy().getUserId()));
			preparedStatement.setString(++parameterIndex, (usage.getCreatedDTTM() == null ? null :usage.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(++parameterIndex,(usage.getUpdatedBy() == null ? null : usage.getUpdatedBy().getUserId()));
			preparedStatement.setString(++parameterIndex,(usage.getUpdatedDTTM() == null ? null :usage.getUpdatedDTTMSQLFormat()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
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

	public int addSemenInventoryUsage(Inventory inventory) {
		int recordAdded = -1;
		String qryString = "insert into imd.INV_SEMEN_USAGE (ORG_ID,"
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
			preparedStatement.setString(1, inventory.getOrgId());
			preparedStatement.setString(2, inventory.getItemSKU());
			preparedStatement.setString(3, inventory.getItemType());
			preparedStatement.setFloat(4,  inventory.getQuantity());
			preparedStatement.setString(5, (inventory.getInventoryAddDttmStr() == null ? null : Util.getDateTimeInSQLFormat(inventory.getInventoryAddDttm())));
			preparedStatement.setString(6,  inventory.getAuxValue1());
			preparedStatement.setString(7, (inventory.getCreatedBy() == null ? null : inventory.getCreatedBy().getUserId()));
			preparedStatement.setString(8, (inventory.getCreatedDTTM() == null ? null :inventory.getCreatedDTTMSQLFormat()));
			preparedStatement.setString(9,(inventory.getUpdatedBy() == null ? null : inventory.getUpdatedBy().getUserId()));
			preparedStatement.setString(10,(inventory.getUpdatedDTTM() == null ? null :inventory.getUpdatedDTTMSQLFormat()));
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
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
				+ " FROM imd.semen_rmng_qty_vw A " 
				+ " LEFT OUTER JOIN  LV_SIRE B ON B.id=A.item_id " 
				+ " LEFT OUTER JOIN sire_usage_stats_vw C ON A.item_id=C.CODE  WHERE A.item_id = B.id and A.org_id=? and remaining_qty > 0 "				
				+ " union "
				+ " select 0 as USE_COUNT,0 as success_count,0 as failure_count,0 as tbd_count, \"∞\" as REMAINING_QTY,'N' as SEXED, A.* FROM LV_SIRE A where semen_ind='N' "
				+ " and  A.ID not in (select ANIMAL_TAG FROM ANIMALS where herd_leaving_dttm is not null)";
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
	
	public Inventory getRemainingSemenInventory(String orgId, String itemSKU, String sexedInd) {
		Inventory inv = null;
		ResultSet rs;
		String qryString = "SELECT * FROM imd.semen_rmng_qty_vw where ORG_ID=? AND ITEM_ID=? AND SEXED=?";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, itemSKU);
			preparedStatement.setString(3, sexedInd);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	inv = new Inventory();
				inv.setOrgId(rs.getString("ORG_ID"));
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
		
		
		animalValue = new Sire("GBL", id,DateTime.now(IMDProperties.getServerTimeZone()),true,0d,"PKR");
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
		animalValue.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		animalValue.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		animalValue.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		return animalValue;
	}
	public int deleteFoodInventory(String orgId, String stockTrackingId) {
		String qryString = "DELETE FROM imd.INV_FEED WHERE ORG_ID= ?  AND STOCK_TRACKING_ID = ? ";
		
		int result = -1;
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, stockTrackingId);
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
	public int addFoodInventory(FoodInventoryItem invItem) {
		int recordAdded = -1;
		String qryString = "insert into INV_FEED (STOCK_TRACKING_ID, ORG_ID,"
				+ "CATEGORY_CD, "
				+ "LOOKUP_CD, "
				+ "NO_OF_UNITS, "
				+ "QTY_PER_UNIT, "
				+ "PRICE_PER_UNIT, "
				+ "NO_OF_UNITS_UNIT, " 
				+ "QTY_PER_UNIT_UNIT, "
				+ "PRICE_PER_UNIT_UNIT, "
				+ "ORDER_DTTM, "
				+ "RCVD_DTTM, "
				+ "EXPIRY_DTTM, "
				+ "PURCHASED_FROM, "
				+ "CREATED_BY,"
				+ "CREATED_DTTM,"
				+ "UPDATED_BY,"
				+ "UPDATED_DTTM) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		int index = 1;
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(index++, invItem.getStockTrackingId());
			preparedStatement.setString(index++, invItem.getOrgId());
			preparedStatement.setString(index++, invItem.getCategoryCd());
			preparedStatement.setString(index++, invItem.getLookupCd());
			preparedStatement.setString(index++, invItem.getNumberOfUnits() == null ? null : invItem.getNumberOfUnits().toString());
			preparedStatement.setString(index++, invItem.getQuantityPerUnit() == null ? null : invItem.getQuantityPerUnit().toString());
			preparedStatement.setString(index++, invItem.getPricePerUnit() == null ? null : invItem.getPricePerUnit().toString());
			preparedStatement.setString(index++, invItem.getNumberOfUnitsUnit());
			preparedStatement.setString(index++, invItem.getQuantityPerUnitUnit());
			preparedStatement.setString(index++, invItem.getPricePerUnitUnit());
			preparedStatement.setString(index++, invItem.getOrderDttm() == null ? null : Util.getDateTimeInSQLFormat(invItem.getOrderDttm()));
			preparedStatement.setString(index++, invItem.getReceivedDttm() == null ? null : Util.getDateTimeInSQLFormat(invItem.getReceivedDttm()));
			preparedStatement.setString(index++, invItem.getExpiryDttm() == null ? null : Util.getDateTimeInSQLFormat(invItem.getExpiryDttm()));
			preparedStatement.setString(index++, invItem.getPurchasedFromCd());
			preparedStatement.setString(index++, invItem.getCreatedBy() == null ? null : invItem.getCreatedBy().getUserId());
			preparedStatement.setString(index++, invItem.getCreatedDTTM() == null ? null :invItem.getCreatedDTTMSQLFormat());
			preparedStatement.setString(index++, invItem.getUpdatedBy() == null ? null : invItem.getUpdatedBy().getUserId());
			preparedStatement.setString(index++, invItem.getUpdatedDTTM() == null ? null :invItem.getUpdatedDTTMSQLFormat());
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordAdded = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordAdded = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
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
	
	public List<FoodInventoryItem> retrieveFoodItemInventory(FoodInventoryItemBean searchBean) {
		ResultSet rs;
		List<FoodInventoryItem> searchResults = new ArrayList<FoodInventoryItem>();
		List<String> values = new ArrayList<String>();
		String qryString = "SELECT A.ORG_ID, "
						    + " IFNULL(D.USED_QTY,0) AS USED_QTY, "
						    + " A.STOCK_TRACKING_ID, "
							+ " A.CATEGORY_CD, "
							+ " A.LOOKUP_CD, "
							+ " A.NO_OF_UNITS, " 
							+ " A.QTY_PER_UNIT, "
							+ " A.PRICE_PER_UNIT, "
							+ " A.NO_OF_UNITS_UNIT, " 
							+ " A.QTY_PER_UNIT_UNIT, "
							+ " A.PRICE_PER_UNIT_UNIT, "
							+ " A.ORDER_DTTM, "
							+ " A.RCVD_DTTM, "
							+ " A.EXPIRY_DTTM, "
							+ " A.PURCHASED_FROM, "
							+ " A.CREATED_BY, "
							+ " A.CREATED_DTTM, "
							+ " A.UPDATED_BY, "
							+ " A.UPDATED_DTTM, "
							+ " C.SHORT_DESCR AS ITEM_SHORT_DESCR, "
							+ " C.CATEGORY_CD AS ITEM_CATEGORY_DESCR "
							+ " FROM imd.INV_FEED A "
							+ " LEFT OUTER JOIN "
							+ "  imd.LOOKUP_VALUES C " 
							+ "   ON (A.LOOKUP_CD=C.LOOKUP_CD AND C.CATEGORY_CD = A.CATEGORY_CD) "
					        + " LEFT OUTER JOIN "
							+ "  (SELECT ORG_ID,STOCK_TRACKING_ID, SUM(USAGE_QTY) AS USED_QTY FROM imd.INV_FOOD_USAGE GROUP BY STOCK_TRACKING_ID,ORG_ID) D "
				            + "  ON (A.ORG_ID = D.ORG_ID AND A.STOCK_TRACKING_ID = D.STOCK_TRACKING_ID) ";
							

		String whereClause = "";
		
		if (searchBean == null) {
			return searchResults;
		}
		if (searchBean.getOrgId() != null && !searchBean.getOrgId().isEmpty()) {
			whereClause += " WHERE A.ORG_ID = ? ";
			values.add(searchBean.getOrgId());
		}
		
		if (searchBean.getStockTrackingId() != null && !searchBean.getStockTrackingId().isEmpty()) {
			whereClause += (values.isEmpty() ? " WHERE " : " AND ") + " A.STOCK_TRACKING_ID=? ";
			values.add(searchBean.getStockTrackingId());
		}
		if (searchBean.getCategoryCd() != null && !searchBean.getCategoryCd().isEmpty()) {
			String[] lookupCodes = searchBean.getCategoryCd().split(",");
			String inClause ="";
			if (lookupCodes.length >= 1) {
				inClause = "?";
				for (int i=1; i<lookupCodes.length; i++) {
					values.add(lookupCodes[i]);
					inClause += ",?";
				}
				whereClause += (values.isEmpty() ? " WHERE " : " AND ") + " A.CATEGORY_CD IN (" + inClause + ") ";
				values.add(lookupCodes[0]);
			}
		}
		
		if (searchBean.getLookupCd() != null && !searchBean.getLookupCd().isEmpty()) {
			String[] lookupCodes = searchBean.getLookupCd().split(",");
			String inClause ="";
			if (lookupCodes.length >= 1) {
				inClause = "?";
				for (int i=1; i<lookupCodes.length; i++) {
					values.add(lookupCodes[i]);
					inClause += ",?";
				}
				whereClause += (values.isEmpty() ? " WHERE " : " AND ") + " A.LOOKUP_CD IN (" + inClause + ") ";
				values.add(lookupCodes[0]);
			}
		}
		
		if (searchBean.getOrderFromDttmStr() != null && !searchBean.getOrderFromDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.ORDER_DTTM >= ? ";
			values.add(searchBean.getOrderFromDttmStr());
		}
		if (searchBean.getOrderToDttmStr() != null && !searchBean.getOrderToDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.ORDER_DTTM <= ? ";
			values.add(searchBean.getOrderToDttmStr());
		}
		
		if (searchBean.getReceivedFromDttmStr() != null && !searchBean.getReceivedFromDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.RCVD_DTTM >= ? ";
			values.add(searchBean.getReceivedFromDttmStr());
		}
		if (searchBean.getReceivedToDttmStr() != null && !searchBean.getReceivedToDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.RCVD_DTTM <= ? ";
			values.add(searchBean.getReceivedToDttmStr());
		}
		
		if (searchBean.getExpiryFromDttmStr() != null && !searchBean.getExpiryFromDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.EXPIRY_DTTM >= ? ";
			values.add(searchBean.getExpiryFromDttmStr());
		}
		if (searchBean.getExpiryToDttmStr() != null && !searchBean.getExpiryToDttmStr().isEmpty()) {
			whereClause +=  (values.isEmpty() ? " WHERE " : " AND ") + " A.EXPIRY_DTTM <= ? ";
			values.add(searchBean.getExpiryToDttmStr());
		}
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString + whereClause + " ORDER BY A.ORDER_DTTM DESC");
			for (int i=0; i < values.size(); i++) {
				preparedStatement.setString(i+1, values.get(i));
			}
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	FoodInventoryItem inv = new FoodInventoryItem();
		    	inv.setOrgId(rs.getString("ORG_ID"));
		    	inv.setUsedQuantity(rs.getFloat("USED_QTY"));
		    	inv.setStockTrackingId(rs.getString("STOCK_TRACKING_ID"));
		    	inv.setCategoryCd(rs.getString("CATEGORY_CD"));
		    	inv.setLookupCd(rs.getString("LOOKUP_CD"));
		    	inv.setNumberOfUnits(rs.getFloat("NO_OF_UNITS"));
		    	inv.setNumberOfUnitsUnit(rs.getString("NO_OF_UNITS_UNIT"));
		    	inv.setQuantityPerUnit(rs.getFloat("QTY_PER_UNIT"));
		    	inv.setQuantityPerUnitUnit(rs.getString("QTY_PER_UNIT_UNIT"));
		    	inv.setPricePerUnit(rs.getFloat("PRICE_PER_UNIT"));
		    	inv.setPricePerUnitUnit(rs.getString("PRICE_PER_UNIT_UNIT"));
		    	inv.setLookupCodeDescr(rs.getString("ITEM_SHORT_DESCR"));
		    	inv.setCategoryDescr(rs.getString("ITEM_CATEGORY_DESCR"));
		    	inv.setTotalQuantity(inv.getNumberOfUnits() * inv.getQuantityPerUnit());
		    	inv.setRemainingQuantity(inv.getTotalQuantity()-inv.getUsedQuantity());
		    	inv.setOrderDttm(new DateTime(rs.getTimestamp("ORDER_DTTM"), IMDProperties.getServerTimeZone()));
		    	inv.setReceivedDttm(new DateTime(rs.getTimestamp("RCVD_DTTM"), IMDProperties.getServerTimeZone()));
		    	inv.setExpiryDttm(new DateTime(rs.getTimestamp("EXPIRY_DTTM"), IMDProperties.getServerTimeZone()));
		    	inv.setPurchasedFromCd(rs.getString("PURCHASED_FROM"));
		    	inv.setCreatedBy(new User(rs.getString("CREATED_BY")));
		    	inv.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		    	inv.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
		    	inv.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
		    	searchResults.add(inv);
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
		return searchResults;
	}
	
	public List<FoodUsage> retrieveFeedInventoryUsage(String orgId, String stockTrackingId, String orderBy) {
		List<FoodUsage> usage = new ArrayList<FoodUsage>();
		ResultSet rs;
		String qryString = "SELECT A.*, C.SHORT_DESCR AS USAGE_SHORT_DESCR FROM imd.INV_FOOD_USAGE A"
				+ " LEFT OUTER JOIN "
				+ "  imd.LOOKUP_VALUES C " 
				+ "   ON (A.USAGE_CD=C.LOOKUP_CD AND C.CATEGORY_CD = 'FEED_USAGE') "
				+ " where A.ORG_ID=? AND A.STOCK_TRACKING_ID=? ORDER BY " + 
				(orderBy == null ? "A.USAGE_DTTM DESC" : orderBy);
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			preparedStatement.setString(1, orgId);
			preparedStatement.setString(2, stockTrackingId);
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
		    rs = preparedStatement.executeQuery();
		    while (rs.next()) {
		    	FoodUsage usg = new FoodUsage();
		    	usg.setOrgId(rs.getString("ORG_ID"));
		    	usg.setStockTrackingId(rs.getString("STOCK_TRACKING_ID"));
		    	usg.setUsageCd(rs.getString("USAGE_CD"));
		    	usg.setUsageShortDescription(rs.getString("USAGE_SHORT_DESCR"));
		    	usg.setComments(rs.getString("COMMENTS"));

		    	usg.setConsumptionQuantity(rs.getFloat("USAGE_QTY"));
		    	usg.setConsumptionTimestamp(new DateTime(rs.getTimestamp("USAGE_DTTM"), IMDProperties.getServerTimeZone()));
		    	usg.setCreatedBy(new User(rs.getString("CREATED_BY")));
		    	usg.setCreatedDTTM(new DateTime(rs.getTimestamp("CREATED_DTTM"),IMDProperties.getServerTimeZone()));
		    	usg.setUpdatedBy(new User(rs.getString("UPDATED_BY")));
				usg.setUpdatedDTTM(new DateTime(rs.getTimestamp("UPDATED_DTTM"),IMDProperties.getServerTimeZone()));
				usage.add(usg);
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
		return usage;
	}
	
	public List<FoodInventoryItem> retrieveFoodItemInventory(String orgId, String stockTrackingId) {
		FoodInventoryItemBean searchBean = new FoodInventoryItemBean();
		searchBean.setOrgId(orgId);
		searchBean.setStockTrackingId(stockTrackingId);
		return this.retrieveFoodItemInventory(searchBean);
	}

	public int updateFoodInventoryItem(FoodInventoryItem invItem2) {
		int recordUpdated = -1;
		List<String> values = new ArrayList<String>();
		
		String qryString = "UPDATE imd.INV_FEED SET ";
		if (invItem2.getNumberOfUnits() != null) {
			qryString += "NO_OF_UNITS=?,";
			values.add(invItem2.getNumberOfUnits().toString());
		} 
		if (invItem2.getNumberOfUnitsUnit() != null) {
			qryString += "NO_OF_UNITS_UNIT=?,";
			values.add(invItem2.getNumberOfUnitsUnit());
		}
		if (invItem2.getQuantityPerUnit() != null) {
			qryString += "QTY_PER_UNIT=?,";
			values.add(invItem2.getQuantityPerUnit().toString());
		} 
		if (invItem2.getQuantityPerUnitUnit() != null) {
			qryString += "QTY_PER_UNIT_UNIT=?,";
			values.add(invItem2.getQuantityPerUnitUnit());
		}
		if (invItem2.getPricePerUnit() != null) {
			qryString += "PRICE_PER_UNIT=?,";
			values.add(invItem2.getPricePerUnit().toString());
		}
		if (invItem2.getPricePerUnitUnit() != null) {
			qryString += "PRICE_PER_UNIT_UNIT=?,";
			values.add(invItem2.getPricePerUnitUnit());
		}
		if (invItem2.getOrderDttm() != null) {
			qryString += "ORDER_DTTM=?,";
			values.add(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm()));
		}
		if (invItem2.getReceivedDttm() != null) {
			qryString += "RCVD_DTTM=?,";
			values.add(Util.getDateTimeInSQLFormat(invItem2.getReceivedDttm()));
		}
		if (invItem2.getExpiryDttm() != null) {
			qryString += "EXPIRY_DTTM=?,";
			values.add(Util.getDateTimeInSQLFormat(invItem2.getExpiryDttm()));
		}
		if (invItem2.getPurchasedFromCd() != null) {
			qryString += "PURCHASED_FROM=?,";
			values.add(invItem2.getPurchasedFromCd());
		}
		if (!values.isEmpty()) {
			qryString += "UPDATED_DTTM=? WHERE ORG_ID=? AND STOCK_TRACKING_ID=?";
			values.add(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			values.add(invItem2.getOrgId());
			values.add(invItem2.getStockTrackingId());
		} else {
			IMDLogger.log("Nothing to update: " + qryString, Util.WARNING);
			return 0;
		}
		PreparedStatement preparedStatement = null;
		Connection conn = DBManager.getDBConnection();
		try {
			preparedStatement = conn.prepareStatement(qryString);
			for (int parameterIndex=0; parameterIndex< values.size(); parameterIndex++) {
				preparedStatement.setString(parameterIndex+1, values.get(parameterIndex));
			}
			IMDLogger.log(preparedStatement.toString(), Util.INFO);
			recordUpdated = preparedStatement.executeUpdate();
		} catch (java.sql.SQLIntegrityConstraintViolationException ex) {
			recordUpdated = Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION;
			ex.printStackTrace();
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
				if (preparedStatement != null && !preparedStatement.isClosed()) {
					preparedStatement.close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return recordUpdated;
	}
	
}










