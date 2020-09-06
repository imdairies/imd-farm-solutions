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
import com.imd.loader.PerformanceMilestoneLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
import com.imd.util.Util;

class SecondLactationPeakMilestoneEvaluatorTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		TestDataCreationUtil.deleteFarmMilkingRecordsWithNoAnimalMilkingEvent("IMD");
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal male = TestDataCreationUtil.createTestAnimal(orgId, maleTag,now.minusDays(1000), false);
			
			assertTrue(anmlLdr.deleteAnimal(male) >= 0);
			
			assertEquals(1, anmlLdr.insertAnimal(male));

			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, male.getOrgId(), male.getAnimalTag(), Util.LanguageCode.ENG);			
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());

			assertEquals(1, TestDataCreationUtil.deleteAnimal(male));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal zeroStarFemale = TestDataCreationUtil.createTestAnimal(orgId, zeroStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(zeroStarFemale) >= 0);

			assertEquals(1, TestDataCreationUtil.insertAnimal(zeroStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float oneStarValue = milestone.getOneStarThreshold();
			
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, zeroStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, zeroStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(zeroStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((oneStarValue-1)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(zeroStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, zeroStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3 ));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.NO_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(zeroStarFemale.getOrgId(), zeroStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(zeroStarFemale));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal oneStarFemale = TestDataCreationUtil.createTestAnimal(orgId, oneStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(oneStarFemale) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(oneStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float oneStarValue = milestone.getTwoStarThreshold();
			
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(oneStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((oneStarValue-0.1)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(oneStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, oneStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ONE_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(oneStarFemale.getOrgId(), oneStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(oneStarFemale));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal twoStarFemale = TestDataCreationUtil.createTestAnimal(orgId, twoStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(twoStarFemale) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(twoStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float twoStarValue = milestone.getThreeStarThreshold();
			
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(twoStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((twoStarValue-0.1)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(twoStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, twoStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3 ));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.TWO_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(twoStarFemale.getOrgId(), twoStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(twoStarFemale));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal threeStarFemale = TestDataCreationUtil.createTestAnimal(orgId, threeStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(threeStarFemale) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(threeStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float threeStarValue = milestone.getFourStarThreshold();
			
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(threeStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((threeStarValue-0.5)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(threeStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, threeStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3 ));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.THREE_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(threeStarFemale.getOrgId(), threeStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(threeStarFemale));
			
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
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal fourStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fourStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(fourStarFemale) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fourStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float fourStarValue = milestone.getFiveStarThreshold();
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(fourStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((fourStarValue-0.5)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3 ));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FOUR_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(fourStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}
	}


	@Test
	void testFiveRating() {
		String orgId = "IMD";
		String fourStarFemaleTag = "-999";
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			PerformanceMilestoneLoader perfLdr = new PerformanceMilestoneLoader();
			AnimalLoader anmlLdr = new AnimalLoader();
			
			PerformanceMilestone milestone = perfLdr.retrieveSpecificPerformanceMilestone(orgId, Util.PerformanceMilestone.SECONDLACTATIONPEAK).get(0);
			SpecifiedLactationPeakMilestoneEvaluator evl = new SpecifiedLactationPeakMilestoneEvaluator(Util.PerformanceMilestone.SECONDLACTATIONPEAK,2);
			
			Animal fourStarFemale = TestDataCreationUtil.createTestAnimal(orgId, fourStarFemaleTag, 
					now.minusDays(1000), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag())>= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()) >= 0);
			assertTrue(anmlLdr.deleteAnimal(fourStarFemale) >= 0);

			assertEquals(1, anmlLdr.insertAnimal(fourStarFemale));
			
			PerformanceMilestone outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
			
			float fiveStarValue = milestone.getFiveStarThreshold();
			
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays(14*30));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30) + 1));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275));

			LocalDate milkDate = new LocalDate(fourStarFemale.getDateOfBirth().plusDays(14*30+275+10),IMDProperties.getServerTimeZone());
			float oneTimeMilk = Float.parseFloat(Util.formatToSpecifiedDecimalPlaces((fiveStarValue+3)/3,1));
			MilkingDetail rec1_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,1, oneTimeMilk);
			MilkingDetail rec1_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,2, oneTimeMilk);
			MilkingDetail rec1_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate,3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec1_3 ));
			
			MilkingDetail rec2_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),1, oneTimeMilk);
			MilkingDetail rec2_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),2, oneTimeMilk);
			MilkingDetail rec2_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(1),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec2_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.ANIMAL_NOT_ELIGIBLE, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));

			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Came in heat : To be deleted", Util.LifeCycleEvents.HEAT, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Inseminated : To be deleted", Util.LifeCycleEvents.INSEMINATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+91));
			TestDataCreationUtil.insertEvent(fourStarFemale.getAnimalTag(), "Calved : To be deleted", Util.LifeCycleEvents.PARTURATE, fourStarFemale.getDateOfBirth().plusDays((14*30)+275+90+275));

			MilkingDetail rec3_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),1, oneTimeMilk);
			MilkingDetail rec3_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),2, oneTimeMilk);
			MilkingDetail rec3_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(2),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec3_3 ));

			MilkingDetail rec4_1 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),1, oneTimeMilk);
			MilkingDetail rec4_2 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),2, oneTimeMilk);
			MilkingDetail rec4_3 = TestDataCreationUtil.createMilkingRecord(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(),milkDate.plusDays(100+275),3, oneTimeMilk);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_1 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_2 ));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(rec4_3 ));
			
			outcome = evl.evaluatePerformanceMilestone(milestone, fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag(), Util.LanguageCode.ENG);
			assertEquals(Util.StarRating.FIVE_STAR, outcome.getStarRating());
//			assertEquals(Util.formatToSpecifiedDecimalPlaces(oneTimeMilk*3,1), Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(outcome.getEvaluationValue()),1));
			
			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(fourStarFemale.getOrgId(), fourStarFemale.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(fourStarFemale));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception : " + e.getMessage());
		}
	}

}






