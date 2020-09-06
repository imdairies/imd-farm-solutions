package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

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

import com.imd.dto.Animal;
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
		TestDataCreationUtil.deleteFarmMilkingRecordsWithNoAnimalMilkingEvent("IMD");
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
		dam.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
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
		milkingRecord.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		milkingRecord.setUpdatedBy(new User("KASHIF"));
		milkingRecord.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		return milkingRecord;
	}
	
	
	@Test
	void testMiMCalculation() {
		try {
			String animalTag = "-999";
			String orgID = "IMD";
			User user = new User("KASHIF");
			DateTime createdTS = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20);
			int tenDaysInPast = 10;
			LocalDate recordDate1 = new LocalDate(createdTS.getYear(),createdTS.getMonthOfYear(),createdTS.getDayOfMonth());
			LocalDate recordDateBeforeParturation = recordDate1.minusMonths(1); //new LocalDate(createdTS.getYear(),createdTS.getMonthOfYear()-1,createdTS.getDayOfMonth());
			Dam dam = createDam(orgID, animalTag, createdTS.minusYears(4), Util.AnimalTypes.DRYPREG);
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

			TestDataCreationUtil.deleteAllAnimalEvents(orgID, animalTag);
			TestDataCreationUtil.deleteAnimal(orgID, animalTag);
			
			assertEquals(1,animalLoader.insertAnimal(dam));

			assertNotEquals(MilkingDetailLoader.ANIMAL_IS_NOT_LACTATING,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag));
			
			assertEquals(1,animalLoader.deleteAnimal(orgID, animalTag));
			dam.setAnimalType(Util.AnimalTypes.LACTATING);
			assertEquals(1,animalLoader.insertAnimal(dam));
			
			assertEquals(MilkingDetailLoader.NO_PARTURATION_OR_ABORTION_EVENT_FOUND,milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag));
			
			assertTrue(eventsLoader.insertLifeCycleEvent(parturationEvent)>0);

			assertEquals(Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), createdTS.minusDays(tenDaysInPast)),milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1));

			assertEquals(Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), createdTS.minusDays(tenDaysInPast)),milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordBeforeParturation));

			assertEquals(Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), createdTS.minusDays(tenDaysInPast)),milkDetailloader.getDaysInMilkingOfCow(orgID, animalTag).intValue());
			
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgID, animalTag) >=0 );
			assertEquals(1,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecordBeforeParturation.getOrgId(),milkingRecordBeforeParturation.getRecordDate(),null));
			assertEquals(1,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1.getOrgId(),milkingRecord1.getRecordDate(),null));
			
			
			assertEquals(1,TestDataCreationUtil.deleteAllAnimalEvents(orgID, animalTag));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(orgID, animalTag));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}		
	}
	
	
	@Test
	void testAnimalProcessing() {
		try {
			LocalDate referenceDate = new LocalDate(LocalDate.now(IMDProperties.getServerTimeZone()).minusYears(20).getYear(),1,1);
			MilkingDetail milkingRecord1_1 = createMilkingRecord("-990", referenceDate, new LocalTime(5,0,0));
			milkingRecord1_1.setComments("Morning Milking");
			milkingRecord1_1.setMilkingEventNumber((short) 1);
			MilkingDetailLoader loader = new MilkingDetailLoader();
			
			
			Animal animal = TestDataCreationUtil.createTestAnimal(milkingRecord1_1.getOrgId(), milkingRecord1_1.getAnimalTag(), 
					new DateTime(milkingRecord1_1.getRecordDate().minusYears(3).getYear(),
							milkingRecord1_1.getRecordDate().minusYears(3).getMonthOfYear(),
							milkingRecord1_1.getRecordDate().minusYears(3).getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),
					true);

			
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(milkingRecord1_1.getOrgId(), milkingRecord1_1.getAnimalTag()) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);

			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));
			
			

//			loader.deleteMilkingRecordOfaDay("IMD", "-990", new LocalDate(2019,2,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "-990", new LocalDate(2019,1,1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1));
			
			MilkingDetail milkingRecord1_2 = createMilkingRecord(milkingRecord1_1.getAnimalTag(), milkingRecord1_1.getRecordDate(), new LocalTime(13,0,0));
			milkingRecord1_2.setComments("Noon Milking");
			milkingRecord1_2.setMilkingEventNumber((short) 2);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2));
			
			MilkingDetail milkingRecord1_3 = createMilkingRecord(milkingRecord1_2.getAnimalTag(), milkingRecord1_2.getRecordDate(),  new LocalTime(21,0,0));
			milkingRecord1_3.setComments("Night Milking");
			milkingRecord1_3.setMilkingEventNumber((short) 3);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_3));
			
			MilkingDetail milkingRecord2_1 = createMilkingRecord(milkingRecord1_1.getAnimalTag(), milkingRecord1_3.getRecordDate().plusMonths(1), new LocalTime(5,0,0));
			milkingRecord2_1.setComments("Morning Milking");
			milkingRecord2_1.setMilkingEventNumber((short) 1);
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_1));
			
			List <MilkingDetail>  milkRecords = loader.retrieveAllMilkingRecordsOfCow(milkingRecord1_1);
			Iterator<MilkingDetail> it = milkRecords.iterator();
			MilkingDetail milkRec = null;
			boolean found = false;
			while (it.hasNext()) {
				milkRec = it.next();
				if (milkRec.getOrgId().equalsIgnoreCase("IMD") && milkRec.getAnimalTag().equalsIgnoreCase("-990")) {
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
			assertEquals(milkingRecord1_1.getRecordDate().toString(),milkRec.getRecordDate().toString());
			assertEquals(21,milkRec.getRecordTime().getHourOfDay(), " Record Time should have been 21:00");
			assertEquals(0,milkRec.getRecordTime().getMinuteOfHour(), " Record Time should have been 21:00");


			MilkingDetailBean searchBean = new MilkingDetailBean();
			searchBean.setOrgID(milkingRecord1_1.getOrgId());
			searchBean.setAnimalTag(milkingRecord1_1.getAnimalTag());
			searchBean.setRecordDate(milkingRecord1_1.getRecordDate());
			searchBean.setRecordTime(milkingRecord1_1.getRecordTime());
						
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

			searchBean.setRecordDate(milkingRecord2_1.getRecordDate());
			searchBean.setMilkingEventNumber(milkingRecord2_1.getMilkingEventNumber());
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
			assertEquals(milkingRecord2_1.getRecordDate().toString(),milkRec.getRecordDate().toString());
			assertEquals(milkingRecord2_1.getRecordTime().getHourOfDay(),milkRec.getRecordTime().getHourOfDay());
			assertEquals(0,milkRec.getRecordTime().getMinuteOfHour());
			assertTrue(milkRec.getAdditionalStatistics()== null || milkRec.getAdditionalStatistics().size() == 0 ? false: true," There should be some additional statistics e.g. " + Util.MilkingDetailStatistics.SEQ_NBR_MONTHLY_AVERAGE);

			assertEquals(4,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(milkingRecord2_1.getOrgId(), milkingRecord2_1.getAnimalTag()),"four records should have been deleted");	
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_1.getOrgId(),milkingRecord1_1.getRecordDate(),null));
			assertEquals(1,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord2_1.getOrgId(),milkingRecord2_1.getRecordDate(),null));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));

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
			
			
			MilkingDetail milkRec1_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"04:00", 1, 15f);
			MilkingDetail milkRec1_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"12:00", 2, 15f);
			MilkingDetail milkRec1_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"20:00", 3, 12f);
			MilkingDetail milkRec2_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1),"04:00",  1, 14f);
			MilkingDetail milkRec2_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1),"12:00",  2, 14f);
			MilkingDetail milkRec2_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusDays(1),"20:00",  3, 15f);

			
			Animal animal = TestDataCreationUtil.createTestAnimal(milkRec1_1.getOrgId(), milkRec1_1.getAnimalTag(), 
					new DateTime(startDate.minusYears(3).getYear(),
							startDate.minusYears(3).getMonthOfYear(),
							startDate.minusYears(3).getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),
					true);

			
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgId, animalTag) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal) >= 0);

			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec1_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec1_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec1_3));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec2_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec2_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkRec2_3));
			
			
			MilkingDetail allMilkingRecords[] = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(startDate, null);
			assertTrue(allMilkingRecords.length >= 2);
			allMilkingRecords = loader.retrieveFarmMilkVolumeForEachDayOfSpecifiedDateRange(startDate, milkRec2_3.getRecordDate());
			assertNotEquals(null, allMilkingRecords);
			assertEquals(31,allMilkingRecords.length);
			assertEquals(42f+43f,allMilkingRecords[0].getMilkVolume() + allMilkingRecords[1].getMilkVolume());
			assertEquals(1f,allMilkingRecords[0].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(42f,allMilkingRecords[0].getMilkVolume().floatValue());
			assertEquals(1f,allMilkingRecords[1].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(43f,allMilkingRecords[1].getMilkVolume().floatValue());
			assertEquals(0f,allMilkingRecords[30].getAdditionalStatistics().get(Util.MilkingDetailStatistics.LACTATING_ANIMALS_COUNT).floatValue());
			assertEquals(0f,allMilkingRecords[30].getMilkVolume().floatValue());

			assertEquals(6,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgId, animalTag));

			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkRec1_1.getOrgId(),milkRec1_1.getRecordDate(),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkRec2_1.getOrgId(),milkRec2_1.getRecordDate(),null));
			
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));

					
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
	@Test
	void testMonthlyConsolidatedMilkingRecords() {
		try {
			MilkingDetail milkingRecord1_1 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecord1_1.setComments("Morning Milking");
			milkingRecord1_1.setMilkingEventNumber((short) 1);
			milkingRecord1_1.setMilkVolume(10.0f);
			MilkingDetail milkingRecord1_2 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecord1_2.setComments("Afternoon Milking");
			milkingRecord1_2.setMilkingEventNumber((short) 2);
			milkingRecord1_2.setMilkVolume(11.0f);
			MilkingDetail milkingRecord1_3 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecord1_3.setComments("Night Milking");
			milkingRecord1_3.setMilkingEventNumber((short) 3);
			milkingRecord1_3.setMilkVolume(12.0f);
			

			MilkingDetail milkingRecordTSTTST1_1 = createMilkingRecord("-990990", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecordTSTTST1_1.setComments("Morning Milking");
			milkingRecordTSTTST1_1.setMilkingEventNumber((short) 1);
			milkingRecordTSTTST1_1.setMilkVolume(12.0f);
			MilkingDetail milkingRecordTSTTST1_2 = createMilkingRecord("-990990", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecordTSTTST1_2.setComments("Afternoon Milking");
			milkingRecordTSTTST1_2.setMilkingEventNumber((short) 2);
			milkingRecordTSTTST1_2.setMilkVolume(12.5f);
			MilkingDetail milkingRecordTSTTST1_3 = createMilkingRecord("-990990", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecordTSTTST1_3.setComments("Night Milking");
			milkingRecordTSTTST1_3.setMilkingEventNumber((short) 3);
			milkingRecordTSTTST1_3.setMilkVolume(12.5f);
			
			
			MilkingDetail milkingRecord2_1 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(5,0,0));
			milkingRecord2_1.setComments("Morning Milking");
			milkingRecord2_1.setMilkingEventNumber((short) 1);
			milkingRecord2_1.setMilkVolume(13.0f);
			MilkingDetail milkingRecord2_2 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(13,0,0));
			milkingRecord2_2.setComments("Afternoon Milking");
			milkingRecord2_2.setMilkingEventNumber((short) 2);
			milkingRecord2_2.setMilkVolume(14.0f);
			MilkingDetail milkingRecord2_3 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(21,0,0));
			milkingRecord2_3.setComments("Night Milking");
			milkingRecord2_3.setMilkingEventNumber((short) 3);
			milkingRecord2_3.setMilkVolume(15.0f);
			
			MilkingDetail milkingRecord3_1 = createMilkingRecord("-990", new LocalDate(1900,1,10), new LocalTime(5,0,0));
			milkingRecord3_1.setComments("Morning Milking");
			milkingRecord3_1.setMilkingEventNumber((short) 1);
			milkingRecord3_1.setMilkVolume(16.0f);
			MilkingDetail milkingRecord3_2 = createMilkingRecord("-990", new LocalDate(1900,1,10), new LocalTime(13,0,0));
			milkingRecord3_2.setComments("Afternoon Milking");
			milkingRecord3_2.setMilkingEventNumber((short) 2);
			milkingRecord3_2.setMilkVolume(17.0f);
			MilkingDetail milkingRecord3_3 = createMilkingRecord("-990", new LocalDate(1900,1,10), new LocalTime(21,0,0));
			milkingRecord3_3.setComments("Night Milking");
			milkingRecord3_3.setMilkingEventNumber((short) 3);
			milkingRecord3_3.setMilkVolume(18.0f);
			

//			
//			MilkingDetail milkingRecord4_1 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(5,0,0));
//			milkingRecord4_1.setComments("Morning Milking");
//			milkingRecord4_1.setMilkingEventNumber((short) 1);
//			milkingRecord4_1.setMilkVolume(16.0f);
//			MilkingDetail milkingRecord4_2 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(13,0,0));
//			milkingRecord4_2.setComments("Afternoon Milking");
//			milkingRecord4_2.setMilkingEventNumber((short) 2);
//			milkingRecord4_2.setMilkVolume(17.0f);
//			MilkingDetail milkingRecord4_3 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(21,0,0));
//			milkingRecord4_3.setComments("Night Milking");
//			milkingRecord4_3.setMilkingEventNumber((short) 3);
//			milkingRecord4_3.setMilkVolume(18.0f);
			
			
			MilkingDetailLoader loader = new MilkingDetailLoader();

			TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal("IMD", "-990");
			TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal("IMD", "-990990");
			TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal("IMD", "-990_990");

			Animal animal1 = TestDataCreationUtil.createTestAnimal(milkingRecord1_1.getOrgId(), milkingRecord1_1.getAnimalTag(), 
					new DateTime(milkingRecord1_1.getRecordDate().getYear(),milkingRecord1_1.getRecordDate().getMonthOfYear(),
							milkingRecord1_1.getRecordDate().getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),true);

			
			Animal animal2 = TestDataCreationUtil.createTestAnimal(milkingRecordTSTTST1_1.getOrgId(), milkingRecordTSTTST1_1.getAnimalTag(), 
					new DateTime(milkingRecordTSTTST1_1.getRecordDate().getYear(),milkingRecordTSTTST1_1.getRecordDate().getMonthOfYear(),
							milkingRecordTSTTST1_1.getRecordDate().getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),true);

			
			assertTrue(TestDataCreationUtil.deleteAnimal(animal2) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal1) >= 0);

			assertEquals(1,TestDataCreationUtil.insertAnimal(animal2));
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal1));
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_3));

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTST1_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTST1_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTST1_3));
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_3));

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_1));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_2));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_3));

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
			
			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(milkingRecord1_1.getOrgId(), 
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


			assertEquals(9,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal("IMD", "-990"));
			assertEquals(3,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal("IMD", "-990990"));

			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_1.getOrgId(),milkingRecord1_1.getRecordDate(),null));
			assertEquals(0,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecordTSTTST1_1.getOrgId(),milkingRecordTSTTST1_1.getRecordDate(),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord2_1.getOrgId(),milkingRecord2_1.getRecordDate(),null));
			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord3_1.getOrgId(),milkingRecord3_1.getRecordDate(),null));

			assertTrue(TestDataCreationUtil.deleteAnimal(animal2) >= 0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal1) >= 0);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Milk Information testing failed.");
		}
	}
	
	

	@Test
	void testYearlyConsolidatedMilkingRecords() {
		try {
			MilkingDetail milkingRecord1_1_1 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(5,0,0));
			milkingRecord1_1_1.setComments("Morning Milking");
			milkingRecord1_1_1.setMilkingEventNumber((short) 1);
			milkingRecord1_1_1.setMilkVolume(10.0f);
			milkingRecord1_1_1.setLrValue(28f);
			milkingRecord1_1_1.setFatValue(3.8f);
			
			MilkingDetail milkingRecord1_1_2 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(13,0,0));
			milkingRecord1_1_2.setComments("Afternoon Milking");
			milkingRecord1_1_2.setMilkingEventNumber((short) 2);
			milkingRecord1_1_2.setMilkVolume(11.0f);
			milkingRecord1_1_2.setLrValue(28f);
			milkingRecord1_1_2.setFatValue(3.9f);

			MilkingDetail milkingRecord1_1_3 = createMilkingRecord("-990", new LocalDate(1900,1,1), new LocalTime(21,0,0));
			milkingRecord1_1_3.setComments("Night Milking");
			milkingRecord1_1_3.setMilkingEventNumber((short) 3);
			milkingRecord1_1_3.setMilkVolume(12.0f);
			milkingRecord1_1_3.setLrValue(28f);
			milkingRecord1_1_3.setFatValue(4.0f);
			

			MilkingDetail milkingRecord1_2_1 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(5,0,0));
			milkingRecord1_2_1.setComments("Morning Milking");
			milkingRecord1_2_1.setMilkingEventNumber((short) 1);
			milkingRecord1_2_1.setMilkVolume(13.0f);
			milkingRecord1_2_1.setLrValue(30f);
			milkingRecord1_2_1.setFatValue(4.0f);
			milkingRecord1_2_1.setTemperatureInCentigrade(6.5f);

			MilkingDetail milkingRecord1_2_2 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(13,0,0));
			milkingRecord1_2_2.setComments("Afternoon Milking");
			milkingRecord1_2_2.setMilkingEventNumber((short) 2);
			milkingRecord1_2_2.setMilkVolume(14.0f);
			milkingRecord1_2_2.setLrValue(28f);
			milkingRecord1_2_2.setFatValue(3.95f);
			milkingRecord1_2_2.setTemperatureInCentigrade(20.5f);

			MilkingDetail milkingRecord1_2_3 = createMilkingRecord("-990", new LocalDate(1900,1,2), new LocalTime(21,0,0));
			milkingRecord1_2_3.setComments("Night Milking");
			milkingRecord1_2_3.setMilkingEventNumber((short) 3);
			milkingRecord1_2_3.setMilkVolume(15.0f);
			milkingRecord1_2_3.setLrValue(27.5f);
			milkingRecord1_2_3.setFatValue(3.85f);
			milkingRecord1_2_3.setTemperatureInCentigrade(18.5f);

			// TOTAL MILK Month of Jan 1900: 75 ltrs
			

			MilkingDetail milkingRecord2_10_1 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(5,0,0));
			milkingRecord2_10_1.setComments("Morning Milking");
			milkingRecord2_10_1.setMilkingEventNumber((short) 1);
			milkingRecord2_10_1.setMilkVolume(20.0f);
			MilkingDetail milkingRecord2_10_2 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(13,0,0));
			milkingRecord2_10_2.setComments("Afternoon Milking");
			milkingRecord2_10_2.setMilkingEventNumber((short) 2);
			milkingRecord2_10_2.setMilkVolume(30.0f);
			MilkingDetail milkingRecord2_10_3 = createMilkingRecord("-990", new LocalDate(1900,2,10), new LocalTime(21,0,0));
			milkingRecord2_10_3.setComments("Night Milking");
			milkingRecord2_10_3.setMilkingEventNumber((short) 3);
			milkingRecord2_10_3.setMilkVolume(40.0f);
			milkingRecord2_10_3.setLrValue(27.0f);
			milkingRecord2_10_3.setFatValue(3.8f);
			milkingRecord2_10_3.setTemperatureInCentigrade(20.5f);

			
			MilkingDetail milkingRecordTSTTSTS2_10_1 = createMilkingRecord("-990_990", new LocalDate(1900,12,31), new LocalTime(5,0,0));
			milkingRecordTSTTSTS2_10_1.setComments("Morning Milking");
			milkingRecordTSTTSTS2_10_1.setMilkingEventNumber((short) 1);
			milkingRecordTSTTSTS2_10_1.setMilkVolume(20.0f);
			MilkingDetail milkingRecordTSTTSTS2_10_2 = createMilkingRecord("-990_990", new LocalDate(1900,12,31), new LocalTime(13,0,0));
			milkingRecordTSTTSTS2_10_2.setComments("Afternoon Milking");
			milkingRecordTSTTSTS2_10_2.setMilkingEventNumber((short) 2);
			milkingRecordTSTTSTS2_10_2.setMilkVolume(25.0f);
			MilkingDetail milkingRecordTSTTSTS2_10_3 = createMilkingRecord("-990_990", new LocalDate(1900,12,31), new LocalTime(21,0,0));
			milkingRecordTSTTSTS2_10_3.setComments("Night Milking");
			milkingRecordTSTTSTS2_10_3.setMilkingEventNumber((short) 3);
			milkingRecordTSTTSTS2_10_3.setMilkVolume(30.0f);
			
			// TOTAL MILK Month of Feb 1900: 165 ltrs
			
			// TOTAL Milk year 1900: 240 ltrs. In Jan 75, in Feb 165. other months zero.

			
			
			MilkingDetailLoader loader = new MilkingDetailLoader();

//			loader.deleteMilkingRecordOfaDay("IMD", "-990", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "-990990", new LocalDate(1900,1,1));
//			loader.deleteMilkingRecordOfaDay("IMD", "-990", new LocalDate(1900,1,2));
//			loader.deleteMilkingRecordOfaDay("IMD", "-990", new LocalDate(1900,2,10));
//			loader.deleteMilkingRecordOfaDay("IMD", "-990_990", new LocalDate(1900,12,31));

			loader.deleteAllMilkingRecordOfanAnimal("IMD", "-990");
			loader.deleteAllMilkingRecordOfanAnimal("IMD", "-990_990");
			
			AnimalLoader anmlLdr = new AnimalLoader();
			Animal animal1 = TestDataCreationUtil.createTestAnimal("IMD", milkingRecord1_1_1.getAnimalTag(), 
					new DateTime(milkingRecord1_1_1.getRecordDate().getYear(),milkingRecord1_1_1.getRecordDate().getMonthOfYear(),
							milkingRecord1_1_1.getRecordDate().getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),true);

			
			Animal animal2 = TestDataCreationUtil.createTestAnimal("IMD", milkingRecordTSTTSTS2_10_1.getAnimalTag(), 
					new DateTime(milkingRecordTSTTSTS2_10_1.getRecordDate().getYear(),milkingRecordTSTTSTS2_10_1.getRecordDate().getMonthOfYear(),
							milkingRecordTSTTSTS2_10_1.getRecordDate().getDayOfMonth(),0,0,IMDProperties.getServerTimeZone()),true);

			
			assertTrue(anmlLdr.deleteAnimal(animal2) >= 0);
			assertEquals(1,anmlLdr.insertAnimal(animal2));
			
			assertTrue(anmlLdr.deleteAnimal(animal1) >= 0);
			assertEquals(1,anmlLdr.insertAnimal(animal1));


			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1_3), "One record should have been inserted");

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2_3), "One record should have been inserted");

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_10_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_10_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_10_3), "One record should have been inserted");

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTSTS2_10_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTSTS2_10_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecordTSTTSTS2_10_3), "One record should have been inserted");

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
			
			assertEquals(9,loader.deleteAllMilkingRecordOfanAnimal("IMD", "-990"));
			assertEquals(0,loader.deleteAllMilkingRecordOfanAnimal("IMD", "-990990"));
			assertEquals(3,loader.deleteAllMilkingRecordOfanAnimal("IMD", "-990_990"));


			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_1_1.getOrgId(), milkingRecord1_1_1.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_1_2.getOrgId(), milkingRecord1_1_2.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_1_3.getOrgId(), milkingRecord1_1_3.getRecordDate(),null));

			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_2_1.getOrgId(), milkingRecord1_2_1.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_2_2.getOrgId(), milkingRecord1_2_2.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord1_2_3.getOrgId(), milkingRecord1_2_3.getRecordDate(),null));

			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord2_10_1.getOrgId(), milkingRecord2_10_1.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord2_10_2.getOrgId(), milkingRecord2_10_2.getRecordDate(),null));
//			assertEquals(3,TestDataCreationUtil.deleteFarmMilkingRecord(milkingRecord2_10_3.getOrgId(), milkingRecord2_10_3.getRecordDate(),null));
			
			
			assertEquals(1,anmlLdr.deleteAnimal(animal1));
			assertEquals(1,anmlLdr.deleteAnimal(animal2));

			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}
	
	
	@Test
	void testLifeTimeMilkingRecordForAnimal() {
		try {
			String orgId = "IMD";
			String animalTag = "-999";
			DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(20+4);
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			LocalDate startDate = new LocalDate(dob.plusYears(20), IMDProperties.getServerTimeZone());
			
			MilkingDetail milkingRecord1_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"4:00", 1, 13.0f);
			MilkingDetail milkingRecord1_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"12:00", 2, 14.0f);
			MilkingDetail milkingRecord1_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"20:00",  3, 15.0f);
			
			MilkingDetail milkingRecord2_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(1),"4:00", 1, 16.0f);
			MilkingDetail milkingRecord2_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(1),"12:00", 2, 17.0f);
			MilkingDetail milkingRecord2_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(1),"20:00",  3, 18.0f);

			MilkingDetail milkingRecord3_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(2), "4:00",1, 16.0f);
			MilkingDetail milkingRecord3_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(2),"12:00", 2, 17.0f);
			MilkingDetail milkingRecord3_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(2),"20:00", 3, 18.0f);

			MilkingDetail milkingRecord4_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(3),"4:00", 1, 17.0f);
			MilkingDetail milkingRecord4_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(3),"12:00",  2, 17.0f);
			MilkingDetail milkingRecord4_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate.plusYears(3),"20:00",  3, 17.0f);
			

			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgId, animalTag) >=0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal)>=0);			
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_3), "One record should have been inserted");

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord2_3), "One record should have been inserted");

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord3_3), "One record should have been inserted");

			MilkingDetailLoader loader = new MilkingDetailLoader();

			List <MilkingDetail>  milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgId, animalTag, null,null, false);
			
			assertEquals(3,milkRecords.size());
			assertEquals(milkingRecord1_1.getRecordDate(),milkRecords.get(0).getRecordDate());
			assertEquals(milkingRecord2_1.getRecordDate(),milkRecords.get(1).getRecordDate());
			assertEquals(milkingRecord3_1.getRecordDate(),milkRecords.get(2).getRecordDate());
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord4_1), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord4_2), "One record should have been inserted");
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord4_3), "One record should have been inserted");

			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgId, animalTag, null,null, false);
			assertEquals(4,milkRecords.size());
			assertEquals(milkingRecord1_1.getRecordDate(),milkRecords.get(0).getRecordDate());
			assertEquals(milkingRecord2_1.getRecordDate(),milkRecords.get(1).getRecordDate());
			assertEquals(milkingRecord3_1.getRecordDate(),milkRecords.get(2).getRecordDate());
			assertEquals(milkingRecord4_1.getRecordDate(),milkRecords.get(3).getRecordDate());

			milkRecords = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgId, animalTag, null,milkingRecord4_1.getRecordDate(), true);
			assertEquals(Util.getDaysBetween(milkingRecord4_1.getRecordDate(), new LocalDate(animal.getDateOfBirth()))+1,milkRecords.size());
			assertEquals(new LocalDate(animal.getDateOfBirth()),milkRecords.get(0).getRecordDate());
			assertEquals(milkingRecord4_1.getRecordDate(),milkRecords.get(milkRecords.size()-1).getRecordDate());

			assertEquals(12,loader.deleteAllMilkingRecordOfanAnimal(orgId, animalTag));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Milk Information testing failed.");
		}
	}
	
	

	@Test
	void testUploadMilkingData() {
		try {
			String orgId = "IMD";
			String animalTag = "-999";
			DateTime dob = DateTime.now(IMDProperties.getServerTimeZone()).minusYears(100);
			Animal animal = TestDataCreationUtil.createTestAnimal(orgId, animalTag, dob, true);
			LocalDate startDate = new LocalDate(dob.plusYears(4), IMDProperties.getServerTimeZone());
			
			MilkingDetail milkingRecord1_1 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"4:00", 1, 13.0f);
			MilkingDetail milkingRecord1_2 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"12:00", 2, 14.0f);
			MilkingDetail milkingRecord1_3 = TestDataCreationUtil.createMilkingRecord(orgId, animalTag, startDate,"20:00",  3, 15.0f);			

			milkingRecord1_1.setPhValue(6.8f);
			milkingRecord1_1.setForCalvesUse(6f);
			milkingRecord1_1.setForFamilyUse(5f);
			milkingRecord1_1.setForFarmUse(3.5f);
			milkingRecord1_1.setForOtherUse(1f);
			milkingRecord1_1.setForPersonalUse(15f);
			milkingRecord1_1.setForWasteAdj(1f);			
			
			milkingRecord1_2.setPhValue(26.8f);
			milkingRecord1_2.setForCalvesUse(26f);
			milkingRecord1_2.setForFamilyUse(25f);
			milkingRecord1_2.setForFarmUse(23.5f);
			milkingRecord1_2.setForOtherUse(21f);
			milkingRecord1_2.setForPersonalUse(215f);
			milkingRecord1_2.setForWasteAdj(21f);			

			milkingRecord1_3.setPhValue(36.8f);
			milkingRecord1_3.setForCalvesUse(36f);
			milkingRecord1_3.setForFamilyUse(35f);
			milkingRecord1_3.setForFarmUse(33.5f);
			milkingRecord1_3.setForOtherUse(31f);
			milkingRecord1_3.setForPersonalUse(315f);
			milkingRecord1_3.setForWasteAdj(31f);			
			
			assertTrue(TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgId, animalTag) >=0);
			assertTrue(TestDataCreationUtil.deleteFarmMilkingRecordsWithNoAnimalMilkingEvent(orgId) >=0);
			assertTrue(TestDataCreationUtil.deleteAnimal(animal)>=0);			
			
			assertEquals(1,TestDataCreationUtil.insertAnimal(animal));
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_1));
			
			MilkingDetailLoader loader = new MilkingDetailLoader();
			List<MilkingDetail> farmRecords = loader.retrieveFarmMilkRecord(milkingRecord1_1);
			assertEquals(1,farmRecords.size());
			assertEquals(milkingRecord1_1.getTemperatureInCentigrade(),farmRecords.get(0).getTemperatureInCentigrade());
			assertEquals(milkingRecord1_1.getPhValue(),farmRecords.get(0).getPhValue());
			assertEquals(milkingRecord1_1.getForCalvesUse(),farmRecords.get(0).getForCalvesUse());
			assertEquals(milkingRecord1_1.getForFamilyUse(),farmRecords.get(0).getForFamilyUse());
			assertEquals(milkingRecord1_1.getForFarmUse(),farmRecords.get(0).getForFarmUse());
			assertEquals(milkingRecord1_1.getForOtherUse(),farmRecords.get(0).getForOtherUse());
			assertEquals(milkingRecord1_1.getForPersonalUse(),farmRecords.get(0).getForPersonalUse());
			assertEquals(milkingRecord1_1.getForWasteAdj(),farmRecords.get(0).getForWasteAdj());
			
			milkingRecord1_1.setForFarmUse(1.5f);
			milkingRecord1_1.setForOtherUse(12f);
			milkingRecord1_1.setForPersonalUse(13f);
			milkingRecord1_1.setForWasteAdj(0f);

			assertEquals(Util.Outcome.NOT_OVERWRITTEN,loader.addOrUpdateFarmMilkingRecord(milkingRecord1_1, true));
			
			farmRecords = loader.retrieveFarmMilkRecord(milkingRecord1_1);
			assertEquals(1,farmRecords.size());
			assertEquals(milkingRecord1_1.getTemperatureInCentigrade(),farmRecords.get(0).getTemperatureInCentigrade());
			assertEquals(milkingRecord1_1.getPhValue(),farmRecords.get(0).getPhValue());
			assertEquals(milkingRecord1_1.getForCalvesUse(),farmRecords.get(0).getForCalvesUse());
			assertEquals(milkingRecord1_1.getForFamilyUse(),farmRecords.get(0).getForFamilyUse());
			assertNotEquals(milkingRecord1_1.getForFarmUse(),farmRecords.get(0).getForFarmUse());
			assertNotEquals(milkingRecord1_1.getForOtherUse(),farmRecords.get(0).getForOtherUse());
			assertNotEquals(milkingRecord1_1.getForPersonalUse(),farmRecords.get(0).getForPersonalUse());
			assertNotEquals(milkingRecord1_1.getForWasteAdj(),farmRecords.get(0).getForWasteAdj());

			
			assertEquals(Util.Outcome.EDIT,loader.addOrUpdateFarmMilkingRecord(milkingRecord1_1, false));
			
			farmRecords = loader.retrieveFarmMilkRecord(milkingRecord1_1);
			assertEquals(1,farmRecords.size());
			assertEquals(milkingRecord1_1.getTemperatureInCentigrade(),farmRecords.get(0).getTemperatureInCentigrade());
			assertEquals(milkingRecord1_1.getPhValue(),farmRecords.get(0).getPhValue());
			assertEquals(milkingRecord1_1.getForCalvesUse(),farmRecords.get(0).getForCalvesUse());
			assertEquals(milkingRecord1_1.getForFamilyUse(),farmRecords.get(0).getForFamilyUse());
			assertEquals(milkingRecord1_1.getForFarmUse(),farmRecords.get(0).getForFarmUse());
			assertEquals(milkingRecord1_1.getForOtherUse(),farmRecords.get(0).getForOtherUse());
			assertEquals(milkingRecord1_1.getForPersonalUse(),farmRecords.get(0).getForPersonalUse());
			assertEquals(milkingRecord1_1.getForWasteAdj(),farmRecords.get(0).getForWasteAdj());
			
			
			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_2));
			farmRecords = loader.retrieveFarmMilkRecord(milkingRecord1_2);
			assertEquals(1,farmRecords.size());
			assertEquals(milkingRecord1_2.getTemperatureInCentigrade(),farmRecords.get(0).getTemperatureInCentigrade());
			assertEquals(milkingRecord1_2.getPhValue(),farmRecords.get(0).getPhValue());
			assertEquals(milkingRecord1_2.getForCalvesUse(),farmRecords.get(0).getForCalvesUse());
			assertEquals(milkingRecord1_2.getForFamilyUse(),farmRecords.get(0).getForFamilyUse());
			assertEquals(milkingRecord1_2.getForFarmUse(),farmRecords.get(0).getForFarmUse());
			assertEquals(milkingRecord1_2.getForOtherUse(),farmRecords.get(0).getForOtherUse());
			assertEquals(milkingRecord1_2.getForPersonalUse(),farmRecords.get(0).getForPersonalUse());
			assertEquals(milkingRecord1_2.getForWasteAdj(),farmRecords.get(0).getForWasteAdj());

			assertEquals(1,TestDataCreationUtil.insertMilkingRecord(milkingRecord1_3));
			farmRecords = loader.retrieveFarmMilkRecord(milkingRecord1_3);
			assertEquals(1,farmRecords.size());
			assertEquals(milkingRecord1_3.getTemperatureInCentigrade(),farmRecords.get(0).getTemperatureInCentigrade());
			assertEquals(milkingRecord1_3.getPhValue(),farmRecords.get(0).getPhValue());
			assertEquals(milkingRecord1_3.getForCalvesUse(),farmRecords.get(0).getForCalvesUse());
			assertEquals(milkingRecord1_3.getForFamilyUse(),farmRecords.get(0).getForFamilyUse());
			assertEquals(milkingRecord1_3.getForFarmUse(),farmRecords.get(0).getForFarmUse());
			assertEquals(milkingRecord1_3.getForOtherUse(),farmRecords.get(0).getForOtherUse());
			assertEquals(milkingRecord1_3.getForPersonalUse(),farmRecords.get(0).getForPersonalUse());
			assertEquals(milkingRecord1_3.getForWasteAdj(),farmRecords.get(0).getForWasteAdj());

			assertEquals(3,TestDataCreationUtil.deleteAllMilkingRecordOfanAnimal(orgId, animalTag));
			assertEquals(1,TestDataCreationUtil.deleteFarmMilkingRecord(orgId, milkingRecord1_1.getRecordDate(),1));
			assertEquals(2,TestDataCreationUtil.deleteFarmMilkingRecordsWithNoAnimalMilkingEvent(orgId));
			assertEquals(1,TestDataCreationUtil.deleteAnimal(animal));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Milk Information testing failed.");
		}		
	}	
	
}







