package com.imd.dto;

import java.net.URI;

import com.imd.util.IMDException;

public class Contact {
	private String firstName;
	private String middleName;
	private String lastName;
	private String namePrefix;
	private String nameSuffix;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String addressCity;
	private String addressStateOrProvince;
	private String addressCountryCode;
	private String addressInstructions;
	private String addressMapPin;
	private String addressZipCode;
	private URI webURI;
	
	public Contact(String fName, String mName, String lName) throws IMDException {
		setFirstName(fName);
		setMiddleName(mName);
		setLastName(lName);
	}

	public Contact(String fName) throws IMDException {
		setFirstName(fName);
	}

	/**
	 * Returns the string concatenated in the following order: Prefix firstName middleName lastName Suffix
	 * It does not concatenate null values if any of these name parts are null those will be ignored.
	 * @return
	 */
	public String getFormalName() {
		String formalName = getDisplayName();
		if (namePrefix != null)
			formalName = namePrefix + " " + formalName;
		if (nameSuffix != null)
			formalName += ", " + nameSuffix;

		return formalName.trim();
	}
	
	/**
	 * Returns the string concatenated in the following order: firstName middleName lastName
	 * It does not concatenate null values if any of these name parts are null those will be ignored.
	 * @return
	 */
	public String getDisplayName() {
		String displayName = "";
		displayName += firstName + " ";
		if (middleName != null)
			displayName += middleName + " ";
		if (lastName != null)
			displayName += lastName + " ";
			
		return displayName.trim();
	}
	
	public String getFirstName() {
		return firstName;
	}
	/**
	 * First name is mandatory, the rest can be null if desired.
	 * @param fName
	 * @param mName
	 * @param lName
	 */
	public void setName(String fName, String mName, String lName, String prefix, String suffix) throws IMDException{
		if (fName == null)
			throw new IMDException("First Name can not be null");
		else {
			this.firstName = fName;
			this.namePrefix = prefix;
			this.nameSuffix = suffix;
			this.middleName = mName;
			this.lastName = lName;
		}
	}

	public void setFirstName(String fName) throws IMDException{
		if (fName == null || fName.isEmpty())
			throw new IMDException ("First Name can not be null or empty");
		else
			this.firstName = fName;
	}
	public void setMiddleName(String mName) {
		this.middleName = mName;
	}
	public void setLastName(String lName) {
		this.lastName = lName;
	}
	public void setNameSuffix(String suffix) {
		this.nameSuffix = suffix;
	}
	public void setNamePrefix(String prefix) {
		this.namePrefix = prefix;
	}
	public String getMiddleName() {
		return middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getNamePrefix() {
		return namePrefix;
	}
	public String getNameSuffix() {
		return nameSuffix;
	}
	public String getAddressMapPin() {
		return addressMapPin;
	}
	public void setAddressMapPin(String mapPin) {
		this.addressMapPin = mapPin;
	}
	
	public void setAddress(String fLine, String sLine, String tLine, String city, String stateOrProv, String countryCode, String zipCode) {
		this.addressLine1 = fLine;
		this.addressLine2 = sLine;
		this.addressLine3 = tLine;
		this.addressCity = city;
		this.addressStateOrProvince = stateOrProv;
		this.addressCountryCode = countryCode;
		this.addressZipCode = zipCode;
	}
	
	/**
	 * returns the full postal address that includes the formal name with the complete address. The address is properly formatted with line breaks
	 * @return
	 */
	public String getFullPostalAddress() {
		return (getFormalName() +  System.lineSeparator() + getFullAddress());
	}

	/**
	 * returns the address that concatenates : Address Line 1, 2, 3, City, State, Country, Zip code, with line breaks between each.
	 * If no address field exists then it returns an empty string.
	 * @return
	 */
	public String getFullAddress() {
		String postalAddress = "";
		if (addressLine1 != null && !addressLine1.isEmpty())
			postalAddress += System.lineSeparator() + this.addressLine1;
		if (addressLine2 != null  && !addressLine2.isEmpty())
			postalAddress += System.lineSeparator() + this.addressLine2;
		if (addressLine3 != null  && !addressLine3.isEmpty())
			postalAddress += System.lineSeparator() + this.addressLine3;
		if (addressCity != null  && !addressCity.isEmpty())
			postalAddress += System.lineSeparator() + this.addressCity;
		if (addressStateOrProvince != null && !addressStateOrProvince.isEmpty())
			postalAddress += System.lineSeparator() + this.addressStateOrProvince;
		if (addressCountryCode != null && !addressCountryCode.isEmpty())
			postalAddress += System.lineSeparator() + this.addressCountryCode;
		if (addressZipCode != null && !addressZipCode.isEmpty())
			postalAddress += System.lineSeparator() + this.addressZipCode;		
		return postalAddress.trim();
		
	}

	public String getAddressInstructions() {
		return addressInstructions;
	}

	public void setAddressInstructions(String addressInstructions) {
		this.addressInstructions = addressInstructions;
	}

	public URI getWebURI() {
		return webURI;
	}

	public void setWebURI(URI webURI) {
		this.webURI = webURI;
	}

}
