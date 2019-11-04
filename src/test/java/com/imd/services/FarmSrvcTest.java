package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class FarmSrvcTest {

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
	public Animal createTestAnimal(String animalTag, String type) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType(type);
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setHerdJoiningDate(DateTime.now().minusMonths(12));
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
	}
	@Test
	void testUpcomingCalvings() {
		int loggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			Animal anml = createTestAnimal("-999", Util.AnimalTypes.DRYPREG);
			String controller = "IMD";
			User user = new User("TEST");

			AnimalLoader aLoader = new AnimalLoader();
			LifeCycleEventsLoader eLoader = new LifeCycleEventsLoader();
			
			
			assertTrue(aLoader.deleteAnimal(anml.getOrgID(), anml.getAnimalTag()) >= 0);
			assertTrue(eLoader.deleteAnimalLifecycleEvents(anml.getOrgID(), anml.getAnimalTag()) >= 0);
			
			
			LifecycleEvent inseminationEvent = new LifecycleEvent(controller,0,anml.getAnimalTag(),Util.LifeCycleEvents.INSEMINATE,
					user,DateTime.now(IMDProperties.getServerTimeZone()),
					user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setAuxField1Value("TEST-SIRE");
			inseminationEvent.setAuxField2Value(Util.NO);
			inseminationEvent.setAuxField3Value(Util.YES);
			inseminationEvent.setAuxField4Value(null);
			inseminationEvent.setEventOperator(new Person("TEST","TEST","TEST","TEST"));
			inseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(276));
//			inseminationEvent.setCreatedBy(user);
//			inseminationEvent.setUpdatedBy(user);
//			inseminationEvent.setCreatedDTTM(DateTime.now());
//			inseminationEvent.setUpdatedDTTM(DateTime.now());
			

			assertEquals(1,aLoader.insertAnimal(anml));
			assertTrue(eLoader.insertLifeCycleEvent(inseminationEvent) > 0);
			
			
			FarmSrvc farmSrvc = new FarmSrvc();
			
			
			String expectedCalvingThisMonthListValue = farmSrvc.retrieveBreedingEventForThisMonth().getEntity().toString();
			int start = expectedCalvingThisMonthListValue.indexOf("\"expectedCalvingThisMonthList\"");
			assertTrue(start>=0, "" + start);
			int end = expectedCalvingThisMonthListValue.indexOf("\",", start);
			assertTrue(end>start);
			assertTrue(expectedCalvingThisMonthListValue.substring(start, end).indexOf(anml.getAnimalTag()) >=0,expectedCalvingThisMonthListValue.substring(start, end));
			
		
			assertEquals(1,eLoader.deleteAnimalLifecycleEvents(anml.getOrgID(), anml.getAnimalTag()));
			assertEquals(1,aLoader.deleteAnimal(anml.getOrgID(), anml.getAnimalTag()));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception in FarmSrvcTest.testUpcomingCalvings");
		} finally {
			IMDLogger.loggingMode = loggingMode;
		}
		
		

	}

}
