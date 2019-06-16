package com.imd.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
			User user = new User("KASHIF");
			LifecycleEvent event = new LifecycleEvent("IMD", (int)0, "021", "VACCINE",user,DateTime.now(),user,DateTime.now());
			IMDLogger.log(event.dtoToJson("  "),Util.INFO);
			assertTrue(event.dtoToJson("  ").indexOf("\"orgID\":\"IMD\"")>=0,"Org ID should be IMD");
			assertTrue(event.dtoToJson("  ").indexOf("\"eventCode\":\"VACCINE\"")>=0,"Event Cd should be VACCINE");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}
}
