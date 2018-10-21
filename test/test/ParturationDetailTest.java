package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Note;
import com.imd.dto.ParturationDetail;

class ParturationDetailTest {

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
		ParturationDetail parDet = new ParturationDetail();
		parDet.setHeatDateTime(LocalDateTime.parse("2016-04-05T00:00:00")); 
		parDet.setInseminationDateTime(LocalDateTime.parse("2016-04-05T00:00:00"));
		parDet.setInseminationCount(2);
		parDet.setStandingHeatDateTime(LocalDateTime.parse("2016-04-05T00:00:00"));
		parDet.setParturationDateTime(LocalDateTime.parse("2017-01-075T00:00:00"));
		parDet.setDoubleDose(false);
		parDet.setCalf(null);
		parDet.setSemenSireDetail(null);
		parDet.setParturationComments(new Note(0,"This parturation happened at the seller's farm so we are not aware of particular details since the calf was not sold with this cow",LocalDateTime.now()));
		fail("Test not yet completed.");

	}

}
