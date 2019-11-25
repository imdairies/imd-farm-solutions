package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import org.joda.time.DateTime;
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
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.DBManager;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class LifecycleEventSrvcTest {

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
	void testAutoCalfAdditionUponParturationEvent() {
		String orgID = "IMD";
		String animalTag = "-999";
		String calfIncorrectTag = "-999";
		String calfCorrectTag = "-998";
		String sire = "1HO10219";
		int originalMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;

		DateTime inseminationTS = DateTime.now(IMDProperties.getServerTimeZone()).minusDays(280);
		DateTime pregTestTS = inseminationTS.plusDays(90);
		DateTime parturitionTS = DateTime.now(IMDProperties.getServerTimeZone());
		
		try {
			LifeCycleEventCode inseminateEventCD = new LifeCycleEventCode(Util.LifeCycleEvents.INSEMINATE,"","");
			LifeCycleEventCode matingEventCD = new LifeCycleEventCode(Util.LifeCycleEvents.MATING,"","");
			LifeCycleEventCode pregTestCD = new LifeCycleEventCode(Util.LifeCycleEvents.PREGTEST,"","");
			LifeCycleEventCode parturitionCD = new LifeCycleEventCode(Util.LifeCycleEvents.PARTURATE,"","");
			
			LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
			AnimalLoader animalLoader = new AnimalLoader();

			User user1 = new User("TEST");
			Person kashif = new Person("KASHIF","KASHIF","KASHIF","KASHIF");
			Animal animal = createTestAnimal(animalTag);			
			

//			LifecycleEvent matingEvent = new LifecycleEvent(orgID,0,animalTag,matingEventCD.getEventCode(),user,DateTime.now(),user,DateTime.now());
//			matingEvent.setAuxField1Value(sire);
//			matingEvent.setAuxField2Value(Util.YES);
//			matingEvent.setAuxField3Value(null);
//			matingEvent.setAuxField4Value(null);
//			matingEvent.setEventOperator(kashif);
//			matingEvent.setEventTimeStamp(inseminationTS);
//			matingEvent.setCreatedBy(user);
//			matingEvent.setUpdatedBy(user);
//			matingEvent.setCreatedDTTM(inseminationTS);
//			matingEvent.setUpdatedDTTM(inseminationTS);		
			
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,animalTag,inseminateEventCD.getEventCode(),user1,DateTime.now(),user1,DateTime.now());
			inseminationEvent.setAuxField1Value(sire);
			inseminationEvent.setAuxField2Value(Util.YES /*isSexed*/);
			inseminationEvent.setAuxField3Value(Util.YES /*isInseminationSuccessful*/);
			inseminationEvent.setAuxField4Value(null);
			inseminationEvent.setEventOperator(kashif);
			inseminationEvent.setEventTimeStamp(inseminationTS);
			inseminationEvent.setCreatedBy(user1);
			inseminationEvent.setUpdatedBy(user1);
			inseminationEvent.setCreatedDTTM(inseminationTS);
			inseminationEvent.setUpdatedDTTM(inseminationTS);
			
			LifecycleEvent pregTestEvent = new LifecycleEvent(orgID,0,animalTag,pregTestCD.getEventCode(),user1,pregTestTS,user1,pregTestTS);
			pregTestEvent.setAuxField1Value(Util.YES.toUpperCase()); //  Pregnant = NO
			pregTestEvent.setAuxField2Value(Util.YES.toUpperCase()); // Update last insemination outcome = YES
			pregTestEvent.setAuxField3Value(null);
			pregTestEvent.setAuxField4Value(null);
			pregTestEvent.setEventTimeStamp(pregTestTS);
			pregTestEvent.setEventNote("test");
			pregTestEvent.setCreatedBy(user1);
			pregTestEvent.setUpdatedBy(user1);
			pregTestEvent.setCreatedDTTM(pregTestTS);
			pregTestEvent.setUpdatedDTTM(pregTestTS);

			LifecycleEvent parturitionEvent = new LifecycleEvent(orgID,0,animalTag,parturitionCD.getEventCode(),user1,DateTime.now(),user1,DateTime.now());
			parturitionEvent.setOrgID(orgID);
			parturitionEvent.setAnimalTag(animalTag);
			parturitionEvent.setAuxField1Value(Util.Gender.FEMALE); 
			parturitionEvent.setAuxField2Value(null);
			parturitionEvent.setAuxField3Value(null);
			parturitionEvent.setAuxField4Value(null);
			parturitionEvent.setEventTimeStamp(parturitionTS);
			parturitionEvent.setEventNote("test");
			
			assertTrue(eventLoader.deleteAnimalLifecycleEvents(orgID, animalTag)>=0);
			assertTrue(eventLoader.deleteAnimalLifecycleEvents(orgID, calfCorrectTag)>=0);
			assertTrue(animalLoader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag())>=0);
			assertTrue(animalLoader.deleteAnimal(animal.getOrgID(), calfCorrectTag)>=0);

			assertTrue(eventLoader.insertLifeCycleEvent(inseminationEvent) > 0);
			assertTrue(eventLoader.insertLifeCycleEvent(pregTestEvent) > 0);

			assertEquals(1,animalLoader.insertAnimal(animal));

			
			//assertTrue(eventLoader.insertLifeCycleEvent(pregTestEvent) > 0);
			//loader.insertLifeCycleEvent(pregTestEvent);
			
			LifecycleEventSrvc lifecycleSrvc = new LifecycleEventSrvc();
			
			LifeCycleEventBean parturitionEventBean = new LifeCycleEventBean();
			parturitionEventBean.setAnimalTag(animal.getAnimalTag());
			parturitionEventBean.setOrgID(animal.getOrgID());
			parturitionEventBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			parturitionEventBean.setAuxField1Value(Util.Gender.FEMALE);
			parturitionEventBean.setAuxField2Value(Util.NO);
			parturitionEventBean.setAuxField3Value(calfIncorrectTag);
			parturitionEventBean.setEventComments("The calf should NOT be added as the tag is in use");
			parturitionEventBean.setEventTimeStamp(Util.getDateTimeInSpecifiedFormat(DateTime.now(IMDProperties.getServerTimeZone()),"MM/dd/yyyy, hh:mm:ss aa"));
			
			String responseStr = lifecycleSrvc.addEvent(parturitionEventBean).getEntity().toString();
			assertTrue(responseStr.indexOf("This tag# is already in use") >= 0, "Since the calf tag# was the same as the parturating cow the service should have thrown an invalid tag# error");

			assertEquals(2,eventLoader.deleteAnimalLifecycleEvents(animal.getOrgID(), animal.getAnimalTag()));
			assertEquals(0,eventLoader.deleteAnimalLifecycleEvents(animal.getOrgID(), calfCorrectTag));
			assertEquals(1,animalLoader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag()));
			assertEquals(0,animalLoader.deleteAnimal(animal.getOrgID(), calfCorrectTag));

			parturitionEventBean.setAuxField3Value(calfCorrectTag);
			parturitionEventBean.setEventComments("The calf should be added as the tag is NOT in use");

			assertTrue(eventLoader.insertLifeCycleEvent(inseminationEvent) > 0);
			assertTrue(eventLoader.insertLifeCycleEvent(pregTestEvent) > 0);
			assertEquals(1,animalLoader.insertAnimal(animal));
			
			
			UserLoader userLoader = new UserLoader();
			User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
			assertTrue(user != null);
			assertTrue(user.getPassword() != null);
			parturitionEventBean.setLoginToken(user.getPassword());
			
			
			
			responseStr = lifecycleSrvc.addEvent(parturitionEventBean).getEntity().toString();
			assertTrue(responseStr.indexOf("This tag# is already in use") < 0, "Since the calf tag# was the same as the parturating cow the service should have thrown an invalid tag# error");
			assertTrue(responseStr.indexOf("The calf with the tag# " + calfCorrectTag + " has been successfully added to the herd") >= 0, responseStr);
			
			assertEquals(3,eventLoader.deleteAnimalLifecycleEvents(animal.getOrgID(), animal.getAnimalTag()));
			assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(animal.getOrgID(), calfCorrectTag));
			assertEquals(1,animalLoader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag()));
			assertEquals(1,animalLoader.deleteAnimal(animal.getOrgID(), calfCorrectTag));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			IMDLogger.loggingMode = originalMode;
		}

	}

}
