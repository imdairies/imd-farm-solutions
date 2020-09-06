package com.imd.performance;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.PerformanceMilestone;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
import com.imd.util.Util;

class CalvingRateMilestoneEvaluatorTest {

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
	void testNoRating() {
		String orgId = "IMD";
		String noStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal noStarFemale = TestDataCreationUtil.createTestAnimal(orgId, noStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*4), true);
			
			assertTrue(anmlLdr.deleteAnimal(noStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(noStarFemale.getOrgId(), noStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(noStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, noStarFemale.getOrgId(), noStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, noStarFemale.getOrgId(), noStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int noStarCalvingAvg = milestone.getOneStarThreshold().intValue() + 1;

			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg));
			
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg + noStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg + noStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(noStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, noStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + noStarCalvingAvg + noStarCalvingAvg));

			outcome = evl.evaluatePerformanceMilestone(milestone, noStarFemale.getOrgId(), noStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(noStarFemale));
			assertEquals(9,evtLdr.deleteAnimalLifecycleEvents(noStarFemale.getOrgId(), noStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}

	@Test
	void testNotEligible() {
		String orgId = "IMD";
		String youngFemaleTag = "-999";
		String youngMaleTag = "-998";
		String oldMaleTag = "-997";
		String oldFemaleTag = "-996";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal youngFemale = TestDataCreationUtil.createTestAnimal(orgId, youngFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())-1), true);
			
			Animal oldFemale = TestDataCreationUtil.createTestAnimal(orgId, oldFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*2), true);
			
			Animal youngMale = TestDataCreationUtil.createTestAnimal(orgId, youngMaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())-1), false);

			Animal oldMale = TestDataCreationUtil.createTestAnimal(orgId, oldMaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*2), false);
			
			assertTrue(anmlLdr.deleteAnimal(youngFemale) >= 0);
			assertTrue(anmlLdr.deleteAnimal(youngMale) >= 0);
			assertTrue(anmlLdr.deleteAnimal(oldMale) >= 0);
			assertTrue(anmlLdr.deleteAnimal(oldFemale) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(youngFemale));
			assertEquals(1, anmlLdr.insertAnimal(youngMale));
			assertEquals(1, anmlLdr.insertAnimal(oldMale));
			assertEquals(1, anmlLdr.insertAnimal(oldFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, youngFemale.getOrgId(), youngFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			outcome = evl.evaluatePerformanceMilestone(milestone, youngMale.getOrgId(), youngMale.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			outcome = evl.evaluatePerformanceMilestone(milestone, oldMale.getOrgId(), oldMale.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oldFemale.getOrgId(), oldFemale.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			assertEquals(1,anmlLdr.deleteAnimal(youngFemale));
			assertEquals(1, anmlLdr.deleteAnimal(youngMale));
			assertEquals(1, anmlLdr.deleteAnimal(oldMale));
			assertEquals(1, anmlLdr.deleteAnimal(oldFemale));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	@Test
	void testOneRating() {
		String orgId = "IMD";
		String oneStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal oneStarFemale = TestDataCreationUtil.createTestAnimal(orgId, oneStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*2), true);
			
			assertTrue(anmlLdr.deleteAnimal(oneStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(oneStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int oneStarCalvingAvg = milestone.getTwoStarThreshold().intValue() + 1;

			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + oneStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + oneStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + oneStarCalvingAvg));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(oneStarFemale));
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}


	@Test
	void testTwoRating() {
		String orgId = "IMD";
		String twoStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal twoStarFemale = TestDataCreationUtil.createTestAnimal(orgId, twoStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*2), true);
			
			assertTrue(anmlLdr.deleteAnimal(twoStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(twoStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int twoStarCalvingAvg = milestone.getThreeStarThreshold().intValue() + 1;

			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + twoStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + twoStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + twoStarCalvingAvg));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(twoStarFemale));
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}


	@Test
	void testThreeRating() {
		String orgId = "IMD";
		String threeStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal threeStarFemale = TestDataCreationUtil.createTestAnimal(orgId, threeStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*4), true);
			
			assertTrue(anmlLdr.deleteAnimal(threeStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(threeStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int threeStarCalvingAvg = milestone.getFourStarThreshold().intValue() + 1;

			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg));
			
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg + threeStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg + threeStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + threeStarCalvingAvg + threeStarCalvingAvg));

			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(threeStarFemale));
			assertEquals(9,evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}


	@Test
	void testFourRating() {
		String orgId = "IMD";
		String fourStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal fourStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fourStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*4), true);
			
			assertTrue(anmlLdr.deleteAnimal(fourStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(fourStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int fourStarCalvingAvg = milestone.getFiveStarThreshold().intValue() + 1;

			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg));
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg + fourStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg + fourStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fourStarCalvingAvg + fourStarCalvingAvg));

			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(fourStarFemale));
			assertEquals(9,evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}


	@Test
	void testFiveRating() {
		String orgId = "IMD";
		String fiveStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.CALVINGRATE).get(0);
			CalvingRateMilestoneEvaluator evl = new CalvingRateMilestoneEvaluator();
			
			Animal fiveStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fiveStarFemaleTag, 
					now.minusDays(Integer.parseInt(milestone.getAuxInfo1())*4), true);
			
			assertTrue(anmlLdr.deleteAnimal(fiveStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgId(), fiveStarFemale.getAnimalTag())>= 0);

			assertEquals(1, anmlLdr.insertAnimal(fiveStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgId(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			int firstCalvingAgeInDays = 365 + 275;
			
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays - 275 + 1));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays));

			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgId(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());
			
			int fiveStarCalvingAvg = milestone.getFiveStarThreshold().intValue();

			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg));
			
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg + fiveStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg + fiveStarCalvingAvg - 275));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays(firstCalvingAgeInDays + fiveStarCalvingAvg + fiveStarCalvingAvg));

			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgId(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			
			assertEquals(1,anmlLdr.deleteAnimal(fiveStarFemale));
			assertEquals(9,evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgId(), fiveStarFemale.getAnimalTag()));
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}

}


















