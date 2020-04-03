package com.imd.performance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
import com.imd.util.Util;

class PerformanceMilestoneManagerTest {

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
	void testFertilityBadge() {
		String orgId = "IMD";
		String animalTag = "-999";
		String langCd = Util.LanguageCode.ENG;
		DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(3);
		try {
			Animal testAnimal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, 
					dob, true);
			assertTrue(TestDataCreationUtil.deleteAnimal(testAnimal) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllAnimalEvents(testAnimal.getOrgID(), testAnimal.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(testAnimal.getOrgID(), testAnimal.getAnimalTag()) >= 0);
			
			int heatEventId = TestDataCreationUtil.insertEvent(testAnimal.getAnimalTag(), "Heat", 
					Util.LifeCycleEvents.HEAT, dob.plusMonths(13));
			assertTrue(heatEventId >= 0);

			int inseminationEventId = TestDataCreationUtil.insertEvent(testAnimal.getAnimalTag(), "Inseminate", 
					Util.LifeCycleEvents.INSEMINATE, dob.plusMonths(13).plusDays(1),
					"1HO10219", Util.YES, Util.YES,null);
			assertTrue(inseminationEventId >= 0);

			int calvingEventId = TestDataCreationUtil.insertEvent(testAnimal.getAnimalTag(), "Calving", 
					Util.LifeCycleEvents.PARTURATE, dob.plusMonths(13).plusDays(275),
					Util.Gender.FEMALE, Util.NO, null,null);
			assertTrue(calvingEventId >= 0);

			int dryOffEventID = TestDataCreationUtil.insertEvent(testAnimal.getAnimalTag(), "Dry Off",
					Util.LifeCycleEvents.DRYOFF, dob.plusMonths(13).plusDays(275).plusDays(365));
			assertTrue(dryOffEventID >= 0);


			MilkingDetail milkRecord = TestDataCreationUtil.createMilkingRecord(testAnimal.getOrgID(), testAnimal.getAnimalTag(), 
					new LocalDate(dob.plusMonths(13).plusDays(275).plusDays(30), IMDProperties.getServerTimeZone()),
					1, 9000);
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRecord));
			assertEquals(1,TestDataCreationUtil.insertAnimal(testAnimal));
			
			PerformanceMilestoneManager manager = new PerformanceMilestoneManager();
			List<PerformanceMilestone> milestoneEvaluationOutcome = new ArrayList<PerformanceMilestone>();
			milestoneEvaluationOutcome = manager.evaluateAllActivePerformanceMilestones(orgId, animalTag, langCd);
			HashMap<String, Boolean> badges = manager.getAllBadges(milestoneEvaluationOutcome );
			assertTrue(badges.get(Util.PerformanceBadges.MILK_PLUS_BADGE));
			
			assertEquals(1,TestDataCreationUtil.deleteAnimal(testAnimal));
			assertEquals(4,TestDataCreationUtil.deleteAllAnimalEvents(testAnimal.getOrgID(), testAnimal.getAnimalTag()));
			assertEquals(1,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(testAnimal.getOrgID(), testAnimal.getAnimalTag()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception in animal creation");
		}		
	}

}
