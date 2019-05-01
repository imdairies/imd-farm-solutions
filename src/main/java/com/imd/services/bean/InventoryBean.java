package com.imd.services.bean;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class InventoryBean {
	private String itemSKU;
	private String itemType;
	private Float quantity;
	private Float price;
	private Float discount;
	private String orderDttmStr;
	private String receivedDttmStr;
	private String inventoryAddDttmStr;
	private Float invoiceAmount;
	private String auxValue1;
	private String auxValue2;
	private String auxValue3;
	private String auxValue4;
	private String auxValue5;
	public String getItemSKU() {
		return itemSKU;
	}
	public void setItemSKU(String itemSKU) {
		this.itemSKU = itemSKU;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public Float getQuantity() {
		return quantity;
	}
	public void setQuantity(Float quantity) {
		this.quantity = quantity;
	}
	public Float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	public Float getDiscount() {
		return discount;
	}
	public void setDiscount(Float discount) {
		this.discount = discount;
	}
	public String getOrderDttmStr() {
		return orderDttmStr;
	}
	public void setOrderDttmStr(String orderDttm) {
		this.orderDttmStr = orderDttm;
	}
	public String getReceivedDttmStr() {
		return receivedDttmStr;
	}
	public void setReceivedDttmStr(String receivedDttm) {
		this.receivedDttmStr = receivedDttm;
	}
	public String getInventoryAddDttmStr() {
		return inventoryAddDttmStr;
	}
	public void setInventoryAddDttmStr(String inventoryAddDttm) {
		this.inventoryAddDttmStr = inventoryAddDttm;
	}
	public Float getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(Float invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public String getAuxValue1() {
		return auxValue1;
	}
	public void setAuxValue1(String auxValue1) {
		this.auxValue1 = auxValue1;
	}
	public String getAuxValue2() {
		return auxValue2;
	}
	public void setAuxValue2(String auxValue2) {
		this.auxValue2 = auxValue2;
	}
	public String getAuxValue3() {
		return auxValue3;
	}
	public void setAuxValue3(String auxValue3) {
		this.auxValue3 = auxValue3;
	}
	public String getAuxValue4() {
		return auxValue4;
	}
	public void setAuxValue4(String auxValue4) {
		this.auxValue4 = auxValue4;
	}
	public String getAuxValue5() {
		return auxValue5;
	}
	public void setAuxValue5(String auxValue5) {
		this.auxValue5 = auxValue5;
	}	

	public String toString() {
		return 	"\n itemSKU:" + itemSKU + 
				"\n itemType:" + itemType + 
				"\n quantity:" + quantity + 
				"\n price:" + price + 
				"\n discount:" + discount + 
				"\n invoiceAmount:" + invoiceAmount + 
				"\n orderDttmStr:" + orderDttmStr + 
				"\n receivedDttmStr:" + receivedDttmStr + 
				"\n inventoryAddDttmStr:" + inventoryAddDttmStr + 
				"\n auxValue1:" + auxValue1 +
				"\n auxValue2:" + auxValue2 +
				"\n auxValue3:" + auxValue3 +
				"\n auxValue4:" + auxValue4 +
				"\n auxValue5:" + auxValue5;
	}
	public String validateValues() {
		String errorMessage = "";
		if (itemSKU == null || itemSKU.trim().isEmpty())
			errorMessage = "You must specify the item code";
		else if (this.itemType == null || itemType.isEmpty())
			errorMessage = "You must specify the item type";
		else if (price == null || price <= 0)
			errorMessage = "You must specify a valid item price";
		else if (discount == null || discount < 0 || discount > 100)
			errorMessage = "You must specify a valid item discount";
		else if (quantity == null || quantity < 0)
			errorMessage = "You must specify the item quantity";
		else if (orderDttmStr == null || orderDttmStr.isEmpty())
			errorMessage = "You must specify the item order date/time";
		else if (inventoryAddDttmStr == null || inventoryAddDttmStr.isEmpty())
			errorMessage = "You must specify the inventory added date/time";

		if (errorMessage.isEmpty() && (orderDttmStr != null && !orderDttmStr.isEmpty())) {
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
				DateTime.parse(orderDttmStr, fmt);
			} catch (Exception ex) {
				ex.printStackTrace();
				errorMessage = "You must specify the order date/time in the following format: yyyy-mm-DD H:mm";
			}
		}
		
		if (errorMessage.isEmpty() && (receivedDttmStr != null && !receivedDttmStr.isEmpty())) {
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
				DateTime.parse(receivedDttmStr, fmt);
			} catch (Exception ex) {
				ex.printStackTrace();
				errorMessage = "You must specify the order received date/time in the following format: yyyy-mm-DD H:mm";
			}
		}
		
		if (errorMessage.isEmpty() && (inventoryAddDttmStr != null && !inventoryAddDttmStr.isEmpty())) {
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
				DateTime.parse(inventoryAddDttmStr, fmt);
			} catch (Exception ex) {
				ex.printStackTrace();
				errorMessage = "You must specify the inventory added date/time in the following format: yyyy-mm-DD H:mm";
			}
		} 
		return errorMessage;
	}
	public void setOrderDttm(DateTime dateTime) {
		if (dateTime == null)
			this.orderDttmStr = null;
		else {
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
			this.orderDttmStr = dateTime.toString(fmt);
		}
		
	}

	public void setReceivedDttm(DateTime dateTime) {
		if (dateTime == null)
			this.receivedDttmStr = null;
		else {
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
			this.receivedDttmStr = dateTime.toString(fmt);
		}
	}

	public void setInventoryAddDttm(DateTime dateTime) {
		if (dateTime == null)
			this.inventoryAddDttmStr = null;
		else {
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
			this.inventoryAddDttmStr = dateTime.toString(fmt);
		}
	}

}
