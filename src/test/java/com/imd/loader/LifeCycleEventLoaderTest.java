package com.imd.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.LifeCycleEvent;
import com.imd.dto.Person;
import com.imd.util.DBManager;
import com.imd.util.IMDException;

class LifeCycleEventLoaderTest {

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
			assertFalse("DB Connection should have been open",conn.isClosed());
			conn.close();
		} catch (Exception ex) {
			fail("Exception occurred !");
			ex.printStackTrace();
		}
	}
	@Test
	void testDBProcessing() {
		// 1: Insert a new Lifecycle event
		LifeCycleEventLoader loader = new LifeCycleEventLoader();
		LifeCycleEvent event;
		try {
			event = new LifeCycleEvent("IMD",null, "Heat", "Indicates when the cow is in heat");
			fail("The eventCode was null therefore an exception should have been thrown");
		} catch (IMDException e) {
			;
		}
		try {
			event = new LifeCycleEvent("IMD", "HEAT", "Heat", "Indicates when the cow is in heat");
			event.setCreatedBy(new Person("KASHIF","Kashif","","Manzoor"));
			event.setCreatedDTTM(DateTime.now());
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.markActive();
			assertEquals("One record should have been successfully inserted",1,loader.insertLifeCycleEvent(event));
			//DBManager.getDBConnection().commit();
			// 2: Search for the newly inserted event
			event = loader.retrieveLifeCycleEvent(event.getOrgCode(), event.getEventCode());
			assertEquals("Retrieved Record should have the correct Org ID","IMD",event.getOrgCode());
			assertEquals("Retrieved Record should have the correct Event Code","HEAT",event.getEventCode());
			// 3: Retrieve All events
			List<LifeCycleEvent> events = loader.retrieveAllActiveLifeCycleEvents();
			Iterator<LifeCycleEvent> it = events.iterator();
			boolean found = false;
			while (it.hasNext()) {
				LifeCycleEvent evt = it.next();
				if (evt.getOrgCode().equalsIgnoreCase(event.getOrgCode()) && evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
					found = true;
					break;
				}
			}
			assertTrue("Could not find the recently inserted record",found);
			
			// 9: Delete the newly inserted event
			assertEquals("One record should have been deleted",1,loader.deleteLifeCycleEvent(event.getOrgCode(), event.getEventCode()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
		
		
		// 4: Update the newly inserted event
		// 5: Inactivate the newly inserted event.
		// 6: retrieve all the active events.
		// 7: Activate the newly inserted event.
		// 8: Search for the newly activated event.
		// 10: Search for the deleted event.
		
		//fail("Not yet implemented");
	}

}
