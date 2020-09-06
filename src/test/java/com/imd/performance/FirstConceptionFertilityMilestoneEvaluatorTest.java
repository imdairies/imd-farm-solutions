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

class FirstConceptionFertilityMilestoneEvaluatorTest {

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
	void testNotEligible() {
		String orgId = "IMD";
		String maleTag = "-998";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag, dob, false);
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(male.getOrgId(), male.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));
			assertEquals(1, anmlLdr.insertAnimal(female));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgId(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, female.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, female.getDateOfBirth().plusDays((14*30) + 1),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, female.getDateOfBirth().plusDays((14*30)+275));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertTrue(outcome.getStarRating() > 0, "Rating: " + outcome.getStarRating());
			
			
			evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(male));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(3, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(0, evtLdr.deleteAnimalLifecycleEvents(male.getOrgId(), male.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testZeroRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(7, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testOneStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(22);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","YES","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(7, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testTwoStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","YES","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(7, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testThreeStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(5, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testFourRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","YES","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			
			assertEquals(5, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(3, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}


	@Test
	void testFiveStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","YES","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(4, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	@Test
	void testUnCalvedHeiferStarRating() {
		String orgId = "IMD";
		String heiferTag = "-999";
		DateTime now = new DateTime(2018,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONFERTILITY,1);
			
			Animal heifer = TestDataCreationUtil.createTestAnimal(orgId, heiferTag, now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(heifer) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(heifer.getOrgId(), heifer.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(heifer));

			DateTime firstHeatDate = heifer.getDateOfBirth().plusMonths(22);

//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(heifer.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(heifer.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","NO","NO",null);
			TestDataCreationUtil.insertEvent(heifer.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(heifer.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
//			TestDataCreationUtil.insertEvent(heifer.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, heifer.getOrgId(), heifer.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			assertEquals("[2NN]", outcome.getEvaluationValue());
			
			assertEquals(1, anmlLdr.deleteAnimal(heifer));
			assertEquals(4, evtLdr.deleteAnimalLifecycleEvents(heifer.getOrgId(), heifer.getAnimalTag()));

//			evl = new SpecifiedLactationFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
//			outcome = evl.evaluatePerformanceMilestone(milestone, heifer.getOrgID(), "034", Util.LanguageCode.ENG);
			
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
}



