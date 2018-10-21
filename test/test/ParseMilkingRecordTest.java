package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.MilkingDetail;
import com.imd.util.IMDException;
import com.imd.util.ParseMilkingRecord;

class ParseMilkingRecordTest {

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
	void test() {
		try {
			HashMap <String, MilkingDetail> milkingReport = ParseMilkingRecord.readMilkingRecordsFromSMS("041{6\n001=4.25\n014:9\n034[5.75\n026{13\nM20{8\n" + 
					"17 7.5\n15{4.5\n21{9\n", LocalDate.now(),(short)3,1);
			assertEquals(9,milkingReport.size());
			assertEquals(6.0f,milkingReport.get("041").getMilkVolume());
			assertEquals(4.25f,milkingReport.get("001").getMilkVolume());
			assertEquals(9.0f,milkingReport.get("014").getMilkVolume());
			assertEquals(5.75f,milkingReport.get("034").getMilkVolume());
			assertEquals(13.0f,milkingReport.get("026").getMilkVolume());
			assertEquals(8.0f,milkingReport.get("M20").getMilkVolume());
			assertEquals(7.5f,milkingReport.get("17").getMilkVolume());
			assertEquals(4.5f,milkingReport.get("15").getMilkVolume());
			assertEquals(9.0f,milkingReport.get("21").getMilkVolume());
			assertEquals(1,milkingReport.get("21").getMilkingEventNumber().shortValue());
			
			 milkingReport = ParseMilkingRecord.readMilkingRecordsFromSMS("041{5a\n001=3.5\n014:7\n034[5\n026{11.5\n20{6.5\n17 6\n15{3.5\n21{8z\n", LocalDate.now(),(short)3,2);
			assertEquals(7,milkingReport.size());
			assertTrue(milkingReport.get("041") == null , " Tag 041 had an invalid record so it's record should not have been parsed.");
			assertTrue(milkingReport.get("21") == null , " Tag 21 had an invalid record so it's record should not have been parsed.");
			assertEquals(2,milkingReport.get("17").getMilkingEventNumber().shortValue());
			
		} catch (IMDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	void testCSVFileLoad() {
		try
		{
			String fileURI = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "26_RECORD.csv";
			Map<LocalDate, HashMap<Integer,MilkingDetail>> milkingReport = ParseMilkingRecord.readMilkingRecordsFromCSV(fileURI);
			float actualVol = milkingReport.get(LocalDate.parse("2018-02-19")).get(new Integer(1)).getMilkVolume();
			assertEquals(13.0f,actualVol);
			assertEquals(283,milkingReport.size());

		} catch (IMDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Could not load CSV file");
		}
	}
}
