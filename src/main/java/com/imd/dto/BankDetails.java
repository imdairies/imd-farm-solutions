package com.imd.dto;

import com.imd.util.IMDException;

/**
 * This class holds the account information of the contact with which online bank transactions are made.
 * @author kashif.manzoor
 *
 */
public class BankDetails {
	private String bankName;
	private String accountName;
	private String accountIBANNumber;
	private String accountNumber;
	private String bankBranchCity;
	private String bankBranchArea;
	private String bankBranchCountry;
	public String getBankName() {
		return bankName;
	}
	
	public BankDetails (String bank, String acctName, String acctNumber, String acctIBAN) throws IMDException {
		if ((bank == null || bank.isEmpty()) || (acctName == null || acctName.isEmpty()) ||
			((acctNumber == null || acctNumber.isEmpty()) && (acctIBAN == null || acctIBAN.isEmpty()))) {
			throw new IMDException("Bank Name, Account Name can not be null and either the account number or IBAN should be specified");
		}
		else {
			this.bankName = bank;
			this.accountName = acctName;
			this.accountNumber = acctNumber;
			this.accountIBANNumber = acctIBAN;
		}
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAccountIBANNumber() {
		return accountIBANNumber;
	}
	public void setAccountIBANNumber(String accountIBANNumber) {
		this.accountIBANNumber = accountIBANNumber;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getBankBranchCity() {
		return bankBranchCity;
	}
	public void setBankBranchCity(String bankBranchCity) {
		this.bankBranchCity = bankBranchCity;
	}
	public String getBankBranchArea() {
		return bankBranchArea;
	}
	public void setBankBranchArea(String bankBranchArea) {
		this.bankBranchArea = bankBranchArea;
	}
	public String getBankBranchCountry() {
		return bankBranchCountry;
	}
	public void setBankBranchCountry(String bankBranchCountry) {
		this.bankBranchCountry = bankBranchCountry;
	}
}
