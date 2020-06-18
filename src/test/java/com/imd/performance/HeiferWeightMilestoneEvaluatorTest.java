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
import com.imd.performance.HeiferWeightMilestoneEvaluator;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;


class HeiferWeightMilestoneEvaluatorTest {

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
	void testYoungerThanAgeLimitEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int oneStarLimit = milestones.get(0).getOneStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit-1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (oneStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+2));
			oneStarWtEvent2.setAuxField1Value("" + (oneStarLimit - 10));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(null,evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertTrue(evaluatedMilestone.getEvaluationResultMessage().indexOf("younger than") >= 0);
				assertEquals((float)Util.StarRating.ANIMAL_NOT_ELIGIBLE,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception ");
		}
	}
	
	@Test
	void testNoStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int oneStarLimit = milestones.get(0).getOneStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (oneStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+2));
			oneStarWtEvent2.setAuxField1Value("" + (oneStarLimit - 10));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("296.67",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.NO_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception ");
		}
	}
	@Test
	void testOneStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int oneStarLimit = milestones.get(0).getOneStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (oneStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+2));
			oneStarWtEvent2.setAuxField1Value("" + (oneStarLimit + 10));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("303.33",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.ONE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception ");
		}
	}
	@Test
	void testTwoStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int twoStarLimit = milestones.get(0).getTwoStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (twoStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+1));
			oneStarWtEvent2.setAuxField1Value("" + (twoStarLimit + 5));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("307.5",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.TWO_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception ");
		}
	}
	@Test
	void testThreeStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int threeStarLimit = milestones.get(0).getThreeStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (threeStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+1));
			oneStarWtEvent2.setAuxField1Value("" + (threeStarLimit + 5));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("312.5",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.THREE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception ");
		}
	}

	@Test
	void testFourStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int fourStarLimit = milestones.get(0).getFourStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (fourStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+1));
			oneStarWtEvent2.setAuxField1Value("" + (fourStarLimit + 4));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("317",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FOUR_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception ");
		}
	}

	@Test
	void testFiveStarEvaluation() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.HEIFERWEIGHT);
		MilestoneEvaluator eval = new HeiferWeightMilestoneEvaluator();
		try {
			int minAgeLimit = Integer.parseInt(milestones.get(0).getAuxInfo1());
			int fiveStarLimit = milestones.get(0).getFiveStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(minAgeLimit+1));
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit-1));
			oneStarWtEvent1.setAuxField1Value("" + (fiveStarLimit));
			LifecycleEvent oneStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT, 
					user, now, user, now);
			oneStarWtEvent2.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(minAgeLimit+1));
			oneStarWtEvent2.setAuxField1Value("" + (fiveStarLimit + 5));
			
			addAnimalMilestoneTestData(oneStarAnimal, oneStarWtEvent1, oneStarWtEvent2);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals("322.5",evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FIVE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());

			}
			assertEquals(2,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception ");
		}
	}

	
	private void addAnimalMilestoneTestData(Animal oneStarAnimal, LifecycleEvent oneStarWtEvent1, LifecycleEvent oneStarWtEvent2) throws Exception {
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();
		anmlLdr.deleteAnimal(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag());
		anmlLdr.insertAnimal(oneStarAnimal);		
		evtLdr.deleteAnimalLifecycleEvents(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag());
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



