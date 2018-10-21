package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.LifeCycleEvent;
import com.imd.util.IMDException;
import com.imd.util.Util;

class LifecycleEventTest {

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
	void testEventCreation() {
		LifeCycleEvent event;
		try {
			event = new LifeCycleEvent("1,De-worming,Usual treatment is to use Oxafandole or Thunder de-wormer,1,2016-12-01T00:00:00,Kashif Manzoor,Levamisole salt was used");
			assertEquals("2016-12-01T00:00",event.getEventTimeStamp().toString());
			assertEquals("Kashif Manzoor",event.getEventOperator());
			assertEquals("De-worming",event.getEventShortDescription());
			
			// test comma escape scenario as well.
			event = new LifeCycleEvent("1,De-worming,Usual treatment is to use Oxafandol\\,e or Thunder de-wormer,1,2016-12-01T00:00:00,Kashif Manzoor,Levamisole salt was used");
			// test comma escape scenario as well.
			event = new LifeCycleEvent("1,He\\,at,Make\\, sure\\, we measure standing heat time also,2,2016-12-01T00:00:00,,Levamisole\\, salt was used");
			assertEquals("2016-12-01T00:00",event.getEventTimeStamp().toString());
			assertEquals("",event.getEventOperator());
			assertEquals("He,at",event.getEventShortDescription());
			assertEquals("Make, sure, we measure standing heat time also", event.getEventLongDescription());
			assertEquals("Levamisole, salt was used", event.getEventNote());
			//Verify proper functioning if the last field is empty or with escape sequence.
			event = new LifeCycleEvent("1,He\\,at,Make\\, sure\\, we measure standing heat time also,2,2016-12-01T00:00:00,,");
			assertEquals("", event.getEventNote());
			event = new LifeCycleEvent("1,He\\,at,Make\\, sure\\, we measure standing heat time also,2,2016-12-01T00:00:00,,\\,");
			assertEquals(",", event.getEventNote());
			event = new LifeCycleEvent("1,He\\,at,Make\\, sure\\, we measure standing heat time also,2,2016-12-01T00:00:00,,\\, ");
			assertEquals(", ", event.getEventNote());
			
		} catch (IMDException e) {
			e.printStackTrace();
			fail("Following exception occured while executing the test" + e.getMessage());
		}
	}

}
