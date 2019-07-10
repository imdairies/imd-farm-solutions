package com.imd.controller.feed;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
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
			maleCalf.setDateOfBirth(DateTime.now().minusDays(100));
			maleCalf.setAnimalTypeCD(Util.AnimalTypes.MALECALF);
			maleCalf.setBreed(Util.Breed.HFCROSS);
			maleCalf.setHerdJoiningDate(maleCalf.getDateOfBirth());
			maleCalf.setCreatedBy(new User("KASHIF"));
			maleCalf.setCreatedDTTM(DateTime.now());
			maleCalf.setUpdatedBy(maleCalf.getCreatedBy());
			maleCalf.setUpdatedDTTM(maleCalf.getCreatedDTTM());
			
			Sire bull = new Sire("TEST-BULL");
			bull.setOrgID(maleCalf.getOrgID());
			bull.setDateOfBirth(DateTime.now().minusDays(300));
			bull.setAnimalTypeCD(Util.AnimalTypes.BULL);
			bull.setBreed(maleCalf.getBreed());
			bull.setHerdJoiningDate(maleCalf.getHerdJoiningDate());
			bull.setCreatedBy(new User("KASHIF"));
			bull.setCreatedDTTM(DateTime.now());
			bull.setUpdatedBy(maleCalf.getCreatedBy());
			bull.setUpdatedDTTM(maleCalf.getCreatedDTTM());
			
			anmLdr.deleteAnimal(maleCalf.getOrgID(), maleCalf.getAnimalTag());
			anmLdr.deleteAnimal(bull.getOrgID(), bull.getAnimalTag());			
			
			assertEquals(1,anmLdr.insertAnimal(maleCalf));
			assertEquals(1,anmLdr.insertAnimal(bull));
			assertEquals(Util.FeedCohortType.BULL,manager.getAnimalFeedCohortType(bull.getOrgID(), bull.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.MALECALF,manager.getAnimalFeedCohortType(maleCalf.getOrgID(), maleCalf.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			
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
		dam.setCreatedDTTM(DateTime.now());
		dam.setHerdJoiningDate(dob);
		dam.setHerdLeavingDate(null);
		dam.setUpdatedBy(dam.getCreatedBy());
		dam.setUpdatedDTTM(dam.getCreatedDTTM());
		dam.setAnimalDam(null);
		Note newNote = new Note (1,"test note", LocalDateTime.now());		
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
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now().minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now().minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRPREGN);
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(),user,DateTime.now());
			inseminationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));


			eventLoader.deleteAnimalLifecycleEvents(orgID, nonPregHeiferTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, pregHeiferTag);
			anmLdr.deleteAnimal(orgID, nonPregHeiferTag);
			anmLdr.deleteAnimal(orgID, pregHeiferTag);
			
			anmLdr.insertAnimal(pregnantHeifer);
			anmLdr.insertAnimal(nonPregnantHeifer);
			eventLoader.insertLifeCycleEvent(inseminationEvent);
			assertEquals(Util.FeedCohortType.HEIFER,manager.getAnimalFeedCohortType(orgID, nonPregHeiferTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.HFRCLOSEUP,manager.getAnimalFeedCohortType(orgID, pregHeiferTag).getFeedCohortLookupValue().getLookupValueCode());
			

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
			Dam freshLactation = createDam(orgID,freshLactationTag,DateTime.now().minusDays(4*365),Util.AnimalTypes.LACTATING);
			Dam midLactation = createDam(orgID,midLactationTag,DateTime.now().minusDays(4*365),Util.AnimalTypes.LCTINSEMIN);
			Dam oldLactation = createDam(orgID,oldLactationTag,DateTime.now().minusDays(4*365),Util.AnimalTypes.LCTPRGNT);

			
			LifecycleEvent freshParturationEvent = new LifecycleEvent(orgID,0,freshLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(),user,DateTime.now());
			freshParturationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.RECENT_PARTURATION_DAYS_LIMIT));
			
			LifecycleEvent midParturationEvent = new LifecycleEvent(orgID,0,midLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(),user,DateTime.now());
			midParturationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.RECENT_PARTURATION_DAYS_LIMIT + 1));

			LifecycleEvent oldParturationEvent = new LifecycleEvent(orgID,0,oldLactationTag,Util.LifeCycleEvents.PARTURATE,user,DateTime.now(),user,DateTime.now());
			oldParturationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.DRYOFF_BY_DAYS));
			
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

			assertEquals(Util.FeedCohortType.LCTEARLY,manager.getAnimalFeedCohortType(orgID, freshLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTMID,manager.getAnimalFeedCohortType(orgID, midLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.LCTOLD,manager.getAnimalFeedCohortType(orgID, oldLactationTag).getFeedCohortLookupValue().getLookupValueCode());
			
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
			
			Dam closeupDryPreg = createDam(orgID,closeupDryPregTag,DateTime.now().minusDays(4*365),Util.AnimalTypes.DRYPREG);
			Dam faroffDryPreg = createDam(orgID,faroffDryPregTag,DateTime.now().minusDays(4*365),Util.AnimalTypes.DRYPREG);

			
			LifecycleEvent closeupInseminationEvent = new LifecycleEvent(orgID,0,closeupDryPregTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(),user,DateTime.now());
			closeupInseminationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));
			
			LifecycleEvent farInseminationEvent = new LifecycleEvent(orgID,0,faroffDryPregTag,Util.LifeCycleEvents.MATING,user,DateTime.now(),user,DateTime.now());
			farInseminationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS - 10));

			
			eventLoader.deleteAnimalLifecycleEvents(orgID, closeupDryPregTag);
			eventLoader.deleteAnimalLifecycleEvents(orgID, faroffDryPregTag);
			anmLdr.deleteAnimal(orgID, closeupDryPregTag);
			anmLdr.deleteAnimal(orgID, faroffDryPregTag);
			
			anmLdr.insertAnimal(closeupDryPreg);
			anmLdr.insertAnimal(faroffDryPreg);

			eventLoader.insertLifeCycleEvent(closeupInseminationEvent);
			eventLoader.insertLifeCycleEvent(farInseminationEvent);

			assertEquals(Util.FeedCohortType.NEARPRTRT,manager.getAnimalFeedCohortType(orgID, closeupDryPregTag).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.FARPRTRT,manager.getAnimalFeedCohortType(orgID, faroffDryPregTag).getFeedCohortLookupValue().getLookupValueCode());

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
			Dam nonPregnantHeifer = createDam(orgID,nonPregHeiferTag,DateTime.now().minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRAWTHEAT);
			Dam pregnantHeifer = createDam(orgID,pregHeiferTag,DateTime.now().minusDays(FeedManager.HEIFER_MIN_AGE_IN_DAYS),Util.AnimalTypes.HFRPREGN);
			LifecycleEvent inseminationEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.INSEMINATE,user,DateTime.now(),user,DateTime.now());
			inseminationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.PREGNANCY_DURATION_DAYS - FeedManager.NEAR_PARTURATION_THRESHOLD_DAYS + 1));
//			inseminationEvent.setEventTimeStamp(DateTime.now().minusDays(FeedManager.DRYOFF_BY_DAYS+30));

			LifecycleEvent weightEvent = new LifecycleEvent(orgID,0,pregHeiferTag,Util.LifeCycleEvents.WEIGHT,user,DateTime.now(),user,DateTime.now());
			weightEvent.setAuxField1Value("400");
			weightEvent.setEventTimeStamp(DateTime.now());


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
			boolean weightFound = false;
			while (it.hasNext()) {
				Animal anml = it.next();
				anml.dtoToJson("   ");
				if (anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode().equals(Util.FeedCohortType.UNDETERMINED))
					undeterminedCount++;
				if (anml.getAnimalTag().equals(nonPregHeiferTag)) {
					assertEquals(Util.FeedCohortType.HEIFER,anml.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode());
					nonPregHeiferFound = true;
				} else if (anml.getAnimalTag().equals(pregHeiferTag)) {
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
			femaleCalf.setDateOfBirth(DateTime.now().minusDays(50));
			femaleCalf.setAnimalTypeCD(Util.AnimalTypes.FEMALECALF);
			femaleCalf.setBreed(Util.Breed.HFCROSS);
			femaleCalf.setHerdJoiningDate(femaleCalf.getDateOfBirth());
			femaleCalf.setCreatedBy(new User("KASHIF"));
			femaleCalf.setCreatedDTTM(DateTime.now());
			femaleCalf.setUpdatedBy(femaleCalf.getCreatedBy());
			femaleCalf.setUpdatedDTTM(femaleCalf.getCreatedDTTM());
			
			Dam femaleCalfWeanedOff = new Dam("TEST-FEMALEWEANEDOFF");
			femaleCalfWeanedOff.setOrgID(femaleCalf.getOrgID());
			femaleCalfWeanedOff.setDateOfBirth(DateTime.now().minusDays(100));
			femaleCalfWeanedOff.setAnimalTypeCD(Util.AnimalTypes.FEMALECALF);
			femaleCalfWeanedOff.setBreed(femaleCalf.getBreed());
			femaleCalfWeanedOff.setHerdJoiningDate(femaleCalfWeanedOff.getDateOfBirth());
			femaleCalfWeanedOff.setCreatedBy(new User("KASHIF"));
			femaleCalfWeanedOff.setCreatedDTTM(DateTime.now());
			femaleCalfWeanedOff.setUpdatedBy(femaleCalf.getCreatedBy());
			femaleCalfWeanedOff.setUpdatedDTTM(femaleCalf.getCreatedDTTM());
			
			
			LifecycleEvent event = new LifecycleEvent(femaleCalfWeanedOff.getOrgID(), 0, femaleCalfWeanedOff.getAnimalTag(),Util.LifeCycleEvents.WEANEDOFF,user,DateTime.now(),user,DateTime.now());			
			event.setEventTimeStamp(femaleCalfWeanedOff.getDateOfBirth().plusDays(60));
			event.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event.setCreatedBy(new User("KASHIF"));
			event.setCreatedDTTM(DateTime.now());
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
			assertEquals(Util.FeedCohortType.FEMALECALF,manager.getAnimalFeedCohortType(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(Util.FeedCohortType.FMLWNDOFF,manager.getAnimalFeedCohortType(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()).getFeedCohortLookupValue().getLookupValueCode());
			
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
			feedItem.setCreatedDTTM(DateTime.now());
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
			assertEquals(feedItem.getUpdatedDTTMSQLFormat(), item.getUpdatedDTTMSQLFormat());
			assertEquals(feedItem.getCreatedDTTMSQLFormat(), item.getCreatedDTTMSQLFormat());
			
			FeedCohort feedCohort = new FeedCohort(feedItem.getOrgID(), feedItem.getFeedCohortCD(),"");
			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, femaleCalf.getAnimalTag());
			assertTrue(plan != null && feedItem != null);
			assertEquals(feedItem.getFeedCohortCD().getLookupValueCode(), plan.getFeedCohort().getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(feedItem.getOrgID(), plan.getOrgID());
			
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				IMDLogger.log(it.next().getPersonalizedFeedMessage(), Util.INFO);
			}			
			
			
			
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
	void testFeedPlans() {
		String orgID = "IMD";
		String femaleCalfTag = "-999";
		String bullTag = "-999";
		FeedManager manager = new FeedManager();
		Dam femaleCalf;
		try {
			femaleCalf = createDam(orgID,femaleCalfTag,DateTime.now().minusDays(90), Util.AnimalTypes.FEMALECALF);
			Sire bull = new Sire(bullTag);
			
			LookupValues lv = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.FEMALECALF,Util.FeedCohortType.FEMALECALF,"");
			
			FeedCohort feedCohort = new FeedCohort(orgID, lv,"");
			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, femaleCalf.getAnimalTag());
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				IMDLogger.log(it.next().getPersonalizedFeedMessage(), Util.INFO);
			}			
			
			LookupValues lv2 = new LookupValues(Util.LookupValues.FEEDCOHORT,Util.FeedCohortType.BULL,Util.FeedCohortType.BULL,"");
			feedCohort = new FeedCohort(orgID, lv2,"");
			plan = manager.getPersonalizedFeedPlan(feedCohort, bull.getAnimalTag());
			assertTrue(plan!=null && plan.getFeedPlan() != null && !plan.getFeedPlan().isEmpty());
			
			it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				IMDLogger.log(it.next().getPersonalizedFeedMessage(), Util.INFO);
			}			
			
			
			// Clean up all the test records added.
	//		assertEquals(1,loader.deleteFeedPlanItem(feedItem, " AND START >= ? AND END >= ?", feedItem.getStart(), feedItem.getEnd()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()));
	//		assertEquals(1,anmLdr.deleteAnimal(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
	//		assertEquals(1,eventLoader.deleteAnimalLifecycleEvents(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()));
			
		} catch (IMDException e) {
			e.printStackTrace();
			fail("Exception");
		}

	}
	
	@Test
	void testNonExistentAnimal() {
		FeedManager manager = new FeedManager();
		try {
			manager.getAnimalFeedCohortType("IMD", "DUMMY");
			fail("The animal should not have existed in our records");
		} catch (IMDException ex) {
			assertTrue(ex.getMessage().indexOf("Animal not found") >= 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

}
