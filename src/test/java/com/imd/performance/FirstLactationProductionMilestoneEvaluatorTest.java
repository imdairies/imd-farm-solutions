package com.imd.performance;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.MilkingDetail;
import com.imd.dto.PerformanceMilestone;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
import com.imd.util.Util;

class FirstLactationProductionMilestoneEvaluatorTest {

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
		String maleTag = "-999";
		String femaleTag1 = "-998";
		String femaleTag2 = "-997";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag, now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + 10), false);
			Animal youngFemale = TestDataCreationUtil.createTestAnimal(orgId, femaleTag1, now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) - 10), true);
			Animal oldFemale = TestDataCreationUtil.createTestAnimal(orgId, femaleTag2, now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + 10), true);
			
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(oldFemale.getOrgID(), oldFemale.getAnimalTag()) >= 0);
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			assertTrue(anmlLdr.deleteAnimal(youngFemale) >= 0);
			assertTrue(anmlLdr.deleteAnimal(oldFemale) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));
			assertEquals(1, anmlLdr.insertAnimal(youngFemale));
			assertEquals(1, anmlLdr.insertAnimal(oldFemale));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgID(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			outcome = evl.evaluatePerformanceMilestone(milestone, youngFemale.getOrgID(), youngFemale.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("older than") >= 0);

			outcome = evl.evaluatePerformanceMilestone(milestone, oldFemale.getOrgID(), oldFemale.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("has calved at least") >= 0,outcome.getEvaluationResultMessage());

			DateTime firstCalvingTS = DateTime.now().minusDays(Integer.parseInt(milestone.getAuxInfo1())-10);
			
			TestDataCreationUtil.insertEvent(oldFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstCalvingTS.minusDays(275));
			TestDataCreationUtil.insertEvent(oldFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstCalvingTS.minusDays(275));
			TestDataCreationUtil.insertEvent(oldFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstCalvingTS);

			
			outcome = evl.evaluatePerformanceMilestone(milestone, oldFemale.getOrgID(), oldFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("days before evaluating this milestone") >= 0,outcome.getEvaluationResultMessage());
			
			
			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(oldFemale.getOrgID(), oldFemale.getAnimalTag()));
			assertEquals(1, anmlLdr.deleteAnimal(male));
			assertEquals(1, anmlLdr.deleteAnimal(youngFemale));
			assertEquals(1, anmlLdr.deleteAnimal(oldFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}	
	}

	@Test
	void testZeroRating() {
		String orgId = "IMD";
		String zeroStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal zeroStarFemale = TestDataCreationUtil.createTestAnimal(orgId, zeroStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(zeroStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(zeroStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float oneStarValue = milestone.getOneStarThreshold();
			DateTime firstHeatTS = zeroStarFemale.getDateOfBirth().plusMonths(15); 
			
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
//			DateTime firstLacMilkEndTS = firstLacMilkStartTS.plusDays(Integer.parseInt(milestone.getAuxInfo1()));

			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("have any milking record") >= 0,outcome.getEvaluationResultMessage());
			
			
			float avgLpd = 18f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((avgLpd)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			
			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()));
			assertEquals(9,milkLdr.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(zeroStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
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
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal oneStarFemale = TestDataCreationUtil.createTestAnimal(orgId, oneStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(oneStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(oneStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float oneStarValue = milestone.getTwoStarThreshold() - 1f;
			DateTime firstHeatTS = oneStarFemale.getDateOfBirth().plusMonths(15); 
			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
			
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			
			float dailyProduction = oneStarValue/3f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((dailyProduction)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()));
			assertEquals(9,milkLdr.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(oneStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
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
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal twoStarFemale = TestDataCreationUtil.createTestAnimal(orgId, twoStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(twoStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(twoStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float twoStarValue = milestone.getThreeStarThreshold() - 1f;
			DateTime firstHeatTS = twoStarFemale.getDateOfBirth().plusMonths(15); 
			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
			
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			
			float dailyProduction = twoStarValue/3f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((dailyProduction)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()));
			assertEquals(9,milkLdr.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(twoStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
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
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal threeStarFemale = TestDataCreationUtil.createTestAnimal(orgId, threeStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(threeStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(threeStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float threeStarValue = milestone.getFourStarThreshold() - 1f;
			DateTime firstHeatTS = threeStarFemale.getDateOfBirth().plusMonths(15); 
			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
			
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			
			float dailyProduction = threeStarValue/3f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((dailyProduction)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()));
			assertEquals(9,milkLdr.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(threeStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
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
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal fourStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fourStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(fourStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fourStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float fourStarValue = milestone.getFiveStarThreshold() - 1f;
			DateTime firstHeatTS = fourStarFemale.getDateOfBirth().plusMonths(15); 
			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			
			float dailyProduction = fourStarValue/3f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((dailyProduction)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()));
			assertEquals(9,milkLdr.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(fourStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
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
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION).get(0);
			SpecifiedLactationTotalProductionMilestoneEvaluator evl = new SpecifiedLactationTotalProductionMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPRODUCTION,1);
			
			Animal fiveStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fiveStarFemaleTag,now.minusDays(Integer.parseInt(milestone.getAuxInfo2()) + Integer.parseInt(milestone.getAuxInfo2()) ), true);
			
			assertTrue(anmlLdr.deleteAnimal(fiveStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fiveStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			assertTrue(outcome.getEvaluationResultMessage().indexOf("doesn't have any") >= 0,outcome.getEvaluationResultMessage());
			
			float fiveStarValue = milestone.getFiveStarThreshold();
			DateTime firstHeatTS = fiveStarFemale.getDateOfBirth().plusMonths(15); 
			DateTime firstLacMilkStartTS = firstHeatTS.plusMonths(9).plusDays(Util.DefaultValues.CLOSTRUM_DAYS+1);
			
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, firstHeatTS);
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, firstHeatTS);
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, firstHeatTS.plusMonths(9));

			
			float dailyProduction = fiveStarValue/3f;
			
			LocalDate milkDate = new LocalDate(firstLacMilkStartTS,IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((dailyProduction)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec1_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec1_3.getMilkingDetailBean()));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec2_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec2_3.getMilkingDetailBean()));
			
			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			// this one is outside the calculation time window so this will not be counted.
			MilkingDetail recx_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(Integer.parseInt(milestone.getAuxInfo1()) + 10),1, oneTimeMilk);
			MilkingDetail recx_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(Integer.parseInt(milestone.getAuxInfo1()) + 10),2, oneTimeMilk);
			MilkingDetail recx_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(Integer.parseInt(milestone.getAuxInfo1()) + 10),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(recx_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(recx_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(recx_3.getMilkingDetailBean()));

			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating(), outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*9,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(fiveStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}
	}	
}






