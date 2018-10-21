package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.MilkingDetail;
import com.imd.util.IMDException;

class MilkingDetailTest {

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
		MilkingDetail milking;
		try {
			// passing null record date throws exception
			milking = new MilkingDetail((short)3,true,null,null,0.0f);
		} catch (IMDException ex) {
			assertTrue(ex.getMessage().contains("Record Date can't be null"));
		}
		try {
			// proper construction
			milking = new MilkingDetail((short)3,true,LocalDate.of(2018,2,14),LocalTime.of(5,0,0),7.0f);
			milking.setLastParturationDate(LocalDate.parse("2018-02-03"));
			// Validate days since last parturation calculation.
			int years = milking.getDaysSinceLastParturation().getYears();
			int months = milking.getDaysSinceLastParturation().getMonths();
			int days = milking.getDaysSinceLastParturation().getDays();
			assertTrue(years ==0, "Years should have been 0 instead of " + years);		
			assertTrue(months ==0, "Months should have been 0 instead of " + months);		
			assertTrue(days == 11, "Days should have been 11 instead of " + days);			
		} catch (IMDException ex) {
			fail(ex.getMessage());
		}

	}

}
