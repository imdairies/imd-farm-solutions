package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class LookupValuesLoaderTest {

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
	void testCRUDOperations() {
		// 1: Insert a new record.
		LookupValuesLoader loader = new LookupValuesLoader();
		LookupValues luValue;
		try {
			// 0: Search for the test record, if it exists then delete it so that we can start afresh.
			List<LookupValues> luValues = loader.retrieveLookupValues(new LookupValuesBean("BREED","DUMMY"));
			if (luValues != null && luValues.size() > 0) {
				loader.deleteLookupValue("BREED","DUMMY");
				IMDLogger.log("BREED-DUMMY record already exists, have deleted it now", Util.ERROR);
			}
			// 1: Now insert the test record without having to worry about whether it already exists or not.
			luValue = new LookupValues("BREED","DUMMY", "Holstien-Sahiwal-Cross", "Holestien Cross Breed with Sahiwal \" ' %  - notice the special character in the description to test SQL escape functionality."); 			
			luValue.setCreatedBy(new User("KASHIF"));
			luValue.setCreatedDTTM(DateTime.now());
			luValue.setUpdatedBy(luValue.getCreatedBy());
			luValue.setUpdatedDTTM(luValue.getCreatedDTTM());
			luValue.markActive();
			assertEquals(1,loader.insertLookupValues(luValue),"One record should have been successfully inserted");
			IMDLogger.log("BREED-DUMMY record has been successfully inserted", Util.INFO);

			// 2a: Search for the newly inserted event and verify it is retrieved properly\
			LookupValuesBean searchBean = new LookupValuesBean("BREED","DUMMY");
			luValue = loader.retrieveLookupValues(searchBean).get(0);
			assertEquals("BREED-DUMMY",luValue.getCategoryCode()+"-"+luValue.getLookupValueCode(),"Retrieved Record should have the correct Lookup Value");
			IMDLogger.log("BREED-DUMMY record has been successfully retrieved through retrieveLookupValues ", Util.INFO);

			// 2b: Search for the newly inserted event and verify it is retrieved properly
			List<LookupValues> records = null;
			searchBean.setLookupValueCode("%" + luValue.getLookupValueCode().substring(2) + "%");
			searchBean.setCategoryCode("%" + luValue.getCategoryCode().substring(2) + "%");
			records = loader.retrieveMatchingLookupValues(searchBean);
			int size = records.size();
			assertEquals("DUMMY",records.get(0).getLookupValueCode(),"Retrieved Record should have the correct Lookup Value Code");
			assertEquals("BREED",records.get(0).getCategoryCode(),"Retrieved Record should have the correct Lookup Value Category");
			
//			bean.setEventCode(null);
//			bean.setEventShortDescription("%1Test Hea%");
//			records = loader.retrieveMatchingLifeCycleEvents(bean);
//			size = (size < records.size() ? records.size() : size);
//			assertTrue(records != null && records.size() >0," The test event should have been retrieved by retrieveMatchingLifeCycleEvents method");
//			event = records.get(0);
//			assertEquals("TSTHEAT",event.getEventCode(),"Retrieved Record should have the correct Event Code [" + bean.getEventCode() + "]");
//
//			bean.setEventCode("haha");
//			bean.setEventShortDescription("%1Test Hea%");
//			records = loader.retrieveMatchingLifeCycleEvents(bean);
//			assertTrue(records.size() ==0," No record should have been retrieved");
//			
//			
//			bean.setEventCode(null);
//			bean.setEventShortDescription("");
//			records = loader.retrieveMatchingLifeCycleEvents(bean);
//			event = records.get(0);			
//			assertTrue(size <= records.size(), " Search with no where clause should NOT have brought less records than the search with a where clause");						
//			// 3: Retrieve All events and ensure one of them is the one that we inserted above.
//			events = loader.retrieveAllActiveLifeCycleEvents();
//			Iterator<LifeCycleEventCode> it = events.iterator();
//			boolean found = false;
//			while (it.hasNext()) {
//				LifeCycleEventCode evt = it.next();
//				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
//					found = true;
//					break;
//				}
//			}
//			assertTrue(found,"Could not find the recently inserted record");
//			IMDLogger.log("TSTHEAT record has been successfully retrieved through retrieveAllActiveLifeCycleEvents", Util.INFO);

			// 4: Update the newly inserted event and verify the update
			luValue.setCategoryCode("BREED");
			luValue.setLookupValueCode("DUMMY");
			luValue.setShortDescription("This is Test Short Description");
			DateTime updatedDTTM = DateTime.now().plusDays(3);
			luValue.setUpdatedDTTM(updatedDTTM);
			luValue.markInActive();
			int updatedRecCount = loader.updateLookupValues(luValue);
			assertEquals(1, updatedRecCount, "Exactly one record should have been updated");			
			IMDLogger.log(luValue.getCategoryCode() + "-" + luValue.getLookupValueCode() + " record has been successfully updated", Util.INFO);

			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);
			searchBean.setCategoryCode(luValue.getCategoryCode());
			searchBean.setLookupValueCode(luValue.getLookupValueCode());
			LookupValues val = loader.retrieveLookupValues(searchBean).get(0);
			IMDLogger.log(val.toString(), Util.INFO);
			assertFalse(val.isActive(), "The event should have been marked inactive");
			assertEquals("This is Test Short Description",val.getShortDescription(),"The short description should have been updated");
			assertEquals(updatedDTTMStr,val.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
			IMDLogger.log(luValue.getCategoryCode() + "-" + luValue.getLookupValueCode() + " record has been successfully updated and all new values have been verified", Util.INFO);

			// Retrieve All active events and ensure the above event does NOT appear as we had marked it inactive.
//			events = loader.retrieveAllActiveLifeCycleEvents();
//			it = events.iterator();
//			found = false;
//			while (it.hasNext()) {
//				evt = it.next();
//				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
//					found = true;
//					break;
//				}
//			}
//			assertFalse(found,"The inactive event should NOT have been picked up in the list of all active events.");	

			// Retrieve All events and ensure the above event DOES appear as we had marked it inactive.
//			events = loader.retrieveAllLifeCycleEvents();
//			it = events.iterator();
//			found = false;
//			while (it.hasNext()) {
//				evt = it.next();
//				if (evt.getEventCode().equalsIgnoreCase(event.getEventCode())) {
//					found = true;
//					break;
//				}
//			}
//			assertTrue(found,"The inactive event should have been picked up in the list of all events.");	
//			
			
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLookupValue("BREED","DUMMY"),"One record should have been deleted");

			// 6: Search for the deleted event and verify it is not retrieved.
//			events = loader.retrieveLifeCycleEvent(event.getEventCode());
//			assertTrue(events == null || events.size()==0,"Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}
}
