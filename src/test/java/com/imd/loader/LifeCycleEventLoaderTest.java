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

import com.imd.dto.LifecycleEvent;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

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
	void testDatabaseConnection() {
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
	void testDatabaseProcessing() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		LifecycleEvent event;
		try {			
			event = new LifecycleEvent("IMD", 0, "017","PREGTEST");
			event.setEventTimeStamp(DateTime.now());
			event.setEventNote("Positive, الحمدُ للہ");
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now());
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			int transactionID = loader.insertLifeCycleEvent(event);
			event.setEventTransactionID(transactionID);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			// 2: Search for the newly inserted event and verify it is retrieved properly
			event = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			assertEquals("PREGTEST",event.getEventType().getEventCode(),"Retrieved Record should have the correct Event Code");
			// 3: Retrieve All events and ensure one of them is the one that we inserted above.
			List<LifecycleEvent> events = loader.retrieveAllLifeCycleEventsForAnimal("IMD","017");
			Iterator<LifecycleEvent> it = events.iterator(); 
			boolean found = false;
			while (it.hasNext()) {
				LifecycleEvent evt = it.next();
				IMDLogger.log(evt.dtoToJson(" ", DateTimeFormat.forPattern("d MMM yyyy h:mm a")), Util.INFO);				
				if (evt.getEventTransactionID() == event.getEventTransactionID()) {
					found = true;
					break;
				}
			}
			assertTrue(found,"Could not find the recently inserted record");

			// 4: Update the newly inserted event and verify the update
			event.getEventType().setEventCode("HEAT");
			DateTime updatedDTTM = DateTime.now().plusDays(3);
			event.setUpdatedDTTM(updatedDTTM);
			event.setAuxField1Value("UNIQUE 1HO10660\"'%\n\n");
			event.setAuxField2Value("CONVENTIONAL");
			int updatedRecCount = loader.updateLifeCycleEvent(event);
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);

			
			LifecycleEvent evt = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			IMDLogger.log(event.toString(), Util.INFO);
			assertEquals(1, updatedRecCount, " Only one record should have been updated");
			assertEquals("HEAT",evt.getEventType().getEventCode(),"Event Code should have been updated");
			assertEquals("UNIQUE 1HO10660\"'%\n\n",evt.getAuxField1Value(),"Aux Field1 Value should have been updated");
			assertEquals("CONVENTIONAL",evt.getAuxField2Value(),"Aux Field2 Value should have been updated");
			assertEquals(updatedDTTMStr,evt.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
					
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLifeCycleEvent("IMD",event.getEventTransactionID()),"One record should have been deleted");

			// 6: Search for the deleted event and verify it is not retrieved.
			event = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			assertEquals(event,null, "Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}

}
