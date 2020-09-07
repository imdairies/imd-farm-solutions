package com.imd.performance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

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
import com.imd.dto.PerformanceMilestone;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;


class CalfWeightMilestoneEvaluatorTest {

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
	void testAllPossibleStarRatings() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		String twoStarTag = "-998";
		String threeStarTag = "-997";
		String fourStarTag = "-996";
		String fiveStarTag = "-995";
		String noStarTag = "-994";
		String neverWeighedAnimalTag = "-993";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.CALFWEIGHT);
		MilestoneEvaluator eval = new CalfWeightMilestoneEvaluator();
		try {
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusYears(2));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(60));
			oneStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(120));
			oneStarWtEvent2.setAuxField1Value("120");
			
			Animal twoStarAnimal = createTestAnimal(orgID,twoStarTag,now.minusYears(3));
			LifecycleEvent twoStarWtEvent1 = new LifecycleEvent(orgID, 0, twoStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			twoStarWtEvent1.setEventTimeStamp(twoStarAnimal.getDateOfBirth().plusDays(60));
			twoStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent twoStarWtEvent2 = new LifecycleEvent(orgID, 0, twoStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			twoStarWtEvent2.setEventTimeStamp(twoStarAnimal.getDateOfBirth().plusDays(120));
			twoStarWtEvent2.setAuxField1Value("132");
			
			Animal threeStarAnimal = createTestAnimal(orgID,threeStarTag,now.minusYears(4));
			LifecycleEvent threeStarWtEvent1 = new LifecycleEvent(orgID, 0, threeStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			threeStarWtEvent1.setEventTimeStamp(threeStarAnimal.getDateOfBirth().plusDays(50));
			threeStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent threeStarWtEvent2 = new LifecycleEvent(orgID, 0, threeStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			threeStarWtEvent2.setEventTimeStamp(threeStarAnimal.getDateOfBirth().plusDays(120));
			threeStarWtEvent2.setAuxField1Value("132");

			Animal fourStarAnimal = createTestAnimal(orgID,fourStarTag,now.minusYears(5));
			LifecycleEvent fourStarWtEvent1 = new LifecycleEvent(orgID, 0, fourStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			fourStarWtEvent1.setEventTimeStamp(fourStarAnimal.getDateOfBirth().plusDays(50));
			fourStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent fourStarWtEvent2 = new LifecycleEvent(orgID, 0, fourStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			fourStarWtEvent2.setEventTimeStamp(fourStarAnimal.getDateOfBirth().plusDays(112));
			fourStarWtEvent2.setAuxField1Value("132");

			Animal fiveStarAnimal = createTestAnimal(orgID,fiveStarTag,now.minusYears(6));
			LifecycleEvent fiveStarWtEvent1 = new LifecycleEvent(orgID, 0, fiveStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			fiveStarWtEvent1.setEventTimeStamp(fiveStarAnimal.getDateOfBirth().plusDays(35));
			fiveStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent fiveStarWtEvent2 = new LifecycleEvent(orgID, 0, fiveStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			fiveStarWtEvent2.setEventTimeStamp(fiveStarAnimal.getDateOfBirth().plusDays(112));
			fiveStarWtEvent2.setAuxField1Value("132");
			
			Animal noStarAnimal = createTestAnimal(orgID,noStarTag,now.minusYears(7));
			LifecycleEvent noStarWtEvent1 = new LifecycleEvent(orgID, 0, noStarAnimal.getAnimalTag(), Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			noStarWtEvent1.setEventTimeStamp(noStarAnimal.getDateOfBirth().plusDays(82));
			noStarWtEvent1.setAuxField1Value("60");
			LifecycleEvent noStarWtEvent2 = new LifecycleEvent(orgID, 0, noStarAnimal.getAnimalTag(), Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			noStarWtEvent2.setEventTimeStamp(noStarAnimal.getDateOfBirth().plusDays(112));
			noStarWtEvent2.setAuxField1Value("132");

			Animal neverWeighedAnimal = createTestAnimal(orgID,neverWeighedAnimalTag,now.minusYears(9));

			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, twoStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, threeStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, fourStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, fiveStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, noStarTag) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(orgID, neverWeighedAnimal.getAnimalTag()) >= 0);
			assertTrue(anmlLdr.deleteAnimal(neverWeighedAnimal.getOrgId(),neverWeighedAnimal.getAnimalTag()) >=0);
			assertEquals(1,anmlLdr.insertAnimal(neverWeighedAnimal));
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			addAnimalMilestoneTestData(twoStarAnimal, twoStarWtEvent1, twoStarWtEvent2);
			addAnimalMilestoneTestData(threeStarAnimal, threeStarWtEvent1, threeStarWtEvent2);
			addAnimalMilestoneTestData(fourStarAnimal, fourStarWtEvent1, fourStarWtEvent2);
			addAnimalMilestoneTestData(fiveStarAnimal, fiveStarWtEvent1, fiveStarWtEvent2);
			addAnimalMilestoneTestData(noStarAnimal, noStarWtEvent1, noStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgId(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("90",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.ONE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(twoStarAnimal.getOrgId(), twoStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("96",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.TWO_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(threeStarAnimal.getOrgId(), threeStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("101.14",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.THREE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(fourStarAnimal.getOrgId(), fourStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("106.45",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FOUR_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(fiveStarAnimal.getOrgId(), fiveStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("111.43",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FIVE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(noStarAnimal.getOrgId(), noStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("79.2",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.NO_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

				evaluatedMilestone = eval.evaluatePerformanceMilestone(neverWeighedAnimal.getOrgId(), neverWeighedAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals((float)Util.StarRating.COULD_NOT_COMPUTE_RATING,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, twoStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, twoStarTag));
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, threeStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, threeStarTag));
			
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, fourStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, fourStarTag));
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, fiveStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, fiveStarTag));
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, noStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, noStarTag));
			assertEquals(0,evtLdr.deleteAnimalLifecycleEvents(orgID, neverWeighedAnimalTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, neverWeighedAnimalTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception ");
		}
	}

	private void addAnimalMilestoneTestData(Animal oneStarAnimal, LifecycleEvent oneStarWtEvent1, LifecycleEvent oneStarWtEvent2) throws Exception {
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();
		anmlLdr.deleteAnimal(oneStarAnimal.getOrgId(), oneStarAnimal.getAnimalTag());
		anmlLdr.insertAnimal(oneStarAnimal);		
		evtLdr.deleteAnimalLifecycleEvents(oneStarAnimal.getOrgId(), oneStarAnimal.getAnimalTag());
		evtLdr.insertLifeCycleEvent(oneStarWtEvent1);
		evtLdr.insertLifeCycleEvent(oneStarWtEvent2);
	}
	public Animal createTestAnimal(String orgId, String animalTag, DateTime dob) throws Exception {
		Dam c000 = new Dam(/*orgid*/orgId,/*tag*/animalTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/4.png");
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setHerdJoiningDate(DateTime.now().minusDays(10));
		c000.setHerdLeavingDate(null);
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
	}
}




