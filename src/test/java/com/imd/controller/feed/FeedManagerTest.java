package com.imd.controller.feed;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.Dam;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedItemNutritionalStats;
import com.imd.dto.FeedPlan;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.LookupValues;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class FeedManagerTest {

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

	private MilkingDetail createMilkingRecord(String tagNbr, LocalDate milkingDate, LocalTime milkingTime) throws IMDException {
		short milkFreq = 3;
		float milkingVol = 13.0f;
		boolean isMachineMilked = true;	
		MilkingDetail milkingRecord = new MilkingDetail("IMD", tagNbr, milkFreq, isMachineMilked, milkingDate, milkingTime, milkingVol,(short)1);
		milkingRecord.setHumidity(50.0f);
		milkingRecord.setTemperatureInCentigrade(19.3f);
		milkingRecord.setLrValue(28.0f);
		milkingRecord.setFatValue(3.80f);
		milkingRecord.setToxinValue(0.11f);
		milkingRecord.setCreatedBy(new User("KASHIF"));
		milkingRecord.setCreatedDTTM(DateTime.now());
		milkingRecord.setUpdatedBy(new User("KASHIF"));
		milkingRecord.setUpdatedDTTM(DateTime.now());
		return milkingRecord;
	}
	
	@Test
	void testBullAndMaleCalf() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		try {
			Sire maleCalf = new Sire("IMD","TEST-MALECALF");
			maleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(100));
			maleCalf.setAnimalTypeCD(Util.AnimalTypes.MALECALF);
			maleCalf.setBreed(Util.Breed.HFCROSS);
			maleCalf.setHerdJoiningDate(maleCalf.getDateOfBirth());
			maleCalf.setCreatedBy(new User("KASHIF"));
			maleCalf.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			maleCalf.setUpdatedBy(maleCalf.getCreatedBy());
			maleCalf.setUpdatedDTTM(maleCalf.getCreatedDTTM());
			
			Sire bull = new Sire(maleCalf.getOrgID(),"TEST-BULL");
			bull.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(300));
			bull.setAnimalTypeCD(Util.AnimalTypes.BULL);
			bull.setBreed(maleCalf.getBreed());
			bull.setHerdJoiningDate(maleCalf.getHerdJoiningDate());
			bull.setCreatedBy(new User("KASHIF"));
			bull.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			bull.setUpdatedBy(maleCalf.getCreatedBy());
			bull.setUpdatedDTTM(maleCalf.getCreatedDTTM());
			
			anmLdr.deleteAnimal(maleCalf.getOrgID(), maleCalf.getAnimalTag());
			anmLdr.deleteAnimal(bull.getOrgID(), bull.getAnimalTag());			
			
			assertEquals(1,anmLdr.insertAnimal(maleCalf));
			assertEquals(1,anmLdr.insertAnimal(bull));
			assertEquals(Util.FeedCohortType.BULL,manager.getAnimalFeedCohort(bull.getOrgID(), bull.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.MALECALF,manager.getAnimalFeedCohort(maleCalf.getOrgID(), maleCalf.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			
			assertEquals(1,anmLdr.deleteAnimal(maleCalf.getOrgID(), maleCalf.getAnimalTag()));
			assertEquals(1,anmLdr.deleteAnimal(bull.getOrgID(), bull.getAnimalTag()));

		} catch (IMDException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}	
	
	private Dam createDam(String orgID, String damTag, DateTime dob, String animalType) throws IMDException {
		Dam dam = new Dam(orgID,damTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		dam.setAlias(damTag + "alias");
		dam.setBreed(Util.Breed.HFCROSS);
		dam.setAnimalType(animalType);
		dam.setFrontSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/1.png");
		dam.setBackSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/2.png");
		dam.setRightSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/3.png");
		dam.setLeftSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/4.png");

		dam.setPurchaseDate(DateTime.parse("2017-02-08"));
		dam.setCreatedBy(new User("KASHIF"));
		dam.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		dam.setHerdJoiningDate(dob);
		dam.setHerdLeavingDate(null);
		dam.setUpdatedBy(dam.getCreatedBy());
		dam.setUpdatedDTTM(dam.getCreatedDTTM());
		dam.setAnimalDam(null);
		Note newNote = new Note (1,"test note", DateTime.now(IMDProperties.getServerTimeZone()));		
		dam.addNote(newNote);
		return dam;		
	}
	
	@Test
	void testHeifer() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		int originalMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		try {
			String nonPregHeiferTag = "-999";
			String pregHeiferTag = "-998";
			String orgID = "IMD";
			User user = new User("KASHIF");
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS+1),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS+1),Util.AnimalTypes.HFRPREGN);
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));


			eventLoader.deleteAnimalLifecycleEvents(orgID, nonPregHeiferTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, pregHeiferTag);
			anmLdr.deleteAnimal(orgID, nonPregHeiferTag);
			anmLdr.deleteAnimal(orgID, pregHeiferTag);
			
			anmLdr.insertAnimal(pregnantHeifer);
			anmLdr.insertAnimal(nonPregnantHeifer);
			eventLoader.insertLifeCycleEvent(inseminationEvent);
			assertEquals(Util.FeedCohortType.HEIFER,manager.getAnimalFeedCohort(orgID, nonPregHeiferTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.HFRCLOSEUP,manager.getAnimalFeedCohort(orgID, pregHeiferTag).getFeedCohortLookupValue().getLookupValueCode());
			

			assertEquals(0,eventLoader.deleteAnimalLifecycleEvents(orgID, nonPregHeiferTag));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, pregHeiferTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, nonPregHeiferTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, pregHeiferTag));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred");
		} finally {
			IMDLogger.loggingMode = originalMode;
		}
	}

	
	@Test
	void testVariousLactationStages() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		MilkingDetailLoader milkDetailloader = new MilkingDetailLoader();
		try {
			String freshLactationTag = "-999";
			String midLactationTag = "-998";
			String oldLactationTag = "-997";
			String orgID = "IMD";
			User user = new User("KASHIF");
			Dam freshLactation = createDam(orgID,freshLactationTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4*365),Util.AnimalTypes.LACTATING);
			Dam midLactation = createDam(orgID,midLactationTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4*365),Util.AnimalTypes.LCTINSEMIN);
			Dam oldLactation = createDam(orgID,oldLactationTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4*365),Util.AnimalTypes.LCTPRGNT);

			
			LifecycleEvent freshParturationEvent = new LifecycleEvent(orgID,0,freshLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			freshParturationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.RECENT_PARTURATION_DAYS_LIMIT));
			
			LifecycleEvent midParturationEvent = new LifecycleEvent(orgID,0,midLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			midParturationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.RECENT_PARTURATION_DAYS_LIMIT + 1));

			LifecycleEvent oldParturationEvent = new LifecycleEvent(orgID,0,oldLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			oldParturationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.DRYOFF_BY_DAYS));

			
			LocalDate today = LocalDate.now(IMDProperties.getServerTimeZone());
			
			assertTrue(milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, midLactationTag)>=0);
			assertTrue(milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, oldLactationTag)>=0);
			assertTrue(milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, freshLactationTag)>=0);
						
			eventLoader.deleteAnimalLifecycleEvents(orgID, freshLactationTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, midLactationTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, oldLactationTag);
			anmLdr.deleteAnimal(orgID, freshLactationTag);
			anmLdr.deleteAnimal(orgID, midLactationTag);
			anmLdr.deleteAnimal(orgID, oldLactationTag);
			
			anmLdr.insertAnimal(freshLactation);
			anmLdr.insertAnimal(midLactation);
			anmLdr.insertAnimal(oldLactation);
			eventLoader.insertLifeCycleEvent(freshParturationEvent);
			eventLoader.insertLifeCycleEvent(midParturationEvent);
			eventLoader.insertLifeCycleEvent(oldParturationEvent);

			assertEquals(Util.FeedCohortType.LCTMID,manager.getAnimalFeedCohort(orgID, midLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTOLD,manager.getAnimalFeedCohort(orgID, oldLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTEARLY,manager.getAnimalFeedCohort(orgID, freshLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			
			
			MilkingDetail milkingRecord1 = createMilkingRecord(midLactationTag, today, new LocalTime(4,0,0));
			milkingRecord1.setComments("Milking Record to be deleted after the unit test");
			
			milkingRecord1.setRecordDate(today.minusDays(3));
			milkingRecord1.setAnimalTag(oldLactationTag);
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
	
			milkingRecord1.setAnimalTag(freshLactationTag);
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));			
			
			
			milkingRecord1.setAnimalTag(midLactationTag);
			milkingRecord1.setRecordDate(today);
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(1));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(2));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(3));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(4));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			assertEquals(Util.FeedCohortType.LCTMIDHIGH,manager.getAnimalFeedCohort(orgID, midLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTOLDHIGH,manager.getAnimalFeedCohort(orgID, oldLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTEARLY,manager.getAnimalFeedCohort(orgID, freshLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			
			assertEquals(28.0f,manager.getMilkAverage(orgID, midLactationTag, today,5).floatValue());
			

			assertEquals(15,milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, midLactationTag));
			
			assertEquals(3,milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, freshLactationTag));

			assertEquals(3,milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, oldLactationTag));
			
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, freshLactationTag));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, midLactationTag));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, oldLactationTag));

			assertEquals(1,anmLdr.deleteAnimal(orgID, freshLactationTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, midLactationTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, oldLactationTag));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred");
		}
	}	

	@Test
	void testVariousDryStages() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		try {
			String closeupDryPregTag = "-999";
			String faroffDryPregTag = "-998";
			String orgID = "IMD";
			User user = new User("KASHIF");
			
			Dam closeupDryPreg = createDam(orgID,closeupDryPregTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4*365),Util.AnimalTypes.DRYPREG);
			Dam faroffDryPreg = createDam(orgID,faroffDryPregTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4*365),Util.AnimalTypes.DRYPREG);

			
			LifecycleEvent closeupInseminationEvent = new LifecycleEvent(orgID,0,closeupDryPregTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			closeupInseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));
			closeupInseminationEvent.setAuxField1Value("M017"); /*Sire*/
			closeupInseminationEvent.setAuxField2Value(Util.N); /*Sexed*/
			closeupInseminationEvent.setAuxField3Value(Util.Y); /*Insemination Successful?*/
			
			LifecycleEvent farInseminationEvent = new LifecycleEvent(orgID,0,faroffDryPregTag,Util.LifeCycleEvents.MATING,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			farInseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS - 10));
			farInseminationEvent.setAuxField1Value("M014"); /*Sire*/
			farInseminationEvent.setAuxField2Value(Util.N); /*Sexed*/
			farInseminationEvent.setAuxField3Value(Util.Y); /*Insemination Successful?*/

			
			eventLoader.deleteAnimalLifecycleEvents(orgID, closeupDryPregTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, faroffDryPregTag);
			anmLdr.deleteAnimal(orgID, closeupDryPregTag);
			anmLdr.deleteAnimal(orgID, faroffDryPregTag);
			
			anmLdr.insertAnimal(closeupDryPreg);
			anmLdr.insertAnimal(faroffDryPreg);

			eventLoader.insertLifeCycleEvent(closeupInseminationEvent);
			eventLoader.insertLifeCycleEvent(farInseminationEvent);

			assertEquals(Util.FeedCohortType.NEARPRTRT,manager.getAnimalFeedCohort(orgID, closeupDryPregTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.FARPRTRT,manager.getAnimalFeedCohort(orgID, faroffDryPregTag).getFeedCohortLookupValue().getLookupValueCode());

			
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, closeupDryPregTag));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(orgID, faroffDryPregTag));

			assertEquals(1,anmLdr.deleteAnimal(orgID, closeupDryPregTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, faroffDryPregTag));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred");
		}
	}	
	
	@Test
	void testFeedCohortInformationForFarmActiveAnimals() {
		
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		String responseJson = "";
		try {
			String nonPregHeiferTag = "-999";
			String pregHeiferTag = "-998";
			String orgID = "IMD";
			User user = new User("KASHIF");
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS+1),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS+1),Util.AnimalTypes.HFRPREGN);
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));

			LifecycleEvent weightEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.WEIGHT,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			weightEvent.setAuxField1Value("400");
			weightEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()));


			eventLoader.deleteAnimalLifecycleEvents(orgID, nonPregHeiferTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, pregHeiferTag);
			anmLdr.deleteAnimal(orgID, nonPregHeiferTag);
			anmLdr.deleteAnimal(orgID, pregHeiferTag);
			
			anmLdr.insertAnimal(pregnantHeifer);
			anmLdr.insertAnimal(nonPregnantHeifer);
			eventLoader.insertLifeCycleEvent(inseminationEvent);
			eventLoader.insertLifeCycleEvent(weightEvent);
			
			List<Animal> herd = null;
			
			try {
				herd = manager.getFeedCohortInformationForFarmActiveAnimals("-+-");
			} catch (IMDException ex) {
				assertTrue(ex.getMessage().equals("The specified animals do not exist in the farm."),ex.getMessage());
			}

			assertEquals(null,herd);

			herd = manager.getFeedCohortInformationForFarmActiveAnimals(orgID);
			assertTrue(herd.size() >= 2);
			Iterator<Animal> it = herd.iterator();
			int undeterminedCount = 0;
			boolean nonPregHeiferFound = false;
			boolean pregHeiferFound = false;
			boolean nutritionalNeedsFound = false;
			while (it.hasNext()) {
				Animal anml = it.next();
				if (anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode().equals(Util.FeedCohortType.UNDETERMINED)) {
					undeterminedCount++;
					IMDLogger.log("Could not determine the feed cohort of: " + anml.getAnimalTag(), Util.INFO);
				}
				if (anml.getAnimalTag().equals(nonPregHeiferTag)) {
					assertEquals(Util.FeedCohortType.HEIFER,anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode());
					nonPregHeiferFound = true;
				} else if (anml.getAnimalTag().equals(pregHeiferTag)) {
					assertEquals(400f,anml.getWeight().floatValue());
					assertEquals(Util.FeedCohortType.HFRCLOSEUP,anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode());
					pregHeiferFound = true;
				}
				assertTrue(anml.getFeedCohortInformation().getAnimalFeedCohortDeterminatationMessage().indexOf("ERROR") <0);
				if (anml.getAnimalNutritionalNeeds() != null) {
					assertTrue(anml.getAnimalNutritionalNeeds().getDryMatter() >= 0);
					assertTrue(anml.getAnimalNutritionalNeeds().getCrudeProtein() >= 0);
					assertTrue(anml.getAnimalNutritionalNeeds().getMetabloizableEnergy() >= 0);
					assertTrue(anml.dtoToJson("  ").indexOf("nutritionalNeedsFeedCohortCD") >= 0);
					nutritionalNeedsFound = true;
				}
				if (anml.getAnimalTag().equals(pregHeiferTag)) {
					assertEquals(400f,anml.getWeight().floatValue());
				}
				responseJson += "{\n" + anml.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
			}
			assertEquals(true, nutritionalNeedsFound, " At least one animal should have nutritional needs specified in the database");
	    	if (responseJson != null && !responseJson.trim().isEmpty() )
	    		responseJson = "[" + responseJson.substring(0,responseJson.lastIndexOf(",\n")) + "]";
	    	else
	    		responseJson = "[]";
	    	IMDLogger.log(responseJson, Util.INFO);
	    	assertTrue(responseJson.indexOf("feedCohortTypeCD") >= 0);
	    	assertTrue(responseJson.indexOf("weight") >= 0);
			assertTrue(pregHeiferFound);
			assertTrue(nonPregHeiferFound);
			assertEquals(0,undeterminedCount, "Feed Cohort of " + undeterminedCount + " of the animals could not be determined");
			
			assertEquals(0,eventLoader.deleteAnimalLifecycleEvents(orgID, nonPregHeiferTag));
			assertEquals(2,eventLoader.deleteAnimalLifecycleEvents(orgID, pregHeiferTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, nonPregHeiferTag));
			assertEquals(1,anmLdr.deleteAnimal(orgID, pregHeiferTag));
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred");
		}
	}
	
	@Test
	void testFemaleCalves() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		User user = new User("KASHIF");
		try {
			Dam femaleCalf = new Dam("IMD","TEST-FEMALECALF");
			femaleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(50));
			femaleCalf.setAnimalTypeCD(Util.AnimalTypes.FEMALECALF);
			femaleCalf.setBreed(Util.Breed.HFCROSS);
			femaleCalf.setHerdJoiningDate(femaleCalf.getDateOfBirth());
			femaleCalf.setCreatedBy(new User("KASHIF"));
			femaleCalf.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			femaleCalf.setUpdatedBy(femaleCalf.getCreatedBy());
			femaleCalf.setUpdatedDTTM(femaleCalf.getCreatedDTTM());
			
			Dam femaleCalfWeanedOff = new Dam(femaleCalf.getOrgID(),"TEST-FEMALEWEANEDOFF");
			femaleCalfWeanedOff.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(100));
			femaleCalfWeanedOff.setAnimalTypeCD(Util.AnimalTypes.FEMALECALF);
			femaleCalfWeanedOff.setBreed(femaleCalf.getBreed());
			femaleCalfWeanedOff.setHerdJoiningDate(femaleCalfWeanedOff.getDateOfBirth());
			femaleCalfWeanedOff.setCreatedBy(new User("KASHIF"));
			femaleCalfWeanedOff.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			femaleCalfWeanedOff.setUpdatedBy(femaleCalf.getCreatedBy());
			femaleCalfWeanedOff.setUpdatedDTTM(femaleCalf.getCreatedDTTM());
			
			LifecycleEvent event = new LifecycleEvent(femaleCalfWeanedOff.getOrgID(), 0, femaleCalfWeanedOff.getAnimalTag(),Util.LifeCycleEvents.WEANEDOFF,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));			
			event.setEventTimeStamp(femaleCalfWeanedOff.getDateOfBirth().plusDays(60));
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(event.getCreatedBy());
			event.setUpdatedDTTM(event.getCreatedDTTM());
			event.setEventNote("Testing");
			
			eventLoader.deleteAnimalLifecycleEvents(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag());
			anmLdr.deleteAnimal(femaleCalf.getOrgID(), femaleCalf.getAnimalTag());
			anmLdr.deleteAnimal(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag());			

			int transactionID = eventLoader.insertLifeCycleEvent(event);
			assertTrue(transactionID>0,"Exactly one event should have been added successfully");

			assertEquals(1,anmLdr.insertAnimal(femaleCalf));
			assertEquals(1,anmLdr.insertAnimal(femaleCalfWeanedOff));
			assertEquals(Util.FeedCohortType.FEMALECALF,manager.getAnimalFeedCohort(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.FMLWNDOFF,manager.getAnimalFeedCohort(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			
			// Create FEMALECALF feedplan
			FeedLoader loader = new FeedLoader();
			FeedItem feedItem = new FeedItem();
			feedItem.setOrgID("IMD");
			LookupValues feedItemLV = new LookupValues(Util.LookupValues.FEED,"TST_ALFHAY", "","","","");
			feedItem.setFeedItemLookupValue(feedItemLV);		
			
			LookupValues feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.FEMALECALF, "","","","");
			feedItem.setFeedCohortCD(feedCohortLV);

			feedItem.setStart(0.0f);
			feedItem.setEnd(90.0f);
			feedItem.setMinimumFulfillment(0.5f);
			feedItem.setFulfillmentPct(1.0f);
			feedItem.setMaximumFulfillment(1.5f);
			feedItem.setUnits("Kgs");
			feedItem.setFulFillmentTypeCD(Util.FulfillmentType.FREEFLOW);
			feedItem.setDailyFrequency((Integer)null);
			feedItem.setComments("Put alfaalfa hay infront of the calves and let them eat as much as they wish");
			feedItem.setCreatedBy(new User("KASHIF"));
			feedItem.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			feedItem.setUpdatedBy(feedItem.getCreatedBy());
			feedItem.setUpdatedDTTM(feedItem.getCreatedDTTM());
			assertTrue(loader.deleteFeedPlanItem(feedItem) >= 0);
			assertEquals(1,loader.insertFeedPlanItem(feedItem));
			
			assertEquals(null,loader.retrieveFeedPlanItem(feedItem, 120f, 120f));
			
			float ageInDays = femaleCalf.getCurrentAgeInDays();
			FeedItem item = loader.retrieveFeedPlanItem(feedItem, ageInDays);

			assertTrue(item != null);

			assertEquals(feedItem.getFeedItemLookupValue().getLookupValueCode(), item.getFeedItemLookupValue().getLookupValueCode());
			assertEquals(feedItem.getFeedCohortCD().getLookupValueCode(), item.getFeedCohortCD().getLookupValueCode());
			assertEquals(feedItem.getStart(), item.getStart());
			assertEquals(feedItem.getEnd(), item.getEnd());
			assertEquals(feedItem.getMinimumFulfillment(), item.getMinimumFulfillment());
			assertEquals(feedItem.getFulfillmentPct(), item.getFulfillmentPct());
			assertEquals(feedItem.getMaximumFulfillment(), item.getMaximumFulfillment());
			assertEquals(feedItem.getDailyFrequency(), item.getDailyFrequency());
			assertEquals(feedItem.getUnits(), item.getUnits());
			assertEquals(feedItem.getComments(), item.getComments());
			assertEquals(feedItem.getCreatedBy().getUserId(), item.getCreatedBy().getUserId());
			assertEquals(feedItem.getUpdatedBy().getUserId(), item.getUpdatedBy().getUserId());
			assertEquals(feedItem.getUpdatedDTTMSQLFormat(), item.getUpdatedDTTMSQLFormat(), "Feed Item [" + feedItem.getFeedItemLookupValue().getLookupValueCode() + "] should have been: " + feedItem.getUpdatedDTTMSQLFormat() + " it actually was: " + item.getUpdatedDTTMSQLFormat());
			assertEquals(feedItem.getCreatedDTTMSQLFormat(), item.getCreatedDTTMSQLFormat());
			
//			FeedCohort feedCohort = new FeedCohort(feedItem.getOrgID(), feedItem.getFeedCohortCD(),"");
//			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, femaleCalf);
//			assertTrue(plan != null && feedItem != null);
//			assertEquals(feedItem.getFeedCohortCD().getLookupValueCode(), plan.getFeedCohort().getFeedCohortLookupValue().getLookupValueCode());
//			assertEquals(feedItem.getOrgID(), plan.getOrgID());
//			
//			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
//			while (it.hasNext()) {
//				IMDLogger.log(it.next().getPersonalizedFeedMessage(), Util.INFO);
//			}			
//			
//			
			
			// Clean up all the test records added.
			assertEquals(1,loader.deleteFeedPlanItem(feedItem, " AND START >= ? AND END >= ?", feedItem.getStart(), feedItem.getEnd()));
			assertEquals(1,anmLdr.deleteAnimal(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()));
			assertEquals(1,anmLdr.deleteAnimal(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));

		} catch (IMDException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
	
	@Test
	void testPersonalizedFeedPlanOfFemaleCalf() {
		String orgID = "IMD";
		String femaleCalfTag = "-999";
//		String bullTag = "-998";
		
		FeedManager manager = new FeedManager();
		Dam femaleCalf;
		try {
			femaleCalf = createDam(orgID,femaleCalfTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(60), Util.AnimalTypes.FEMALECALF);
			
			AnimalLoader anmLdr = new AnimalLoader();
			
			anmLdr.deleteAnimal(orgID,femaleCalfTag);
			assertEquals(1,anmLdr.insertAnimal(femaleCalf));
			
			femaleCalf.setWeight(50f);
			
			LookupValues lv = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.FEMALECALF,Util.FeedCohortType.FEMALECALF,"","","");
			
			FeedCohort feedCohort = new FeedCohort(orgID, lv,"");
			CohortNutritionalNeeds nutritionalNeeds = setCohortNutritionalNeeds(Util.FeedCohortType.FEMALECALF, femaleCalf.getWeight());
			feedCohort.setCohortNutritionalNeeds(nutritionalNeeds);
			femaleCalf.setFeedCohortInformation(feedCohort);
			
			HashMap<String,Float> minFulfillment = new HashMap<String,Float> ();
			HashMap<String,Float>  maxFulfillment = new HashMap<String,Float> ();
			
			FeedPlan cohortFeedPlan = getCohortFeedPlan(feedCohort, femaleCalf, minFulfillment, maxFulfillment);
			
			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, cohortFeedPlan, femaleCalf);
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				FeedItem item = it.next();
				IMDLogger.log(item.getPersonalizedFeedMessage(), Util.INFO);
				if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.MILK))
					assertEquals(6f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.ALFAHAY))
					assertEquals(1.5f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.WATER))
					assertEquals(5f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.VANDA))
					assertEquals(0.5f,item.getDailyIntake().floatValue());
			}
			float expectedPlanCost = (6f * 56f /*6 liters milk*/) + (1.5f * 3f /* 1.5 Kgs Alfahay*/) + 
									 (5f * 0f /*5 liters water*/) + (0.5f * 43.25f /* 0.5 Kgs Vanda*/);
			IMDLogger.log("The animal's feedplan will give it:\n" + Util.formatTwoDecimalPlaces(plan.getPlanDM()) + " Kgs. of Dry Matter. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanCP()) + " Kgs. of Crude Protein. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getCrudeProtein() * nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanME()) + " MJ of Metabolizable Energy. Required:"+ nutritionalNeeds.getMetabloizableEnergy() + " MJ.\n", Util.INFO);
			assertEquals(Util.formatToSpecifiedDecimalPlaces(32.612096f,4),Util.formatToSpecifiedDecimalPlaces(plan.getPlanME(),4));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(0.5427758f,4),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCP(),4));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(1.793f,3),Util.formatToSpecifiedDecimalPlaces(plan.getPlanDM(),3));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(expectedPlanCost,0),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCost(),0));
			
			maxFulfillment.put(Util.FeedItems.MILK, new Float(3));
			minFulfillment.put(Util.FeedItems.ALFAHAY, new Float(5));
			
			cohortFeedPlan = getCohortFeedPlan(feedCohort, femaleCalf, minFulfillment, maxFulfillment);
			
			plan = manager.getPersonalizedFeedPlan(feedCohort, cohortFeedPlan, femaleCalf);
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			expectedPlanCost = (3f * 56f /*6 liters milk*/) + (5f * 3f /* 1.5 Kgs Alfahay*/) + 
					 (5f * 0f /*5 liters water*/) + (0.5f * 43.25f /* 0.5 Kgs Vanda*/);
			
			it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				FeedItem item = it.next();
				IMDLogger.log(item.getPersonalizedFeedMessage(), Util.INFO);
				if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.MILK))
					assertEquals(3f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.ALFAHAY))
					assertEquals(5f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.WATER))
					assertEquals(5f,item.getDailyIntake().floatValue());
				else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.VANDA))
					assertEquals(0.5f,item.getDailyIntake().floatValue());
			}
			IMDLogger.log("The animal's feedplan will give it:\n" + Util.formatTwoDecimalPlaces(plan.getPlanDM()) + " Kgs. of Dry Matter. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanCP()) + " Kgs. of Crude Protein. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getCrudeProtein() * nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanME()) + " MJ of Metabolizable Energy. Required:"+ nutritionalNeeds.getMetabloizableEnergy() + " MJ.\n", Util.INFO);
			assertFalse(Util.formatTwoDecimalPlaces(30.715136d).equals(Util.formatTwoDecimalPlaces(plan.getPlanME())));
			assertFalse(Util.formatTwoDecimalPlaces(0.499775d).equals(Util.formatTwoDecimalPlaces(plan.getPlanCP())));
			assertFalse(Util.formatTwoDecimalPlaces(1.565d).equals(Util.formatTwoDecimalPlaces(plan.getPlanDM())));			
			assertEquals(Util.formatToSpecifiedDecimalPlaces(expectedPlanCost,0),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCost(),0));
			
			
			assertEquals(1,anmLdr.deleteAnimal(orgID,femaleCalfTag));
			
			// Clean up all the test records added.
	//		assertEquals(1,loader.deleteFeedPlanItem(feedItem, " AND START >= ? AND END >= ?", feedItem.getStart(), feedItem.getEnd()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
	//		assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}

	}

	@Test
	void testPersonalizedFeedPlanOfFreshHighProducer() {
		String orgID = "IMD";
		String lactatingTag = "-999";
		int sixtyMonthsOld = 60;
		
		FeedManager manager = new FeedManager();
		Dam lactatingHighProducer;
		try {
			lactatingHighProducer = createDam(orgID,lactatingTag,DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(sixtyMonthsOld), Util.AnimalTypes.LCTAWTHEAT);
			
			AnimalLoader anmLdr = new AnimalLoader();
			
			anmLdr.deleteAnimal(orgID,lactatingTag);
			assertEquals(1,anmLdr.insertAnimal(lactatingHighProducer));
			
			lactatingHighProducer.setWeight(627f);
			
			LookupValues lctEarlyHi = new LookupValues(Util.LookupValues.FEEDCOHORT, Util.FeedCohortType.LCTEARLYHI, "Freshly calved lactating high producer", "Freshly Calved high producer", "2227", "2227");
			
			FeedCohort feedCohort = new FeedCohort(orgID, lctEarlyHi,"Freshly calved lactating high producer");
			CohortNutritionalNeeds nutritionalNeeds = setCohortNutritionalNeeds(Util.FeedCohortType.LCTEARLYHI, lactatingHighProducer.getWeight());
			feedCohort.setCohortNutritionalNeeds(nutritionalNeeds);
			lactatingHighProducer.setFeedCohortInformation(feedCohort);
			
			HashMap<String,Float> minFulfillment = new HashMap<String,Float> ();
			HashMap<String,Float>  maxFulfillment = new HashMap<String,Float> ();
			
			FeedPlan cohortFeedPlan = getCohortFeedPlan(feedCohort, lactatingHighProducer, minFulfillment, maxFulfillment);
			
			MilkingDetailLoader milkDetailloader = new MilkingDetailLoader();
			LocalDate today = LocalDate.now(IMDProperties.getServerTimeZone());
			MilkingDetail milkingRecord1 = createMilkingRecord(lactatingTag, today, new LocalTime(4,0,0));
			milkingRecord1.setComments("Milking Record to be deleted after the unit test");
			
//			assertTrue(milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today) >= 0);
//			assertTrue(milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(1)) >= 0);
//			assertTrue(milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(2)) >= 0);
//			assertTrue(milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(3)) >= 0);
//			assertTrue(milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(4)) >= 0);
			
			assertTrue(milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, lactatingTag) >= 0);

			milkingRecord1.setAnimalTag(lactatingTag);
			milkingRecord1.setRecordDate(today);
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(11.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(1));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(12.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(2));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(12.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(3));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			milkingRecord1.setRecordDate(today.minusDays(4));
			milkingRecord1.setMilkingEventNumber((short) 1);
			milkingRecord1.setMilkVolume(11.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 2);
			milkingRecord1.setMilkVolume(10.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			milkingRecord1.setMilkingEventNumber((short) 3);
			milkingRecord1.setMilkVolume(9.0f);
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));
			
			lactatingHighProducer.setStatusIndicators(AnimalLoader.LACTATING_INDICATOR);
			
			
			
			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, cohortFeedPlan, lactatingHighProducer);
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			
			float cp =0;
			float me = 0;
			float dm = 0;
			
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				FeedItem item = it.next();
				IMDLogger.log(item.getPersonalizedFeedMessage(), Util.INFO);
				if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.ALFAALFA)) {
					assertEquals("48.46",Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(48.46f * 3f)),Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(48.46f * 0.2426f * 0.2283f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(48.46f * 0.2426f * 9.3f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += (48.46f * 0.2426f * 0.2283f);
					me += (48.46f * 0.2426f * 9.3f);
				} else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.CORNSILAGE)) {
					assertEquals(Util.formatTwoDecimalPlaces(new Float("18.47410714")),Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(18.47410714f * 4.75f)),Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(18.47410714f * 0.28f * 0.072f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(18.47410714f * 0.28f * 10.8f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += (18.47410714f * 0.28f * 0.072f);
					me += (18.47410714f * 0.28f * 10.8f);
				} else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.BYPASFAT84)) {
					assertEquals("0.4",Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(new Float(0.40f * 135f),new Float(Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue())));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += 0f;
					me += 0f;
				} else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.HDVANDA)) {
					assertEquals("12",Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(new Float(12f * 42.75f),new Float(Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue())));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(12f * 0.88f * 0.22f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(12f * 0.88f * 13.3888f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += (12f * 0.88f * 0.22f);
					me += (12f * 0.88f * 13.3888f);
				} else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.MEETHASODA)) {
					assertEquals("0.1", Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(new Float(0.1f * 64.0f ),new Float(Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue())));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += 0f;
					me += 0f;
				} else if (item.getFeedItemLookupValue().getLookupValueCode().equals(Util.FeedItems.MNRLBRKT)) {
					assertEquals("0.1",Util.formatTwoDecimalPlaces(item.getDailyIntake().floatValue()));
					assertEquals(new Float(0.1f * 200f),new Float(Util.formatTwoDecimalPlaces(item.getCostOfIntake().floatValue())));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0f)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getCrudeProtein().floatValue()));
					assertEquals(Util.formatTwoDecimalPlaces(new Float(0)),Util.formatTwoDecimalPlaces(item.getFeedItemNutritionalStats().getMetabolizableEnergy().floatValue()));
					cp += 0f;
					me += 0f;
				}
			}
			
			float expectedPlanCost = (48.46f * 3f /*48.46 Kgs Alfalfa*/) + 
					(18.47f * 4.75f /* 18.47 Kgs Corn Silage*/) + 
					(0.40f * 135f /*400 grams of bypass fat*/) + 
					(12f * 42.75f /* 12 Kgs Vanda*/) +
					(0.1f * 64.0f /*100 grams of meetha soda */) + 
					(0.1f * 200f /* 100 grams Mineral*/);
			
			IMDLogger.log("The animal's feedplan will give it:\n" + Util.formatTwoDecimalPlaces(plan.getPlanDM()) + " Kgs. of Dry Matter. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getDryMatter() * lactatingHighProducer.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanCP()) + " Kgs. of Crude Protein. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getCrudeProtein() * nutritionalNeeds.getDryMatter() * lactatingHighProducer.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanME()) + " MJ of Metabolizable Energy. Required:"+ nutritionalNeeds.getMetabloizableEnergy() + " MJ.\n", Util.INFO);
			assertEquals(Util.formatToSpecifiedDecimalPlaces(27.487996f,2),Util.formatToSpecifiedDecimalPlaces(plan.getPlanDM(),2));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(expectedPlanCost,0),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCost(),0));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(cp,4),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCP(),4));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(me,1),Util.formatToSpecifiedDecimalPlaces(plan.getPlanME(),1));
			
//			assertEquals(3,milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today));
//			assertEquals(3,milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(1)));
//			assertEquals(3,milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(2)));
//			assertEquals(3,milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(3)));
//			assertEquals(3,milkDetailloader.deleteMilkingRecordOfaDay(orgID, lactatingTag, today.minusDays(4)));

			assertEquals(15,milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, lactatingTag));
			
			
			assertEquals(1,anmLdr.deleteAnimal(orgID,lactatingHighProducer.getAnimalTag()));
			
			// Clean up all the test records added.
	//		assertEquals(1,loader.deleteFeedPlanItem(feedItem, " AND START >= ? AND END >= ?", feedItem.getStart(), feedItem.getEnd()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
	//		assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}

	}	
	
	private FeedPlan getCohortFeedPlan(FeedCohort feedCohort, Animal animal, HashMap<String,Float> minFulfillment, HashMap<String,Float> maxFulfillment) {
		String cohortCD = feedCohort.getFeedCohortLookupValue().getLookupValueCode();
		FeedPlan feedPlan = new FeedPlan();
		feedPlan.setOrgID(animal.getOrgID());
		feedPlan.setFeedCohort(feedCohort);
		feedPlan.setPlanCP(0f);
		feedPlan.setPlanDM(0f);
		feedPlan.setPlanME(0f);
		List<FeedItem> plan = new ArrayList<FeedItem>();
		
		if (cohortCD.equalsIgnoreCase(Util.FeedCohortType.FEMALECALF)) {
			FeedItem item1 = new FeedItem();
			item1.setOrgID(animal.getOrgID());
			item1.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.ALFAHAY,Util.FeedItems.ALFAHAY,"","",""));
			item1.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item1.setStart(4f);
			item1.setEnd(90f);
			item1.setFulfillmentPct(0.030f);
			item1.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.ALFAHAY));
			item1.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.ALFAHAY));
			item1.setFulFillmentTypeCD(Util.FulfillmentType.BODYWEIGHT);
			item1.setUnits("Kgs.");
			item1.setComments("Alfahay");
			item1.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item1.getFeedItemNutritionalStats().setDryMatter(0.9020f);
			item1.getFeedItemNutritionalStats().setCrudeProtein(0.1886f);
			item1.getFeedItemNutritionalStats().setMetabolizableEnergy(8.32f);
			item1.getFeedItemNutritionalStats().setCostPerUnit(3.0f);
			plan.add(item1);

			FeedItem item2 = new FeedItem();
			item2.setOrgID(animal.getOrgID());
			item2.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.MILK,Util.FeedItems.MILK,"","",""));
			item2.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item2.setStart(0f);
			item2.setEnd(90f);
			item2.setFulfillmentPct(0.12f);
			item2.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.MILK));
			item2.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.MILK));
			item2.setFulFillmentTypeCD(Util.FulfillmentType.BODYWEIGHT);
			item2.setUnits("Liters");
			item2.setComments("Milk");
			item2.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			// From wikipedia:
			// Cow's milk contains, on average, 3.4% protein, 3.6% fat, and 4.6% lactose,
			// 0.7% minerals[88] and supplies 66 kcal (0.276144 MJ) of energy per 100 grams i.e. 0.276 MJ % volume.
			item2.getFeedItemNutritionalStats().setDryMatter(Util.FulfillmentType.NO_DM_MEASUREONVOLUME);
			item2.getFeedItemNutritionalStats().setCrudeProtein(0.034f);
			item2.getFeedItemNutritionalStats().setMetabolizableEnergy(2.76144f); // 
			item2.getFeedItemNutritionalStats().setCostPerUnit(56.0f);
			plan.add(item2);

			FeedItem item3 = new FeedItem();
			item3.setOrgID(animal.getOrgID());
			item3.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.VANDA,Util.FeedItems.VANDA,"","",""));
			item3.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item3.setStart(0f);
			item3.setEnd(90f);
			item3.setFulfillmentPct(0.01f);
			item3.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.VANDA));
			item3.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.VANDA));
			item3.setFulFillmentTypeCD(Util.FulfillmentType.BODYWEIGHT);
			item3.setUnits("Kgs.");
			item3.setComments("Vanda # 68");
			item3.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item3.getFeedItemNutritionalStats().setDryMatter(0.88f);
			item3.getFeedItemNutritionalStats().setCrudeProtein(0.19f);
			item3.getFeedItemNutritionalStats().setMetabolizableEnergy(10.8784f/*8.32f*/);//2600 Kcal = 10.8784 MJ 
			item3.getFeedItemNutritionalStats().setCostPerUnit(43.25f);
			plan.add(item3);
			
			FeedItem item4 = new FeedItem();
			item4.setOrgID(animal.getOrgID());
			item4.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.WATER,Util.FeedItems.WATER,"","",""));
			item4.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item4.setStart(0f);
			item4.setEnd(9999f);
			item4.setFulfillmentPct(0.10f);
			item4.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.WATER));
			item4.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.WATER));
			item4.setFulFillmentTypeCD(Util.FulfillmentType.BODYWEIGHT);
			item4.setUnits("Liters");
			item4.setComments("Water");
			item4.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item4.getFeedItemNutritionalStats().setDryMatter(0f);
			item4.getFeedItemNutritionalStats().setCrudeProtein(0f);
			item4.getFeedItemNutritionalStats().setMetabolizableEnergy(0f);
			plan.add(item4);
		} else if (cohortCD.equalsIgnoreCase(Util.FeedCohortType.LCTEARLYHI)) {
			FeedItem item1 = new FeedItem();
			item1.setOrgID(animal.getOrgID());
			item1.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.ALFAALFA,"Alfalfa","Alfalfa","2001","2001"));
			item1.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item1.setStart(-9999f);
			item1.setEnd(9999f);
			item1.setFulfillmentPct(0.50f);
			item1.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.ALFAALFA));
			item1.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.ALFAALFA));
			item1.setFulFillmentTypeCD(Util.FulfillmentType.BYDMREQPCT);
			item1.setUnits("Kgs.");
			item1.setComments("Alfalfa");
			item1.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item1.getFeedItemNutritionalStats().setDryMatter(0.2426f);
			item1.getFeedItemNutritionalStats().setCrudeProtein(0.2283f);
			item1.getFeedItemNutritionalStats().setMetabolizableEnergy(9.3f);
			item1.getFeedItemNutritionalStats().setCostPerUnit(3.0f);
			plan.add(item1);

			FeedItem item2 = new FeedItem();
			item2.setOrgID(animal.getOrgID());
			item2.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.CORNSILAGE,"Corn Silage","Corn Silage","2009","2009"));
			item2.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item2.setStart(-9999f);
			item2.setEnd(9999f);
			item2.setFulfillmentPct(0.22f);
			item2.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.CORNSILAGE));
			item2.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.CORNSILAGE));
			item2.setFulFillmentTypeCD(Util.FulfillmentType.BYDMREQPCT);
			item2.setUnits("Kgs.");
			item2.setComments("Corn Silage");
			item2.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item2.getFeedItemNutritionalStats().setDryMatter(0.28f);
			item2.getFeedItemNutritionalStats().setCrudeProtein(0.072f);
			item2.getFeedItemNutritionalStats().setMetabolizableEnergy(10.8f); // 
			item2.getFeedItemNutritionalStats().setCostPerUnit(4.75f);
			plan.add(item2);

			FeedItem item3 = new FeedItem();
			item3.setOrgID(animal.getOrgID());
			item3.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.HDVANDA,"Vanda # 68","Vanda # 68","2043","2043"));
			item3.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item3.setStart(-9999f);
			item3.setEnd(9999f);
			item3.setFulfillmentPct(2.5f);
			item3.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.HDVANDA));
			item3.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.HDVANDA));
			item3.setFulFillmentTypeCD(Util.FulfillmentType.MILKPROD);
			item3.setUnits("Kgs.");
			item3.setComments("Vanda # 68");
			item3.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item3.getFeedItemNutritionalStats().setDryMatter(0.88f);
			item3.getFeedItemNutritionalStats().setCrudeProtein(0.22f);
			item3.getFeedItemNutritionalStats().setMetabolizableEnergy(13.3888f);
			item3.getFeedItemNutritionalStats().setCostPerUnit(42.75f);
			plan.add(item3);
			
			FeedItem item4 = new FeedItem();
			item4.setOrgID(animal.getOrgID());
			item4.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.BYPASFAT84,"Bypass Fat 84%","Bypass Fat 84%","2044","2044"));
			item4.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item4.setStart(0f);
			item4.setEnd(9999f);
			item4.setFulfillmentPct(0.40f);
			item4.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.BYPASFAT84));
			item4.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.BYPASFAT84));
			item4.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			item4.setUnits("Kgs.");
			item4.setComments("Bypass Fat 84%");
			item4.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item4.getFeedItemNutritionalStats().setDryMatter(0f);
			item4.getFeedItemNutritionalStats().setCrudeProtein(0f);
			item4.getFeedItemNutritionalStats().setMetabolizableEnergy(0f);
			item4.getFeedItemNutritionalStats().setCostPerUnit(135.0f);
			plan.add(item4);
			
			FeedItem item5 = new FeedItem();
			item5.setOrgID(animal.getOrgID());
			item5.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.MEETHASODA,"Sodium Bicarbonate","Sodium Bicarbonate","2017","2017"));
			item5.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item5.setStart(0f);
			item5.setEnd(9999f);
			item5.setFulfillmentPct(0.10f);
			item5.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.MEETHASODA));
			item5.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.MEETHASODA));
			item5.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			item5.setUnits("Kgs.");
			item5.setComments("Sodium Bicarbonate");
			item5.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item5.getFeedItemNutritionalStats().setDryMatter(0f);
			item5.getFeedItemNutritionalStats().setCrudeProtein(0f);
			item5.getFeedItemNutritionalStats().setMetabolizableEnergy(0f);
			item5.getFeedItemNutritionalStats().setCostPerUnit(64.0f);
			plan.add(item5);
			
			FeedItem item6 = new FeedItem();
			item6.setOrgID(animal.getOrgID());
			item6.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.MNRLBRKT,"Mineral Intraco Premix 100","Mineral Intraco Premix 100","2046","2046"));
			item6.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item6.setStart(0f);
			item6.setEnd(9999f);
			item6.setFulfillmentPct(0.10f);
			item6.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.MNRLBRKT));
			item6.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.MNRLBRKT));
			item6.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			item6.setUnits("Kgs.");
			item6.setComments("Mineral Intraco Premix 100");
			item6.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item6.getFeedItemNutritionalStats().setDryMatter(0f);
			item6.getFeedItemNutritionalStats().setCrudeProtein(0f);
			item6.getFeedItemNutritionalStats().setMetabolizableEnergy(0f);
			item6.getFeedItemNutritionalStats().setCostPerUnit(200.0f);
			plan.add(item6);
		}
		feedPlan.setFeedPlan(plan);
		return feedPlan;
	}

	private CohortNutritionalNeeds setCohortNutritionalNeeds(String cohortCD, Float weight) {
		CohortNutritionalNeeds needs = new CohortNutritionalNeeds();
		if (cohortCD.equalsIgnoreCase(Util.FeedCohortType.FEMALECALF)) {
			if (weight <= 80.0f) {
				Float femaleCalfUnder80KgDM = 0.03f; // 3.0 % of Body weight.
				Float femaleCalfUnder80KgCP = 0.183f; // 18.3 % of DM assuming 1.0 kg/day gain.
				Float femaleCalfUnder80KgME = 31f; // 31 MJ/day assuming 1.0 kg/day gain.
				needs.setDryMatter(femaleCalfUnder80KgDM);
				needs.setCrudeProtein(femaleCalfUnder80KgCP);
				needs.setMetabloizableEnergy(femaleCalfUnder80KgME);
			} else if (weight > 80.0f && weight <= 140) {
				Float femaleCalfUnder140KgDM = 0.026f;
				Float femaleCalfUnder140KgCP = 0.143f; 
				Float femaleCalfUnder140KgME = 43f; 
				needs.setDryMatter(femaleCalfUnder140KgDM);
				needs.setCrudeProtein(femaleCalfUnder140KgCP);
				needs.setMetabloizableEnergy(femaleCalfUnder140KgME);
			} else if (weight > 140.0f && weight <= 200) {
				Float femaleCalfUnder200KgDM = 0.024f;
				Float femaleCalfUnder200KgCP = 0.121f; 
				Float femaleCalfUnder200KgME = 55f; 
				needs.setDryMatter(femaleCalfUnder200KgDM);
				needs.setCrudeProtein(femaleCalfUnder200KgCP);
				needs.setMetabloizableEnergy(femaleCalfUnder200KgME);
			}
		} else if (cohortCD.equalsIgnoreCase(Util.FeedCohortType.LCTEARLYHI)) {
				Float dmRequirement = 0.0375f; // 3.0 % of Body weight.
				Float cpRequirement = 0.183f; // 18.3 % of DM assuming 1.0 kg/day gain.
				Float meRequirement = 0.0f; // ME is calculated by the system.
				needs.setDryMatter(dmRequirement);
				needs.setCrudeProtein(cpRequirement);
				needs.setMetabloizableEnergy(meRequirement);
			}
		return needs;
	}

	@Test
	void testNonExistentAnimal() {
		FeedManager manager = new FeedManager();
		try {
			manager.getAnimalFeedCohort("IMD", "DUMMY");
			fail("The animal should not have existed in our records");
		} catch (IMDException ex) {
			assertTrue(ex.getMessage().indexOf("Animal not found") >= 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	@Test
	void testMaintenanceEnergyCalculation() {
		FeedManager manager = new FeedManager();
		try { 
			assertEquals(null,manager.getMaintenanceEnergyRequirement(null));
			assertEquals(null,manager.getMaintenanceEnergyRequirement(null));

			assertEquals(0f,manager.getMaintenanceEnergyRequirement(0d).getMetabloizableEnergy().floatValue());
			assertEquals(0f,manager.getMaintenanceEnergyRequirement(0d).getNutritionalNeedsTDN().floatValue());
		
			assertEquals(8.5f,manager.getMaintenanceEnergyRequirement(50d).getMetabloizableEnergy().floatValue());
			assertEquals(0.6f,manager.getMaintenanceEnergyRequirement(50d).getNutritionalNeedsTDN().floatValue());

			assertEquals(17f,manager.getMaintenanceEnergyRequirement(100d).getMetabloizableEnergy().floatValue());
			assertEquals(1.2f,manager.getMaintenanceEnergyRequirement(100d).getNutritionalNeedsTDN().floatValue());
			
			assertEquals(19.5f,manager.getMaintenanceEnergyRequirement(125d).getMetabloizableEnergy().floatValue());
			assertEquals(1.35f,manager.getMaintenanceEnergyRequirement(125d).getNutritionalNeedsTDN().floatValue());

			assertEquals(22f,manager.getMaintenanceEnergyRequirement(150d).getMetabloizableEnergy().floatValue());
			assertEquals(1.5f,manager.getMaintenanceEnergyRequirement(150d).getNutritionalNeedsTDN().floatValue());
			
			assertEquals(25f,manager.getMaintenanceEnergyRequirement(180d).getMetabloizableEnergy().floatValue());
			assertEquals(1.74f,manager.getMaintenanceEnergyRequirement(180d).getNutritionalNeedsTDN().floatValue());

			assertEquals(27f,manager.getMaintenanceEnergyRequirement(200d).getMetabloizableEnergy().floatValue());
			assertEquals(1.9f,manager.getMaintenanceEnergyRequirement(200d).getNutritionalNeedsTDN().floatValue());
			
			assertEquals(27.8f,manager.getMaintenanceEnergyRequirement(210d).getMetabloizableEnergy().floatValue());
			assertEquals(1.96f,manager.getMaintenanceEnergyRequirement(210d).getNutritionalNeedsTDN().floatValue());

			assertEquals(31f,manager.getMaintenanceEnergyRequirement(250d).getMetabloizableEnergy().floatValue());
			assertEquals(2.2f,manager.getMaintenanceEnergyRequirement(250d).getNutritionalNeedsTDN().floatValue());

			assertEquals(31.55f,manager.getMaintenanceEnergyRequirement(255.5d).getMetabloizableEnergy().floatValue());
			assertEquals(2.23f,manager.getMaintenanceEnergyRequirement(255.5d).getNutritionalNeedsTDN().floatValue());

			assertEquals(36f,manager.getMaintenanceEnergyRequirement(300d).getMetabloizableEnergy().floatValue());
			assertEquals(2.5f,manager.getMaintenanceEnergyRequirement(300d).getNutritionalNeedsTDN().floatValue());

			assertEquals(37.36f,manager.getMaintenanceEnergyRequirement(317d).getMetabloizableEnergy().floatValue());
			assertEquals(2.60f,manager.getMaintenanceEnergyRequirement(317d).getNutritionalNeedsTDN().floatValue());

			assertEquals(40f,manager.getMaintenanceEnergyRequirement(350d).getMetabloizableEnergy().floatValue());
			assertEquals(2.8f,manager.getMaintenanceEnergyRequirement(350d).getNutritionalNeedsTDN().floatValue());

			assertEquals(44f,manager.getMaintenanceEnergyRequirement(390d).getMetabloizableEnergy().floatValue());
			assertEquals(3.04f,manager.getMaintenanceEnergyRequirement(390d).getNutritionalNeedsTDN().floatValue());

			assertEquals(45f,manager.getMaintenanceEnergyRequirement(400d).getMetabloizableEnergy().floatValue());
			assertEquals(3.1f,manager.getMaintenanceEnergyRequirement(400d).getNutritionalNeedsTDN().floatValue());

			assertEquals(46.04f,manager.getMaintenanceEnergyRequirement(413d).getMetabloizableEnergy().floatValue());
			assertEquals(3.18f,manager.getMaintenanceEnergyRequirement(413d).getNutritionalNeedsTDN().floatValue());

			assertEquals(49f,manager.getMaintenanceEnergyRequirement(450d).getMetabloizableEnergy().floatValue());
			assertEquals(3.4f,manager.getMaintenanceEnergyRequirement(450d).getNutritionalNeedsTDN().floatValue());

			assertEquals(51.9f,manager.getMaintenanceEnergyRequirement(479d).getMetabloizableEnergy().floatValue());
			assertEquals(3.63f,manager.getMaintenanceEnergyRequirement(479d).getNutritionalNeedsTDN().floatValue());

			assertEquals(54f,manager.getMaintenanceEnergyRequirement(500d).getMetabloizableEnergy().floatValue());
			assertEquals(3.8f,manager.getMaintenanceEnergyRequirement(500d).getNutritionalNeedsTDN().floatValue());

			assertEquals(57.7f,manager.getMaintenanceEnergyRequirement(537d).getMetabloizableEnergy().floatValue());
			assertEquals(4.02f,manager.getMaintenanceEnergyRequirement(537d).getNutritionalNeedsTDN().floatValue());

			assertEquals(59f,manager.getMaintenanceEnergyRequirement(550d).getMetabloizableEnergy().floatValue());
			assertEquals(4.1f,manager.getMaintenanceEnergyRequirement(550d).getNutritionalNeedsTDN().floatValue());

			assertEquals(62.04f,manager.getMaintenanceEnergyRequirement(588d).getMetabloizableEnergy().floatValue());
			assertEquals(4.33f,manager.getMaintenanceEnergyRequirement(588d).getNutritionalNeedsTDN().floatValue());

			assertEquals(63f,manager.getMaintenanceEnergyRequirement(600d).getMetabloizableEnergy().floatValue());
			assertEquals(4.4f,manager.getMaintenanceEnergyRequirement(600d).getNutritionalNeedsTDN().floatValue());

			assertEquals(71f,manager.getMaintenanceEnergyRequirement(700d).getMetabloizableEnergy().floatValue());
			assertEquals(5f,manager.getMaintenanceEnergyRequirement(700d).getNutritionalNeedsTDN().floatValue());

			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	@Test
	void testPregnancyAdditionalEnergyCalculation() {
		FeedManager manager = new FeedManager();
		try { 
			assertEquals(null,manager.getPregnancyEnergyRequirement(null));
			assertEquals(null,manager.getPregnancyEnergyRequirement(null));

			assertEquals(0f,manager.getPregnancyEnergyRequirement(0).getMetabloizableEnergy().floatValue());
			assertEquals(0f,manager.getPregnancyEnergyRequirement(0).getNutritionalNeedsTDN().floatValue());
		
			assertEquals(0f,manager.getPregnancyEnergyRequirement(50).getMetabloizableEnergy().floatValue());
			assertEquals(0f,manager.getPregnancyEnergyRequirement(50).getNutritionalNeedsTDN().floatValue());

			assertEquals(0f,manager.getPregnancyEnergyRequirement(150).getMetabloizableEnergy().floatValue());
			assertEquals(0f,manager.getPregnancyEnergyRequirement(150).getNutritionalNeedsTDN().floatValue());
			
			assertEquals(8f,manager.getPregnancyEnergyRequirement(180).getMetabloizableEnergy().floatValue());
			assertEquals(0.6f,manager.getPregnancyEnergyRequirement(180).getNutritionalNeedsTDN().floatValue());

			assertEquals(10f,manager.getPregnancyEnergyRequirement(Util.HTTPCodes.OK).getMetabloizableEnergy().floatValue());
			assertEquals(0.7f,manager.getPregnancyEnergyRequirement(Util.HTTPCodes.OK).getNutritionalNeedsTDN().floatValue());
			
			assertEquals(10f,manager.getPregnancyEnergyRequirement(210).getMetabloizableEnergy().floatValue());
			assertEquals(0.7f,manager.getPregnancyEnergyRequirement(210).getNutritionalNeedsTDN().floatValue());

			assertEquals(15f,manager.getPregnancyEnergyRequirement(240).getMetabloizableEnergy().floatValue());
			assertEquals(1.1f,manager.getPregnancyEnergyRequirement(240).getNutritionalNeedsTDN().floatValue());

			assertEquals(20f,manager.getPregnancyEnergyRequirement(255).getMetabloizableEnergy().floatValue());
			assertEquals(1.4f,manager.getPregnancyEnergyRequirement(255).getNutritionalNeedsTDN().floatValue());

			assertEquals(20f,manager.getPregnancyEnergyRequirement(300).getMetabloizableEnergy().floatValue());
			assertEquals(1.4f,manager.getPregnancyEnergyRequirement(300).getNutritionalNeedsTDN().floatValue());

			assertEquals(20f,manager.getPregnancyEnergyRequirement(317).getMetabloizableEnergy().floatValue());
			assertEquals(1.4f,manager.getPregnancyEnergyRequirement(317).getNutritionalNeedsTDN().floatValue());
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testMilkingAdditionalEnergyCalculation() {
		FeedManager manager = new FeedManager();
		try { 
			double milkVol = 1f;
			assertEquals(Util.formatTwoDecimalPlaces(5.3f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,null,null).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.4f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,null,null).getNutritionalNeedsTDN().doubleValue()));

			assertEquals(Util.formatTwoDecimalPlaces(7.1f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,6d,4.4d).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.5f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,6d,4.4d).getNutritionalNeedsTDN().doubleValue()));

			assertEquals(Util.formatTwoDecimalPlaces(7.1f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,16d,14.4d).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.5f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,16d,14.4d).getNutritionalNeedsTDN().doubleValue()));

			assertEquals(Util.formatTwoDecimalPlaces(6.9f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,5.8d,4.2d).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.5f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,5.8d,4.2d).getNutritionalNeedsTDN().doubleValue()));

			milkVol = 23.7d;
			assertEquals(Util.formatTwoDecimalPlaces(6.4f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,6d,2.6d).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.5f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,6d,2.6d).getNutritionalNeedsTDN().doubleValue()));

			assertEquals(Util.formatTwoDecimalPlaces(6.2f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,5.2d,3.4d).getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.4f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,5.2d,3.4d).getNutritionalNeedsTDN().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.4f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,3.7d,2.3d).getNutritionalNeedsTDN().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(0.3f * milkVol),Util.formatTwoDecimalPlaces(manager.getMilkingEnergyRequirement(milkVol,3.5d,2.7d).getNutritionalNeedsTDN().doubleValue()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}	
	@Test
	void testMetabolizableEnergyRequirements() {
		FeedManager manager = new FeedManager();
		try { 
			CohortNutritionalNeeds needs = manager.getMetabolizableEnergyRequiremnt(493d, 17.5d/*milking avg*/, 0/*days into pregnancy*/,Util.FeedCohortType.LCTOLD, 3.6d, 3.2d);
			assertEquals(Util.formatTwoDecimalPlaces(142.55d),Util.formatTwoDecimalPlaces(needs.getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(10.74),Util.formatTwoDecimalPlaces(needs.getNutritionalNeedsTDN().doubleValue()));

			needs = manager.getMetabolizableEnergyRequiremnt(493d, 17.5d/*milking avg*/, 0/*days into pregnancy*/,Util.FeedCohortType.LCTOLD, null,null);
			assertEquals(Util.formatTwoDecimalPlaces(146.05d),Util.formatTwoDecimalPlaces(needs.getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(10.74),Util.formatTwoDecimalPlaces(needs.getNutritionalNeedsTDN().doubleValue()));

			needs = manager.getMetabolizableEnergyRequiremnt(550d, 10d/*milking avg*/, 181/*days into pregnancy*/,Util.FeedCohortType.LCTOLD, 3.6d,3.2d);
			assertEquals(Util.formatTwoDecimalPlaces(120d),Util.formatTwoDecimalPlaces(needs.getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(8.8d),Util.formatTwoDecimalPlaces(needs.getNutritionalNeedsTDN().doubleValue()));
			
			needs = manager.getMetabolizableEnergyRequiremnt(550d, 20d/*milking avg*/, 60/*days into pregnancy*/,Util.FeedCohortType.LCTOLD, 3.6d,3.2d);
			assertEquals(Util.formatTwoDecimalPlaces(161d),Util.formatTwoDecimalPlaces(needs.getMetabloizableEnergy().doubleValue()));
			assertEquals(Util.formatTwoDecimalPlaces(12.1d),Util.formatTwoDecimalPlaces(needs.getNutritionalNeedsTDN().doubleValue()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}		
}
