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
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;


class FirstHeatMilestoneEvaluatorTest {

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
	void testAnimalNotEligible() {
		PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();

		String orgID = "IMD";
		String oneStarTag = "-999";
		User user = new User("KASHIF");
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		assertEquals(1,milestones.size());
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int oneStarLimit = milestones.get(0).getOneStarThreshold().intValue();
			Animal notEligibleAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(oneStarLimit+10), false);
			
			assertTrue(anmlLdr.deleteAnimal(orgID, oneStarTag) >= 0);
			assertEquals(1,anmlLdr.insertAnimal(notEligibleAnimal));
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(notEligibleAnimal.getOrgID(), notEligibleAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(null,evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.ANIMAL_NOT_ELIGIBLE,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(0,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			notEligibleAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(oneStarLimit-1), true);
			
			assertTrue(anmlLdr.deleteAnimal(orgID, oneStarTag) >= 0);
			assertEquals(1,anmlLdr.insertAnimal(notEligibleAnimal));
			
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(notEligibleAnimal.getOrgID(), notEligibleAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(null,evaluatedMilestone.getEvaluationValue(),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.ANIMAL_NOT_ELIGIBLE,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(0,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
		} catch (Exception e) {
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
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int thisStarLimit = milestones.get(0).getTwoStarThreshold().intValue();
			int nextStarLimit = milestones.get(0).getThreeStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(thisStarLimit + 60), true);
			LifecycleEvent thisStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.HEAT,
					user, now, user, now);
			thisStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(nextStarLimit + 1));
			thisStarWtEvent1.setAuxField1Value("Y");
			LifecycleEvent prevStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT,
					user, now, user, now);
			prevStarWtEvent2.setEventTimeStamp(thisStarWtEvent1.getEventTimeStamp().plusDays(21));
			prevStarWtEvent2.setAuxField1Value("" + (thisStarLimit - 10));
			addAnimalAndMilestoneTestData(oneStarAnimal, thisStarWtEvent1, null);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(Util.getDaysBetween(thisStarWtEvent1.getEventTimeStamp(),oneStarAnimal.getDateOfBirth()),Integer.parseInt(evaluatedMilestone.getEvaluationValue()),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.TWO_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(1,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
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
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int thisStarLimit = milestones.get(0).getFiveStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(thisStarLimit + 60), true);
			LifecycleEvent thisStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.HEAT,
					user, now, user, now);
			thisStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(thisStarLimit));
			thisStarWtEvent1.setAuxField1Value("Y");
			LifecycleEvent prevStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT,
					user, now, user, now);
			prevStarWtEvent2.setEventTimeStamp(thisStarWtEvent1.getEventTimeStamp().plusDays(21));
			prevStarWtEvent2.setAuxField1Value("" + (thisStarLimit + 10));
			addAnimalAndMilestoneTestData(oneStarAnimal, thisStarWtEvent1, null);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(Util.getDaysBetween(thisStarWtEvent1.getEventTimeStamp(),oneStarAnimal.getDateOfBirth()),Integer.parseInt(evaluatedMilestone.getEvaluationValue()),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FIVE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(1,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
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
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int thisStarLimit = milestones.get(0).getFourStarThreshold().intValue();
			int nextStarLimit = milestones.get(0).getFiveStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(thisStarLimit + 60), true);
			LifecycleEvent thisStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.HEAT,
					user, now, user, now);
			thisStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(nextStarLimit + 1));
			thisStarWtEvent1.setAuxField1Value("Y");
			LifecycleEvent prevStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT,
					user, now, user, now);
			prevStarWtEvent2.setEventTimeStamp(thisStarWtEvent1.getEventTimeStamp().plusDays(21));
			prevStarWtEvent2.setAuxField1Value("" + (thisStarLimit - 10));
			addAnimalAndMilestoneTestData(oneStarAnimal, thisStarWtEvent1, null);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(Util.getDaysBetween(thisStarWtEvent1.getEventTimeStamp(),oneStarAnimal.getDateOfBirth()),Integer.parseInt(evaluatedMilestone.getEvaluationValue()),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.FOUR_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(1,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
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
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int thisStarLimit = milestones.get(0).getThreeStarThreshold().intValue();
			int nextStarLimit = milestones.get(0).getFourStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(thisStarLimit + 60), true);
			LifecycleEvent thisStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.HEAT,
					user, now, user, now);
			thisStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(nextStarLimit + 1));
			thisStarWtEvent1.setAuxField1Value("Y");
			LifecycleEvent prevStarWtEvent2 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.WEIGHT,
					user, now, user, now);
			prevStarWtEvent2.setEventTimeStamp(thisStarWtEvent1.getEventTimeStamp().plusDays(21));
			prevStarWtEvent2.setAuxField1Value("" + (thisStarLimit - 10));
			addAnimalAndMilestoneTestData(oneStarAnimal, thisStarWtEvent1, null);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(Util.getDaysBetween(thisStarWtEvent1.getEventTimeStamp(),oneStarAnimal.getDateOfBirth()),Integer.parseInt(evaluatedMilestone.getEvaluationValue()),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.THREE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(1,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
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
		
		List<PerformanceMilestone> milestones = perfLdr.retrieveSpecificPerformanceMilestone(orgID,Util.PerformanceMilestone.FIRSTHEAT);
		MilestoneEvaluator eval = new FirstHeatMilestoneEvaluator();
		try {
			int thisStarLimit = milestones.get(0).getOneStarThreshold().intValue();
			int nextStarLimit = milestones.get(0).getTwoStarThreshold().intValue();
			Animal oneStarAnimal = createTestAnimal(orgID,oneStarTag,now.minusDays(thisStarLimit + 60), true);
			LifecycleEvent oneStarWtEvent1 = new LifecycleEvent(orgID, 0, oneStarTag, Util.LifeCycleEvents.HEAT,
					user, now, user, now);
			oneStarWtEvent1.setEventTimeStamp(oneStarAnimal.getDateOfBirth().plusDays(nextStarLimit+1));
			oneStarWtEvent1.setAuxField1Value("Y");
			addAnimalAndMilestoneTestData(oneStarAnimal, oneStarWtEvent1, null);
			
			Iterator<PerformanceMilestone> milestoneIt = milestones.iterator();
			PerformanceMilestone perfmile = null;
			IMDLogger.loggingMode = Util.INFO;
			while (milestoneIt.hasNext()) {
				perfmile = milestoneIt.next();
				assertTrue(perfmile.isEnabled() && perfmile.isEnabledForOrg());
				PerformanceMilestone evaluatedMilestone = eval.evaluatePerformanceMilestone(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag(), Util.LanguageCode.ENG);
				assertEquals(Util.getDaysBetween(oneStarWtEvent1.getEventTimeStamp(),oneStarAnimal.getDateOfBirth()),Integer.parseInt(evaluatedMilestone.getEvaluationValue()),evaluatedMilestone.getEvaluationResultMessage());
				assertEquals((float)Util.StarRating.ONE_STAR,evaluatedMilestone.getStarRating().floatValue(),evaluatedMilestone.getEvaluationResultMessage());
			}
			assertEquals(1,evtLdr.deleteAnimalLifecycleEvents(orgID, oneStarTag));
			assertEquals(1,anmlLdr.deleteAnimal(orgID, oneStarTag));
			
			assertEquals(1,milestones.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception ");
		}
	}
	private void addAnimalAndMilestoneTestData(Animal oneStarAnimal, LifecycleEvent oneStarWtEvent1, LifecycleEvent oneStarWtEvent2) throws Exception {
		LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
		AnimalLoader anmlLdr = new AnimalLoader();
		anmlLdr.deleteAnimal(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag());
		anmlLdr.insertAnimal(oneStarAnimal);		
		evtLdr.deleteAnimalLifecycleEvents(oneStarAnimal.getOrgID(), oneStarAnimal.getAnimalTag());
		if (oneStarWtEvent1 != null)
			evtLdr.insertLifeCycleEvent(oneStarWtEvent1);
		if (oneStarWtEvent2 != null)
			evtLdr.insertLifeCycleEvent(oneStarWtEvent2);
	}
	public Animal createTestAnimal(String orgId, String animalTag, DateTime dob, boolean isFemale) throws Exception {
		Animal c000 = null;
		if (isFemale)
			c000 = new Dam(/*orgid*/orgId,/*tag*/animalTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		else
			c000 = new Sire(/*orgid*/orgId,/*tag*/animalTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
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




