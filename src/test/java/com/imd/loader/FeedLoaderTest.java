package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.User;
import com.imd.util.Util;

class FeedLoaderTest {

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
	void testFeedPlanItemInsertionDeletion() {
		try {
			FeedLoader loader = new FeedLoader();
			FeedItem feedItem = new FeedItem();
			feedItem.setOrgID("IMD");
			feedItem.setFeedItemCD("TST_ALFHAY");
			feedItem.setFeedCohortCD("LAC_TST");
			feedItem.setStart(0.0f);
			feedItem.setEnd(0.0f);
			feedItem.setMinimumFulfillment(3.3f);
			feedItem.setFulfillmentPct(3.0f);
			feedItem.setMaximumFulfillment(null);
			feedItem.setUnits("Kgs");
			feedItem.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem.setDailyFrequency((Integer)null);
			feedItem.setComments("Put alfaalfa hay infront of the calves and let them eat as much as they wish");
			feedItem.setCreatedBy(new User("KASHIF"));
			feedItem.setCreatedDTTM(DateTime.now());
			feedItem.setUpdatedBy(feedItem.getCreatedBy());
			feedItem.setUpdatedDTTM(feedItem.getCreatedDTTM());
			assertTrue(loader.deleteFeedPlanItem(feedItem) >= 0);
			assertEquals(1,loader.insertFeedPlanItem(feedItem));
			assertEquals(1,loader.deleteFeedPlanItem(feedItem, " AND START >= ? AND END >= ?", feedItem.getStart(), feedItem.getEnd()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}	
	@Test
	void testFoodPlanRetrieval() {
		try {
			FeedLoader loader = new FeedLoader();
			FeedItem feedItem1 = new FeedItem();
			FeedItem feedItem2 = new FeedItem();
			feedItem1.setOrgID("IMD");
			feedItem1.setFeedItemCD("TST_AHAY");
			feedItem1.setFeedCohortCD("LAC_TST");
			feedItem1.setStart(0.0f);
			feedItem1.setEnd(0.0f);
			feedItem1.setMinimumFulfillment(3.3f);
			feedItem1.setFulfillmentPct(3.0f);
			feedItem1.setMaximumFulfillment(null);
			feedItem1.setUnits("Kgs");
			feedItem1.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem1.setDailyFrequency((Integer)null);
			feedItem1.setComments("Put alfaalfa hay infront of the calves and let them eat as much as they wish");
			feedItem1.setCreatedBy(new User("KASHIF"));
			feedItem1.setCreatedDTTM(DateTime.now());
			feedItem1.setUpdatedBy(feedItem1.getCreatedBy());
			feedItem1.setUpdatedDTTM(feedItem1.getCreatedDTTM());
			

			feedItem2.setOrgID("IMD");
			feedItem2.setFeedItemCD("TST_MILK");
			feedItem2.setFeedCohortCD("LAC_TST");
			feedItem2.setStart(0.0f);
			feedItem2.setEnd(0.0f);
			feedItem2.setMinimumFulfillment(3.3f);
			feedItem2.setFulfillmentPct(3.0f);
			feedItem2.setMaximumFulfillment(null);
			feedItem2.setUnits("Liters");
			feedItem2.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem2.setDailyFrequency((Integer)1);
			feedItem2.setCreatedBy(new User("KASHIF"));
			feedItem2.setCreatedDTTM(DateTime.now());
			feedItem2.setUpdatedBy(feedItem2.getCreatedBy());
			feedItem2.setUpdatedDTTM(feedItem2.getCreatedDTTM());
			
			
			assertTrue(loader.deleteFeedPlanItem(feedItem1) >= 0);			
			assertTrue(loader.deleteFeedPlanItem(feedItem2) >= 0);

			assertEquals(1,loader.insertFeedPlanItem(feedItem1));
			assertEquals(1,loader.insertFeedPlanItem(feedItem2));
			
						
			FeedPlan plan = loader.retrieveFeedPlan(feedItem1.getOrgID(), feedItem1.getFeedCohortCD());
			assertEquals(2,plan.getFeedPlan().size());
			assertEquals(feedItem1.getFeedCohortCD(),plan.getFeedCohort().getFeedCohortTypeCD());
			assertEquals(feedItem1.getOrgID(),plan.getOrgID());
			assertTrue(plan.getFeedPlan().get(0).getFeedItemCD().equals(feedItem1.getFeedItemCD()) || plan.getFeedPlan().get(0).getFeedItemCD().equals(feedItem2.getFeedItemCD()));
			assertTrue(plan.getFeedPlan().get(1).getFeedItemCD().equals(feedItem1.getFeedItemCD()) || plan.getFeedPlan().get(1).getFeedItemCD().equals(feedItem2.getFeedItemCD()));
			
			assertEquals(1,loader.deleteFeedPlanItem(feedItem1, " AND START >= ? AND END >= ?", feedItem1.getStart(), feedItem1.getEnd()));
			assertEquals(1,loader.deleteFeedPlanItem(feedItem2, " AND START >= ? AND END >= ?", feedItem2.getStart(), feedItem2.getEnd()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}	
		

	@Test
	void testNutritionalNeedsInsertionDeletion() {
		try {
			FeedLoader loader = new FeedLoader();
			CohortNutritionalNeeds dietReq = new CohortNutritionalNeeds();
			dietReq.setOrgID("IMD");
			dietReq.setFeedCohortCD("LAC_TST");
			dietReq.setStart(0.0f);
			dietReq.setEnd(0.0f);
			dietReq.setDryMatter(3f); //3% of body weight
			dietReq.setCrudeProtein(18f); //18% of DM
			dietReq.setMetabloizableEnergy(3.1f);
			dietReq.setCreatedBy(new User("KASHIF"));
			dietReq.setCreatedDTTM(DateTime.now());
			dietReq.setUpdatedBy(dietReq.getCreatedBy());
			dietReq.setUpdatedDTTM(dietReq.getCreatedDTTM());
			assertTrue(loader.deleteCohortNutritionalNeeds(dietReq) >= 0);
			assertEquals(1,loader.insertCohortNutritionalNeeds(dietReq));
			assertEquals(1,loader.deleteCohortNutritionalNeeds(dietReq));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}
		
	
	@Test
	void testNutritionalNeedsRetrieval() {
		try {
			FeedLoader loader = new FeedLoader();
			CohortNutritionalNeeds dietReq = new CohortNutritionalNeeds();
			dietReq.setOrgID("IMD");
			dietReq.setFeedCohortCD("LAC_TST");
			dietReq.setStart(0.0f);
			dietReq.setEnd(0.0f);
			dietReq.setDryMatter(3f); //3% of body weight
			dietReq.setCrudeProtein(18f); //18% of DM
			dietReq.setMetabloizableEnergy(3.1f);
			dietReq.setCreatedBy(new User("KASHIF"));
			dietReq.setCreatedDTTM(DateTime.now());
			dietReq.setUpdatedBy(dietReq.getCreatedBy());
			dietReq.setUpdatedDTTM(dietReq.getCreatedDTTM());
			
			assertTrue(loader.deleteCohortNutritionalNeeds(dietReq) >= 0);

			dietReq.setFeedCohortCD("HEIFER_TST");
			assertTrue(loader.deleteCohortNutritionalNeeds(dietReq) >= 0);

			dietReq.setFeedCohortCD("LAC_TST");
			assertEquals(1,loader.insertCohortNutritionalNeeds(dietReq));

			List<FeedCohort> animalTypes = new ArrayList<FeedCohort> ();
			animalTypes.add(new FeedCohort(dietReq.getOrgID(), dietReq.getFeedCohortCD(),""));
			animalTypes.add(new FeedCohort(dietReq.getOrgID(), "HEIFER_TST",""));
			List<CohortNutritionalNeeds> reqs = loader.getCohortNutritionalNeeds(dietReq.getOrgID(),animalTypes);
			assertEquals(1,reqs.size());
			Iterator<CohortNutritionalNeeds> it = reqs.iterator();
			while (it.hasNext()) {
				CohortNutritionalNeeds req = it.next();
				assertEquals(req.getFeedCohortCD(),dietReq.getFeedCohortCD());
				assertEquals(req.getDryMatter(),dietReq.getDryMatter());
				assertEquals(req.getCrudeProtein(),dietReq.getCrudeProtein());
				assertEquals(req.getMetabloizableEnergy(),dietReq.getMetabloizableEnergy());
				assertEquals(req.getCreatedBy().getUserId(),dietReq.getCreatedBy().getUserId());
				assertEquals(Util.getDateInSQLFormart(req.getCreatedDTTM()),Util.getDateInSQLFormart(dietReq.getCreatedDTTM()));
				assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
				assertEquals(Util.getDateInSQLFormart(req.getUpdatedDTTM()),Util.getDateInSQLFormart(dietReq.getUpdatedDTTM()));
				break;
			}
			
			dietReq.setFeedCohortCD("HEIFER_TST");
			assertEquals(1,loader.insertCohortNutritionalNeeds(dietReq));
			
			reqs = loader.getCohortNutritionalNeeds(dietReq.getOrgID(),animalTypes);
			assertEquals(2,reqs.size());
			it = reqs.iterator();
			while (it.hasNext()) {
				CohortNutritionalNeeds req = it.next();
				if (req.getFeedCohortCD().equals("HEIFER_TST")) {
					assertEquals(req.getFeedCohortCD(),dietReq.getFeedCohortCD());
					assertEquals(req.getDryMatter(),dietReq.getDryMatter());
					assertEquals(req.getCrudeProtein(),dietReq.getCrudeProtein());
					assertEquals(req.getMetabloizableEnergy(),dietReq.getMetabloizableEnergy());
					assertEquals(req.getCreatedBy().getUserId(),dietReq.getCreatedBy().getUserId());
					assertEquals(Util.getDateInSQLFormart(req.getCreatedDTTM()),Util.getDateInSQLFormart(dietReq.getCreatedDTTM()));
					assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
					assertEquals(Util.getDateInSQLFormart(req.getUpdatedDTTM()),Util.getDateInSQLFormart(dietReq.getUpdatedDTTM()));
				}
			}
			dietReq.setFeedCohortCD("LAC_TST");
			assertEquals(1,loader.deleteCohortNutritionalNeeds(dietReq));
			dietReq.setFeedCohortCD("HEIFER_TST");
			assertEquals(1,loader.deleteCohortNutritionalNeeds(dietReq));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}
}