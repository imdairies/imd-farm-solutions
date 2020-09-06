package com.imd.dto;


public class Person extends IMDairiesDTO {
	private String personID;
	private String firstName;
	private String lastName;
	private String middleName;
	private String postFix;
	private String prefix;
	private String nic;
	public Person(String userid, String fname, String mname, String lname) {
		this.personID = userid;
		this.firstName = fname;
		this.middleName = mname;
		this.lastName = lname;
	}
	public String getPersonID() {
		return personID;
	}
	public void setPersonID(String userID) {
		this.personID = userID;
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
	public String getDisplayName() {
		return (firstName == null ? "" : firstName) + (lastName == null ? "" : lastName);
	}

}
