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
import com.imd.dto.LookupValues;
import com.imd.dto.User;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
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
	void testFeedPlanEdit() {
		int originalLoggingMode = IMDLogger.loggingMode;
		try {
			IMDLogger.loggingMode = Util.INFO;
			String orgID = "IMD";
			String feedCohortCD = Util.FeedCohortType.FEMALECALF;
			FeedLoader feedLoader = new FeedLoader();
			LookupValuesLoader lvLoader = new LookupValuesLoader();
			LookupValuesBean searchBean = new LookupValuesBean();
			searchBean.setActiveIndicator(Util.Y);
			searchBean.setCategoryCode(Util.LookupValues.FEED);
			
			FeedPlan feedPlan = feedLoader.retrieveFeedPlan(orgID, feedCohortCD);
			assertTrue(feedPlan != null && feedPlan.getFeedPlan() != null && !feedPlan.getFeedPlan().isEmpty(), "No Feed Plan exists for " + feedCohortCD + ". This unit test assumes that a feedplan exists for the cohort: "+ feedCohortCD);
			
			List<LookupValues> feedItemsMasterList = lvLoader.retrieveLookupValues(searchBean);
			assertTrue(feedItemsMasterList != null && !feedItemsMasterList.isEmpty(),"This unit test assumes that we have some lookup values for the category " + Util.LookupValues.FEED);
			
			Iterator<LookupValues> itemMasterListIt = feedItemsMasterList.iterator();
			LookupValues availableFeedItem = null;
			while (itemMasterListIt.hasNext()) {
				LookupValues feedItemLV = itemMasterListIt.next();
				boolean found = false;
				Iterator<FeedItem> itemIt = feedPlan.getFeedPlan().iterator();
				while (itemIt.hasNext()) {
					FeedItem item = itemIt.next();
					if (item.getFeedItemLookupValue().getLookupValueCode().equals(feedItemLV.getLookupValueCode())) {
						found = true;
						break;
					}
				}
				if (!found) {
					availableFeedItem = feedItemLV;
					break;
				}
			}
			assertTrue(availableFeedItem != null, "The feedplan for the cohort " + feedCohortCD + 
					" already contains ALL the available Feed Items listed in the lookup category " + Util.LookupValues.FEED + 
					" the unit test needs at least one available unused feeditem in this cohort plan so "
					+ "that it can use that value for testing addition, edit and deletion.");
			
			FeedItem feedItem1 = new FeedItem();
			feedItem1.setOrgID(feedPlan.getOrgID());
			feedItem1.setFeedCohortCD(lvLoader.retrieveLookupValue(Util.LookupValues.FEEDCOHORT,feedCohortCD));
			feedItem1.setFeedItemLookupValue(availableFeedItem);
			
			feedItem1.setStart(0.0f);
			feedItem1.setEnd(9999.0f);
			feedItem1.setMinimumFulfillment(2.0f);
			feedItem1.setFulfillmentPct(3.0f);
			feedItem1.setMaximumFulfillment(10.0f);
			feedItem1.setUnits("Kgs");
			feedItem1.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem1.setDailyFrequency(1);
			feedItem1.setComments("Give " + availableFeedItem.getLookupValueCode() + 
					" to " + feedCohortCD + " once a day");
			feedItem1.setCreatedBy(new User("KASHIF"));
			feedItem1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			feedItem1.setUpdatedBy(feedItem1.getCreatedBy());
			feedItem1.setUpdatedDTTM(feedItem1.getCreatedDTTM());
			assertEquals(0,feedLoader.deleteFeedPlanItem(feedItem1));
			assertEquals(1,feedLoader.insertFeedPlanItem(feedItem1));
			
			
			feedPlan = feedLoader.retrieveFeedPlan(orgID, feedCohortCD);
			assertTrue(feedPlan != null && feedPlan.getFeedPlan() != null && !feedPlan.getFeedPlan().isEmpty(), "No Feed Plan exists for " + feedCohortCD + ". This unit test assumes that a feedplan exists for the cohort: "+ feedCohortCD);
			
			boolean found = false;
			Iterator<FeedItem> itemIt = feedPlan.getFeedPlan().iterator();
			while (itemIt.hasNext()) {
				FeedItem item = itemIt.next();
				if (item.getFeedItemLookupValue().getLookupValueCode().equals(availableFeedItem.getLookupValueCode())) {
					found = true;
					break;
				}
			}
			assertTrue(found, "The added feed item: " + availableFeedItem.getLookupValueCode() + 
					" in feed plan of the cohort: " + feedCohortCD + 
					" was not found. It should have been added successfully.");			

			
			
			assertEquals(1,feedLoader.deleteFeedPlanItem(feedItem1), "The added feed item: " + availableFeedItem.getLookupValueCode() + 
					" in feed plan of the cohort: " + feedCohortCD + 
					" was not found. It should have been deleted successfully.");
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred");
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;
			
		}
	}

	@Test
	void testFeedPlanItemInsertionDeletion() {
		try {
			FeedLoader loader = new FeedLoader();
			FeedItem feedItem = new FeedItem();
			feedItem.setOrgID("IMD");
			LookupValues feedItemLV = new LookupValues(Util.LookupValues.FEED,"TST_ALFHAY", "","");
			feedItem.setFeedItemLookupValue(feedItemLV);		

			LookupValues feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,"LAC_TST", "","");
			feedItem.setFeedCohortCD(feedCohortLV);
			
			feedItem.setStart(0.0f);
			feedItem.setEnd(0.0f);
			feedItem.setMinimumFulfillment(3.3f);
			feedItem.setFulfillmentPct(3.0f);
//			feedItem.setMaximumFulfillment(null);
			feedItem.setUnits("Kgs");
			feedItem.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem.setDailyFrequency((Integer)null);
			feedItem.setComments("Put alfaalfa hay infront of the calves and let them eat as much as they wish");
			feedItem.setCreatedBy(new User("KASHIF"));
			feedItem.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
			LookupValues feedItemLV = new LookupValues(Util.LookupValues.FEED,"TST_AHAY", "","");
			feedItem1.setFeedItemLookupValue(feedItemLV);

			LookupValues feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,"LAC_TST", "","");
			feedItem1.setFeedCohortCD(feedCohortLV);
			
			feedItem1.setStart(0.0f);
			feedItem1.setEnd(0.0f);
			feedItem1.setMinimumFulfillment(3.3f);
			feedItem1.setFulfillmentPct(3.0f);
//			feedItem1.setMaximumFulfillment(null);
			feedItem1.setUnits("Kgs");
			feedItem1.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem1.setDailyFrequency((Integer)null);
			feedItem1.setComments("Put alfaalfa hay infront of the calves and let them eat as much as they wish");
			feedItem1.setCreatedBy(new User("KASHIF"));
			feedItem1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			feedItem1.setUpdatedBy(feedItem1.getCreatedBy());
			feedItem1.setUpdatedDTTM(feedItem1.getCreatedDTTM());

			feedItem2.setOrgID("IMD");
			
			feedItemLV = new LookupValues(Util.LookupValues.FEED,"TST_MILK", "","");
			feedItem2.setFeedItemLookupValue(feedItemLV);

			feedCohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT,"LAC_TST", "","");
			feedItem2.setFeedCohortCD(feedCohortLV);

			feedItem2.setStart(0.0f);
			feedItem2.setEnd(0.0f);
			feedItem2.setMinimumFulfillment(3.3f);
			feedItem2.setFulfillmentPct(3.0f);
//			feedItem2.setMaximumFulfillment(null);
			feedItem2.setUnits("Liters");
			feedItem2.setFulFillmentTypeCD(Util.FulfillmentType.ABSOLUTE);
			feedItem2.setDailyFrequency((Integer)1);
			feedItem2.setCreatedBy(new User("KASHIF"));
			feedItem2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			feedItem2.setUpdatedBy(feedItem2.getCreatedBy());
			feedItem2.setUpdatedDTTM(feedItem2.getCreatedDTTM());
			
			assertTrue(loader.deleteFeedPlanItem(feedItem1) >= 0);
			assertTrue(loader.deleteFeedPlanItem(feedItem2) >= 0);

			assertEquals(1,loader.insertFeedPlanItem(feedItem1));
			assertEquals(1,loader.insertFeedPlanItem(feedItem2));
						
			FeedPlan plan = loader.retrieveFeedPlan(feedItem1.getOrgID(), feedItem1.getFeedCohortCD().getLookupValueCode());
			assertEquals(2,plan.getFeedPlan().size());
			assertEquals(feedItem1.getFeedCohortCD().getLookupValueCode(),plan.getFeedCohort().getFeedCohortLookupValue().getLookupValueCode());
			assertEquals(feedItem1.getOrgID(),plan.getOrgID());
			assertTrue(plan.getFeedPlan().get(0).getFeedItemLookupValue().getLookupValueCode().equals(feedItem1.getFeedItemLookupValue().getLookupValueCode()) || plan.getFeedPlan().get(0).getFeedItemLookupValue().getLookupValueCode().equals(feedItem2.getFeedItemLookupValue().getLookupValueCode()));
			assertTrue(plan.getFeedPlan().get(1).getFeedItemLookupValue().getLookupValueCode().equals(feedItem1.getFeedItemLookupValue().getLookupValueCode()) || plan.getFeedPlan().get(1).getFeedItemLookupValue().getLookupValueCode().equals(feedItem2.getFeedItemLookupValue().getLookupValueCode()));

			plan = loader.retrieveDistinctFeedItemsInFeedPlan(feedItem1.getOrgID());
			assertTrue(plan.getFeedPlan().size()>= 2);
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while(it.hasNext()) {
				FeedItem item = it.next();
				if (item.getFeedItemLookupValue().getLookupValueCode().equalsIgnoreCase(feedItem1.getFeedItemLookupValue().getLookupValueCode())) {
					assertEquals(null,item.getFeedItemLookupValue().getLongDescription());
					assertEquals(feedItem1.getFeedItemLookupValue().getLookupValueCode(),item.getFeedItemLookupValue().getShortDescription());
				} else 	if (item.getFeedItemLookupValue().getLookupValueCode().equalsIgnoreCase(feedItem2.getFeedItemLookupValue().getLookupValueCode())) {
					assertEquals(null,item.getFeedItemLookupValue().getLongDescription());
					assertEquals(feedItem2.getFeedItemLookupValue().getLookupValueCode(),item.getFeedItemLookupValue().getShortDescription());
				}
			}
			IMDLogger.log(plan.dtoToJson("  "), Util.INFO);
			
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
			dietReq.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
			
			LookupValues cohortLV1 = new LookupValues(Util.LookupValues.FEEDCOHORT,dietReq.getFeedCohortCD(),"","");
			LookupValues cohortLV2 = new LookupValues(Util.LookupValues.FEEDCOHORT,"HEIFER_TST","","");
			
			
			animalTypes.add(new FeedCohort(dietReq.getOrgID(),cohortLV1,""));
			animalTypes.add(new FeedCohort(dietReq.getOrgID(),cohortLV2,""));
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
				assertEquals(Util.getDateTimeInSQLFormat(req.getCreatedDTTM()),Util.getDateTimeInSQLFormat(dietReq.getCreatedDTTM()));
				assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
				assertEquals(Util.getDateTimeInSQLFormat(req.getUpdatedDTTM()),Util.getDateTimeInSQLFormat(dietReq.getUpdatedDTTM()));
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
					assertEquals(Util.getDateTimeInSQLFormat(req.getCreatedDTTM()),Util.getDateTimeInSQLFormat(dietReq.getCreatedDTTM()));
					assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
					assertEquals(Util.getDateTimeInSQLFormat(req.getUpdatedDTTM()),Util.getDateTimeInSQLFormat(dietReq.getUpdatedDTTM()));
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
