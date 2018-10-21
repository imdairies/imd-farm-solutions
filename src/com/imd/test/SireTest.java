package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Contact;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.util.IMDException;

class SireTest {

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
	void testSireCreation() {
		Sire sire = null;
		try {
			sire = new Sire(null, null, null, false, 0, null);
		} catch (IMDException e) {
			// TODO Auto-generated catch block
			assertTrue(e.getMessage().contains("One or more values contain null."), "Null values check failed");
		}
		
		try {
			sire = new Sire("", "NLDM000291306935", LocalDate.parse("2000-02-10"), false, 0, "Rs.");
			sire.setAlias("MANDERS MARIUS");
			sire.setMarketedByCompany(new Contact("Not known"));
			Contact company = new Contact("CRV");
			company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
			sire.setOwnerCompany(company);
			sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal information of the sire of 026 from Babar Hameed Jathol",LocalDateTime.now()));
		} catch (IMDException e) {
			fail(e.getMessage());
		}

	}

}
