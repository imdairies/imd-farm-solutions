package com.imd.controller.feed;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.LifeCycleEventsLoader;
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

	@Test
	void testBullAndMaleCalf() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		try {
			Sire maleCalf = new Sire("TEST-MALECALF");
			maleCalf.setOrgID("IMD");
			maleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(100));
			maleCalf.setAnimalTypeCD(Util.AnimalTypes.MALECALF);
			maleCalf.setBreed(Util.Breed.HFCROSS);
			maleCalf.setHerdJoiningDate(maleCalf.getDateOfBirth());
			maleCalf.setCreatedBy(new User("KASHIF"));
			maleCalf.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			maleCalf.setUpdatedBy(maleCalf.getCreatedBy());
			maleCalf.setUpdatedDTTM(maleCalf.getCreatedDTTM());
			
			Sire bull = new Sire("TEST-BULL");
			bull.setOrgID(maleCalf.getOrgID());
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
		try {
			String nonPregHeiferTag = "-999";
			String pregHeiferTag = "-998";
			String orgID = "IMD";
			User user = new User("KASHIF");
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRPREGN);
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
		}
	}

	
	@Test
	void testVariousLactationStages() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
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

			assertEquals(Util.FeedCohortType.LCTEARLY,manager.getAnimalFeedCohort(orgID, freshLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTMID,manager.getAnimalFeedCohort(orgID, midLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTOLD,manager.getAnimalFeedCohort(orgID, oldLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			
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
			
			LifecycleEvent farInseminationEvent = new LifecycleEvent(orgID,0,faroffDryPregTag,Util.LifeCycleEvents.MATING,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			farInseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS - 10));

			
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

//			assertEquals(Util.FeedCohortType.FAROFFPARTURATION,manager.getAnimalFeedCohortType(orgID, "018").getFeedCohortTypeCD());
//			assertEquals(Util.FeedCohortType.FAROFFPARTURATION,manager.getAnimalFeedCohortType(orgID, "023").getFeedCohortTypeCD());
		
			
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
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRPREGN);
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(IMDProperties.getServerTimeZone()),user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));
//			inseminationEvent.setEventTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(FeedManager.DRYOFF_BY_DAYS+30));

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
				assertTrue(ex.getMessage().equals("The farm does not seem to have any active animal"));
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
				if (anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode().equals(Util.FeedCohortType.UNDETERMINED))
					undeterminedCount++;
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
			Dam femaleCalf = new Dam("TEST-FEMALECALF");
			femaleCalf.setOrgID("IMD");
			femaleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(50));
			femaleCalf.setAnimalTypeCD(Util.AnimalTypes.FEMALECALF);
			femaleCalf.setBreed(Util.Breed.HFCROSS);
			femaleCalf.setHerdJoiningDate(femaleCalf.getDateOfBirth());
			femaleCalf.setCreatedBy(new User("KASHIF"));
			femaleCalf.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			femaleCalf.setUpdatedBy(femaleCalf.getCreatedBy());
			femaleCalf.setUpdatedDTTM(femaleCalf.getCreatedDTTM());
			
			Dam femaleCalfWeanedOff = new Dam("TEST-FEMALEWEANEDOFF");
			femaleCalfWeanedOff.setOrgID(femaleCalf.getOrgID());
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
			LookupValues feedItemLV = new LookupValues(Util.LookupValues.FEED,"TST_ALFHAY", "","");
			feedItem.setFeedItemLookupValue(feedItemLV);		
			
			LookupValues feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.FEMALECALF, "","");
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
			
			float ageInDays = femaleCalf.getCurrentAgeInDays().getDays();
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
			assertEquals("Feed Item [" + feedItem.getFeedItemLookupValue().getLookupValueCode() + "] updated DTTM should have been : " + feedItem.getUpdatedDTTMSQLFormat(), feedItem.getUpdatedDTTMSQLFormat(), item.getUpdatedDTTMSQLFormat());
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
//			Sire bull = new Sire(bullTag);
//			bull.setOrgID(orgID);
//			bull.setWeight(300f);
			
			AnimalLoader anmLdr = new AnimalLoader();
			
			anmLdr.deleteAnimal(orgID,femaleCalfTag);
//			anmLdr.deleteAnimal(orgID,bullTag);
			assertEquals(1,anmLdr.insertAnimal(femaleCalf));
			
			femaleCalf.setWeight(50f);
//			femaleCalf.setWeight(78f);
			
			LookupValues lv = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.FEMALECALF,Util.FeedCohortType.FEMALECALF,"");
			
			FeedCohort feedCohort = new FeedCohort(orgID, lv,"");
			CohortNutritionalNeeds nutritionalNeeds = setCohortNutritionalNeeds(Util.FeedCohortType.FEMALECALF, femaleCalf.getWeight());
			feedCohort.setCohortNutritionalNeeds(nutritionalNeeds);
			femaleCalf.setFeedCohortInformation(feedCohort);
			
			HashMap<String,Float> minFulfillment = new HashMap<String,Float> ();
			HashMap<String,Float>  maxFulfillment = new HashMap<String,Float> ();
//			maxFulfillment.put(Util.FeedItems.MILK, new Float(6));
			
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
			IMDLogger.log("The animal's feedplan will give it:\n" + Util.formatTwoDecimalPlaces(plan.getPlanDM()) + " Kgs. of Dry Matter. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanCP()) + " Kgs. of Crude Protein. Required:"+ Util.formatTwoDecimalPlaces(nutritionalNeeds.getCrudeProtein() * nutritionalNeeds.getDryMatter() * femaleCalf.getWeight()) + " Kgs.\n" +
					Util.formatTwoDecimalPlaces(plan.getPlanME()) + " MJ of Metabolizable Energy. Required:"+ nutritionalNeeds.getMetabloizableEnergy() + " MJ.\n", Util.INFO);
			assertEquals(Util.formatToSpecifiedDecimalPlaces(32.612096f,4),Util.formatToSpecifiedDecimalPlaces(plan.getPlanME(),4));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(0.5427758f,4),Util.formatToSpecifiedDecimalPlaces(plan.getPlanCP(),4));
			assertEquals(Util.formatToSpecifiedDecimalPlaces(1.793f,3),Util.formatToSpecifiedDecimalPlaces(plan.getPlanDM(),3));
			
			maxFulfillment.put(Util.FeedItems.MILK, new Float(3));
			minFulfillment.put(Util.FeedItems.ALFAHAY, new Float(5));
			
			cohortFeedPlan = getCohortFeedPlan(feedCohort, femaleCalf, minFulfillment, maxFulfillment);
			
			plan = manager.getPersonalizedFeedPlan(feedCohort, cohortFeedPlan, femaleCalf);
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			
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
			item1.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.ALFAHAY,Util.FeedItems.ALFAHAY,""));
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
			plan.add(item1);

			FeedItem item2 = new FeedItem();
			item2.setOrgID(animal.getOrgID());
			item2.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.MILK,Util.FeedItems.MILK,""));
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
			plan.add(item2);

			FeedItem item3 = new FeedItem();
			item3.setOrgID(animal.getOrgID());
			item3.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.VANDA,Util.FeedItems.VANDA,""));
			item3.setFeedCohortCD(feedCohort.getFeedCohortLookupValue());
			item3.setStart(0f);
			item3.setEnd(90f);
			item3.setFulfillmentPct(0.01f);
			item3.setMinimumFulfillment(minFulfillment.get(Util.FeedItems.VANDA));
			item3.setMaximumFulfillment(maxFulfillment.get(Util.FeedItems.VANDA));
			item3.setFulFillmentTypeCD(Util.FulfillmentType.BODYWEIGHT);
			item3.setUnits("Kgs.");
			item3.setComments("Vanda # 12");
			item3.setFeedItemNutritionalStats(new FeedItemNutritionalStats());
			item3.getFeedItemNutritionalStats().setDryMatter(0.88f);
			item3.getFeedItemNutritionalStats().setCrudeProtein(0.19f);
			item3.getFeedItemNutritionalStats().setMetabolizableEnergy(10.8784f/*8.32f*/);//2600 Kcal = 10.8784 MJ 
			plan.add(item3);
			
			FeedItem item4 = new FeedItem();
			item4.setOrgID(animal.getOrgID());
			item4.setFeedItemLookupValue(new LookupValues(Util.LookupValues.FEED,Util.FeedItems.WATER,Util.FeedItems.WATER,""));
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

			assertEquals(10f,manager.getPregnancyEnergyRequirement(200).getMetabloizableEnergy().floatValue());
			assertEquals(0.7f,manager.getPregnancyEnergyRequirement(200).getNutritionalNeedsTDN().floatValue());
			
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
