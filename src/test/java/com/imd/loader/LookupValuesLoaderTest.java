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
import com.imd.dto.LookupValuesPhoto;
import com.imd.dto.User;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
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
			loader.deleteLookupValue("BREED","DUMMY");
			// 0: Search for the test record, if it exists then delete it so that we can start afresh.
//			List<LookupValues> luValues = loader.retrieveLookupValues(new LookupValuesBean("BREED","DUMMY"));
//			if (luValues != null && luValues.size() > 0) {
//				loader.deleteLookupValue("BREED","DUMMY");
//				IMDLogger.log("BREED-DUMMY record already exists, have deleted it now", Util.ERROR);
//			}
			// 1: Now insert the test record without having to worry about whether it already exists or not.
			luValue = new LookupValues("BREED","DUMMY", "Holstien-Sahiwal-Cross", "Holestien Cross Breed with Sahiwal \" ' %  - notice the special character in the description to test SQL escape functionality.","999999",null); 			
			luValue.markActive();
			luValue.setCreatedBy(new User("KASHIF"));
			luValue.setCreatedDTTM(DateTime.now());
			luValue.setUpdatedBy(luValue.getCreatedBy());
			luValue.setUpdatedDTTM(luValue.getCreatedDTTM());
			luValue.markActive();
			assertEquals(1,loader.insertLookupValues(luValue),"One record should have been successfully inserted");
			IMDLogger.log("BREED-DUMMY record has been successfully inserted", Util.INFO);

			// 2a: the event was marked active and we are searching for an inactive one. Should not return anything.
			LookupValuesBean searchBean = new LookupValuesBean("BREED","DUMMY");
			searchBean.setActiveIndicator("N");
			assertTrue(loader.retrieveLookupValues(searchBean).isEmpty());

			// should find the value as we are not specifying any active_indicator value, so all active and inactive should be retrieved.
			searchBean = new LookupValuesBean("BREED","DUMMY");
			searchBean.setActiveIndicator(null);
			luValue = loader.retrieveLookupValues(searchBean).get(0);
			assertEquals(null,luValue.getLongDescriptionMessageCd());
			assertEquals("999999",luValue.getShortDescriptionMessageCd());
			assertEquals("BREED-DUMMY",luValue.getCategoryCode()+"-"+luValue.getLookupValueCode(),"Retrieved Record should have the correct Lookup Value");
			IMDLogger.log("BREED-DUMMY record has been successfully retrieved through retrieveLookupValues ", Util.INFO);
			
			// should retrieve the event as its active and we are searching for active events.
			searchBean = new LookupValuesBean("BREED","DUMMY");
			searchBean.setActiveIndicator("Y");
			luValue = loader.retrieveLookupValues(searchBean).get(0);
			assertEquals("BREED-DUMMY",luValue.getCategoryCode()+"-"+luValue.getLookupValueCode(),"Retrieved Record should have the correct Lookup Value");
			IMDLogger.log("BREED-DUMMY record has been successfully retrieved through retrieveLookupValues ", Util.INFO);

			// 2b: Search for the newly inserted event and verify it is retrieved properly
			List<LookupValues> records = null;
			searchBean.setLookupValueCode("%" + luValue.getLookupValueCode().substring(2) + "%");
			searchBean.setCategoryCode("%" + luValue.getCategoryCode().substring(2) + "%");
			records = loader.retrieveMatchingLookupValues(searchBean);
//			int size = records.size();
			assertEquals("DUMMY",records.get(0).getLookupValueCode(),"Retrieved Record should have the correct Lookup Value Code");
			assertEquals("BREED",records.get(0).getCategoryCode(),"Retrieved Record should have the correct Lookup Value Category");
			

			// 4: Update the newly inserted event and verify the update
			luValue.setCategoryCode("BREED");
			luValue.setLookupValueCode("DUMMY");
			luValue.setShortDescription("This is Test Short Description");
			luValue.setShortDescriptionMessageCd("999988");
			luValue.setLongDescriptionMessageCd("999989");
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
			searchBean.setActiveIndicator(null);
			LookupValues val = loader.retrieveLookupValues(searchBean).get(0);
			IMDLogger.log(val.toString(), Util.INFO);
			assertFalse(val.isActive(), "The event should have been marked inactive");
			assertEquals("This is Test Short Description",val.getShortDescription(),"The short description should have been updated");
			assertEquals(updatedDTTMStr,val.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
			assertEquals("999988",luValue.getShortDescriptionMessageCd());
			assertEquals("999989",luValue.getLongDescriptionMessageCd());
			IMDLogger.log(luValue.getCategoryCode() + "-" + luValue.getLookupValueCode() + " record has been successfully updated and all new values have been verified", Util.INFO);

		
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLookupValue("BREED","DUMMY"),"One record should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LookupValue CRUD Operation Failure.");
		}
	}

	@Test
	void testPhotoCRUDOperations() {
		LookupValuesLoader loader = new LookupValuesLoader();
		User user = new User("Kashif");
		LookupValues luValue = new LookupValues("TESTCTGRY", "VALUECD", "Testing", "Testing", "-9999", "-9999");
		try {
			luValue.setCreatedBy(user);
			luValue.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			luValue.setUpdatedBy(luValue.getCreatedBy());
			luValue.setUpdatedDTTM(luValue.getCreatedDTTM());
			assertTrue(loader.deleteLookupValuesPhotos(luValue.getCategoryCode(), luValue.getLookupValueCode()) >= 0);
			assertTrue(loader.deleteLookupValue(luValue.getCategoryCode(), luValue.getLookupValueCode())>=0);
			assertEquals(1,loader.insertLookupValues(luValue));
			
			LookupValuesPhoto photo1 = new LookupValuesPhoto("TESTID1","test1.jpg","test comments 1", luValue.getCreatedDTTM());
			LookupValuesPhoto photo2 = new LookupValuesPhoto("TESTID2","test2.jpg",null, null);
			photo1.setCreatedBy(luValue.getCreatedBy());
			photo1.setUpdatedBy(photo1.getCreatedBy());
			photo1.setCreatedDTTM(luValue.getCreatedDTTM());
			photo1.setUpdatedDTTM(photo1.getCreatedDTTM());

			photo2.setCreatedBy(luValue.getCreatedBy());
			photo2.setUpdatedBy(photo2.getCreatedBy());
			photo2.setCreatedDTTM(luValue.getCreatedDTTM());
			photo2.setUpdatedDTTM(photo2.getCreatedDTTM());

			
			luValue.addPhoto(photo1);
			
			assertTrue(loader.deleteLookupValuesPhotos(luValue.getCategoryCode(), luValue.getLookupValueCode(), photo1.getPhotoID()) >= 0);
			assertEquals(1,loader.insertLookupValuesPhotos(luValue));
			luValue.emptyLookupValuesPhotoList();
			luValue.addPhoto(photo2);
			assertTrue(loader.deleteLookupValuesPhotos(luValue.getCategoryCode(), luValue.getLookupValueCode(), photo2.getPhotoID()) >= 0);
			assertEquals(1,loader.insertLookupValuesPhotos(luValue));
			
			LookupValues result = loader.retrieveLookupValuesPhotos("", "",null);
			assertTrue(result == null);

			result = loader.retrieveLookupValuesPhotos(luValue.getCategoryCode(), luValue.getLookupValueCode(),null);
			assertTrue(result != null);
			assertEquals(luValue.getCategoryCode(),result.getCategoryCode());
			assertEquals(luValue.getLookupValueCode(),result.getLookupValueCode());
			assertEquals(2,result.getLookupValuesPhotoList().size());

			int index = 0;
			assertEquals(photo1.getPhotoID(),result.getLookupValuesPhotoList().get(index).getPhotoID());
			assertEquals(photo1.getPhotoURI(),result.getLookupValuesPhotoList().get(index).getPhotoURI());
			assertEquals(Util.getDateInSQLFormat(photo1.getPhotoTimeStamp()),
					Util.getDateInSQLFormat(result.getLookupValuesPhotoList().get(index).getPhotoTimeStamp()));
			assertEquals(photo1.getComments(),result.getLookupValuesPhotoList().get(index).getComments());
			
			index++;
			assertEquals(photo2.getPhotoID(),result.getLookupValuesPhotoList().get(index).getPhotoID());
			assertEquals(photo2.getPhotoURI(),result.getLookupValuesPhotoList().get(index).getPhotoURI());
			assertEquals(photo2.getPhotoTimeStamp(),result.getLookupValuesPhotoList().get(index).getPhotoTimeStamp());
			assertEquals(photo2.getComments(),result.getLookupValuesPhotoList().get(index).getComments());

			assertEquals(2,loader.deleteLookupValuesPhotos(luValue.getCategoryCode(), luValue.getLookupValueCode()));
			assertEquals(1,loader.deleteLookupValue(luValue.getCategoryCode(), luValue.getLookupValueCode()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LookupValuePhotos CRUD Operation Failure.");
		}
	}

}







