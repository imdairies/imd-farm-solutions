package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LookupValues;
import com.imd.dto.Note;
import com.imd.dto.User;
import com.imd.services.bean.LookupValuesBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class FarmLoaderTest {

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
	public Animal createTestAnimal(String animalTag) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setHerdJoiningDate(DateTime.now().minusMonths(12));
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", LocalDateTime.now());		
		c000.addNote(newNote);
		return c000;
	}
	@Test
	void testHerdSizeTrend() {
		// 1: Insert a new record.
		AnimalLoader loader = new AnimalLoader();
		try {
			String animalTagJan1980 = "TST_JAN1980";
			String animalTagFeb1980 = "TST_FEB1980";
			Animal animalJan1980 = createTestAnimal(animalTagJan1980);
			Animal animalFeb1980 = createTestAnimal(animalTagFeb1980);
			int delCount = loader.deleteAnimal(animalJan1980.getOrgID(),animalTagJan1980);
			assertTrue( delCount == 0 || delCount == 1);
			 delCount = loader.deleteAnimal(animalFeb1980.getOrgID(),animalTagFeb1980);
			assertTrue( delCount == 0 || delCount == 1);			
			
			animalJan1980.setHerdJoiningDate(new DateTime(1980,1,31,0,0));
			animalFeb1980.setHerdJoiningDate(new DateTime(1980,2,1,0,0));
			int janCount1 = loader.getActiveHerdCountAtEndOfMonthYear(1, 1980);
			int febCount1 = loader.getActiveHerdCountAtEndOfMonthYear(2, 1980);
			assertTrue(janCount1 >= 0);
			assertTrue(febCount1 >= 0);
			loader.insertAnimal(animalJan1980);
			loader.insertAnimal(animalFeb1980);
			int janCount2 = loader.getActiveHerdCountAtEndOfMonthYear(1, 1980);
			assertEquals(1,janCount2-janCount1);
			int febCount2 = loader.getActiveHerdCountAtEndOfMonthYear(2, 1980);
			assertEquals(2,febCount2 - febCount1);

			assertEquals(1,loader.getActiveHerdCountForDate(new LocalDate(animalJan1980.getHerdJoiningDate()))-janCount1);			
			
			assertEquals(1,loader.deleteAnimal(animalJan1980.getOrgID(),animalTagJan1980));
			assertEquals(1,loader.deleteAnimal(animalFeb1980.getOrgID(),animalTagFeb1980));

			
		} catch (Exception e) {
			e.printStackTrace();
			fail("testHerdSizeTrend Failed.");
		}
	}
}
