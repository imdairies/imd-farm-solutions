package com.imd.controller.feed;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Dam;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.LifecycleEvent;
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
			assertEquals(Util.FeedCohortType.BULL,manager.getAnimalFeedCohortType(bull.getOrgID(), bull.getAnimalTag()).getFeedCohortTypeCD());
			assertEquals(Util.FeedCohortType.MALECALF,manager.getAnimalFeedCohortType(maleCalf.getOrgID(), maleCalf.getAnimalTag()).getFeedCohortTypeCD());
			
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
	
	
	@Test
	void testFemaleCalves() {
		FeedManager manager = new FeedManager();
		AnimalLoader anmLdr = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
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
			
			
			LifecycleEvent event = new LifecycleEvent(femaleCalfWeanedOff.getOrgID(), 0, femaleCalfWeanedOff.getAnimalTag(),Util.LifeCycleEvents.WEANEDOFF);			
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
			assertEquals(Util.FeedCohortType.FEMALECALF,manager.getAnimalFeedCohortType(femaleCalf.getOrgID(), femaleCalf.getAnimalTag()).getFeedCohortTypeCD());
			assertEquals(Util.FeedCohortType.FEMALEWEANEDOFF,manager.getAnimalFeedCohortType(femaleCalfWeanedOff.getOrgID(), femaleCalfWeanedOff.getAnimalTag()).getFeedCohortTypeCD());
			
			
			// Create FEMALECALF feedplan
			FeedLoader loader = new FeedLoader();
			FeedItem feedItem = new FeedItem();
			feedItem.setOrgID("IMD");
			feedItem.setFeedItemCD("TST_ALFHAY");
			feedItem.setFeedCohortCD(Util.FeedCohortType.FEMALECALF);
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

			
			assertEquals(feedItem.getFeedItemCD(), item.getFeedItemCD());
			assertEquals(feedItem.getFeedCohortCD(), item.getFeedCohortCD());
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
			FeedPlan plan = manager.getPersonalizedFeedPlan(feedCohort, ageInDays, ageInDays, femaleCalf);
			assertEquals(feedItem.getFeedCohortCD(), plan.getFeedCohort().getFeedCohortTypeCD());
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
			fail(ex.getMessage());
		}
	}	
	
	@Test
	void test() {
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
