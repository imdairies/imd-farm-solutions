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

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
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


	public Animal createTestAnimal(String animalTag) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
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
	void testRelatedEventUpdates() {

		// add insemination event, then a pregnancy test event that showed negative result ==> the insemination successful field should be automatically set to NO
		// then another insemination event followed by a successful pregtest ==> the insemination successful field should be automatically set to YES
		
		
		String orgID = "IMD";
		String animalTag = "-999";
		String sire = "1HO10219";
		String isSexed = "NO";
		String isInseminationSuccessful = "TBD";
		String cullingReason = "SADQA";
		DateTime soldTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(10);
		DateTime inseminationTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(6);
		DateTime pregTestTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(4);
		DateTime cullingTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(3);
		DateTime parturitionTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(2);
		DateTime deathTS = DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(1);
		
		try {
			
			LifeCycleEventCode inseminateEventCD = new LifeCycleEventCode(Util.LifeCycleEvents.INSEMINATE,"","");
			LifeCycleEventCode matingEventCD = new LifeCycleEventCode(Util.LifeCycleEvents.MATING,"","");
			LifeCycleEventCode pregTestCD = new LifeCycleEventCode(Util.LifeCycleEvents.PREGTEST,"","");
			LifeCycleEventCode heatCD = new LifeCycleEventCode(Util.LifeCycleEvents.HEAT,"","");
			LifeCycleEventCode culledCD = new LifeCycleEventCode(Util.LifeCycleEvents.CULLED,"","");
			LifeCycleEventCode deathCD = new LifeCycleEventCode(Util.LifeCycleEvents.DEATH,"","");
			LifeCycleEventCode soldCD = new LifeCycleEventCode(Util.LifeCycleEvents.SOLD,"","");
			LifeCycleEventCode parturitionCD = new LifeCycleEventCode(Util.LifeCycleEvents.PARTURATE,"","");
			
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			User user = new User("TEST");
			Animal animal = TestDataCreationUtil.createTestAnimal(orgID, animalTag, DateTime.now(IMDProperties.getServerTimeZone()).minusYears(5), true);
			
			LifecycleEvent cullingEvent = new LifecycleEvent(orgID,0,animalTag,culledCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			cullingEvent.setEventNote("test");
			cullingEvent.setAuxField1Value("0");
			cullingEvent.setAuxField2Value(cullingReason);
			cullingEvent.setAuxField3Value(null);
			cullingEvent.setAuxField4Value(null);
			cullingEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			cullingEvent.setEventTimeStamp(cullingTS);
			
			LifecycleEvent deathEvent = new LifecycleEvent(orgID,0,animalTag,deathCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			deathEvent.setOrgId(orgID);
			deathEvent.setAnimalTag(animalTag);
			deathEvent.setEventNote("test");
			deathEvent.setAuxField1Value(null);
			deathEvent.setAuxField2Value(null);
			deathEvent.setAuxField3Value(null);
			deathEvent.setAuxField4Value(null);
			deathEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			deathEvent.setEventTimeStamp(deathTS);
						
			LifecycleEvent soldEvent = new LifecycleEvent(orgID,0,animalTag,soldCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			soldEvent.setOrgId(orgID);
			soldEvent.setAnimalTag(animalTag);
			soldEvent.setEventNote("test");
			soldEvent.setAuxField1Value(null);
			soldEvent.setAuxField2Value(null);
			soldEvent.setAuxField3Value(null);
			soldEvent.setAuxField4Value(null);
			soldEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			soldEvent.setEventTimeStamp(soldTS);

			LifecycleEvent matingEvent = new LifecycleEvent(orgID,0,animalTag,matingEventCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			matingEvent.setAuxField1Value(sire);
			matingEvent.setAuxField2Value(isInseminationSuccessful);
			matingEvent.setAuxField3Value(null);
			matingEvent.setAuxField4Value(null);
			matingEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			matingEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(3));
			matingEvent.setCreatedBy(user);
			matingEvent.setUpdatedBy(user);
			matingEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			matingEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));			
			
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,animalTag,inseminateEventCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setAuxField1Value(sire);
			inseminationEvent.setAuxField2Value(isSexed);
			inseminationEvent.setAuxField3Value(isInseminationSuccessful);
			inseminationEvent.setAuxField4Value(null);
			inseminationEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			inseminationEvent.setEventTimeStamp(inseminationTS);
			inseminationEvent.setCreatedBy(user);
			inseminationEvent.setUpdatedBy(user);
			inseminationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			LifecycleEvent parturitionEvent = new LifecycleEvent(orgID,0,animalTag,parturitionCD.getEventCode(),user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			parturitionEvent.setOrgId(orgID);
			parturitionEvent.setAnimalTag(animalTag);
			parturitionEvent.setAuxField1Value(Util.GENDER_CHAR.FEMALE + ""); 
			parturitionEvent.setAuxField2Value(null);
			parturitionEvent.setAuxField3Value(null);
			parturitionEvent.setAuxField4Value(null);
			parturitionEvent.setEventTimeStamp(parturitionTS);
			parturitionEvent.setEventNote("test");

			LifeCycleEventBean pregTestEventBean = new LifeCycleEventBean();
			pregTestEventBean.setOrgID(orgID);
			pregTestEventBean.setAnimalTag(animalTag);
			pregTestEventBean.setAuxField1Value(Util.NO.toUpperCase()); //  Pregnant = NO
			pregTestEventBean.setAuxField2Value(Util.YES.toUpperCase()); // Update last insemination outcome = YES
			pregTestEventBean.setAuxField3Value(null);
			pregTestEventBean.setAuxField4Value(null);
			pregTestEventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(pregTestTS));
			pregTestEventBean.setEventComments("test");
			pregTestEventBean.setEventCode(pregTestCD.getEventCode());
			pregTestEventBean.setOperatorID(user.getUserId());

			LifecycleEvent pregTestEvent = new LifecycleEvent(pregTestEventBean);
			pregTestEvent.setCreatedBy(user);
			pregTestEvent.setUpdatedBy(user);
			pregTestEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			pregTestEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			LifeCycleEventBean heatEventBean = new LifeCycleEventBean();
			heatEventBean.setOrgID(orgID);
			heatEventBean.setAnimalTag(animalTag);
			heatEventBean.setAuxField1Value(Util.YES.toUpperCase()); // Update last insemination outcome = YES
			heatEventBean.setAuxField2Value(null);
			heatEventBean.setAuxField3Value(null);
			heatEventBean.setAuxField4Value(null);
			heatEventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			heatEventBean.setEventComments("test");
			heatEventBean.setEventCode(heatCD.getEventCode());
			heatEventBean.setOperatorID(user.getUserId());
	
			LifecycleEvent heatEvent = new LifecycleEvent(heatEventBean);
			heatEvent.setCreatedBy(user);
			heatEvent.setUpdatedBy(user);
			heatEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			AnimalLoader animalLoader = new AnimalLoader();

			assertTrue(loader.deleteAnimalLifecycleEvents(orgID, animalTag) >= 0);
			assertTrue(animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			
			assertEquals(1,animalLoader.insertAnimal(animal));
			int inseminationEventID = loader.insertLifeCycleEvent(inseminationEvent);
			//loader.insertLifeCycleEvent(pregTestEvent);
			assertTrue(loader.performPostEventAdditionEventsUpdates(pregTestEvent, animal, user).contains("" + inseminationEventID));
			
			LifecycleEvent updatedEvent = loader.retrieveLifeCycleEvent(orgID, inseminationEventID);
			assertEquals(pregTestEvent.getAuxField1Value(), updatedEvent.getAuxField3Value());
			
			int matingEventID = loader.insertLifeCycleEvent(matingEvent);
			pregTestEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()));
			pregTestEvent.setAuxField1Value(Util.YES.toUpperCase()); // pregnant = YES
			//loader.insertLifeCycleEvent(pregTestEvent);

			pregTestEventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone())));
			pregTestEventBean.setAuxField1Value(Util.YES.toUpperCase()); // pregnant = YES			
			assertTrue(loader.performPostEventAdditionEventsUpdates(pregTestEvent, animal, user).contains("" + matingEventID));
			
			updatedEvent = loader.retrieveLifeCycleEvent(orgID, matingEventID);
			assertEquals(null, updatedEvent.getAuxField3Value());
			assertEquals(pregTestEventBean.getAuxField1Value(), updatedEvent.getAuxField2Value());
			
			matingEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(21));
			int matingEvent2ID = loader.insertLifeCycleEvent(matingEvent);
			assertTrue(loader.performPostEventAdditionEventsUpdates(heatEvent, animal, user).contains("" + matingEvent2ID));
			
			LifecycleEvent updatedHeatInsmnEvent = loader.retrieveLifeCycleEvent(orgID, matingEvent2ID);
			assertEquals(null, updatedHeatInsmnEvent.getAuxField3Value());
			assertEquals(Util.NO.toUpperCase(), updatedHeatInsmnEvent.getAuxField2Value());

			loader.deleteAnimalLifecycleEvents(orgID, animalTag);
			
			assertTrue(animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertEquals(0,loader.performPostEventAdditionEventsUpdates(cullingEvent, animal, user).indexOf(". " + Util.ERROR_POSTFIX + "The animal's herd leaving date could NOT be set to :"));

			assertEquals(1,animalLoader.insertAnimal(animal));
			assertEquals(0,loader.performPostEventAdditionEventsUpdates(cullingEvent, animal, user).indexOf(". The animal's herd leaving date has been set to :"));

			Animal retAnimal = animalLoader.getAnimalRawInfo(animal).get(0);
			assertEquals(Util.getDateTimeInSQLFormat(retAnimal.getHerdLeavingDate()), Util.getDateTimeInSQLFormat(cullingEvent.getEventTimeStamp()));

			assertEquals(0,loader.performPostEventAdditionEventsUpdates(deathEvent, animal, user).indexOf(". The animal's herd leaving date has been set to :"));
			retAnimal = animalLoader.getAnimalRawInfo(animal).get(0);
			assertEquals(Util.getDateTimeInSQLFormat(retAnimal.getHerdLeavingDate()), Util.getDateTimeInSQLFormat(deathEvent.getEventTimeStamp()));

			assertEquals(0,loader.performPostEventAdditionEventsUpdates(soldEvent, animal, user).indexOf(". The animal's herd leaving date has been set to :"));
			retAnimal = animalLoader.getAnimalRawInfo(animal).get(0);
			assertEquals(Util.getDateTimeInSQLFormat(retAnimal.getHerdLeavingDate()), Util.getDateTimeInSQLFormat(soldEvent.getEventTimeStamp()));
			
			assertTrue(loader.insertLifeCycleEvent(parturitionEvent)>0);
			assertTrue(loader.insertLifeCycleEvent(inseminationEvent)>0);
			assertEquals(0,loader.performPostEventAdditionEventsUpdates(heatEvent, animal, user).indexOf(". We could not find any insemination or mating event for this animal since its latest parturition/abortion;"));
			inseminationEvent.setEventTimeStamp(parturitionTS.plusDays(21));
			assertTrue(loader.insertLifeCycleEvent(inseminationEvent)>0);
			assertEquals(0,loader.performPostEventAdditionEventsUpdates(heatEvent, animal, user).indexOf(". The outcome of the latest insemination/mating event"));
			
			assertEquals(3,loader.deleteAnimalLifecycleEvents(orgID, animalTag));
			assertEquals(1,animalLoader.deleteAnimal(orgID, animalTag));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}

	
	}

	@Test
	void testEventRetrievalOfParticularType() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		AnimalLoader animalLoader = new AnimalLoader();
		LifecycleEvent event;
		User user = new User("KASHIF");
		try {
			Animal animal999 = createTestAnimal("-999");
			
			animalLoader.deleteAnimal("IMD", animal999.getAnimalTag());
			eventLoader.deleteAnimalLifecycleEvents("IMD","-999");
			
			event = new LifecycleEvent(animal999.getOrgId(), 0, animal999.getAnimalTag(),Util.LifeCycleEvents.VACCINE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));			
			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10));
			event.setAuxField1Value("BQ");
			event.setAuxField2Value("BQGOVT");
			event.setAuxField3Value(null);			
			event.setAuxField4Value("YES");			
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("Testing Black Quarter Vaccination");
			int transactionID = animalLoader.insertAnimal(animal999);
			assertTrue(transactionID == 1,"Exactly one animal should have been added successfully");
			transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID>0,"Exactly one event should have been added successfully");
			
			List<LifecycleEvent> retevents = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal999.getOrgId(), animal999.getAnimalTag(),null);
			assertEquals(1,retevents.size());
			LifecycleEvent retevent = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(animal999.getOrgId(), animal999.getAnimalTag(),Util.LifeCycleEvents.VACCINE).get(0);
			assertEquals(event.getAuxField1Value(),retevent.getAuxField1Value());
			assertEquals(event.getAuxField2Value(),retevent.getAuxField2Value());
			assertEquals(event.getAuxField3Value(),retevent.getAuxField3Value());
			assertEquals(event.getAuxField4Value(),retevent.getAuxField4Value());
			assertEquals(event.getEventType().getEventCode(),retevent.getEventType().getEventCode());
			assertEquals("CATEGORY_CD",retevent.getEventType().getField1DataType());
			assertEquals("DISEASE",retevent.getEventType().getField1DataUnit());
			assertEquals("CATEGORY_CD",retevent.getEventType().getField2DataType());
			assertEquals("VACCINE",retevent.getEventType().getField2DataUnit());
			assertEquals(null,retevent.getEventType().getField3DataType());
			assertEquals(null,retevent.getEventType().getField3DataUnit());
			assertEquals(null,retevent.getEventType().getField4DataType());
			assertEquals(null,retevent.getEventType().getField4DataUnit());
			
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(animal999.getOrgId(), animal999.getAnimalTag()),"One record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal(animal999.getOrgId(), animal999.getAnimalTag()),"One record should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}	
	
	@Test
	void testLookupValuesAndTransactionValuesJson() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		AnimalLoader animalLoader = new AnimalLoader();
		LifecycleEvent event;
		User user = new User("KASHIF");
		try {
			Animal animal999 = createTestAnimal("-999");
			
			animalLoader.deleteAnimal("IMD", animal999.getAnimalTag());
			eventLoader.deleteAnimalLifecycleEvents("IMD","-999");
			event = new LifecycleEvent(animal999.getOrgId(), 0, animal999.getAnimalTag(),Util.LifeCycleEvents.VACCINE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));			
			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10));
			event.setAuxField1Value("BQ");
			event.setAuxField2Value("BQGOVT");
			event.setAuxField3Value(null);			
			event.setAuxField4Value("NO");			
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("Testing Black Quarter Vaccination");
			int transactionID = animalLoader.insertAnimal(animal999);
			assertTrue(transactionID == 1,"Exactly one animal should have been added successfully");
			transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID>0,"Exactly one event should have been added successfully");
			
			LifecycleEvent retevent = eventLoader.retrieveLifeCycleEvent(animal999.getOrgId(), transactionID);
			assertEquals(event.getAuxField1Value(),retevent.getAuxField1Value());
			assertEquals(event.getAuxField2Value(),retevent.getAuxField2Value());
			assertEquals(event.getAuxField3Value(),retevent.getAuxField3Value());
			assertEquals(event.getAuxField4Value(),retevent.getAuxField4Value());
			assertEquals("CATEGORY_CD",retevent.getEventType().getField1DataType());
			assertEquals("DISEASE",retevent.getEventType().getField1DataUnit());
			assertEquals("CATEGORY_CD",retevent.getEventType().getField2DataType());
			assertEquals("VACCINE",retevent.getEventType().getField2DataUnit());
			assertEquals(null,retevent.getEventType().getField3DataType());
			assertEquals(null,retevent.getEventType().getField3DataUnit());
			assertEquals(null,retevent.getEventType().getField4DataType());
			assertEquals(null,retevent.getEventType().getField4DataUnit());
			
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(animal999.getOrgId(), animal999.getAnimalTag()),"One record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal(animal999.getOrgId(), animal999.getAnimalTag()),"One record should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}	
	
	
	@Test
	void testInseminationAttemptCountInCurrentLactation() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		LifecycleEvent event;
		User user = new User("KASHIF");
		String orgId = "IMD";
		String tag999 = "-999";
		try {			
			AnimalLoader anmlLoader = new AnimalLoader();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, tag999, DateTime.now(IMDProperties.getServerTimeZone()).minusYears(5), true);
			
			assertTrue(loader.deleteAnimalLifecycleEvents(animal.getOrgId(),animal.getAnimalTag()) >=0);
			assertTrue(anmlLoader.deleteAnimal(animal) >= 0);

			event = new LifecycleEvent(animal.getOrgId(),0, animal.getAnimalTag(),Util.LifeCycleEvents.PARTURATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(180));
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF")); 
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("...");

			
			assertEquals(1,anmlLoader.insertAnimal(animal));
			int transactionID = loader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");

			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(120));
			event.setEventType(new LifeCycleEventCode(Util.LifeCycleEvents.INSEMINATE, "", ""));
			transactionID = loader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");

			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(99));
			event.setEventType(new LifeCycleEventCode(Util.LifeCycleEvents.MATING, "", ""));
			transactionID = loader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");

			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(78));
			event.setEventType(new LifeCycleEventCode(Util.LifeCycleEvents.INSEMINATE, "", ""));
			transactionID = loader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");
			
			assertEquals(3,loader.determineInseminationAttemptCountInCurrentLactation("IMD", "-999"), "Two insemination attempts and one mating attempts should have been found.");
			//Delete the newly inserted events so that we don't have any test data in our DB.
			assertEquals(4,loader.deleteAnimalLifecycleEvents(animal.getOrgId(),animal.getAnimalTag()));
			assertEquals(1,anmlLoader.deleteAnimal(animal));

//			assertEquals(4,loader.deleteAnimalLifecycleEvents("IMD","-999"),"Four records should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
		
	}
	
	@Test
	void testBreedingWidgetQueries() {
		String orgID = "IMD";
		DateTime startDate = new DateTime(2019,9,1,0,0,0,Util.FARM_TIMEZONE);
		DateTime endDate = startDate.plusMonths(1).minusDays(1);
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		AnimalLoader animalLoader = new AnimalLoader();
		LifecycleEvent event;
		User user = new User("KASHIF");
		Person person = new Person("EMP000'", "Kashif", "", "Manzoor");
		try {
			Animal animal999 = createTestAnimal("-999");
			
			animalLoader.deleteAnimal("IMD", animal999.getAnimalTag());
			eventLoader.deleteAnimalLifecycleEvents("IMD","-999");

			event = new LifecycleEvent(animal999.getOrgId(), 0, animal999.getAnimalTag(),Util.LifeCycleEvents.BIRTH,user,
					startDate.plusDays(3),user,DateTime.now(IMDProperties.getServerTimeZone()));			
			event.setEventTimeStamp(endDate.minusDays(10));
			event.setAuxField1Value(Util.GENDER_CHAR.FEMALE + "");
			event.setAuxField2Value(Util.YES);
			event.setAuxField2Value(null);
			event.setAuxField3Value(null);			
			event.setAuxField4Value(null);
			event.setEventOperator(person);
			event.setEventNote("");
			int recordCount = animalLoader.insertAnimal(animal999);
			assertTrue(recordCount == 1,"Exactly one animal should have been added successfully");
			int transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");
			event.setEventType(new LifeCycleEventCode(Util.LifeCycleEvents.ABORTION,"",""));
			event.setEventTimeStamp(startDate);
			transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");
			event.setEventTimeStamp(startDate.minusDays(1));
			transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0,"Event should have been added successfully");
			List<LifecycleEvent> resultEvents = eventLoader.getBirthsInSpecificDateRange(orgID, startDate, endDate);
			assertTrue(resultEvents.size()>=1, "The birth events should have been retrieved");
			
			List<LifecycleEvent> calved = eventLoader.getBirthsInSpecificDateRange(orgID,startDate, endDate);
			Iterator<LifecycleEvent> it = calved.iterator();
			boolean found = false;
			while (it.hasNext()) {
				LifecycleEvent event1 = it.next();
				if (event1.getAnimalTag().equals(animal999.getAnimalTag())) {
					found = true;
					assertEquals(Util.LifeCycleEvents.BIRTH,event1.getEventType().getEventCode());
				}
			}
			assertTrue(found);
			assertEquals(Util.LifeCycleEvents.BIRTH,resultEvents.get(0).getEventType().getEventCode());
			assertEquals(3,eventLoader.deleteAnimalLifecycleEvents(animal999.getOrgId(), animal999.getAnimalTag()),"Three records should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal(animal999.getOrgId(), animal999.getAnimalTag()),"One record should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}		
	}
	
	@Test
	void testDatabaseProcessing() {
		// 1: Insert a new Lifecycle event and verify the correct insertion.
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		LifecycleEvent event;
		User user = new User("KASHIF");
		String tag999 = "-999";
		String orgId = "IMD";
		try {
			AnimalLoader anmlLoader = new AnimalLoader();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, tag999, DateTime.now(IMDProperties.getServerTimeZone()).minusYears(5), true);
			
			assertTrue(loader.deleteAnimalLifecycleEvents("IMD",animal.getAnimalTag()) >=0);
			assertTrue(anmlLoader.deleteAnimal(animal) >= 0);
			
			event = new LifecycleEvent(animal.getOrgId(), 0, animal.getAnimalTag(),"PREGTEST",user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("Part of the 10-5 cow exchange. Approximately sold for Rs. 150K. "
					+ "Was bought for Rs. 200K. The cow was pregnant with a female calf through natural mating. "
					+ "Had conception issues - was a repeater. "
					+ "This cow had dried off after approximately 6 months of first lactation and had been "
					+ "dried for several months by the time we sold it");
			
			
			assertEquals(1,anmlLoader.insertAnimal(animal));

			int transactionID = loader.insertLifeCycleEvent(event);
			assertEquals(Util.ERROR_CODE.DATA_LENGTH_ISSUE,transactionID, "Length of comments field should have been too long");
			event.setEventNote("Positive, الحمدُ للہ");
			transactionID = loader.insertLifeCycleEvent(event);
			assertTrue(transactionID > 0);

			event.setEventTransactionID(transactionID);
			//assertTrue(transactionID > 0,"Record should have been successfully inserted");
			// 2: Search for the newly inserted event and verify it is retrieved properly
			event = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			assertEquals("PREGTEST",event.getEventType().getEventCode(),"Retrieved Record should have the correct Event Code");
			// 3: Retrieve All events and ensure one of them is the one that we inserted above.
			List<LifecycleEvent> events = loader.retrieveAllLifeCycleEventsForAnimal("IMD","-999");
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
			DateTime updatedDTTM = DateTime.now(IMDProperties.getServerTimeZone()).plusDays(3);
			event.setUpdatedDTTM(updatedDTTM);
			event.setAuxField1Value("UNIQUE 1HO10660\"'%\n\n");
			event.setAuxField2Value("CONVENTIONAL");
			event.setAuxField3Value("3VALUE");
			event.setAuxField4Value(null);
			int updatedRecCount = loader.updateLifeCycleEvent(event);
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);

			LifecycleEvent evt = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			IMDLogger.log(event.toString(), Util.INFO);
			assertEquals(1, updatedRecCount, " Only one record should have been updated");
			assertEquals("HEAT",evt.getEventType().getEventCode(),"Event Code should have been updated");
			assertEquals("UNIQUE 1HO10660\"'%\n\n",evt.getAuxField1Value(),"Aux Field1 Value should have been updated");
			assertEquals("CONVENTIONAL",evt.getAuxField2Value(),"Aux Field2 Value should have been updated");
			assertEquals("3VALUE",evt.getAuxField3Value(),"Aux Field3 Value should have been updated");
			assertEquals(null,evt.getAuxField4Value(),"Aux Field4 Value should have been null");
			assertEquals(updatedDTTMStr,evt.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
					
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLifeCycleEvent(animal.getOrgId(),event.getEventTransactionID()),"One record should have been deleted");
			assertEquals(1,anmlLoader.deleteAnimal(animal));

			// 6: Search for the deleted event and verify it is not retrieved.
			event = loader.retrieveLifeCycleEvent(animal.getOrgId(),event.getEventTransactionID());
			assertEquals(event,null, "Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}

	@Test
	void testSireInseminationRecord() {
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		LifecycleEvent event;
		String tag999 = "-999";
		User user = new User("KASHIF");
		String orgId = "IMD";
		try {
			AnimalLoader anmlLoader = new AnimalLoader();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, tag999, DateTime.now(IMDProperties.getServerTimeZone()).minusYears(5), true);
			
			assertTrue(loader.deleteAnimalLifecycleEvents(animal.getOrgId(),animal.getAnimalTag()) >=0);
			assertTrue(anmlLoader.deleteAnimal(animal) >= 0);

			String testSire = "TSTSIRE";
			event = new LifecycleEvent(animal.getOrgId(), 0, animal.getAnimalTag(),Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			event.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("To be deleted later. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long."
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long. "
					+ "Length should be too long. Length should be too long.  ");
			event.setAuxField1Value(testSire);
			
			assertEquals(1,anmlLoader.insertAnimal(animal));
			
			int transactionID = loader.insertLifeCycleEvent(event);
			assertEquals(Util.ERROR_CODE.DATA_LENGTH_ISSUE,transactionID, "Length of comments field should have been too long");
			event.setEventNote("Positive, الحمدُ للہ");
			transactionID = loader.insertLifeCycleEvent(event);			
			event.setEventTransactionID(transactionID);
			//assertTrue(transactionID > 0,"Record should have been successfully inserted");
			// 2: Search for the newly inserted event and verify it is retrieved properly
			event = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			assertEquals(Util.LifeCycleEvents.INSEMINATE,event.getEventType().getEventCode(),"Retrieved Record should have the correct Event Code");
			// 3: Retrieve All events and ensure one of them is the one that we inserted above.
			List<LifecycleEvent> events = loader.retrieveAllLifeCycleEventsForAnimal("IMD","-999");
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
			DateTime updatedDTTM = DateTime.now(IMDProperties.getServerTimeZone()).plusDays(3);
			event.setUpdatedDTTM(updatedDTTM);
			event.setAuxField1Value("UNIQUE 1HO10660\"'%\n\n");
			event.setAuxField2Value("CONVENTIONAL");
			event.setAuxField3Value("3VALUE");
			event.setAuxField4Value(null);
			int updatedRecCount = loader.updateLifeCycleEvent(event);
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			String updatedDTTMStr = fmt.print(updatedDTTM);

			LifecycleEvent evt = loader.retrieveLifeCycleEvent("IMD",event.getEventTransactionID());
			IMDLogger.log(event.toString(), Util.INFO);
			assertEquals(1, updatedRecCount, " Only one record should have been updated");
			assertEquals("HEAT",evt.getEventType().getEventCode(),"Event Code should have been updated");
			assertEquals("UNIQUE 1HO10660\"'%\n\n",evt.getAuxField1Value(),"Aux Field1 Value should have been updated");
			assertEquals("CONVENTIONAL",evt.getAuxField2Value(),"Aux Field2 Value should have been updated");
			assertEquals("3VALUE",evt.getAuxField3Value(),"Aux Field3 Value should have been updated");
			assertEquals(null,evt.getAuxField4Value(),"Aux Field4 Value should have been null");
			assertEquals(updatedDTTMStr,evt.getUpdatedDTTMSQLFormat(),"The Updated DTTM should have been updated");
					
			
			// 5: Delete the newly inserted event so that we don't have any test data in our DB.
			assertEquals(1,loader.deleteLifeCycleEvent(animal.getOrgId(),event.getEventTransactionID()),"One record should have been deleted");
			assertEquals(1,anmlLoader.deleteAnimal(animal));

			// 6: Search for the deleted event and verify it is not retrieved.
			event = loader.retrieveLifeCycleEvent(animal.getOrgId(),event.getEventTransactionID());
			assertEquals(event,null, "Deleted record should not have been found");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("LifeCycleEvent Creation and/or insertion Failed.");
		}
	}	
}





