package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.BankDetails;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.MessageManager;
import com.imd.util.Util;

class MilkingDetailLoaderTest {

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

	private MilkingDetail createMilkingRecord(String tagNbr, LocalDate recDate, LocalTime recTime) throws IMDException {
		short milkFreq = 3;
		int milkingHr = 5;
		int milkingMin = 30;
		int milkingSec = 0;
		LocalTime milkingTime = new LocalTime(milkingHr, milkingMin, milkingSec);
		LocalDate milkingDate = new LocalDate(2019,1,1);
		float milkingVol = 13.0f;
		boolean isMachineMilked = true;	
		MilkingDetail milkingRecord = new MilkingDetail("IMD", tagNbr, milkFreq, isMachineMilked, milkingDate, milkingTime, milkingVol,(short)1);
		milkingRecord.setHumidity(50.0f);
		milkingRecord.setTemperatureInCentigrade(19.3f);
		milkingRecord.setLrValue(28.0f);
		milkingRecord.setFatValue(3.80f);
		milkingRecord.setToxinValue(0.11f);
		milkingRecord.setCreatedBy(new User("KASHIF"));
		milkingRecord.setCreatedDTTM(DateTime.now());
		milkingRecord.setUpdatedBy(new User("KASHIF"));
		milkingRecord.setUpdatedDTTM(DateTime.now());
		return milkingRecord;
	}
	@Test
	void testAnimalProcessing() {
		try {
			MilkingDetail milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(5,0,0));
			milkingRecord.setComments("Morning Milking");
			milkingRecord.setMilkingEventNumber((short) 1);
			MilkingDetailLoader loader = new MilkingDetailLoader();

			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,1,1));
			assertEquals(1,loader.insertMilkRecord(milkingRecord), "One record should have been inserted");
			milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(13,0,0));
			milkingRecord.setComments("Noon Milking");
			milkingRecord.setMilkingEventNumber((short) 2);
			assertEquals(1,loader.insertMilkRecord(milkingRecord), "One record should have been inserted");
			milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(21,0,0));
			milkingRecord.setComments("Night Milking");
			milkingRecord.setMilkingEventNumber((short) 3);
			assertEquals(1,loader.insertMilkRecord(milkingRecord), "One record should have been inserted");
			
			List <MilkingDetail>  milkRecords = loader.retrieveAllMilkingRecordsOfCow(milkingRecord);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			boolean found = false;
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getOrgID().equalsIgnoreCase("IMD") && milkRec.getAnimalTag().equalsIgnoreCase("TST")) {
					found = true;
					IMDLogger.log(milkRec.dtoToJson(""), Util.INFO);
					break;
				}
			}
			assertTrue(found, "Tag TST milking record should have been found");
			assertEquals(3, milkRecords.size(), "Three milking records should have been found");
			assertEquals(13.0,milkRec.getMilkVolume(), " Milking volume should have been 13.0");
			assertEquals(Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT),milkRec.getVolUnit(), " Milking volume unit should be LTR");
			assertEquals(28f, milkRec.getLrValue().floatValue(), "LR should be 28");
			assertEquals(3.8f, milkRec.getFatValue().floatValue(), "Fat should be 3.8");
			assertEquals(0.11f, milkRec.getToxinValue().floatValue(), "Toxin should be 0.11 ppm");
			assertEquals(19.3f, milkRec.getTemperatureInCentigrade().floatValue(), "Temp should be 19.3");
			assertEquals(50f, milkRec.getHumidity().floatValue(), "Humidify should be 50%");
			assertEquals("2019-01-01",milkRec.getRecordDate().toString(), " Record Date should have been 2019-01-01");
			assertEquals(5,milkRec.getRecordTime().getHourOfDay(), " Record Time should have been 05:30");
			assertEquals(30,milkRec.getRecordTime().getMinuteOfHour(), " Record Time should have been 05:30");
			assertEquals(1,loader.deleteMilkingRecord("IMD", "TST", new LocalDate(2019,1,1), 1),"One record should have been deleted");
			assertEquals(2,loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,1,1)),"Two records should have been deleted");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}
}
