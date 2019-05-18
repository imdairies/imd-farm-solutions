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

import com.imd.dto.DietRequirement;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.User;
import com.imd.util.Util;

class DietLoaderTest {

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
	void testDietRequirementInsertion() {
		try {
			DietLoader loader = new DietLoader();
			DietRequirement dietReq = new DietRequirement();
			dietReq.setOrgID("IMD");
			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("LAC_TST","",""));
			dietReq.setStart(0);
			dietReq.setEnd(0);
			dietReq.setDryMatter(3f); //3% of body weight
			dietReq.setCrudeProtein(18f); //18% of DM
			dietReq.setMetabolizableEnergy(3.1f);
			dietReq.setCreatedBy(new User("KASHIF"));
			dietReq.setCreatedDTTM(DateTime.now());
			dietReq.setUpdatedBy(dietReq.getCreatedBy());
			dietReq.setUpdatedDTTM(dietReq.getCreatedDTTM());
			assertTrue(loader.deleteDietRequirement(dietReq) >= 0);
			assertEquals(1,loader.insertDietRequirement(dietReq));
			assertEquals(1,loader.deleteDietRequirement(dietReq));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}
	@Test
	void testDietRequirementRetrieval() {
		try {
			DietLoader loader = new DietLoader();
			DietRequirement dietReq = new DietRequirement();
			dietReq.setOrgID("IMD");
			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("LAC_TST","",""));
			dietReq.setStart(0);
			dietReq.setEnd(0);
			dietReq.setDryMatter(3f); //3% of body weight
			dietReq.setCrudeProtein(18f); //18% of DM
			dietReq.setMetabolizableEnergy(3.1f);
			dietReq.setCreatedBy(new User("KASHIF"));
			dietReq.setCreatedDTTM(DateTime.now());
			dietReq.setUpdatedBy(dietReq.getCreatedBy());
			dietReq.setUpdatedDTTM(dietReq.getCreatedDTTM());
			assertTrue(loader.deleteDietRequirement(dietReq) >= 0);

			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("HEIFER_TST","",""));
			assertTrue(loader.deleteDietRequirement(dietReq) >= 0);

			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("LAC_TST","",""));
			assertEquals(1,loader.insertDietRequirement(dietReq));

			List<LifeCycleEventCode> animalTypes = new ArrayList<LifeCycleEventCode> ();
			animalTypes.add(dietReq.getApplicableAimalType());
			animalTypes.add(new LifeCycleEventCode("HEIFER_TST","",""));
			List<DietRequirement> reqs = loader.getDietRequirements(dietReq.getOrgID(),animalTypes);
			assertEquals(1,reqs.size());
			Iterator<DietRequirement> it = reqs.iterator();
			while (it.hasNext()) {
				DietRequirement req = it.next();
				assertEquals(req.getApplicableAimalType().getEventCode(),dietReq.getApplicableAimalType().getEventCode());
				assertEquals(req.getDryMatter(),dietReq.getDryMatter());
				assertEquals(req.getCrudeProtein(),dietReq.getCrudeProtein());
				assertEquals(req.getMetabolizableEnergy(),dietReq.getMetabolizableEnergy());
				assertEquals(req.getCreatedBy().getUserId(),dietReq.getCreatedBy().getUserId());
				assertEquals(Util.getDateInSQLFormart(req.getCreatedDTTM()),Util.getDateInSQLFormart(dietReq.getCreatedDTTM()));
				assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
				assertEquals(Util.getDateInSQLFormart(req.getUpdatedDTTM()),Util.getDateInSQLFormart(dietReq.getUpdatedDTTM()));
				break;
			}
			
			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("HEIFER_TST","",""));
			assertEquals(1,loader.insertDietRequirement(dietReq));
			
			reqs = loader.getDietRequirements(dietReq.getOrgID(),animalTypes);
			assertEquals(2,reqs.size());
			it = reqs.iterator();
			while (it.hasNext()) {
				DietRequirement req = it.next();
				if (req.getApplicableAimalType().getEventCode().equals("HEIFER_TST")) {
					assertEquals(req.getApplicableAimalType().getEventCode(),dietReq.getApplicableAimalType().getEventCode());
					assertEquals(req.getDryMatter(),dietReq.getDryMatter());
					assertEquals(req.getCrudeProtein(),dietReq.getCrudeProtein());
					assertEquals(req.getMetabolizableEnergy(),dietReq.getMetabolizableEnergy());
					assertEquals(req.getCreatedBy().getUserId(),dietReq.getCreatedBy().getUserId());
					assertEquals(Util.getDateInSQLFormart(req.getCreatedDTTM()),Util.getDateInSQLFormart(dietReq.getCreatedDTTM()));
					assertEquals(req.getUpdatedBy().getUserId(),dietReq.getUpdatedBy().getUserId());
					assertEquals(Util.getDateInSQLFormart(req.getUpdatedDTTM()),Util.getDateInSQLFormart(dietReq.getUpdatedDTTM()));
				}
			}
			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("LAC_TST","",""));
			assertEquals(1,loader.deleteDietRequirement(dietReq));
			dietReq.setApplicableAimalTypes(new LifeCycleEventCode("HEIFER_TST","",""));
			assertEquals(1,loader.deleteDietRequirement(dietReq));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception Occurred");
		}
	}
}
