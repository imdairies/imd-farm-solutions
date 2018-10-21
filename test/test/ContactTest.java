package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.BankDetails;
import com.imd.dto.Contact;
import com.imd.util.IMDException;

class ContactTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testContactCreation() {
		Contact contact;
		try {
			contact = new Contact(null, "", "");
		} catch (IMDException ex) {
			assertTrue(ex.getMessage().contains("First Name can not be null or empty"));
		}
		try {
			contact = new Contact("", "", "");
		} catch (IMDException ex) {
			assertTrue(ex.getMessage().contains("First Name can not be null or empty"));
		}
		
		try {
			// Name related tests
			contact = new Contact("Babar", "Hameed", "Jathol");
			assertTrue(contact.getFormalName().equals("Babar Hameed Jathol"), "Got [" + contact.getFormalName() + "]");
			contact.setNamePrefix("Mr.");
			assertTrue(contact.getFormalName().equals("Mr. Babar Hameed Jathol"));
			contact.setNameSuffix("PhD");
			assertTrue(contact.getFormalName().equals("Mr. Babar Hameed Jathol, PhD"), "Got [" + contact.getFormalName() + "]");
			assertTrue(contact.getDisplayName().equals("Babar Hameed Jathol"), "Got [" + contact.getDisplayName() + "]");
			
			// Address related tests
			String addresstoCompare = "Mr. Babar Hameed Jathol, PhD" + System.lineSeparator();
			assertTrue(contact.getFullPostalAddress().equals(addresstoCompare), "Got [" + contact.getFullPostalAddress() + "] was expecting[ " + addresstoCompare + "]");
			contact.setAddress("Jathol Farm", "25 Km from Jaran Wala","", "Joray Khoo", "Punjab", "Pakistan", null);
			contact.setAddressInstructions("Take motorway from Lahore to Islamabad and take the first exit and continue straight on that road for 25 km. Then take left.");
			addresstoCompare += "Jathol Farm" + System.lineSeparator() + "25 Km from Jaran Wala" + System.lineSeparator() + "Joray Khoo" + System.lineSeparator() + 
					"Punjab" + System.lineSeparator() + "Pakistan";
			assertTrue(contact.getFullPostalAddress().equals(addresstoCompare), "Got [" + contact.getFullPostalAddress() + "] was expecting[ " + addresstoCompare + "]");
			
			// Test for bank details
			BankDetails bankDetails;
			try {
				 bankDetails = new BankDetails("JATHOL AGRI & DAIRY", "Habib Bank Limited", "", null);
			} catch (IMDException ex) {
				assertTrue(ex.getMessage().contains("Bank Name, Account Name can not be null and either the account number or IBAN should be specified"));	
			}
			bankDetails = new BankDetails("JATHOL AGRI & DAIRY", "Habib Bank Limited", "09977900302603", "HABB0009977900302603");
			contact.setBankAccountInformation(bankDetails);
		} catch (IMDException ex) {
			fail(ex.getMessage());
		}		
		
	}

}
