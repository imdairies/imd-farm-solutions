package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class AnimalSrvcTest {

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
	void testFarmInseminationScreenRecordSorting() {
		try {
		AnimalSrvc srvc = new AnimalSrvc();
		int originalLoggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.WARNING;
		String responseJson = srvc.getAdultFemaleCows(new AnimalBean()).getEntity().toString();
		
		String[] animalJsons = responseJson.split("\n}");
		if (animalJsons.length > 0) {
			double hours = 9999999;
			for (int i=0; i<animalJsons.length; i++) {
				String animalRecord = animalJsons[i];
				int start = animalRecord.indexOf("hoursSinceInsemination\":");
				if (start >= 0) {
					start = animalRecord.indexOf("hoursSinceInsemination\":") + "hoursSinceInsemination\":\"".length();
					int end = animalRecord.indexOf("\"",start);
					assertTrue(hours >= Double.parseDouble(animalRecord.substring(start,end)),"Animals are not correctly sorted in descending order of their insemination date. Previous record's hoursSinceInsemination was: " + hours + " and the current record's hoursSinceInsemination is: " + 
					Double.parseDouble(animalRecord.substring(start,end)) + " so we encountered an older record which was placed below a recent record - this should have been other way around");
					hours = Double.parseDouble(animalRecord.substring(start,end));
//					IMDLogger.log( "Record " + (i+1) + ") hours are: " + hours,Util.WARNING);
				}
			}
		}
		
		IMDLogger.loggingMode = originalLoggingMode;
		
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred while running unit test: AnimalSrvc.testGetAdultFemaleCows " + ex.getMessage());
		}	
	}
}
