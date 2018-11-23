package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class LVLifeCycleEventLoaderTest {

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
	void testDBConnection() {
		try {
			Connection conn = DBManager.getDBConnection();
			assertFalse(conn.isClosed(),"DB Connection should have been open");
			conn.close();
		} catch (Exception ex) {
			fail("Exception occurred !");
			ex.printStackTrace();
		}
	}
	@Test
	void testDBProcessing() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LVLifeCycleEventLoader loader = new LVLifeCycleEventLoader();
		LifeCycleEventCode event;
		try {
			event = new LifeCycleEventCode(null, "Test Heat", "Indicates when the cow is in heat");
			fail("The eventCode was null therefore an exception should have been thrown");
		} catch (IMDException e) {
			; 
		}
		try {
			event = new LifeCycleEventCode("TSTHEAT", "Test Heat", "Indicates when the cow is in heat"); 
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now());
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.markActive();
			assertEquals(1,loader.insertLifeCycleEvent(event),"One record should have been successfully inserted");
			// 2: Search for the newly inserted event and verify it is retrieved properly
			event = loader.retrieveLifeCycleEvent(event.getEventCode());
			assertEquals("TSTHEAT",event.getEventCode(),"Retrieved Record should have the correct Event Code");
			// 3: Retrieve All events and ensure one of them is the one that we inserted above.
			List<LifeCycleEventCode> events = loader.retrieveAllActiveLifeCycleEvents();
			Iterator<LifeCycleEventCode> it = events.iterator();
			boolean found = false;
			while (it.hasNext()) {
				LifeCycleEventCode evt = it.next();
				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
					found = true;
					break;
				}
			}
			assertTrue(found,"Could not find the recently inserted record");

			// 4: Update the newly inserted event and verify the update
			event.setEventShortDescription("This is Test Short Description");
			DateTime updatedDTTM = DateTime.now().plusDays(3);
			event.setUpdatedDTTM(updatedDTTM);
			event.markInActive();
			int updatedRecCount = loader.updateLifeCycleEvent(event);
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);

			
			LifeCycleEventCode evt = loader.retrieveLifeCycleEvent("TSTHEAT");
			IMDLogger.log(event.toString(), Util.INFO);
			assertEquals(1, updatedRecCount, " Only one record should have been updated");
			assertFalse(evt.isActive(), "The event should have been marked inactive");
			assertEquals("This is Test Short Description",evt.getEventShortDescription(),"The short description should have been updated");
			assertEquals(updatedDTTMStr,evt.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
					
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLifeCycleEvent(event.getEventCode()),"One record should have been deleted");

			// 6: Search for the deleted event and verify it is not retrieved.
			event = loader.retrieveLifeCycleEvent(event.getEventCode());
			assertEquals(event,null, "Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}

}