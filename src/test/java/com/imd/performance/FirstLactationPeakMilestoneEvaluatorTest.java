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

class FirstLactationPeakMilestoneEvaluatorTest {

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
		String maleTag = "-997";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag, 
					now.minusDays(1000), false);
			
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgID(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			assertEquals(1, anmlLdr.deleteAnimal(male));
			
		} catch (Exception e) {
			fail("Exception : " + e.getMessage());
			e.printStackTrace();
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal zeroStarFemale = TestDataCreationUtil.createTestAnimal(orgId, zeroStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(zeroStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(zeroStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float oneStarValue = milestone.getOneStarThreshold();
			
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, zeroStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, zeroStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(zeroStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((oneStarValue-1)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, oneStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgID(), zeroStarFemale.getAnimalTag()));
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal oneStarFemale = TestDataCreationUtil.createTestAnimal(orgId, oneStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(oneStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(oneStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float oneStarValue = milestone.getTwoStarThreshold();
			
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(oneStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((oneStarValue-0.5)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating(),outcome.getEvaluationResultMessage());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, oneStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgID(), oneStarFemale.getAnimalTag()));
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal twoStarFemale = TestDataCreationUtil.createTestAnimal(orgId, twoStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(twoStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(twoStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float threeStarValue = milestone.getThreeStarThreshold();
			
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(twoStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((threeStarValue-1)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, threeStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, threeStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, threeStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgID(), twoStarFemale.getAnimalTag()));
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal threeStarFemale = TestDataCreationUtil.createTestAnimal(orgId, threeStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(threeStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(threeStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float fourStarValue = milestone.getFourStarThreshold();
			
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(threeStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((fourStarValue-1)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, fourStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, fourStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, fourStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgID(), threeStarFemale.getAnimalTag()));
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal fourStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fourStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(fourStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fourStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float fourStarValue = milestone.getFiveStarThreshold();
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(fourStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((fourStarValue-0.5)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, fourStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, fourStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, fourStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgID(), fourStarFemale.getAnimalTag()));
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal fiveStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fiveStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(fiveStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fiveStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float fiveStarValue = milestone.getFiveStarThreshold() + 1;
			
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(fiveStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((fiveStarValue)/3,1));
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
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk*2);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk*2);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk*2);
			assertEquals(1,milkLdr.insertMilkRecord(rec3_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec3_3.getMilkingDetailBean()));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, fiveStarValue);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, fiveStarValue);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(),milkDate.plusDays(100+275+275),3, fiveStarValue);
			assertEquals(1,milkLdr.insertMilkRecord(rec4_1.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_2.getMilkingDetailBean()));
			assertEquals(1,milkLdr.insertMilkRecord(rec4_3.getMilkingDetailBean()));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
			assertEquals(Util.formatToSpecifiedDecimalPlaces(((oneTimeMilk*12)/3),1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(12,milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(fiveStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}
	}

	@Test
	void testCouldNotComputeRating() {
		String orgId = "IMD";
		String fiveStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			LifeCycleEventsLoader evtLdr = new LifeCycleEventsLoader();
			MilkingDetailLoader milkLdr = new MilkingDetailLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.FIRSTLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.FIRSTLACTATIONPEAK,1);
			
			Animal fiveStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fiveStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(anmlLdr.deleteAnimal(fiveStarFemale) >= 0);
			assertTrue(evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag())>= 0);
			assertTrue(milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fiveStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fiveStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fiveStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(fiveStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fiveStarFemale.getDateOfBirth().plusDays((14*30)+275));

			outcome = evl.evaluatePerformanceMilestone(milestone, fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.COULD_NOT_COMPUTE_RATING, outcome.getStarRating());

			assertEquals(3,evtLdr.deleteAnimalLifecycleEvents(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(0,milkLdr.deleteAllMilkingRecordOfanAnimal(fiveStarFemale.getOrgID(), fiveStarFemale.getAnimalTag()));
			assertEquals(1,anmlLdr.deleteAnimal(fiveStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}
	}
	
	
}






