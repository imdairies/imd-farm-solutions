package com.imd.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.services.bean.InventoryBean;

public class Inventory extends IMDairiesDTO {
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
	
	public Inventory(InventoryBean invBean) {
		this.itemSKU = invBean.getItemSKU();
		this.itemType = invBean.getItemType();
		this.quantity = invBean.getQuantity();
		this.price = invBean.getPrice();
		this.discount = invBean.getDiscount();
		this.orderDttmStr = invBean.getOrderDttmStr();
		this.receivedDttmStr = invBean.getReceivedDttmStr();
		this.inventoryAddDttmStr = invBean.getInventoryAddDttmStr();
		this.invoiceAmount = invBean.getInvoiceAmount();
		this.auxValue1 = invBean.getAuxValue1();
		this.auxValue2 = invBean.getAuxValue2();
		this.auxValue3 = invBean.getAuxValue3();
		this.auxValue4 = invBean.getAuxValue4();
		this.auxValue5 = invBean.getAuxValue5();		
	}
	
	public Inventory() {
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
		return dtoToJson(" ");
	}
	private String stringify(String prefix) {
		return  prefix + fieldToJson("orgID", getOrgID()) + ",\n" + 
				prefix + fieldToJson("itemSKU", this.itemSKU) + ",\n" +
				prefix + fieldToJson("itemType", this.itemType) + ",\n" +
				prefix + fieldToJson("quantity", this.quantity) + ",\n" +
				prefix + fieldToJson("price", this.price) + ",\n" + 
				prefix + fieldToJson("discount", this.discount) + ",\n" +
				prefix + fieldToJson("invoiceAmount", this.invoiceAmount) + ",\n" + 
				prefix + fieldToJson("orderDttmStr", this.orderDttmStr) + ",\n" + 
				prefix + fieldToJson("receivedDttmStr", this.receivedDttmStr) + ",\n" + 
				prefix + fieldToJson("inventoryAddDttmStr", this.inventoryAddDttmStr) + ",\n" + 
				prefix + fieldToJson("auxValue1", this.auxValue1) + ",\n" + 
				prefix + fieldToJson("auxValue2", this.auxValue2) + ",\n" + 
				prefix + fieldToJson("auxValue3", this.auxValue3) + ",\n" + 
				prefix + fieldToJson("auxValue4", this.auxValue4) + ",\n" +
				prefix + fieldToJson("auxValue5", this.auxValue5) + ",\n";
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}

	public DateTime getOrderDttm() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
		return DateTime.parse(orderDttmStr, fmt);
	}

	public DateTime getReceivedDttm() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
		return DateTime.parse(receivedDttmStr, fmt);
	}

	public DateTime getInventoryAddDttm() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-dd-MM H:mm");
		return DateTime.parse(inventoryAddDttmStr, fmt);
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
