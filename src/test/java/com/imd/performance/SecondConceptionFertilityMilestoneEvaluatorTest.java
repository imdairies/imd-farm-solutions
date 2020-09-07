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

class SecondConceptionFertilityMilestoneEvaluatorTest {

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
		DateTime dob = new DateTime(2017,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag, dob, false);
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(male.getOrgId(), male.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));
			assertEquals(1, anmlLdr.insertAnimal(female));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgId(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, female.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, female.getDateOfBirth().plusDays((14*30) + 1), "UNKNOWN","NO", "YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, female.getDateOfBirth().plusDays((14*30)+275));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, female.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, female.getDateOfBirth().plusDays((14*30)+275+91), "UNKNOWN","NO", "YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, female.getDateOfBirth().plusDays((14*30)+275+90+275));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertTrue(outcome.getStarRating() > Util.StarRating.ANIMAL_NOT_ELIGIBLE);

			evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			
			assertEquals(6, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(0, evtLdr.deleteAnimalLifecycleEvents(male.getOrgId(), male.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(male));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}	
	}
	
	@Test
	void testZeroStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", 
					Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63),
					"UNKNOWN",/*sexed semen*/"NO",
					/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			
			assertEquals(16, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}

	@Test
	void testOneStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", 
					Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63),
					"UNKNOWN",/*sexed semen*/"NO",
					/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());
			IMDLogger.log("Successful Inseminated date: " + secondHeatDate.plusDays(63), Util.INFO);
			assertEquals("[3NY]", outcome.getEvaluationValue());
			
			assertEquals(14, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}

	@Test
	void testFourStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(21),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(21).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			IMDLogger.log("Successful Inseminated date: " + secondHeatDate.plusDays(63), Util.INFO);
			assertEquals("[2NY]", outcome.getEvaluationValue(), "Result should be 2 inseminations, Successful insemination on NORMAL semen, inseminated in HARSH weather");
			
			assertEquals(12, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	@Test
	void testThreeStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63).plusMonths(4));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			IMDLogger.log("Successful Inseminated date: " + secondHeatDate.plusDays(63), Util.INFO);
			assertEquals("[3YY]", outcome.getEvaluationValue(), "Result should be 3 inseminations, Successful insemination on SEXED semen, inseminated in HARSH weather");
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
			
			assertEquals(14, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	@Test
	void testTwoStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63).plusMonths(4));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", 
					Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63).plusMonths(4),
					"UNKNOWN",/*sexed semen*/"YES",
					/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(4).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			IMDLogger.log("Successful Inseminated date: " + secondHeatDate.plusDays(63), Util.INFO);
			assertEquals("[3YN]", outcome.getEvaluationValue(), "Result is 3 inseminations, Successful insemination on SEXED semen, inseminated in favourable weather");
			
			assertEquals(14, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	@Test
	void testFiveStarRating() {
		String orgId = "IMD";
		String femaleTag = "-999";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONFERTILITY,2);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag())>= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusDays(42).plusMonths(9));

			DateTime secondHeatDate = firstHeatDate.plusDays(42).plusMonths(12);
			
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(9));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgId(), female.getAnimalTag(), Util.LanguageCode.ENG);
			IMDLogger.log("Successful Inseminated date: " + secondHeatDate.plusDays(63), Util.INFO);
			assertEquals("[2YY]", outcome.getEvaluationValue(), "Result is 2 inseminations, Successful insemination on SEXED semen, inseminated in HARSH weather");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			
			assertEquals(12, evtLdr.deleteAnimalLifecycleEvents(female.getOrgId(), female.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}

}

