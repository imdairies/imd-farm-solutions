package com.imd.dto;

import org.joda.time.DateTime;

public class User extends IMDairiesDTO{
	private String userId;
	private boolean isActive;
	private String password;
	private Person accountHolder;
	private char gender;
	private DateTime dateOfBirth;
	public User(String userID) {
		this.userId = userID;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Person getAccountHolder() {
		return accountHolder;
	}
	public void setAccountHolder(Person accountHolder) {
		this.accountHolder = accountHolder;
	}
	public DateTime getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(DateTime dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public char getGender() {
		return gender;
	}
	public void setGender(char gender) {
		this.gender = gender;
	}
	

}
