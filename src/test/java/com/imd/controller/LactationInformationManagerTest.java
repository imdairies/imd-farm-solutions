package com.imd.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.LactationInformation;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
import com.imd.util.Util;

class LactationInformationManagerTest {

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
	void testOneLactation() {
		String orgId = "IMD";
		String animalTag = "-999";
		String calfTag = "-998";
		DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20);
		try {
			LactationInformationManager mgr = new LactationInformationManager();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			Animal calf = TestDataCreationUtil.createTestAnimal(orgId, calfTag, dob.plusMonths(14).plusDays(22).plusDays(275), true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), calfTag) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
//			assertTrue(TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(calf) >= 0);
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));
			assertEquals(1,TestDataCreationUtil.insertAnimal(calf));
			List<LactationInformation> lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec == null || lacRec.isEmpty());

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"birth", Util.LifeCycleEvents.BIRTH, dob, "UNKNOWN", "UNKNOWN", null, null) > 1);			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).minusDays(1), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.NO, 
					/*update inventory*/ Util.NO) > 1);

			lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec == null || lacRec.isEmpty());
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 2", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).plusDays(21), Util.NO, null, null, null) > 1);

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 2", Util.LifeCycleEvents.MATING, dob.plusMonths(14).plusDays(22), 
					/*bull*/ "XX1HO12126", 
					/* mating outcome*/ Util.YES, 
					null, null) > 1);

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 1", Util.LifeCycleEvents.PARTURATE, dob.plusMonths(14).plusDays(22).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, null, null) > 1);
			
			DateTime firstCalvingTS = dob.plusMonths(14).plusDays(22).plusDays(275);
			calf.setDateOfBirth(firstCalvingTS);

			assertTrue(TestDataCreationUtil.insertEvent(calf.getAnimalTag(),
					"Birth 1", Util.LifeCycleEvents.BIRTH, calf.getDateOfBirth(), Util.GENDER_CHAR.FEMALE + 
					/*sire*/ "XX1HO12126", 
					/**/animal.getAnimalTag(), 
					null, null) > 1);

			lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec != null && !lacRec.isEmpty());
			assertTrue(lacRec.get(0).getMilkingProduction() != null);
			assertEquals(0f,lacRec.get(0).getMilkingProduction().floatValue());
			assertEquals(0f,lacRec.get(0).getMaxDailyProduction().floatValue());
			assertEquals(0,lacRec.get(0).getDaysInMilking());
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 10f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 9f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 11f)));
			// this record should not be counted as its not after calving
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.minusDays(15),IMDProperties.getServerTimeZone()),"4:00", 1, 11f)));
			
			lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec != null && !lacRec.isEmpty());
			assertEquals(1,lacRec.get(0).getLactationNumber().intValue());
			assertEquals(calf.getAnimalTag(),lacRec.get(0).getCalfTag());
			assertEquals(2,lacRec.get(0).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(0).getMilkingProduction() != null);
			assertEquals(30f,lacRec.get(0).getMilkingProduction().floatValue());
			assertEquals(30f,lacRec.get(0).getMaxDailyProduction().floatValue());
			assertEquals(1,lacRec.get(0).getDaysInMilking());

			assertEquals(6,TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAllAnimalEvents(calf.getOrgId(), calf.getAnimalTag()));
			assertEquals(4,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),null));
			assertEquals(1,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(firstCalvingTS.minusDays(15),IMDProperties.getServerTimeZone()),null));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(calf));
		
		} catch (Exception e) {
			fail("Exception");
			e.printStackTrace();
		}
	}

	
	
	@Test
	void testTwoLactations() {
		String orgId = "IMD";
		String animalTag = "-999";
		DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20);
		try {
			LactationInformationManager mgr = new LactationInformationManager();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"birth", Util.LifeCycleEvents.BIRTH, dob, "UNKNOWN", "UNKNOWN", null, null) > 1);			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.1", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).minusDays(1), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.1", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.NO, 
					/*update inventory*/ Util.NO) > 1);
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.2", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.2", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14).plusDays(22), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 1", Util.LifeCycleEvents.PARTURATE, dob.plusMonths(14).plusDays(22).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-998", null) > 1);
			
			DateTime firstCalvingTS = dob.plusMonths(14).plusDays(21).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 10f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 9f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 11f)));
			///////
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 2.1", Util.LifeCycleEvents.HEAT, firstCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 2.1", Util.LifeCycleEvents.INSEMINATE, firstCalvingTS.plusDays(90), 
					/*sire*/ "YY1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 2", Util.LifeCycleEvents.PARTURATE, firstCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-997", null) > 1);
			
			DateTime secondCalvingTS = firstCalvingTS.plusDays(90).plusDays(275);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 15f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 15f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 15f)));
			//////
			
			List<LactationInformation> lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec != null && lacRec.size() == 2);

			assertEquals(2,lacRec.get(0).getLactationNumber().intValue());
			assertEquals("-997",lacRec.get(0).getCalfTag());
			assertEquals(1,lacRec.get(0).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(0).getMilkingProduction() != null);
			assertEquals(45f,lacRec.get(0).getMilkingProduction().floatValue());
			assertEquals(45f,lacRec.get(0).getMaxDailyProduction().floatValue());
			assertEquals(1,lacRec.get(0).getDaysInMilking());
			
			assertEquals(1,lacRec.get(1).getLactationNumber().intValue());
			assertEquals("-998",lacRec.get(1).getCalfTag());
			assertEquals(2,lacRec.get(1).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(1).getMilkingProduction() != null);
			assertEquals(30f,lacRec.get(1).getMilkingProduction().floatValue());
			assertEquals(30f,lacRec.get(1).getMaxDailyProduction().floatValue());
			assertEquals(1,lacRec.get(1).getDaysInMilking());

			assertEquals(9,TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(6,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(firstCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(secondCalvingTS.plusDays(5)),null));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));

		} catch (Exception e) {
			fail("Exception");
			e.printStackTrace();
		}
	}


	
	@Test
	void testThreeLactations() {
		String orgId = "IMD";
		String animalTag = "-999";
		DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20);
		try {
			LactationInformationManager mgr = new LactationInformationManager();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"birth", Util.LifeCycleEvents.BIRTH, dob, "UNKNOWN", "UNKNOWN", null, null) > 1);			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.1", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).minusDays(1), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.1", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.NO, 
					/*update inventory*/ Util.NO) > 1);
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.2", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.2", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14).plusDays(22), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 1", Util.LifeCycleEvents.PARTURATE, dob.plusMonths(14).plusDays(22).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-998", null) > 1);
			
			DateTime firstCalvingTS = dob.plusMonths(14).plusDays(21).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 10f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 9f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 11f)));
			
			///////
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 2.1", Util.LifeCycleEvents.HEAT, firstCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 2.1", Util.LifeCycleEvents.INSEMINATE, firstCalvingTS.plusDays(90), 
					/*sire*/ "YY1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 2", Util.LifeCycleEvents.PARTURATE, firstCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-997", null) > 1);
			
			DateTime secondCalvingTS = firstCalvingTS.plusDays(90).plusDays(275);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 16f)));
			//////
			
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 3.1", Util.LifeCycleEvents.HEAT, secondCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 3.1", Util.LifeCycleEvents.MATING, secondCalvingTS.plusDays(90), 
					/*bull*/ "M017",
					/*outcome*/ Util.YES, 
					null,null) > 1);

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 3.2", Util.LifeCycleEvents.HEAT, secondCalvingTS.plusDays(89).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 3.2", Util.LifeCycleEvents.INSEMINATE, secondCalvingTS.plusDays(90).plusDays(21), 
					/*sire*/ "ZZ1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 3", Util.LifeCycleEvents.PARTURATE, secondCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-996", null) > 1);
			
			DateTime thirdCalvingTS = secondCalvingTS.plusDays(90).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00", 3, 16f)));

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"4:00", 1, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"12:00", 2, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"20:00", 3, 17f)));
			//////
			
			List<LactationInformation> lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec != null && lacRec.size() == 3);

			assertEquals(3,lacRec.get(0).getLactationNumber().intValue());
			assertEquals("-996",lacRec.get(0).getCalfTag());
			assertEquals(2,lacRec.get(0).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(0).getMilkingProduction() != null);
			assertEquals(99f,lacRec.get(0).getMilkingProduction().floatValue());
			assertEquals((99f/2f),lacRec.get(0).getMaxDailyProduction().floatValue());			
			assertEquals(2,lacRec.get(0).getDaysInMilking());
			
			assertEquals(2,lacRec.get(1).getLactationNumber().intValue());
			assertEquals("-997",lacRec.get(1).getCalfTag());
			assertEquals(1,lacRec.get(1).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(1).getMilkingProduction() != null);
			assertEquals(48f,lacRec.get(1).getMilkingProduction().floatValue());
			assertEquals(48f,lacRec.get(1).getMaxDailyProduction().floatValue());			
			assertEquals(1,lacRec.get(1).getDaysInMilking());
			
			assertEquals(1,lacRec.get(2).getLactationNumber().intValue());
			assertEquals("-998",lacRec.get(2).getCalfTag());
			assertEquals(2,lacRec.get(2).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(2).getMilkingProduction() != null);
			assertEquals(30f,lacRec.get(2).getMilkingProduction().floatValue());
			assertEquals(30f,lacRec.get(2).getMaxDailyProduction().floatValue());
			assertEquals(1,lacRec.get(2).getDaysInMilking());

			assertEquals(14,TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(12,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(firstCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(secondCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(thirdCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(thirdCalvingTS.plusDays(6)),null));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));

			
		} catch (Exception e) {
			fail("Exception");
			e.printStackTrace();
		}
	}

	
	@Test
	void testFourLactations() {
		String orgId = "IMD";
		String animalTag = "-999";
		DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20);
		try {
			LactationInformationManager mgr = new LactationInformationManager();
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"birth", Util.LifeCycleEvents.BIRTH, dob, "UNKNOWN", "UNKNOWN", null, null) > 1);			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.1", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).minusDays(1), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.1", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.NO, 
					/*update inventory*/ Util.NO) > 1);
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 1.2", Util.LifeCycleEvents.HEAT, dob.plusMonths(14).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 1.2", Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(14).plusDays(22), 
					/*sire*/ "XX1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 1", Util.LifeCycleEvents.PARTURATE, dob.plusMonths(14).plusDays(22).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-998", null) > 1);
			
			DateTime firstCalvingTS = dob.plusMonths(14).plusDays(21).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 10f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 9f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(firstCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00",  3, 11f)));
			
			///////
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 2.1", Util.LifeCycleEvents.HEAT, firstCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 2.1", Util.LifeCycleEvents.INSEMINATE, firstCalvingTS.plusDays(90), 
					/*sire*/ "YY1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 2", Util.LifeCycleEvents.PARTURATE, firstCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-997", null) > 1);
			
			DateTime secondCalvingTS = firstCalvingTS.plusDays(90).plusDays(275);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(secondCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00",  3, 16f)));
			//////
			
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 3.1", Util.LifeCycleEvents.HEAT, secondCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 3.1", Util.LifeCycleEvents.MATING, secondCalvingTS.plusDays(90), 
					/*bull*/ "M017",
					/*outcome*/ Util.YES, 
					null,null) > 1);

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 3.2", Util.LifeCycleEvents.HEAT, secondCalvingTS.plusDays(89).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 3.2", Util.LifeCycleEvents.INSEMINATE, secondCalvingTS.plusDays(90).plusDays(21), 
					/*sire*/ "ZZ1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 3", Util.LifeCycleEvents.PARTURATE, secondCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-996", null) > 1);
			
			DateTime thirdCalvingTS = secondCalvingTS.plusDays(90).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00",  3, 16f)));
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"4:00", 1, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"12:00", 2, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(thirdCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"20:00",  3, 17f)));
			//////
			
			
			
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 4.1", Util.LifeCycleEvents.HEAT, thirdCalvingTS.plusDays(89), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 4.1", Util.LifeCycleEvents.MATING, thirdCalvingTS.plusDays(90), 
					/*bull*/ "M014",
					/*outcome*/ Util.YES, 
					null,null) > 1);

			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"heat 4.2", Util.LifeCycleEvents.HEAT, thirdCalvingTS.plusDays(89).plusDays(21), Util.NO, null, null, null) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"insemination 4.2", Util.LifeCycleEvents.INSEMINATE, thirdCalvingTS.plusDays(90).plusDays(21), 
					/*sire*/ "ZZ1HO12126",
					/*sexed*/ Util.YES,
					/*outcome*/ Util.YES, 
					/*update inventory*/ Util.NO) > 1);
			assertTrue(TestDataCreationUtil.insertEvent(animal.getAnimalTag(),
					"Calving 4", Util.LifeCycleEvents.PARTURATE, thirdCalvingTS.plusDays(90).plusDays(275), Util.GENDER_CHAR.FEMALE + "", 
					Util.NO, "-995", null) > 1);
			
			DateTime fourthCalvingTS = thirdCalvingTS.plusDays(90).plusDays(275);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"4:00", 1, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"12:00", 2, 16f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(5),IMDProperties.getServerTimeZone()),"20:00",  3, 16f)));
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"4:00", 1, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"12:00", 2, 17f)));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(TestDataCreationUtil.createMilkingRecord(animal.getOrgId(), animal.getAnimalTag(), 
					new LocalDate(fourthCalvingTS.plusDays(6),IMDProperties.getServerTimeZone()),"20:00", 3, 17f)));
			//////

			List<LactationInformation> lacRec = mgr.getAnimalLactationInformation(animal.getOrgId(), animal.getAnimalTag());
			assertTrue(lacRec != null && lacRec.size() == 4);

			assertEquals(4,lacRec.get(0).getLactationNumber().intValue());
			assertEquals("-995",lacRec.get(0).getCalfTag());
			assertEquals(2,lacRec.get(0).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(0).getMilkingProduction() != null);
			assertEquals(99f,lacRec.get(0).getMilkingProduction().floatValue());			
			assertEquals(2,lacRec.get(0).getDaysInMilking());
		
			
			assertEquals(3,lacRec.get(1).getLactationNumber().intValue());
			assertEquals("-996",lacRec.get(1).getCalfTag());
			assertEquals(2,lacRec.get(1).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(1).getMilkingProduction() != null);
			assertEquals(99f,lacRec.get(1).getMilkingProduction().floatValue());			
			assertEquals(2,lacRec.get(1).getDaysInMilking());
			
			assertEquals(2,lacRec.get(2).getLactationNumber().intValue());
			assertEquals("-997",lacRec.get(2).getCalfTag());
			assertEquals(1,lacRec.get(2).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(2).getMilkingProduction() != null);
			assertEquals(48f,lacRec.get(2).getMilkingProduction().floatValue());
			assertEquals(1,lacRec.get(2).getDaysInMilking());
			
			assertEquals(1,lacRec.get(3).getLactationNumber().intValue());
			assertEquals("-998",lacRec.get(3).getCalfTag());
			assertEquals(2,lacRec.get(3).getInseminationAttemptCount().intValue());
			assertTrue(lacRec.get(3).getMilkingProduction() != null);
			assertEquals(30f,lacRec.get(3).getMilkingProduction().floatValue());
			assertEquals(1,lacRec.get(3).getDaysInMilking());

			assertEquals(19,TestDataCreationUtil.deleteAllAnimalEvents(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(18,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(animal.getOrgId(), animal.getAnimalTag()));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(firstCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(secondCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(thirdCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(thirdCalvingTS.plusDays(6)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(fourthCalvingTS.plusDays(5)),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(animal.getOrgId(), new LocalDate(fourthCalvingTS.plusDays(6)),null));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));

		} catch (Exception e) {
			fail("Exception");
			e.printStackTrace();
		}
	}
	@Test
	void testSpecificIMDFarmAnimalLactationInformation() {
		String orgId = "IMD";
		String animalTag = "033";
		LactationInformationManager mgr = new LactationInformationManager();
		
		mgr.getAnimalLactationInformation(orgId, animalTag);
	}

}



