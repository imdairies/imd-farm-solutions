package com.imd.services.bean;

import org.joda.time.DateTime;

import com.imd.util.Util;


public class FoodInventoryItemBean {
	private String orgId;
	private String stockTrackingId;
	private String categoryCd;
	private String lookupCd;
	private String usageCd;	
	private String comments;
	private Float numberOfUnits;
	private Float quantityPerUnit;
	private Float pricePerUnit;
	private Float consumptionQuantity;
	private boolean shouldShowReorderDays;
	
	private DateTime orderFromDttm;
	private DateTime orderToDttm;
	private DateTime receivedFromDttm;
	private DateTime receivedToDttm;
	private DateTime expiryFromDttm;
	private DateTime expiryToDttm;
	private DateTime consumedFromDttm;
	private DateTime consumedToDttm;

	private String orderFromDttmStr;
	private String receivedFromDttmStr;
	private String expiryFromDttmStr;
	private String orderToDttmStr;
	private String receivedToDttmStr;
	private String expiryToDttmStr;
	private String consumedFromDttmStr;
	private String consumedToDttmStr;
	
	private String packingUnitCode;
	private String quantityUnitCode;
	private String currencyCode;	
	
	private String purchasedFromCd;
	private String loginToken;
	
	public DateTime getConsumedFromDttm() {
		return consumedFromDttm;
	}

	public void setConsumedFromDttm(DateTime consumedFromDttm) {
		this.consumedFromDttm = consumedFromDttm;
	}

	public DateTime getConsumedToDttm() {
		return consumedToDttm;
	}

	public void setConsumedToDttm(DateTime consumedToDttm) {
		this.consumedToDttm = consumedToDttm;
	}

	public String getConsumedFromDttmStr() {
		return consumedFromDttmStr;
	}

	public void setConsumedFromDttmStr(String consumedFromDttmStr) {
		this.consumedFromDttmStr = consumedFromDttmStr;
		if (consumedFromDttmStr != null && !consumedFromDttmStr.isEmpty()) {
			if (consumedFromDttmStr.trim().length() > 16)
				consumedFromDttm =  Util.parseDateTime(consumedFromDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				consumedFromDttm =  Util.parseDateTime(consumedFromDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.consumedFromDttm = null;
		}
	}

	public String getConsumedToDttmStr() {
		return consumedToDttmStr;
	}

	public void setConsumedToDttmStr(String consumedToDttmStr) {
		this.consumedToDttmStr = consumedToDttmStr;
		if (consumedToDttmStr != null && !consumedToDttmStr.isEmpty()) {
			if (consumedToDttmStr.trim().length() > 16)
				consumedToDttm =  Util.parseDateTime(consumedToDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				consumedToDttm =  Util.parseDateTime(consumedToDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.consumedToDttm = null;
		}
	}

	
	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
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

	public DateTime getOrderFromDttm() {
		return orderFromDttm;
	}

	public void setOrderFromDttm(DateTime orderFromDttm) {
		this.orderFromDttm = orderFromDttm;
	}

	public DateTime getOrderToDttm() {
		return orderToDttm;
	}

	public void setOrderToDttm(DateTime orderToDttm) {
		this.orderToDttm = orderToDttm;
	}

	public DateTime getReceivedFromDttm() {
		return receivedFromDttm;
	}

	public void setReceivedFromDttm(DateTime receivedFromDttm) {
		this.receivedFromDttm = receivedFromDttm;
	}

	public DateTime getReceivedToDttm() {
		return receivedToDttm;
	}

	public void setReceivedToDttm(DateTime receivedToDttm) {
		this.receivedToDttm = receivedToDttm;
	}

	public DateTime getExpiryFromDttm() {
		return expiryFromDttm;
	}

	public void setExpiryFromDttm(DateTime expiryFromDttm) {
		this.expiryFromDttm = expiryFromDttm;
	}

	public DateTime getExpiryToDttm() {
		return expiryToDttm;
	}

	public void setExpiryToDttm(DateTime expiryToDttm) {
		this.expiryToDttm = expiryToDttm;
	}

	public String getPurchasedFromCd() {
		return purchasedFromCd;
	}

	public void setPurchasedFromCd(String purchasedFromCd) {
		this.purchasedFromCd = purchasedFromCd;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public String getOrderFromDttmStr() {
		return orderFromDttmStr;
	}

	public void setOrderFromDttmStr(String orderFromDttmStr) {
		this.orderFromDttmStr = orderFromDttmStr;
		if (orderFromDttmStr != null && !orderFromDttmStr.isEmpty()) {
			if (orderFromDttmStr.trim().length() > 16)
				orderFromDttm =  Util.parseDateTime(orderFromDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				orderFromDttm =  Util.parseDateTime(orderFromDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.orderFromDttm = null;
		}
	}

	public String getReceivedFromDttmStr() {
		return receivedFromDttmStr;
	}

	public void setReceivedFromDttmStr(String receivedFromDttmStr) {
		this.receivedFromDttmStr = receivedFromDttmStr;
		if (receivedFromDttmStr != null && !receivedFromDttmStr.isEmpty()) {
			if (receivedFromDttmStr.trim().length() > 16)
				receivedFromDttm =  Util.parseDateTime(receivedFromDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				receivedFromDttm =  Util.parseDateTime(receivedFromDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.receivedFromDttm = null;
		}
		
	}

	public String getExpiryFromDttmStr() {
		return expiryFromDttmStr;
	}

	public void setExpiryFromDttmStr(String expiryFromDttmStr) {
		this.expiryFromDttmStr = expiryFromDttmStr;
		if (expiryFromDttmStr != null && !expiryFromDttmStr.isEmpty()) {
			if (expiryFromDttmStr.trim().length() > 16)
				expiryFromDttm =  Util.parseDateTime(expiryFromDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				expiryFromDttm =  Util.parseDateTime(expiryFromDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.expiryFromDttm = null;
		}
	}

	public String getOrderToDttmStr() {
		return orderToDttmStr;
	}

	public void setOrderToDttmStr(String orderToDttmStr) {
		this.orderToDttmStr = orderToDttmStr;
		if (orderToDttmStr != null && !orderToDttmStr.isEmpty()) {
			if (orderToDttmStr.trim().length() > 16)
				orderToDttm =  Util.parseDateTime(orderToDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				orderToDttm =  Util.parseDateTime(orderToDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.orderToDttm = null;
		}
	}

	public String getReceivedToDttmStr() {
		return receivedToDttmStr;
	}

	public void setReceivedToDttmStr(String receivedToDttmStr) {
		this.receivedToDttmStr = receivedToDttmStr;
		if (receivedToDttmStr != null && !receivedToDttmStr.isEmpty()) {
			if (receivedToDttmStr.trim().length() > 16)
				receivedToDttm =  Util.parseDateTime(receivedToDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				receivedToDttm =  Util.parseDateTime(receivedToDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.receivedToDttm = null;
		}
	}

	public String getExpiryToDttmStr() {
		return expiryToDttmStr;
	}

	public void setExpiryToDttmStr(String expiryToDttmStr) {
		this.expiryToDttmStr = expiryToDttmStr;
		if (expiryToDttmStr != null && !expiryToDttmStr.isEmpty()) {
			if (expiryToDttmStr.trim().length() > 16)
				expiryToDttm =  Util.parseDateTime(expiryToDttmStr, "yyyy-MM-dd HH:mm:ss");
			else
				expiryToDttm =  Util.parseDateTime(expiryToDttmStr, "yyyy-MM-dd HH:mm");
		} else {
			this.expiryToDttm = null;
		}
	}

	public String getPackingUnitCode() {
		return packingUnitCode;
	}

	public void setPackingUnitCode(String packingUnitCode) {
		this.packingUnitCode = packingUnitCode;
	}

	public String getQuantityUnitCode() {
		return quantityUnitCode;
	}

	public void setQuantityUnitCode(String quantityUnitCode) {
		this.quantityUnitCode = quantityUnitCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public FoodInventoryItemBean() {
	}
	
	public FoodInventoryItemBean(String trackingId, String orgId, String categoryCd, String lookupCd, Float numOfUnits, Float qtyPerUnit, Float pricePerUnit,
			DateTime orderFromDttm, DateTime rcvdFromDttm, DateTime expiryFromDttm,  
			DateTime orderToDttm, DateTime rcvdToDttm, DateTime expiryToDttm,  String purchasedFrom) {
		this.orgId = orgId;
		this.stockTrackingId = trackingId;
		this.categoryCd = categoryCd;
		this.lookupCd = lookupCd;
		this.numberOfUnits = numOfUnits;
		this.quantityPerUnit = qtyPerUnit;
		this.pricePerUnit = pricePerUnit;
		this.orderFromDttm = orderFromDttm;
		if (orderFromDttm != null)
			this.orderFromDttmStr = Util.getDateInSQLFormat(orderFromDttm);
		else 
			this.orderFromDttmStr = null;
		this.orderToDttm = orderToDttm;	
		if (orderToDttm != null)
			this.orderToDttmStr = Util.getDateInSQLFormat(orderToDttm);
		else 
			this.orderToDttmStr = null;

		this.receivedFromDttm = rcvdFromDttm;
		if (rcvdFromDttm != null)
			this.receivedFromDttmStr = Util.getDateInSQLFormat(rcvdFromDttm);
		else 
			this.receivedFromDttmStr = null;
		this.receivedToDttm = rcvdToDttm;
		if (rcvdToDttm != null)
			this.receivedToDttmStr = Util.getDateInSQLFormat(rcvdToDttm);
		else 
			this.receivedToDttmStr = null;
		

		this.expiryToDttm = expiryToDttm;
		if (expiryToDttm != null)
			this.expiryToDttmStr = Util.getDateInSQLFormat(expiryToDttm);
		else 
			this.expiryToDttmStr = null;
		this.expiryFromDttm = expiryFromDttm;
		if (expiryFromDttm != null)
			this.expiryFromDttmStr = Util.getDateInSQLFormat(expiryFromDttm);
		else 
			this.expiryFromDttmStr = null;

		this.purchasedFromCd = purchasedFrom;
	}



	public String toString() {
		return 	"\n orgId:" + orgId + 
				"\n stockTrackingId:" + stockTrackingId + 
				"\n categoryCd:" + categoryCd + 
				"\n lookupCd:" + lookupCd + 
				"\n numberOfUnits:" + numberOfUnits + 
				"\n quantityPerUnit:" + quantityPerUnit + 
				"\n pricePerUnit:" + pricePerUnit + 
				"\n consumptionQuantity:" + consumptionQuantity + 
				"\n usageCd:" + usageCd + 
				"\n comments:" + comments + 
				
				"\n ==== Ordering Information =====" + 
				"\n orderFromDttm:" + orderFromDttm + 
				"\n orderToDttm:" + orderToDttm + 
				"\n orderFromDttmStr:" + orderFromDttmStr + 
				"\n orderToDttmStr:" + orderToDttmStr + 

				"\n ==== Receiving Information =====" + 
				"\n receivedFromDttm:" + receivedFromDttm + 
				"\n receivedToDttm:" + receivedToDttm + 
				"\n receivedFromDttmStr:" + receivedFromDttmStr + 
				"\n receivedToDttmStr:"   + receivedToDttmStr + 
				
				"\n ==== Expiry Information =====" + 
				"\n expiryFromDttm:" + expiryFromDttm +
				"\n expiryToDttm:" + expiryToDttm +
				"\n expiryFromDttmStr:" + expiryFromDttmStr +
				"\n expiryToDttmStr:" + expiryToDttmStr +

				"\n ==== Consumption Information =====" + 
				"\n consumedFromDttm:" + consumedFromDttm +
				"\n consumedToDttm:" + consumedToDttm +
				"\n consumedFromDttmStr:" + consumedFromDttmStr +
				"\n consumedToDttmStr:" + consumedToDttmStr +

				"\n shouldShowReorderDays:" + shouldShowReorderDays +
				"\n purchasedFromCd:" + purchasedFromCd;
	}

	public Float getConsumptionQuantity() {
		return consumptionQuantity;
	}

	public void setConsumptionQuantity(Float consumptionQuantity) {
		this.consumptionQuantity = consumptionQuantity;
	}

	public boolean isShouldShowReorderDays() {
		return shouldShowReorderDays;
	}

	public void setShouldShowReorderDays(boolean shouldShowReorderDays) {
		this.shouldShowReorderDays = shouldShowReorderDays;
	}

	public String getUsageCd() {
		return usageCd;
	}

	public void setUsageCd(String usageCd) {
		this.usageCd = usageCd;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}


}
