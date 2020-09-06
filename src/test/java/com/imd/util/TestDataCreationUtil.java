package com.imd.util;

import java.sql.SQLException;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.bean.LifeCycleEventBean;

public class TestDataCreationUtil {
	
	
	public static MilkingDetail createMilkingRecord(String orgId, String animalTag, LocalDate milkingDate, String milkingTime, int seq_nbr, float milkingVol) throws IMDException {
		short milkFreq = 3;
		boolean isMachineMilked = true;	
		MilkingDetail milkingRecord = new MilkingDetail(orgId, animalTag, milkFreq, isMachineMilked, milkingDate, null, milkingVol,(short)seq_nbr);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		LocalTime localTime = fmt.parseLocalTime(milkingTime);
		milkingRecord.setRecordTime(new LocalTime(localTime));
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
	public static MilkingDetail createMilkingRecord(String orgId, String animalTag, LocalDate milkingDate, int seq_nbr, float milkingVol) throws IMDException {
		short milkFreq = 3;
		boolean isMachineMilked = true;	
		MilkingDetail milkingRecord = new MilkingDetail(orgId, animalTag, milkFreq, isMachineMilked, milkingDate, null, milkingVol,(short)seq_nbr);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
		String milkingTime = "";
		if (seq_nbr == 1)
			milkingTime = "4:00";
		else if (seq_nbr == 2)
			milkingTime = "12:00";
		else if (seq_nbr == 3)
			milkingTime = "20:00";
		else
			milkingTime = "0:00";

		LocalTime localTime = fmt.parseLocalTime(milkingTime);
		milkingRecord.setRecordTime(new LocalTime(localTime));
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
	
	public static int deleteFarmMilkingRecord(String orgId, LocalDate recordDate, Integer seqNbr) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		return loader.deleteFarmMilkingRecord(orgId, recordDate, seqNbr);
	}

	
	public static int deleteAllMilkingRecordOfanAnimal(String orgId, String animalTag) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		return loader.deleteAllMilkingRecordOfanAnimal(orgId, animalTag);
	}
	
	public static int insertMilkingRecord(MilkingDetail milkRecord) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		int parentRecordInsertionOutcome = loader.insertFarmMilkRecord(milkRecord );
		if (parentRecordInsertionOutcome == 1 || parentRecordInsertionOutcome == Util.ERROR_CODE.DUPLICATE_ENTRY)
			return loader.insertMilkRecord(milkRecord );
		else
			return parentRecordInsertionOutcome;
	}
	public static Animal createTestAnimal(String orgId, String animalTag, DateTime dob, boolean isFemale) throws Exception {
		Animal c000 = null;
		if (isFemale)
			c000 = new Dam(/*orgid*/orgId,/*tag*/animalTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		else
			c000 = new Sire(/*orgid*/orgId,/*tag*/animalTag,dob,/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/"+ animalTag + "/4.png");
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setHerdJoiningDate(DateTime.now().minusDays(10));
		c000.setHerdLeavingDate(null);
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
	}
	
	public static int insertAnimal(Animal animal) throws Exception {
		AnimalLoader loader = new AnimalLoader();
		return (loader.insertAnimal(animal));
	}

	public static int deleteAnimal(Animal animal) throws Exception {
		return (deleteAnimal(animal.getOrgId(),animal.getAnimalTag()));
	}
	
	public static int deleteAnimal(String orgId, String animalTag) throws Exception {
		AnimalLoader loader = new AnimalLoader();
		return (loader.deleteAnimal(orgId, animalTag));
	}
	
	public static int deleteAllAnimalEvents(String orgId, String animalTag) throws Exception {
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		return (loader.deleteAnimalLifecycleEvents(orgId, animalTag));
	}
	
	public static int deleteLifecycleEvent(String orgId, String transId) throws Exception {
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		return (loader.deleteLifeCycleEvent(orgId, transId));
	}
	
	public static int insertEvent(String animalTag, String comments, String eventCode, DateTime eventDTTM) throws IMDException, SQLException {
		return insertEvent(animalTag, comments, eventCode, eventDTTM, null, null, null, null);
	}	
	public static int insertEvent(String animalTag, String comments, String eventCode, DateTime eventDTTM, String aux1, String aux2, String aux3, String aux4) throws IMDException, SQLException {
		LifeCycleEventBean eventBean = new LifeCycleEventBean();
		eventBean.setAnimalTag(animalTag);
		eventBean.setEventCode(eventCode);
		eventBean.setEventComments(comments);
		eventBean.setOrgID("IMD");
		eventBean.setAuxField1Value(aux1);
		eventBean.setAuxField2Value(aux2);
		eventBean.setAuxField3Value(aux3);
		eventBean.setAuxField4Value(aux4);
		eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(eventDTTM));
		LifecycleEvent lifecycleEvent = new LifecycleEvent(eventBean);
		
		lifecycleEvent.setCreatedBy(new User("KASHIF"));
		lifecycleEvent.setUpdatedBy(new User("KASHIF"));
		lifecycleEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		lifecycleEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		
		return (loader.insertLifeCycleEvent(lifecycleEvent));
		
	}
	public static int deleteFarmMilkingRecordsWithNoAnimalMilkingEvent(String orgId) {
		
		MilkingDetailLoader ldr = new MilkingDetailLoader();
		return ldr.deleteFarmMilkingRecordsWithNoChildRecords(orgId);
	}


}
