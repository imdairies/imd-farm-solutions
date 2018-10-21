package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.util.IMDException;

class AnimalTest {

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
	void testCreation() {
		/**
		 * Create tag number 002 record
		 */
		Animal c026 = null;
		
		try {
			c026 = new Dam("","026",LocalDate.parse("2014-02-09"),false,0,"Rs.");
		} catch (IMDException e) {

			assertTrue(e.getMessage().contains("One or more values contain null."), "Null values check failed");
		}
		try {
			c026 = new Dam(null);
		} catch (IMDException e) {

			assertTrue(e.getMessage().contains("Tag Number can't be null or empty"), "Null values check failed");
		}		
		
		try {
			c026 = new Dam("026","026",LocalDate.parse("2014-02-09"),false,0,"Rs.");
			c026.setPurchaseFrom(new Contact("Babar",null, null));
			Sire sire = new Sire("", "NLDM000291306935", LocalDate.parse("2000-02-10"), false, 0, "Rs.");
			sire.setAlias("MANDERS MARIUS");
			sire.setMarketedByCompany(new Contact("Not known"));
			Contact company = new Contact("CRV");
			company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
			sire.setOwnerCompany(company);
			sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal information of the sire of 026 from Babar Hameed Jathol",LocalDateTime.now()));
			c026.setAnimalSire(sire);
			c026.setAnimalDam(null);
			
			Period age = Period.between(LocalDate.parse("2014-02-09"),LocalDate.now());
			assertTrue(c026.getCurrentAge().equals(age), " Age mismatched was expecting " + age + " got " + c026.getCurrentAge().toString() + " instead");
			assertTrue(c026.getAnimalSire().getTagID().equals("NLDM000291306935"),"Sire Tag does not match");
		} catch (IMDException e) {
			e.printStackTrace();
			fail("Contact could not be created.");
		}
		c026.setPurchaseDate(LocalDate.parse("2017-02-08"));
		assertEquals(2, c026.getAgeAtPurchase().getYears(), " Years do not match");	
		assertEquals(11, c026.getAgeAtPurchase().getMonths(), " Month do not match");
		assertEquals(30, c026.getAgeAtPurchase().getDays(), " Days do not match");
	}

}
