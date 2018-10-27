package com.imd.dto;

public class Person {
	private String userID;
	private String firstName;
	private String lastName;
	private String middleName;
	private String postFix;
	private String prefix;
	private String nic;
	public Person(String userid, String fname, String mname, String lname) {
		this.userID = userid;
		this.firstName = fname;
		this.middleName = mname;
		this.lastName = lname;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getPostFix() {
		return postFix;
	}
	public void setPostFix(String postFix) {
		this.postFix = postFix;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getNic() {
		return nic;
	}
	public void setNic(String nic) {
		this.nic = nic;
	}

}
