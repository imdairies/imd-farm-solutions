package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

public class FoodUsage extends IMDairiesDTO {
	private String stockTrackingId;
	private DateTime consumptionTimestamp;
	private Float consumptionQuantity;
	private String comments;
	private String usageCd;
	private String usageShortDescription;
	private Float remainingQuantity;
	
	public FoodUsage(String orgId, String trackingId, DateTime itemConsumptionTimestamp, Float itemConsumptionQty, 
			String usageCd, String comments,
			DateTime createdAndUpdatedDttm, User createdAndUpdatedBy) {
		setOrgId(orgId);
		this.stockTrackingId = trackingId;
		this.consumptionTimestamp = itemConsumptionTimestamp;
		this.consumptionQuantity = itemConsumptionQty;
		this.usageCd = usageCd;
		this.comments = comments;
		setCreatedBy(createdAndUpdatedBy);
		setCreatedDTTM(createdAndUpdatedDttm);
		setUpdatedBy(createdAndUpdatedBy);
		setUpdatedDTTM(createdAndUpdatedDttm);
	}
	

	public FoodUsage() {
	}


	public Float getConsumptionQuantity() {
		return consumptionQuantity;
	}
	
	public void setConsumptionQuantity(Float consumptionQuantity) {
		this.consumptionQuantity = consumptionQuantity;
	}
	
	public DateTime getConsumptionTimestamp() {
		return consumptionTimestamp;
	}
	
	public void setConsumptionTimestamp(DateTime consumptionTimestamp) {
		this.consumptionTimestamp = consumptionTimestamp;
	}

	private String stringify(String prefix, DateTimeFormatter fmt) {
		String returnValue =  prefix + fieldToJson("orgID", getOrgId()) + ",\n" + 
				prefix + fieldToJson("stockTrackingId", this.stockTrackingId) + ",\n";
		if (fmt == null) {
			returnValue += prefix + fieldToJson("consumptionTimestamp", this.consumptionTimestamp) + ",\n";
		} else {
			returnValue += prefix + fieldToJson("consumptionTimestamp", fmt.print(this.consumptionTimestamp)) + ",\n";
		}
		returnValue += prefix + fieldToJson("consumptionQuantity", this.consumptionQuantity) + ",\n" +
		prefix + fieldToJson("usageCd", this.usageCd) + ",\n" +
		prefix + fieldToJson("remainingQuantity", this.remainingQuantity) + ",\n" +
		prefix + fieldToJson("usageShortDescription", this.usageShortDescription) + ",\n" +
		prefix + fieldToJson("comments", this.comments) + ",\n";
		
		return returnValue;
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix, null) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix, fmt) + super.dtoToJson(prefix, fmt));
	}

	public String getStockTrackingId() {
		return stockTrackingId;
	}

	public void setStockTrackingId(String trackingId) {
		this.stockTrackingId = trackingId;
	}


	public String getComments() {
		return comments;
	}


	public void setComments(String comments) {
		this.comments = comments;
	}


	public String getUsageCd() {
		return usageCd;
	}


	public void setUsageCd(String usageCd) {
		this.usageCd = usageCd;
	}


	public String getUsageShortDescription() {
		return usageShortDescription;
	}


	public void setUsageShortDescription(String usageShortDescription) {
		this.usageShortDescription = usageShortDescription;
	}


	public Float getRemainingQuantity() {
		return remainingQuantity;
	}


	public void setRemainingQuantity(Float remainingQuantity) {
		this.remainingQuantity = remainingQuantity;
	}
	
	
	
}
