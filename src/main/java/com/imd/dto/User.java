package com.imd.dto;

import org.joda.time.DateTime;

public class User extends IMDairiesDTO {
	private String userId;
	private boolean isActive;
	private String password;
	private Person accountHolder;
	private char gender;
	private DateTime dateOfBirth;
	private String personId;
	private String preferredLanguage;
	private String preferredCurrency;
	private String preference1;
	private String preference2;
	private String preference3;
	private String preference4;
	private String preference5;
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
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}
	public String getPreferredCurrency() {
		return preferredCurrency;
	}
	public void setPreferredCurrency(String preferredCurrency) {
		this.preferredCurrency = preferredCurrency;
	}
	
	public String toString() {
		return 
			"userId : " + userId + "\n" +
			"password : " + password + "\n" +
			"accountHolder : " + accountHolder + "\n" +
			"gender : " + gender + "\n" +
			"personId : " + personId + "\n" +
			"preferredLanguage : " + preferredLanguage + "\n" +
			"preferredCurrency : " + preferredCurrency + "\n" +
			"preference1 : " + preference1 + "\n" +
			"preference2 : " + preference2 + "\n" +
			"preference3 : " + preference3 + "\n" +
			"preference4 : " + preference4 + "\n" +
			"preference5 : " + preference5;
	}
	public String getPreference5() {
		return preference5;
	}
	public void setPreference5(String preferrence5) {
		this.preference5 = preferrence5;
	}
	public String getPreference4() {
		return preference4;
	}
	public void setPreference4(String preferrence4) {
		this.preference4 = preferrence4;
	}
	public String getPreference3() {
		return preference3;
	}
	public void setPreference3(String preferrence3) {
		this.preference3 = preferrence3;
	}
	public String getPreference2() {
		return preference2;
	}
	public void setPreference2(String preferrence2) {
		this.preference2 = preferrence2;
	}
	public String getPreference1() {
		return preference1;
	}
	public void setPreference1(String preferrence1) {
		this.preference1 = preferrence1;
	}
	

}
