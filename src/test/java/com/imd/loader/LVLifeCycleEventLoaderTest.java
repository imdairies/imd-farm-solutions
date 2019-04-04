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
import com.imd.dto.User;
import com.imd.services.bean.LifeCycleEventCodeBean;
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
			event = new LifeCycleEventCode(null, "01Test Heat", "Indicates when the cow is in heat");
			fail("The eventCode was null therefore an exception should have been thrown");
		} catch (IMDException e) {
			; 
		}
		try {
			loader.deleteLifeCycleEvent("TSTHEAT");
			// 1: Now insert the test record without having to worry about whether it already exists or not.
			event = new LifeCycleEventCode("TSTHEAT", "01Test Heat", "Indicates when the cow is in heat. \nCow's heat % \"'\" testing - notice the special character in the description to test SQL escape functionality."); 			
			event.setField1Label("Standing Heat Time");
			event.setField1DataType(Util.DataTypes.DATETIME);
			event.setField1DataUnit("hh:mm:ss");
			
			event.setField2Label("Comments");
			event.setField2DataType(Util.DataTypes.TEXT);
			event.setField2DataUnit("haha");

			event.setField3Label("Test3");
			event.setField3DataType(Util.DataTypes.YESNO);
			event.setField3DataUnit(""); 
			
			event.setField4Label("Test4");
			event.setField4DataType(Util.DataTypes.LV_SIRE);
			event.setField4DataUnit("");
			
			event.setNextLifecycleStage(Util.AnimalTypes.LCTINSEMIN + "," + Util.AnimalTypes.LCTPRGNT);
			
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now());
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.markActive();
			assertEquals(1,loader.insertLifeCycleEvent(event),"One record should have been successfully inserted");
			IMDLogger.log("TSTHEAT record has been successfully inserted", Util.INFO);

			// 2a: Search for the newly inserted event and verify it is retrieved properly
			event = loader.retrieveLifeCycleEvent(event.getEventCode()).get(0);
			assertEquals("TSTHEAT",event.getEventCode(),"Retrieved Record should have the correct Event Code");
			
			assertEquals("Standing Heat Time",event.getField1Label());
			assertEquals(Util.DataTypes.DATETIME,event.getField1DataType());
			assertEquals("hh:mm:ss",event.getField1DataUnit());
			
			assertEquals("Comments",event.getField2Label());
			assertEquals(Util.DataTypes.TEXT,event.getField2DataType());
			assertEquals("haha",event.getField2DataUnit());

			assertEquals("Test3",event.getField3Label());
			assertEquals(Util.DataTypes.YESNO,event.getField3DataType());
			assertEquals("",event.getField3DataUnit());

			assertEquals("Test4",event.getField4Label());
			assertEquals(Util.DataTypes.LV_SIRE,event.getField4DataType());
			assertEquals("",event.getField4DataUnit());

			assertEquals(Util.AnimalTypes.LCTINSEMIN + "," + Util.AnimalTypes.LCTPRGNT,event.getNextLifecycleStage());
			assertEquals(2,event.getNextLifecycleStageList().length);

			
			// 2b: Search for the newly inserted event and verify it is retrieved properly
			List<LifeCycleEventCode> records = null;
			LifeCycleEventCodeBean bean = new LifeCycleEventCodeBean();
			bean.setEventCode("%" + event.getEventCode().substring(2) + "%");
			bean.setEventShortDescription(null);
			records = loader.retrieveMatchingLifeCycleEvents(bean);
			int size = records.size();
			event = records.get(0);
			assertEquals("TSTHEAT",event.getEventCode(),"Retrieved Record should have the correct Event Code");
			
			bean.setEventCode(null);
			bean.setEventShortDescription("%1Test Hea%");
			records = loader.retrieveMatchingLifeCycleEvents(bean);
			size = (size < records.size() ? records.size() : size);
			assertTrue(records != null && records.size() >0," The test event should have been retrieved by retrieveMatchingLifeCycleEvents method");
			event = records.get(0);
			assertEquals("TSTHEAT",event.getEventCode(),"Retrieved Record should have the correct Event Code [" + bean.getEventCode() + "]");

			assertEquals("Standing Heat Time",event.getField1Label());
			assertEquals(Util.DataTypes.DATETIME,event.getField1DataType());
			assertEquals("hh:mm:ss",event.getField1DataUnit());

			assertEquals("Comments",event.getField2Label());
			assertEquals(Util.DataTypes.TEXT,event.getField2DataType());
			assertEquals("haha",event.getField2DataUnit());

			bean.setEventCode("haha");
			bean.setEventShortDescription("%1Test Hea%");
			records = loader.retrieveMatchingLifeCycleEvents(bean);
			assertTrue(records.size() ==0," No record should have been retrieved");
			
			
			bean.setEventCode(null);
			bean.setEventShortDescription("");
			records = loader.retrieveMatchingLifeCycleEvents(bean);
			event = records.get(0);			
			assertTrue(size <= records.size(), " Search with no where clause should NOT have brought less records than the search with a where clause");						
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
			IMDLogger.log("TSTHEAT record has been successfully retrieved through retrieveAllActiveLifeCycleEvents", Util.INFO);

			// 4: Update the newly inserted event and verify the update
			event.setEventShortDescription("This is Test Short Description");
			DateTime updatedDTTM = DateTime.now().plusDays(3);
			event.setUpdatedDTTM(updatedDTTM);
			event.markInActive();
			int updatedRecCount = loader.updateLifeCycleEvent(event);
			assertEquals(1, updatedRecCount, "Exactly one record should have been updated");			
			IMDLogger.log("TSTHEAT record has been successfully updated", Util.INFO);

			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);			
			LifeCycleEventCode evt = loader.retrieveLifeCycleEvent("TSTHEAT").get(0);
			IMDLogger.log(event.toString(), Util.INFO);
			assertFalse(evt.isActive(), "The event should have been marked inactive");
			assertEquals("This is Test Short Description",evt.getEventShortDescription(),"The short description should have been updated");
			assertEquals(updatedDTTMStr,evt.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
			IMDLogger.log("TSTHEAT record has been successfully updated and all new values have been verified", Util.INFO);

			// Retrieve All active events and ensure the above event does NOT appear as we had marked it inactive.
			events = loader.retrieveAllActiveLifeCycleEvents();
			it = events.iterator();
			found = false;
			while (it.hasNext()) {
				evt = it.next();
				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
					found = true;
					break;
				}
			}
			assertFalse(found,"The inactive event should NOT have been picked up in the list of all active events.");	

			// Retrieve All events and ensure the above event DOES appear as we had marked it inactive.
			events = loader.retrieveAllLifeCycleEvents();
			it = events.iterator();
			found = false;
			while (it.hasNext()) {
				evt = it.next();
				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
					found = true;
					break;
				}
			}
			assertTrue(found,"The inactive event should have been picked up in the list of all events.");	
			
			
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLifeCycleEvent(event.getEventCode()),"One record should have been deleted");

			// 6: Search for the deleted event and verify it is not retrieved.
			events = loader.retrieveLifeCycleEvent(event.getEventCode());
			assertTrue(events == null || events.size()==0,"Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}

}
