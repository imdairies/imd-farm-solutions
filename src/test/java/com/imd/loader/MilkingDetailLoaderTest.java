package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.User;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.TestDataCreationUtil;
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

	
	private Dam createDam(String orgID, String damTag, DateTime dob, String animalType) throws IMDException {
		Dam dam = new Dam(orgID,damTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		dam.setAlias(damTag + "alias");
		dam.setBreed(Util.Breed.HFCROSS);
		dam.setAnimalType(animalType);
		dam.setFrontSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/1.png");
		dam.setBackSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/2.png");
		dam.setRightSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/3.png");
		dam.setLeftSideImageURL("/assets/img/cow-thumbnails/" + damTag + "/4.png");

		dam.setPurchaseDate(DateTime.parse("2017-02-08"));
		dam.setCreatedBy(new User("KASHIF"));
		dam.setCreatedDTTM(DateTime.now());
		dam.setHerdJoiningDate(dob);
		dam.setHerdLeavingDate(null);
		dam.setUpdatedBy(dam.getCreatedBy());
		dam.setUpdatedDTTM(dam.getCreatedDTTM());
		dam.setAnimalDam(null);
		Note newNote = new Note (1,"test note", DateTime.now(IMDProperties.getServerTimeZone()));		
		dam.addNote(newNote);
		return dam;		
	}	
	
	private MilkingDetail createMilkingRecord(String tagNbr, LocalDate milkingDate, LocalTime milkingTime) throws IMDException {
		short milkFreq = 3;
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
	void testMiMCalculation() {
		try {
			String animalTag = "-999";
			String orgID = "IMD";
			User user = new User("KASHIF");
			DateTime createdTS = DateTime.now();
			int tenDaysInPast = 10;
			LocalDate recordDate1 = new LocalDate(createdTS.getYear(),createdTS.getMonthOfYear(),createdTS.getDayOfMonth());
			LocalDate recordDateBeforeParturation = recordDate1.minusMonths(1); //new LocalDate(createdTS.getYear(),createdTS.getMonthOfYear()-1,createdTS.getDayOfMonth());
			Dam dam = createDam(orgID, animalTag, DateTime.now().minusYears(4), Util.AnimalTypes.DRYPREG);
			LifecycleEvent parturationEvent = new LifecycleEvent(orgID, 0, animalTag, Util.LifeCycleEvents.PARTURATE, user, createdTS, user, createdTS);
			parturationEvent.setEventTimeStamp(createdTS.minusDays(tenDaysInPast));
			
			MilkingDetail milkingRecord1 = createMilkingRecord(animalTag, recordDate1, new LocalTime(5,0,0));
			milkingRecord1.setComments("Morning Milking");
			milkingRecord1.setMilkingEventNumber((short) 1);

			MilkingDetail milkingRecordBeforeParturation = createMilkingRecord(animalTag, recordDateBeforeParturation, new LocalTime(5,0,0));
			milkingRecordBeforeParturation.setComments("Morning Milking");
			milkingRecordBeforeParturation.setMilkingEventNumber((short) 1);
			
			AnimalLoader animalLoader = new AnimalLoader();
			MilkingDetailLoader milkDetailloader = new MilkingDetailLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();

			milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, animalTag);

			eventsLoader.deleteAnimalLifecycleEvents(orgID, animalTag);
			animalLoader.deleteAnimal(orgID, animalTag);
			
			assertEquals(1,animalLoader.insertAnimal(dam));

			assertNotEquals(MilkingDetailLoader.ANIMAL_IS_NOT_LACTATING,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag));
			
			assertEquals(1,animalLoader.deleteAnimal(orgID, animalTag));
			dam.setAnimalType(Util.AnimalTypes.LACTATING);
			assertEquals(1,animalLoader.insertAnimal(dam));
			
			assertEquals(MilkingDetailLoader.NO_PARTURATION_OR_ABORTION_EVENT_FOUND,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag));
			
			assertTrue(eventsLoader.insertLifeCycleEvent(parturationEvent)>0);

			assertEquals(tenDaysInPast,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecord1.getMilkingDetailBean()));

			assertEquals(tenDaysInPast,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			assertEquals(1,milkDetailloader.insertMilkRecord(milkingRecordBeforeParturation.getMilkingDetailBean()));

			assertEquals(tenDaysInPast,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			milkDetailloader.deleteAllMilkingRecordOfanAnimal(orgID, animalTag);
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents(orgID, animalTag));
			assertEquals(1,animalLoader.deleteAnimal(orgID, animalTag));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}		
	}
	
	
	@Test
	void testAnimalProcessing() {
		try {
			MilkingDetail milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(5,0,0));
			milkingRecord.setComments("Morning Milking");
			milkingRecord.setMilkingEventNumber((short) 1);
			MilkingDetailLoader loader = new MilkingDetailLoader();

//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,2,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,1,1));
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST");
			assertEquals(1,loader.insertMilkRecord(milkingRecord.getMilkingDetailBean()), "One record should have been inserted");
			
			milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(13,0,0));
			milkingRecord.setComments("Noon Milking");
			milkingRecord.setMilkingEventNumber((short) 2);
			assertEquals(1,loader.insertMilkRecord(milkingRecord.getMilkingDetailBean()), "One record should have been inserted");
			milkingRecord = createMilkingRecord("TST", new LocalDate(2019,1,1), new LocalTime(21,0,0));
			milkingRecord.setComments("Night Milking");
			milkingRecord.setMilkingEventNumber((short) 3);
			assertEquals(1,loader.insertMilkRecord(milkingRecord.getMilkingDetailBean()), "One record should have been inserted");
			milkingRecord = createMilkingRecord("TST", new LocalDate(2019,02,1), new LocalTime(5,0,0));
			milkingRecord.setComments("Morning Milking");
			milkingRecord.setMilkingEventNumber((short) 1);
			assertEquals(1,loader.insertMilkRecord(milkingRecord.getMilkingDetailBean()), "One record should have been inserted");
			
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
			assertEquals(4, milkRecords.size(), "Three milking records should have been found");
			assertEquals(13.0f,milkRec.getMilkVolume().floatValue(), " Milking volume should have been 13.0");
			assertEquals(Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT),milkRec.getVolUnit(), " Milking volume unit should be LTR");
			assertEquals(28f, milkRec.getLrValue().floatValue(), "LR should be 28");
			assertEquals(3.8f, milkRec.getFatValue().floatValue(), "Fat should be 3.8");
			assertEquals(0.11f, milkRec.getToxinValue().floatValue(), "Toxin should be 0.11 ppm");
			assertEquals(19.3f, milkRec.getTemperatureInCentigrade().floatValue(), "Temp should be 19.3");
			assertEquals(50f, milkRec.getHumidity().floatValue(), "Humidify should be 50%");
			assertEquals("2019-01-01",milkRec.getRecordDate().toString(), " Record Date should have been 2019-01-01");
			assertEquals(21,milkRec.getRecordTime().getHourOfDay(), " Record Time should have been 21:00");
			assertEquals(0,milkRec.getRecordTime().getMinuteOfHour(), " Record Time should have been 21:00");

			milkingRecord.setRecordDate(new LocalDate(2019,1,1));

			MilkingDetailBean searchBean = new MilkingDetailBean();
			searchBean.setOrgID(milkingRecord.getOrgID());
			searchBean.setAnimalTag(milkingRecord.getAnimalTag());
			searchBean.setRecordDate(milkingRecord.getRecordDate());
			searchBean.setRecordTime(milkingRecord.getRecordTime());
						
			milkRecords = loader.retrieveMonthlyMilkingRecordsOfCow(searchBean);
			it = milkRecords.iterator();
			milkRec = null;
			boolean invalidMonth = false;
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getRecordDate().getMonthOfYear() != 1) {
					invalidMonth = true;
					IMDLogger.log(milkRec.dtoToJson(""), Util.INFO);
					break;
				}
			}
			assertFalse(invalidMonth, "Only Jan milking records should have been shown");			
			assertEquals(3, milkRecords.size(), "Three milking records should have been found for Jan");

			searchBean.setRecordDate(new LocalDate(2019,1,1));
			searchBean.setMilkingEventNumber((short)3);
			milkRecords = loader.retrieveSingleMilkingRecordsOfCow(searchBean, true);
			assertEquals(1, milkRecords.size(), "Exactly one milking record should have been returned");
			milkRec = milkRecords.get(0);
			assertEquals(13.0,milkRec.getMilkVolume().floatValue(), " Milking volume should have been 13.0");
			assertEquals(Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.VOL_UNIT),milkRec.getVolUnit(), " Milking volume unit should be LTR");
			assertEquals(28f, milkRec.getLrValue().floatValue(), "LR should be 28");
			assertEquals(3.8f, milkRec.getFatValue().floatValue(), "Fat should be 3.8");
			assertEquals(0.11f, milkRec.getToxinValue().floatValue(), "Toxin should be 0.11 ppm");
			assertEquals(19.3f, milkRec.getTemperatureInCentigrade().floatValue(), "Temp should be 19.3");
			assertEquals(50f, milkRec.getHumidity().floatValue(), "Humidify should be 50%");
			assertEquals("2019-01-01",milkRec.getRecordDate().toString(), " Record Date should have been 2019-01-01");
			assertEquals(21,milkRec.getRecordTime().getHourOfDay(), " Record Time should have been 21:00");
			assertEquals(0,milkRec.getRecordTime().getMinuteOfHour(), " Record Time should have been 21:00");
			assertTrue(milkRec.getAdditionalStatistics()== null || milkRec.getAdditionalStatistics().size() == 0 ? false: true," There should be some additional statistics e.g. " + Util.MilkingDetailStatistics.SEQ_NBR_MONTHLY_AVERAGE);

//			assertEquals(1,loader.deleteOneMilkingRecord("IMD", "TST", new LocalDate(2019,1,1), 1),"One record should have been deleted");
//			assertEquals(2,loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,1,1)),"Two records should have been deleted");
//			assertEquals(1,loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(2019,2,1)),"Two records should have been deleted");			
			assertEquals(4,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST"),"four records should have been deleted");			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}		
	}
	
	@Test
	void testYearMilkingRecordRetrieval() {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		LocalDate startDate = LocalDate.now(IMDProperties.getServerTimeZone()).plusYears(1);
		try {
			while (!Util.isLeapYear(startDate))
				startDate = startDate.plusYears(1);
			loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(startDate);
			loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedYear(startDate.minusYears(2));
		} catch (Exception e) {
			e.printStackTrace();
			fail("The year " + startDate.getYear() + " would not have any record so the output should be 365 days of empty values not an exception");
		}
	}
	
	@Test
	void testCompleteMilkingRecordRetrieval() {
		IMDLogger.loggingMode = Util.INFO;
		MilkingDetailLoader loader = new MilkingDetailLoader();
		LocalDate startDate = 	new LocalDate(1900,1,1);
		try {
			String orgId = "IMD";
			String animalTag = "-999";
			MilkingDetail milkRec1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate, 1, 15f);
			MilkingDetail milkRec2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate, 2, 15f);
			MilkingDetail milkRec3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate, 3, 12f);
			MilkingDetail milkRec4 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1), 1, 14f);
			MilkingDetail milkRec5 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1), 2, 14f);
			MilkingDetail milkRec6 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1), 3, 15f);
			MilkingDetailLoader milkLoader = new MilkingDetailLoader();
			
			assertTrue(milkLoader.deleteAllMilkingRecordOfanAnimal(orgId, animalTag) >= 0);
			
			assertEquals(1,milkLoader.insertMilkRecord(milkRec1.getMilkingDetailBean()));
			assertEquals(1,milkLoader.insertMilkRecord(milkRec2.getMilkingDetailBean()));
			assertEquals(1,milkLoader.insertMilkRecord(milkRec3.getMilkingDetailBean()));
			assertEquals(1,milkLoader.insertMilkRecord(milkRec4.getMilkingDetailBean()));
			assertEquals(1,milkLoader.insertMilkRecord(milkRec5.getMilkingDetailBean()));
			assertEquals(1,milkLoader.insertMilkRecord(milkRec6.getMilkingDetailBean()));
			
			MilkingDetail allMilkingRecords[] = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(startDate, null);
			assertTrue(allMilkingRecords.length >= 2);
			allMilkingRecords = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(startDate, milkRec6.getRecordDate());
			assertNotEquals(null, allMilkingRecords);
			assertEquals(31,allMilkingRecords.length, " Since we added records for 1900-01-01 therefore we should have only two days of records  1 & 2 Jan 1900 with non zero values and the rest 29 days of Jan should be zero volumes");
			assertTrue(allMilkingRecords[0].getMilkVolume() + 
					allMilkingRecords[1].getMilkVolume() + 
					allMilkingRecords[2].getMilkVolume() == 42f+43f, " Since we added records for 1900-01-01 therefore we should have only two days of records  1 & 2 Jan 1900 with non zero values and the rest 29 days of Jan should be zero volumes");
			assertEquals(1f,allMilkingRecords[0].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(42f,allMilkingRecords[0].getMilkVolume().floatValue());
			assertEquals(1f,allMilkingRecords[1].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(43f,allMilkingRecords[1].getMilkVolume().floatValue());
			assertEquals(0f,allMilkingRecords[30].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(0f,allMilkingRecords[30].getMilkVolume().floatValue());

			assertEquals(6,milkLoader.deleteAllMilkingRecordOfanAnimal(orgId, animalTag));
					
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
	@Test
	void testMonthlyConsolidatedMilkingRecords() {
		try {
			MilkingDetail milkingRecord1_1 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecord1_1.setComments("Morning Milking");
			milkingRecord1_1.setMilkingEventNumber((short) 1);
			milkingRecord1_1.setMilkVolume(10.0f);
			MilkingDetail milkingRecord1_2 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecord1_2.setComments("Afternoon Milking");
			milkingRecord1_2.setMilkingEventNumber((short) 2);
			milkingRecord1_2.setMilkVolume(11.0f);
			MilkingDetail milkingRecord1_3 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecord1_3.setComments("Night Milking");
			milkingRecord1_3.setMilkingEventNumber((short) 3);
			milkingRecord1_3.setMilkVolume(12.0f);
			

			MilkingDetail milkingRecordTSTTST1_1 = createMilkingRecord("TSTTST", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecordTSTTST1_1.setComments("Morning Milking");
			milkingRecordTSTTST1_1.setMilkingEventNumber((short) 1);
			milkingRecordTSTTST1_1.setMilkVolume(12.0f);
			MilkingDetail milkingRecordTSTTST1_2 = createMilkingRecord("TSTTST", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecordTSTTST1_2.setComments("Afternoon Milking");
			milkingRecordTSTTST1_2.setMilkingEventNumber((short) 2);
			milkingRecordTSTTST1_2.setMilkVolume(12.5f);
			MilkingDetail milkingRecordTSTTST1_3 = createMilkingRecord("TSTTST", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecordTSTTST1_3.setComments("Night Milking");
			milkingRecordTSTTST1_3.setMilkingEventNumber((short) 3);
			milkingRecordTSTTST1_3.setMilkVolume(12.5f);
			
			
			MilkingDetail milkingRecord2_1 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(5,0,0));
			milkingRecord2_1.setComments("Morning Milking");
			milkingRecord2_1.setMilkingEventNumber((short) 1);
			milkingRecord2_1.setMilkVolume(13.0f);
			MilkingDetail milkingRecord2_2 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(13,0,0));
			milkingRecord2_2.setComments("Afternoon Milking");
			milkingRecord2_2.setMilkingEventNumber((short) 2);
			milkingRecord2_2.setMilkVolume(14.0f);
			MilkingDetail milkingRecord2_3 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(21,0,0));
			milkingRecord2_3.setComments("Night Milking");
			milkingRecord2_3.setMilkingEventNumber((short) 3);
			milkingRecord2_3.setMilkVolume(15.0f);
			
			MilkingDetail milkingRecord3_1 = createMilkingRecord("TST", new LocalDate(1900,1,10), new LocalTime(5,0,0));
			milkingRecord3_1.setComments("Morning Milking");
			milkingRecord3_1.setMilkingEventNumber((short) 1);
			milkingRecord3_1.setMilkVolume(16.0f);
			MilkingDetail milkingRecord3_2 = createMilkingRecord("TST", new LocalDate(1900,1,10), new LocalTime(13,0,0));
			milkingRecord3_2.setComments("Afternoon Milking");
			milkingRecord3_2.setMilkingEventNumber((short) 2);
			milkingRecord3_2.setMilkVolume(17.0f);
			MilkingDetail milkingRecord3_3 = createMilkingRecord("TST", new LocalDate(1900,1,10), new LocalTime(21,0,0));
			milkingRecord3_3.setComments("Night Milking");
			milkingRecord3_3.setMilkingEventNumber((short) 3);
			milkingRecord3_3.setMilkVolume(18.0f);
			

			
			MilkingDetail milkingRecord4_1 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(5,0,0));
			milkingRecord4_1.setComments("Morning Milking");
			milkingRecord4_1.setMilkingEventNumber((short) 1);
			milkingRecord4_1.setMilkVolume(16.0f);
			MilkingDetail milkingRecord4_2 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(13,0,0));
			milkingRecord4_2.setComments("Afternoon Milking");
			milkingRecord4_2.setMilkingEventNumber((short) 2);
			milkingRecord4_2.setMilkVolume(17.0f);
			MilkingDetail milkingRecord4_3 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(21,0,0));
			milkingRecord4_3.setComments("Night Milking");
			milkingRecord4_3.setMilkingEventNumber((short) 3);
			milkingRecord4_3.setMilkVolume(18.0f);
			
			
			
			
			MilkingDetailLoader loader = new MilkingDetailLoader();

//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TSTTST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,2));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,10));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST_TST", new LocalDate(1900,12,31));

			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST");
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TSTTST");
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST_TST");


			assertEquals(1,loader.insertMilkRecord(milkingRecord1_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_3.getMilkingDetailBean()), "One record should have been inserted");

			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTST1_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTST1_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTST1_3.getMilkingDetailBean()), "One record should have been inserted");

			
			assertEquals(1,loader.insertMilkRecord(milkingRecord2_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord2_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord2_3.getMilkingDetailBean()), "One record should have been inserted");

			assertEquals(1,loader.insertMilkRecord(milkingRecord3_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord3_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord3_3.getMilkingDetailBean()), "One record should have been inserted");

			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedMonth(new LocalDate(1900,3,1), true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			assertEquals(31,milkRecords.size(),"Even if there is no milk record for a month, we still expected to receive 31 empty records for the month of March 1900.");
			MilkingDetail milkRec = null;
			int noRecordDays = 0;
			int recordDays = 0;
			int totalMonthVolume = 0;
			float[] volumes = new float[3];
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getMilkVolume() > 0) {
					volumes[recordDays++] = milkRec.getMilkVolume();
					assertEquals(milkRec.getMilkVolume().floatValue(), (float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE), "Only one animal milked on this day so the average should be the same as the milk volume of that animal");
					totalMonthVolume += milkRec.getMilkVolume();
				}
				else 
					noRecordDays++;
			}
			assertEquals(0, recordDays, "No day of this month should have any milking information");
			assertEquals(31, noRecordDays, "31 days should have empty milking information");
			
			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedMonth(new LocalDate(1900,1,1), true);
			it = milkRecords.iterator();
			assertEquals(31, milkRecords.size(), "Jan 1900 should have 31 milk days.");
			milkRec = null;
			noRecordDays = 0;
			recordDays = 0;
			totalMonthVolume = 0;
			volumes = new float[3];
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getMilkVolume() > 0) {
					volumes[recordDays++] = milkRec.getMilkVolume();
					totalMonthVolume += milkRec.getMilkVolume();
				}
				else 
					noRecordDays++;
			}
			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedMonth(new LocalDate(1900,1,1), false);
			it = milkRecords.iterator();
			assertEquals(3, milkRecords.size(), "Only 3 days of record should have been returned, because we only recorded information for 3 days");
			milkRec = null;
			noRecordDays = 0;
			recordDays = 0;
			totalMonthVolume = 0;
			volumes = new float[3];
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getMilkVolume() > 0) {
					if (recordDays == 0) {
						assertEquals(35.0f, (float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE), "Daily average should have been 35");						
					} else {
						assertEquals(milkRec.getMilkVolume().floatValue(), (float)milkRec.getAdditionalStatistics().get(Util.MilkingDetailStatistics.DAILY_AVERAGE), "Only one animal milked on this day so the average should be the same as the milk volume of that animal");	
					}					
					volumes[recordDays++] = milkRec.getMilkVolume();
					totalMonthVolume += milkRec.getMilkVolume();
				}
				else 
					noRecordDays++;
			}
			assertEquals(3, recordDays, "3 days should have any record");
			assertEquals(0, noRecordDays, "28 days should not have any record");
			assertEquals(163.0,totalMonthVolume, " Milking volume for the month should have been 126.0");
			assertEquals(70.0,volumes[0], " first volume should be 70");
			assertEquals(42.0,volumes[1], " second volume should be 42");
			assertEquals(51.0,volumes[2], " third volume should be 33");
			
			LocalDate startDate = new LocalDate(milkingRecord1_1.getRecordDate().getYear(),milkingRecord1_1.getRecordDate().getMonthOfYear(), 1);
			LocalDate endDate = startDate.plusMonths(2).minusDays(1);
			
			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(milkingRecord1_1.getOrgID(), 
					milkingRecord1_1.getAnimalTag(), startDate, endDate,true);
			it = milkRecords.iterator();
			IMDLogger.log(startDate + " " + endDate, Util.INFO);
			assertEquals(Util.getDaysBetween(endDate, startDate)+1, milkRecords.size());
			milkRec = null;
			noRecordDays = 0;
			recordDays = 0;
			totalMonthVolume = 0;
			volumes = new float[3];
			while (it.hasNext()) {
				milkRec = it.next();
				float dailyVol = milkingRecord1_1.getMilkVolume() + milkingRecord1_2.getMilkVolume() + milkingRecord1_3.getMilkVolume();
				assertEquals(dailyVol, (float)milkRec.getMilkVolume());
				IMDLogger.log(milkRec.getAnimalTag() + " " + milkRec.getRecordDate() + " " + milkRec.getMilkVolume(), Util.INFO);
				break;
			}			
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TSTTST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,2));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,10));

			assertEquals(9,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST"));
			assertEquals(3,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TSTTST"));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Milk Information testing failed.");
		}
	}
	
	

	@Test
	void testYearlyConsolidatedMilkingRecords() {
		try {
			MilkingDetail milkingRecord1_1_1 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecord1_1_1.setComments("Morning Milking");
			milkingRecord1_1_1.setMilkingEventNumber((short) 1);
			milkingRecord1_1_1.setMilkVolume(10.0f);
			milkingRecord1_1_1.setLrValue(28f);
			milkingRecord1_1_1.setFatValue(3.8f);
			
			MilkingDetail milkingRecord1_1_2 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecord1_1_2.setComments("Afternoon Milking");
			milkingRecord1_1_2.setMilkingEventNumber((short) 2);
			milkingRecord1_1_2.setMilkVolume(11.0f);
			milkingRecord1_1_2.setLrValue(28f);
			milkingRecord1_1_2.setFatValue(3.9f);

			MilkingDetail milkingRecord1_1_3 = createMilkingRecord("TST", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecord1_1_3.setComments("Night Milking");
			milkingRecord1_1_3.setMilkingEventNumber((short) 3);
			milkingRecord1_1_3.setMilkVolume(12.0f);
			milkingRecord1_1_3.setLrValue(28f);
			milkingRecord1_1_3.setFatValue(4.0f);
			

			MilkingDetail milkingRecord1_2_1 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(5,0,0));
			milkingRecord1_2_1.setComments("Morning Milking");
			milkingRecord1_2_1.setMilkingEventNumber((short) 1);
			milkingRecord1_2_1.setMilkVolume(13.0f);
			milkingRecord1_2_1.setLrValue(30f);
			milkingRecord1_2_1.setFatValue(4.0f);
			milkingRecord1_2_1.setTemperatureInCentigrade(6.5f);

			MilkingDetail milkingRecord1_2_2 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(13,0,0));
			milkingRecord1_2_2.setComments("Afternoon Milking");
			milkingRecord1_2_2.setMilkingEventNumber((short) 2);
			milkingRecord1_2_2.setMilkVolume(14.0f);
			milkingRecord1_2_2.setLrValue(28f);
			milkingRecord1_2_2.setFatValue(3.95f);
			milkingRecord1_2_2.setTemperatureInCentigrade(20.5f);

			MilkingDetail milkingRecord1_2_3 = createMilkingRecord("TST", new LocalDate(1900,1,2), new LocalTime(21,0,0));
			milkingRecord1_2_3.setComments("Night Milking");
			milkingRecord1_2_3.setMilkingEventNumber((short) 3);
			milkingRecord1_2_3.setMilkVolume(15.0f);
			milkingRecord1_2_3.setLrValue(27.5f);
			milkingRecord1_2_3.setFatValue(3.85f);
			milkingRecord1_2_3.setTemperatureInCentigrade(18.5f);

			// TOTAL MILK Month of Jan 1900: 75 ltrs
			

			MilkingDetail milkingRecord2_10_1 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(5,0,0));
			milkingRecord2_10_1.setComments("Morning Milking");
			milkingRecord2_10_1.setMilkingEventNumber((short) 1);
			milkingRecord2_10_1.setMilkVolume(20.0f);
			MilkingDetail milkingRecord2_10_2 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(13,0,0));
			milkingRecord2_10_2.setComments("Afternoon Milking");
			milkingRecord2_10_2.setMilkingEventNumber((short) 2);
			milkingRecord2_10_2.setMilkVolume(30.0f);
			MilkingDetail milkingRecord2_10_3 = createMilkingRecord("TST", new LocalDate(1900,2,10), new LocalTime(21,0,0));
			milkingRecord2_10_3.setComments("Night Milking");
			milkingRecord2_10_3.setMilkingEventNumber((short) 3);
			milkingRecord2_10_3.setMilkVolume(40.0f);
			milkingRecord2_10_3.setLrValue(27.0f);
			milkingRecord2_10_3.setFatValue(3.8f);
			milkingRecord2_10_3.setTemperatureInCentigrade(20.5f);

			
			MilkingDetail milkingRecordTSTTSTS2_10_1 = createMilkingRecord("TST_TST", new LocalDate(1900,12,31), new LocalTime(5,0,0));
			milkingRecordTSTTSTS2_10_1.setComments("Morning Milking");
			milkingRecordTSTTSTS2_10_1.setMilkingEventNumber((short) 1);
			milkingRecordTSTTSTS2_10_1.setMilkVolume(20.0f);
			MilkingDetail milkingRecordTSTTSTS2_10_2 = createMilkingRecord("TST_TST", new LocalDate(1900,12,31), new LocalTime(13,0,0));
			milkingRecordTSTTSTS2_10_2.setComments("Afternoon Milking");
			milkingRecordTSTTSTS2_10_2.setMilkingEventNumber((short) 2);
			milkingRecordTSTTSTS2_10_2.setMilkVolume(25.0f);
			MilkingDetail milkingRecordTSTTSTS2_10_3 = createMilkingRecord("TST_TST", new LocalDate(1900,12,31), new LocalTime(21,0,0));
			milkingRecordTSTTSTS2_10_3.setComments("Night Milking");
			milkingRecordTSTTSTS2_10_3.setMilkingEventNumber((short) 3);
			milkingRecordTSTTSTS2_10_3.setMilkVolume(30.0f);
			
			// TOTAL MILK Month of Feb 1900: 165 ltrs
			
			// TOTAL Milk year 1900: 240 ltrs. In Jan 75, in Feb 165. other months zero.

			
			
			MilkingDetailLoader loader = new MilkingDetailLoader();

//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TSTTST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,2));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,2,10));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST_TST", new LocalDate(1900,12,31));

			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST");
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TSTTST");
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST_TST");


			assertEquals(1,loader.insertMilkRecord(milkingRecord1_1_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_1_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_1_3.getMilkingDetailBean()), "One record should have been inserted");

			assertEquals(1,loader.insertMilkRecord(milkingRecord1_2_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_2_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord1_2_3.getMilkingDetailBean()), "One record should have been inserted");

			assertEquals(1,loader.insertMilkRecord(milkingRecord2_10_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord2_10_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecord2_10_3.getMilkingDetailBean()), "One record should have been inserted");

			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTSTS2_10_1.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTSTS2_10_2.getMilkingDetailBean()), "One record should have been inserted");
			assertEquals(1,loader.insertMilkRecord(milkingRecordTSTTSTS2_10_3.getMilkingDetailBean()), "One record should have been inserted");

			List <MilkingDetail>  milkRecords = loader.retrieveFarmMonthlyMilkVolumeForSpecifiedYear(new LocalDate(1900,1,1), true);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			assertEquals(12, milkRecords.size(), "12 months information should have been returned.");
			MilkingDetail milkRec = null;
			int noRecordMonths = 0;
			int recordMonths = 0;
			int totalMonthVolume = 0;
			float[] volumes = new float[3];
			int[] months = new int[3];
			
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getMilkVolume() > 0) {
					volumes[recordMonths++] = milkRec.getMilkVolume();
					totalMonthVolume += milkRec.getMilkVolume();
					months[recordMonths-1] = milkRec.getRecordDate().getMonthOfYear();
					if (milkRec.getRecordDate().getMonthOfYear() == 1) {
						assertEquals(75f, milkRec.getMilkVolume().floatValue(), "Month of Jan milk volume should be 75 liters");
						assertEquals(28.25f, (float)milkRec.getLrValue(), "Average LR of Jan should be 28 liters");
						assertEquals("3.916", milkRec.getFatValue().toString().substring(0, 5), "Average Fat of Jan should be 3.9 liters");
						//assertEquals(11.375f,(float) milkRec.getTemperatureInCentigrade(), "Average Temp of Jan should be 3.9 liters");
					}
				}
				else 
					noRecordMonths++;
			}
			assertEquals(3, recordMonths, "3 months (Jan, Feb & Dec) should have any record");
			assertEquals(1,months[0], " first month should be Jan");
			assertEquals(2,months[1], " the second month should be Feb");
			assertEquals(12,months[2], " the third month should be Dec");
			assertEquals(9, noRecordMonths, "9 months should not have any record");
			assertEquals(240.0,totalMonthVolume, " Milking volume for the year should have been 240");

			
			milkRecords = loader.retrieveFarmMonthlyMilkVolumeForSpecifiedYear(new LocalDate(1900,1,1), false);
			it = milkRecords.iterator();
			assertEquals(3, milkRecords.size(), "3 months information should have been returned.");
			milkRec = null;
			noRecordMonths = 0;
			recordMonths = 0;
			totalMonthVolume = 0;
			volumes = new float[3];
			months = new int[3];
			
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getMilkVolume() > 0) {
					volumes[recordMonths++] = milkRec.getMilkVolume();
					totalMonthVolume += milkRec.getMilkVolume();
					months[recordMonths-1] = milkRec.getRecordDate().getMonthOfYear();
				}
				else 
					noRecordMonths++;
			}
			assertEquals(3, recordMonths, "3 months (Jan and Feb) should have any record");
			assertEquals(1,months[0], " first month should be Jan");
			assertEquals(2,months[1], " the second month should be Feb");
			assertEquals(12,months[2], " the third month should be Dec");
			assertEquals(0, noRecordMonths, "we should not have received months with no record in result");
			assertEquals(240.0,totalMonthVolume, " Milking volume for the year should have been 240");			
			
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,1,2));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST", new LocalDate(1900,2,10));
//			loader.deleteMilkingRecordOfaDay("IMD", "TST_TST", new LocalDate(1900,2,10));

			assertEquals(9,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST"));
			assertEquals(0,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TSTTST"));
			assertEquals(3,loader.deleteAllMilkingRecordOfanAnimal("IMD", "TST_TST"));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}	
}

