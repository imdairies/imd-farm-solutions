package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.FoodInventoryItemBean;
import com.imd.util.IMDProperties;
import com.imd.util.Util;


public class FoodInventoryItem extends IMDairiesDTO {
	private String stockTrackingId;
	private String categoryCd;
	private String lookupCd;
	private Float numberOfUnits;
	private Float quantityPerUnit;
	private Float pricePerUnit;
	private String numberOfUnitsUnit;
	private String quantityPerUnitUnit;
	private String pricePerUnitUnit;
	private DateTime orderDttm;	
	private DateTime receivedDttm;
	private DateTime expiryDttm;
	private DateTime consumptionDttm;
	private String purchasedFromCd;
	private String categoryDescr;
	private String lookupCodeDescr;
	private Float totalQuantity;
	private Float usedQuantity;
	private Float remainingQuantity;
	private Float remainingDays;
	
	public Float getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Float totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Float getUsedQuantity() {
		return usedQuantity;
	}

	public void setUsedQuantity(Float usedQuantity) {
		this.usedQuantity = usedQuantity;
	}

	public Float getRemainingQuantity() {
		return remainingQuantity;
	}

	public void setRemainingQuantity(Float remainingQuantity) {
		this.remainingQuantity = remainingQuantity;
	}
		
	public FoodInventoryItem() {
	}
	
	public FoodInventoryItem(String trackingId, String orgId, String categoryCd, String lookupCd, Float numOfUnits, Float qtyPerUnit, Float pricePerUnit,
			DateTime orderDttm, DateTime rcvdDttm, DateTime expiryDttm,  String purchasedFrom, User createdUpdatedUser, DateTime createdUpdatedDttm) {
		setOrgId(orgId);
		this.stockTrackingId = trackingId;
		this.categoryCd = categoryCd;
		this.lookupCd = lookupCd;
		this.numberOfUnits = numOfUnits;
		this.quantityPerUnit = qtyPerUnit;
		this.pricePerUnit = pricePerUnit;
		this.orderDttm = orderDttm;	
		this.receivedDttm = rcvdDttm;
		this.expiryDttm = expiryDttm;
		this.purchasedFromCd = purchasedFrom;
		this.setCreatedBy(createdUpdatedUser);
		this.setUpdatedBy(createdUpdatedUser);
		this.setCreatedDTTM(createdUpdatedDttm);
		this.setUpdatedDTTM(createdUpdatedDttm);
	}

	public FoodInventoryItem(FoodInventoryItemBean inventoryBean) {
		if (inventoryBean.getStockTrackingId() == null || inventoryBean.getStockTrackingId().isEmpty()) 
			this.stockTrackingId = inventoryBean.getCategoryCd() + '-' + inventoryBean.getLookupCd() + '-' + 
				Util.getDateTimeInSpecifiedFormat(inventoryBean.getOrderFromDttm(), "yyyy-MM-dd-HH:mm:ss");
		else 
			this.stockTrackingId = inventoryBean.getStockTrackingId();
		this.categoryCd = inventoryBean.getCategoryCd();
		this.lookupCd = inventoryBean.getLookupCd();
		this.numberOfUnits = inventoryBean.getNumberOfUnits();
		this.quantityPerUnit = inventoryBean.getQuantityPerUnit();
		this.pricePerUnit = inventoryBean.getPricePerUnit();
		this.numberOfUnitsUnit = inventoryBean.getPackingUnitCode();
		this.quantityPerUnitUnit = inventoryBean.getQuantityUnitCode();
		this.pricePerUnitUnit = inventoryBean.getCurrencyCode();
		this.purchasedFromCd = inventoryBean.getPurchasedFromCd();
		this.orderDttm = inventoryBean.getOrderFromDttmStr() == null || inventoryBean.getOrderFromDttmStr().isEmpty() ? null : new DateTime(Util.parseDateTime(inventoryBean.getOrderFromDttmStr(), "yyyy-MM-dd HH:mm"),IMDProperties.getServerTimeZone());
		this.receivedDttm = inventoryBean.getReceivedFromDttmStr() == null || inventoryBean.getReceivedFromDttmStr().isEmpty() ? null : new DateTime(Util.parseDateTime(inventoryBean.getReceivedFromDttmStr(), "yyyy-MM-dd HH:mm"),IMDProperties.getServerTimeZone());
		this.expiryDttm = inventoryBean.getExpiryFromDttmStr() == null || inventoryBean.getExpiryFromDttmStr().isEmpty() ? null : new DateTime(Util.parseDateTime(inventoryBean.getExpiryFromDttmStr(), "yyyy-MM-dd HH:mm"),IMDProperties.getServerTimeZone());
		this.consumptionDttm = inventoryBean.getConsumedFromDttmStr() == null || inventoryBean.getConsumedFromDttmStr().isEmpty() ? null : new DateTime(Util.parseDateTime(inventoryBean.getConsumedFromDttmStr(), "yyyy-MM-dd HH:mm"),IMDProperties.getServerTimeZone());
	}

	public FoodInventoryItem(FoodUsage usg) {
		this.setOrgId(usg.getOrgId());
		//usg.getComments();
		this.usedQuantity = usg.getConsumptionQuantity();
		this.consumptionDttm = usg.getConsumptionTimestamp();
		this.stockTrackingId = usg.getStockTrackingId();
	}

	public String getStockTrackingId() {
		return stockTrackingId;
	}


	public void setStockTrackingId(String stockTrackingId) {
		this.stockTrackingId = stockTrackingId;
	}


	public String getCategoryCd() {
		return categoryCd;
	}


	public void setCategoryCd(String categoryCd) {
		this.categoryCd = categoryCd;
	}


	public String getLookupCd() {
		return lookupCd;
	}


	public void setLookupCd(String lookupCd) {
		this.lookupCd = lookupCd;
	}


	public Float getNumberOfUnits() {
		return numberOfUnits;
	}


	public void setNumberOfUnits(Float numberOfUnits) {
		this.numberOfUnits = numberOfUnits;
	}


	public Float getQuantityPerUnit() {
		return quantityPerUnit;
	}


	public void setQuantityPerUnit(Float quantityPerUnit) {
		this.quantityPerUnit = quantityPerUnit;
	}


	public Float getPricePerUnit() {
		return pricePerUnit;
	}


	public void setPricePerUnit(Float pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}


	public DateTime getOrderDttm() {
		return orderDttm;
	}


	public void setOrderDttm(DateTime orderDttm) {
		this.orderDttm = orderDttm;
	}


	public DateTime getReceivedDttm() {
		return receivedDttm;
	}


	public void setReceivedDttm(DateTime receivedDttm) {
		this.receivedDttm = receivedDttm;
	}


	public DateTime getExpiryDttm() {
		return expiryDttm;
	}


	public void setExpiryDttm(DateTime expiryDttm) {
		this.expiryDttm = expiryDttm;
	}


	public String getPurchasedFromCd() {
		return purchasedFromCd;
	}


	public void setPurchasedFromCd(String purchasedFromCd) {
		this.purchasedFromCd = purchasedFromCd;
	}


	public String toString() {
		return dtoToJson(" ");
	}
	private String stringify(String prefix, DateTimeFormatter fmt) {
		String returnValue =  prefix + fieldToJson("stockTrackingId", this.stockTrackingId) + ",\n" +
			prefix + fieldToJson("categoryCd", this.categoryCd) + ",\n" +
			prefix + fieldToJson("categoryDescr", this.categoryDescr) + ",\n" +
			prefix + fieldToJson("lookupCd", this.lookupCd) + ",\n" +
			prefix + fieldToJson("lookupCodeDescr", this.lookupCodeDescr) + ",\n" +
			prefix + fieldToJson("numberOfUnits", this.numberOfUnits) + ",\n" + 
			prefix + fieldToJson("numberOfUnitsUnit", this.numberOfUnitsUnit) + ",\n" + 
			prefix + fieldToJson("quantityPerUnit", this.quantityPerUnit) + ",\n" +
			prefix + fieldToJson("quantityPerUnitUnit", this.quantityPerUnitUnit) + ",\n" +
			prefix + fieldToJson("totalQuantity", this.totalQuantity) + ",\n" +
			prefix + fieldToJson("usedQuantity", this.usedQuantity) + ",\n" +
			prefix + fieldToJson("remainingQuantity", this.remainingQuantity) + ",\n" +
			prefix + fieldToJson("pricePerUnit", this.pricePerUnit) + ",\n" + 
			prefix + fieldToJson("pricePerUnitUnit", this.pricePerUnitUnit) + ",\n";
			if (fmt == null) {
				returnValue += prefix + fieldToJson("orderDttm", this.orderDttm) + ",\n" + 
				prefix + fieldToJson("receivedDttm", this.receivedDttm) + ",\n" + 
				prefix + fieldToJson("expiryDttm", this.expiryDttm) + ",\n" + 
				prefix + fieldToJson("consumptionDttm", this.consumptionDttm) + ",\n";
			} else {
				returnValue += prefix + fieldToJson("orderDttm", fmt.print(this.orderDttm)) + ",\n" + 
				prefix + fieldToJson("receivedDttm", fmt.print(this.receivedDttm)) + ",\n" + 
				prefix + fieldToJson("expiryDttm", fmt.print(this.expiryDttm)) + ",\n" + 
				prefix + fieldToJson("consumptionDttm", fmt.print(this.consumptionDttm)) + ",\n";
			}
				
			returnValue += prefix + fieldToJson("remainingDays", this.remainingDays) + ",\n" + 
			prefix + fieldToJson("purchasedFromCd", this.purchasedFromCd) + ",\n";
		return returnValue;
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix, null) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix, fmt) + super.dtoToJson(prefix, fmt));
	}

	public String getCategoryDescr() {
		return categoryDescr;
	}

	public void setCategoryDescr(String categoryDescr) {
		this.categoryDescr = categoryDescr;
	}

	public String getLookupCodeDescr() {
		return lookupCodeDescr;
	}

	public void setLookupCodeDescr(String lookupCodeDescr) {
		this.lookupCodeDescr = lookupCodeDescr;
	}

	public String getNumberOfUnitsUnit() {
		return numberOfUnitsUnit;
	}

	public void setNumberOfUnitsUnit(String numberOfUnitsUnit) {
		this.numberOfUnitsUnit = numberOfUnitsUnit;
	}

	public String getQuantityPerUnitUnit() {
		return quantityPerUnitUnit;
	}

	public void setQuantityPerUnitUnit(String quantityPerUnitUnit) {
		this.quantityPerUnitUnit = quantityPerUnitUnit;
	}

	public String getPricePerUnitUnit() {
		return pricePerUnitUnit;
	}

	public void setPricePerUnitUnit(String pricePerUnitUnit) {
		this.pricePerUnitUnit = pricePerUnitUnit;
	}

	public DateTime getConsumptionDttm() {
		return consumptionDttm;
	}

	public void setConsumptionDttm(DateTime consumptionDttm) {
		this.consumptionDttm = consumptionDttm;
	}

	public Float getRemainingDays() {
		return remainingDays;
	}

	public void setRemainingDays(Float remainingDays) {
		this.remainingDays = remainingDays;
	}

}
