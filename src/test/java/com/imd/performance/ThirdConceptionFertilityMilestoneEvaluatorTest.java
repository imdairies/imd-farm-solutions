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

class ThirdConceptionFertilityMilestoneEvaluatorTest {

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
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag, dob, false);
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgID(), female.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(male.getOrgID(), male.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));
			assertEquals(1, anmlLdr.insertAnimal(female));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgID(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating(), " Male animal not eligible for this");
			
			DateTime firstHeat = female.getDateOfBirth().plusDays(14*30);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeat);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeat.plusDays(1), "UNKNOWN","NO", "YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeat.plusMonths(9));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgID(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating(), " Only one calving not eligible for this");
			
			DateTime secondHeat = firstHeat.plusMonths(12);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeat);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeat.plusDays(1), "UNKNOWN","NO", "YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeat.plusMonths(9));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgID(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE,outcome.getStarRating(), " Two clavings not eligible for this");

			DateTime thirdHeat = secondHeat.plusMonths(12);

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeat);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeat,"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeat.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeat.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeat.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeat.plusDays(42),"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeat.plusDays(42).plusMonths(9));

			outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgID(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertTrue(outcome.getStarRating() >= 0, "Three inseminations animal is eligible");
			
			
			assertEquals(1, anmlLdr.deleteAnimal(male));
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(13, evtLdr.deleteAnimalLifecycleEvents(female.getOrgID(), female.getAnimalTag()));
			assertEquals(0, evtLdr.deleteAnimalLifecycleEvents(male.getOrgID(), male.getAnimalTag()));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female = TestDataCreationUtil.createTestAnimal(orgId, femaleTag, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female.getOrgID(), female.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female));

			DateTime firstHeatDate = female.getDateOfBirth().plusMonths(17);
			DateTime secondHeatDate = firstHeatDate.plusMonths(13);
			DateTime thirdHeatDate = secondHeatDate.plusDays(63).plusMonths(12);

//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","YES","NO",null);
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate.plusDays(21));
//			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate.plusDays(21),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate.plusMonths(9));

			
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate.plusDays(63));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate.plusDays(21));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate.plusDays(42));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate.plusDays(63));
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate.plusDays(63).plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female.getOrgID(), female.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			IMDLogger.log(outcome.getEvaluationValue(), Util.INFO);
			
			assertEquals(1, anmlLdr.deleteAnimal(female));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female.getOrgID(), female.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}

	@Test
	void testOneStarRating() {
		String orgId = "IMD";
		String femaleTag1 = "-999";
		String femaleTag2 = "-998";
		String femaleTag3 = "-997";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female1 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, dob, true);
			Animal female2 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, dob, true);
			Animal female3 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag3, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female1) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female2) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female3) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female1));
			assertEquals(1, anmlLdr.insertAnimal(female2));
			assertEquals(1, anmlLdr.insertAnimal(female3));

			// will conceive in harsh weather
			DateTime firstHeatDate1 = female1.getDateOfBirth().plusMonths(13);
			DateTime secondHeatDate1 = firstHeatDate1.plusMonths(13);
			DateTime thirdHeatDate1 = secondHeatDate1.plusDays(63).plusMonths(12);

			// will conceive in harsh weather
			DateTime firstHeatDate2 = female2.getDateOfBirth().plusMonths(15);
			DateTime secondHeatDate2 = firstHeatDate2.plusMonths(13);
			DateTime thirdHeatDate2 = secondHeatDate2.plusDays(63).plusMonths(12);

			// will conceive in favourable weather
			DateTime firstHeatDate3 = female3.getDateOfBirth().plusMonths(22);
			DateTime secondHeatDate3 = firstHeatDate3.plusMonths(13);
			DateTime thirdHeatDate3 = secondHeatDate3.plusDays(63).plusMonths(12);
			
			
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate1,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate1.plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate1.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate1.plusDays(63).plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female1.getOrgID(), female1.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4YY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate1.plusDays(63)) + ")");
			assertNotEquals(Util.StarRating.ONE_STAR, outcome.getStarRating(), outcome.getStarRating()+"");// should be two star
			

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate2,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate2.plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate2.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate2.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female2.getOrgID(), female2.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4NY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be Normal semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate2.plusDays(63)) + ")");
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());

			
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate3,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate3.plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate3.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate3.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female3.getOrgID(), female3.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4YN]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate3.plusDays(63)) + ")");
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());

			assertEquals(1, anmlLdr.deleteAnimal(female1));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female2));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female3));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag()));
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}

	@Test
	void testTwoStarRating() {
		String orgId = "IMD";
		String femaleTag1 = "-999";
		String femaleTag2 = "-998";
		String femaleTag3 = "-997";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female1 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, dob, true);
			Animal female2 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, dob, true);
			Animal female3 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag3, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female1) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female2) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female3) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female1));
			assertEquals(1, anmlLdr.insertAnimal(female2));
			assertEquals(1, anmlLdr.insertAnimal(female3));

			// will conceive in harsh weather
			DateTime firstHeatDate1 = female1.getDateOfBirth().plusMonths(13);
			DateTime secondHeatDate1 = firstHeatDate1.plusMonths(13);
			DateTime thirdHeatDate1 = secondHeatDate1.plusDays(63).plusMonths(12);

			// will conceive in harsh weather
			DateTime firstHeatDate2 = female2.getDateOfBirth().plusMonths(15);
			DateTime secondHeatDate2 = firstHeatDate2.plusMonths(13);
			DateTime thirdHeatDate2 = secondHeatDate2.plusDays(63).plusMonths(12);

			// will conceive in favourable weather
			DateTime firstHeatDate3 = female3.getDateOfBirth().plusMonths(22);
			DateTime secondHeatDate3 = firstHeatDate3.plusMonths(13);
			DateTime thirdHeatDate3 = secondHeatDate3.plusDays(63).plusMonths(12);
			
			
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate1,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate1.plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate1.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate1.plusDays(63).plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female1.getOrgID(), female1.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4YY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate1.plusDays(63)) + ")");
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating(), outcome.getStarRating()+"");
			

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate2,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate2.plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate2.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2);
//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate2.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female2.getOrgID(), female2.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[3NY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be Normal semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate2.plusDays(63)) + ")");
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());

			
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate3,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate3.plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate3.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3);
//			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate3.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female3.getOrgID(), female3.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[3NN]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate3.plusDays(63)) + ")");
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());

			assertEquals(1, anmlLdr.deleteAnimal(female1));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female2));
			assertEquals(19, evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female3));
			assertEquals(19, evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag()));
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}
	@Test
	void testThreeStarRating() {
		String orgId = "IMD";
		String femaleTag1 = "-999";
		String femaleTag2 = "-998";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female1 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, dob, true);
			Animal female2 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female1) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female2) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female1));
			assertEquals(1, anmlLdr.insertAnimal(female2));

			// will conceive in harsh weather
			DateTime firstHeatDate1 = female1.getDateOfBirth().plusMonths(13);
			DateTime secondHeatDate1 = firstHeatDate1.plusMonths(13);
			DateTime thirdHeatDate1 = secondHeatDate1.plusDays(63).plusMonths(12);

			// will conceive in favourable weather
			DateTime firstHeatDate2 = female2.getDateOfBirth().plusMonths(20);
			DateTime secondHeatDate2 = firstHeatDate2.plusMonths(13);
			DateTime thirdHeatDate2 = secondHeatDate2.plusDays(63).plusMonths(12);
			
			
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate1,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate1.plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate1.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate1.plusDays(63).plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female1.getOrgID(), female1.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4YY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate1.plusDays(63)) + ")");
			assertNotEquals(Util.StarRating.THREE_STAR, outcome.getStarRating(), outcome.getStarRating()+"");
			

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate2,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate2.plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate2.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2);
//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate2.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female2.getOrgID(), female2.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[3YN]", outcome.getEvaluationValue(),"Should have taken 3 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate2.plusDays(63)) + ")");
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());

			assertEquals(1, anmlLdr.deleteAnimal(female1));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female2));
			assertEquals(19, evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag()));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	@Test
	void testFourStarRating() {
		String orgId = "IMD";
		String femaleTag1 = "-999";
		String femaleTag2 = "-998";
		String femaleTag3 = "-997";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female1 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, dob, true);
			Animal female2 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, dob, true);
			Animal female3 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag3, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female1) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female2) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female3) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female1));
			assertEquals(1, anmlLdr.insertAnimal(female2));
			assertEquals(1, anmlLdr.insertAnimal(female3));

			// will conceive in harsh weather
			DateTime firstHeatDate1 = female1.getDateOfBirth().plusMonths(13);
			DateTime secondHeatDate1 = firstHeatDate1.plusMonths(13);
			DateTime thirdHeatDate1 = secondHeatDate1.plusDays(63).plusMonths(12);

			// will conceive in favourable weather
			DateTime firstHeatDate2 = female2.getDateOfBirth().plusMonths(22);
			DateTime secondHeatDate2 = firstHeatDate2.plusMonths(13);
			DateTime thirdHeatDate2 = secondHeatDate2.plusDays(63).plusMonths(12);

			// will conceive in HARSH weather
			DateTime firstHeatDate3 = female3.getDateOfBirth().plusMonths(12);
			DateTime secondHeatDate3 = firstHeatDate3.plusMonths(13);
			DateTime thirdHeatDate3 = secondHeatDate3.plusDays(63).plusMonths(12);
			
			
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate1,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate1.plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate1.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate1.plusDays(63).plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female1.getOrgID(), female1.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[4YY]", outcome.getEvaluationValue(),"Should have taken 4 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate1.plusDays(63)) + ")");
			assertNotEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating(), outcome.getStarRating()+"");
			

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate2,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate2.plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate2.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2);
//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(21));
//			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate2.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female2.getOrgID(), female2.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[2NN]", outcome.getEvaluationValue(),"Should have taken 2 attempts to conceive and the last one should be Normal semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate2.plusDays(63)) + ")");
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());

			
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate3,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate3.plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate3.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3);
//			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate3.plusDays(63).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female3.getOrgID(), female3.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[3YY]", outcome.getEvaluationValue(),"Should have taken 3 attempts to conceive and the last one should be sexed semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate3.plusDays(63)) + ")");
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());

			assertEquals(1, anmlLdr.deleteAnimal(female1));
			assertEquals(21, evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female2));
			assertEquals(17, evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female3));
			assertEquals(19, evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag()));
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}
	@Test
	void testFiveStarRating() {
		String orgId = "IMD";
		String femaleTag1 = "-999";
		String femaleTag2 = "-998";
		String femaleTag3 = "-997";
		String femaleTag4 = "-996";
		String femaleTag5 = "-995";
		String femaleTag6 = "-994";
		String femaleTag7 = "-993";
		DateTime dob = new DateTime(2016,1,1,0,0,IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.THIRDLACTATIONFERTILITY).get(0);
			SpecifiedConceptionFertilityMilestoneEvaluator evl = new SpecifiedConceptionFertilityMilestoneEvaluator(Util.PerformanceMilestone.THIRDLACTATIONFERTILITY,3);
			
			Animal female1 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, dob, true);
			Animal female2 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, dob, true);
			Animal female3 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag3, dob, true);
			Animal female4 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag4, dob, true);
			Animal female5 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag5, dob, true);
			Animal female6 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag6, dob, true);
			Animal female7 = TestDataCreationUtil.createTestAnimal(orgId, femaleTag7, dob, true);
			
			assertTrue(anmlLdr.deleteAnimal(female1) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female2) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female3) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female4) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female5) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female6) >= 0);
			assertTrue(anmlLdr.deleteAnimal(female7) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female4.getOrgID(), female4.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female5.getOrgID(), female5.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female6.getOrgID(), female6.getAnimalTag())>= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(female7.getOrgID(), female7.getAnimalTag())>= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(female1));
			assertEquals(1, anmlLdr.insertAnimal(female2));
			assertEquals(1, anmlLdr.insertAnimal(female3));
			assertEquals(1, anmlLdr.insertAnimal(female4));
			assertEquals(1, anmlLdr.insertAnimal(female5));
			assertEquals(1, anmlLdr.insertAnimal(female6));
			assertEquals(1, anmlLdr.insertAnimal(female7));

			DateTime firstHeatDate1 = female1.getDateOfBirth().plusMonths(15);
			DateTime secondHeatDate1 = firstHeatDate1.plusMonths(13);
			DateTime thirdHeatDate1 = secondHeatDate1.plusDays(63).plusMonths(12);

			DateTime firstHeatDate2 = female2.getDateOfBirth().plusMonths(22);
			DateTime secondHeatDate2 = firstHeatDate2.plusMonths(13);
			DateTime thirdHeatDate2 = secondHeatDate2.plusDays(63).plusMonths(12);

			DateTime firstHeatDate3 = female3.getDateOfBirth().plusMonths(14);
			DateTime secondHeatDate3 = firstHeatDate3.plusMonths(13);
			DateTime thirdHeatDate3 = secondHeatDate3.plusDays(63).plusMonths(12);
			
			DateTime firstHeatDate4 = female4.getDateOfBirth().plusMonths(15);
			DateTime secondHeatDate4 = firstHeatDate4.plusMonths(13);
			DateTime thirdHeatDate4 = secondHeatDate4.plusDays(63).plusMonths(12);

			DateTime firstHeatDate5 = female5.getDateOfBirth().plusMonths(22);
			DateTime secondHeatDate5 = firstHeatDate5.plusMonths(13);
			DateTime thirdHeatDate5 = secondHeatDate5.plusDays(63).plusMonths(12);

			DateTime firstHeatDate6 = female6.getDateOfBirth().plusMonths(12);
			DateTime secondHeatDate6 = firstHeatDate6.plusMonths(13);
			DateTime thirdHeatDate6 = secondHeatDate6.plusDays(63).plusMonths(12);
			
			DateTime firstHeatDate7 = female7.getDateOfBirth().plusMonths(14);
			DateTime secondHeatDate7 = firstHeatDate7.plusMonths(13);
			DateTime thirdHeatDate7 = secondHeatDate7.plusDays(63).plusMonths(12);

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate1,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate1.plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(21));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(42));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate1.plusDays(63));
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate1.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate1.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate1);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate1,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female1.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate1.plusMonths(9));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, female1.getOrgID(), female1.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[1YY]", outcome.getEvaluationValue(),"Should have taken 1 attempt to conceive and the last one should be sexed semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate1) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating(), outcome.getStarRating()+"");
			

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate2,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate2.plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(21));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(42));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate2.plusDays(63));
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate2.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate2.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate2);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate2.plusDays(1),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female2.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate2.plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female2.getOrgID(), female2.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[1YN]", outcome.getEvaluationValue(),"Should have taken 2 attempts to conceive and the last one should be Normal semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate2.plusDays(1)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());

			
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate3,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate3.plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(21));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(42));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate3.plusDays(63));
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate3.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate3.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate3);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate3.plusDays(1),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female3.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate3.plusDays(1).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female3.getOrgID(), female3.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[1NY]", outcome.getEvaluationValue(),"Should have taken 1 attempt to conceive and the last one should be normal semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate3.plusDays(1)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());

			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate4);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate4,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate4.plusMonths(9));

			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate4);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate4,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate4.plusDays(21));
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate4.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate4.plusDays(42));
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate4.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate4.plusDays(63));
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate4.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate4.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate4);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate4.plusDays(1),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate4.plusDays(21));
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate4.plusDays(22),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female4.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate4.plusDays(22).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female4.getOrgID(), female4.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[2YY]", outcome.getEvaluationValue(),"Should have taken 2 attempts to conceive and the last one should be sexed semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate4.plusDays(22)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());

			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate5);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate5,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate5.plusMonths(9));

			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate5);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate5,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate5.plusDays(21));
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate5.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate5.plusDays(42));
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate5.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate5.plusDays(63));
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate5.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate5.plusDays(63).plusMonths(9));

//			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate5);
//			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate5.plusDays(1),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate5);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate5.plusDays(21),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female5.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate5.plusDays(21).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female5.getOrgID(), female5.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[1NN]", outcome.getEvaluationValue(),"Should have taken 1 attempt to conceive and the last one should be normal semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate5.plusDays(21)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());


			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate6);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate6,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate6.plusMonths(9));

			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate6);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate6,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate6.plusDays(21));
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate6.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate6.plusDays(42));
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate6.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate6.plusDays(63));
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate6.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate6.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate6);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate6.plusDays(1),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate6.plusDays(21));
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate6.plusDays(22),"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female6.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate6.plusDays(22).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female6.getOrgID(), female6.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[2YN]", outcome.getEvaluationValue(),"Should have taken 2 attempts to conceive and the last one should be sexed semen in favourable weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate6.plusDays(22)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatDate7);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatDate7,"UNKNOWN","NO","YES",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatDate7.plusMonths(9));

			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate7);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate7,"UNKNOWN",/*sexed semen*/"YES",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate7.plusDays(21));
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate7.plusDays(22),"UNKNOWN","YES","NO",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate7.plusDays(42));
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate7.plusDays(43),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"NO",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, secondHeatDate7.plusDays(63));
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, secondHeatDate7.plusDays(63),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, secondHeatDate7.plusDays(63).plusMonths(9));

			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate7);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate7.plusDays(1),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, thirdHeatDate7.plusDays(21));
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, thirdHeatDate7.plusDays(22),"UNKNOWN",/*sexed semen*/"NO",/* insemination successful*/"YES",null);
			TestDataCreationUtil.insertEvent(female7.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, thirdHeatDate7.plusDays(22).plusMonths(9));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, female7.getOrgID(), female7.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals("[2NY]", outcome.getEvaluationValue(),"Should have taken 2 attempts to conceive and the last one should be normal semen in HARSH weather (Inseminated on: " + Util.getDateInSQLFormat(thirdHeatDate7.plusDays(22)) + ")");
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			
		

			assertEquals(1, anmlLdr.deleteAnimal(female1));
			assertEquals(15, evtLdr.deleteAnimalLifecycleEvents(female1.getOrgID(), female1.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female2));
			assertEquals(15, evtLdr.deleteAnimalLifecycleEvents(female2.getOrgID(), female2.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female3));
			assertEquals(15, evtLdr.deleteAnimalLifecycleEvents(female3.getOrgID(), female3.getAnimalTag()));
			
			assertEquals(1, anmlLdr.deleteAnimal(female4));
			assertEquals(17, evtLdr.deleteAnimalLifecycleEvents(female4.getOrgID(), female4.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female5));
			assertEquals(15, evtLdr.deleteAnimalLifecycleEvents(female5.getOrgID(), female5.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female6));
			assertEquals(17, evtLdr.deleteAnimalLifecycleEvents(female6.getOrgID(), female6.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(female7));
			assertEquals(17, evtLdr.deleteAnimalLifecycleEvents(female7.getOrgID(), female7.getAnimalTag()));
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
		}	
		
	}

}

