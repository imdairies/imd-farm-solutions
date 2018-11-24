package com.imd.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class LifecycleEventDTOTest {

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
	void testDTOTOJSON() {
		try { 
			LifecycleEvent event = new LifecycleEvent("IMD", (int)0, "021", "VACCINE");
			IMDLogger.log(event.dtoToJson("  "),Util.INFO);
			assertTrue(event.dtoToJson("  ").indexOf("\"orgID\":\"IMD\"")>=0,"Org ID should be IMD");
			assertTrue(event.dtoToJson("  ").indexOf("\"eventCode\":\"VACCINE\"")>=0,"Event Cd should be VACCINE");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}
}
