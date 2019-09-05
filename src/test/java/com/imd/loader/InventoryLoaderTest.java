package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Inventory;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.InventoryBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class InventoryLoaderTest {

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

	public Animal createTestSire(String animalTag) throws Exception {
		Sire c000 = new Sire(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/new DateTime(2014,2,9,1,1,1,IMDProperties.getServerTimeZone()),/*dob estimated*/true,/*price*/100000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType(Util.AnimalTypes.BULL);
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setPurchaseDate(new DateTime(2017,2,8,1,1,1,IMDProperties.getServerTimeZone()));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		c000.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10));
		c000.setHerdLeavingDate(null);
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
	}
//
//	@Test
//	void testInsertSemenInv() {
//		try {
//			InventoryBean invBean = new InventoryBean();
//			invBean.setItemSKU("-999");
//			invBean.setItemType("Y");
//			invBean.setOrderDttm(DateTime.now(IMDProperties.getServerTimeZone()));
//			invBean.setReceivedDttm(DateTime.now(IMDProperties.getServerTimeZone()));
//			invBean.setInventoryAddDttm(DateTime.now(IMDProperties.getServerTimeZone()));
//			
//			assertFalse(invBean.validateValues().isEmpty());
//			invBean.setPrice(1000.0f);
//			invBean.setDiscount(10.0f);
//			invBean.setQuantity(5.0f);
//			
//			Inventory inv = new Inventory(invBean);
//			inv.setOrgID("IMD");
//			inv.setCreatedBy(new User("KASHIF"));
//			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
//			inv.setUpdatedBy(new User("KASHIF"));
//			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
//			
//			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
//			InventoryLoader loader = new InventoryLoader();
//			int result = loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType());
//			assertTrue(result >= 0);
//			assertEquals(1,loader.insertSemenInventory(inv));
//	
//			assertEquals(1,loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType()));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Inventory processing Failed.");
//		}
//	}
//	
//	@Test
//	void testInsertSemenUsageInv() {
//		try {
//			InventoryBean invBean = new InventoryBean();
//			invBean.setItemSKU("-999");
//			invBean.setItemType("Y");
//			invBean.setOrderDttm(DateTime.now(IMDProperties.getServerTimeZone()));
//			invBean.setReceivedDttm(DateTime.now(IMDProperties.getServerTimeZone()));
//			invBean.setInventoryAddDttm(new DateTime(2019,1,1,0,0,0,IMDProperties.getServerTimeZone()));
//			
//			assertFalse(invBean.validateValues().isEmpty());
//			invBean.setQuantity(1.0f);
//			invBean.setAuxValue1("-9999");
//			
//			Inventory inv = new Inventory(invBean);
//			inv.setOrgID("IMD");
//			inv.setCreatedBy(new User("KASHIF"));
//			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
//			inv.setUpdatedBy(new User("KASHIF"));
//			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
//			
//			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
//			InventoryLoader loader = new InventoryLoader();
//			int result = loader.deleteSemenInventoryUsage(inv.getOrgID(),inv.getItemSKU());
//			assertTrue(result >= 0);
//			assertEquals(1,loader.addSemenInventoryUsage(inv));
//	
//			assertEquals(1,loader.deleteSemenInventoryUsage(inv.getOrgID(),inv.getItemSKU()));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Inventory processing Failed.");
//		}
//	}
	@Test
	void testSemenQtyUpdate() {
		try {			
			AnimalLoader animalLoader = new AnimalLoader();
			InventoryLoader invLoader = new InventoryLoader();
			
			SireBean sireBean = new SireBean();
			sireBean.setOrgID("IMD");
			sireBean.setAnimalTag("-999");
			sireBean.setAlias("TEST SIRE");
			sireBean.setBreed(Util.Breed.HFCROSS);
			sireBean.setController("SEMEX");
			sireBean.setCurrentConventionalListPrice(1000.0f);
			sireBean.setCurrentSexListPrice(10000.0f);
			sireBean.setDiscountConventionalPercentage(25.0f);
			sireBean.setDiscountSexPercentage(25.0f);
			sireBean.setPhotoURL("");
			sireBean.setRecordURL("");
			sireBean.setSemenCompany("DRDF");
			sireBean.setSemenInd("Y");
			User user = new User("KASHIF");
			
			
			int result = animalLoader.deleteSire(sireBean.getAnimalTag());
			assertTrue(result == 0 || result ==1);
			result = animalLoader.deleteAnimal(sireBean.getOrgID(), sireBean.getAnimalTag());
			assertTrue(result == 0 || result ==1);
			
			assertEquals(1,animalLoader.insertSire(sireBean, user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone()), user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone())));
			
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setReceivedDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setInventoryAddDttm(new DateTime(2019,1,1,1,1,1,IMDProperties.getServerTimeZone()));
			invBean.setPrice(1000.0f);
			invBean.setDiscount(10.0f);
			invBean.setQuantity(5.0f);

			Inventory inv = new Inventory(invBean);
			inv.setOrgID("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			
			assertTrue(invLoader.deleteSemenInventoryUsage(inv.getOrgID(), invBean.getItemSKU()) >= 0);
			assertTrue(invLoader.deleteSemenInventory(inv.getOrgID(), invBean.getItemSKU(), invBean.getItemType()) >= 0);
			
			invLoader.insertSemenInventory(inv);
			inv.setQuantity(inv.getQuantity()-2);
			invLoader.addSemenInventoryUsage(inv);
			
			List<Sire> sires = animalLoader.retrieveAISire();
			Iterator<Sire> it = sires.iterator();
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertEquals(null,invLoader.getRemainingSemenInventory(inv.getOrgID(),"-NOTPRESENT",  inv.getItemType()));
					Inventory invOutput = invLoader.getRemainingSemenInventory(inv.getOrgID(),sire.getAnimalTag(),  inv.getItemType());
					assertTrue(invOutput != null);
					assertTrue(invOutput.getQuantity() == 5.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue1()) == 3.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue2()) == 2.0f);
					IMDLogger.log(invOutput.dtoToJson("   "), Util.INFO);
					break;
				}
			}
			sires = invLoader.getSiresWithAvailableInventory(inv.getOrgID());
			it = sires.iterator();
			boolean found = false;
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertTrue(sire.getNote(0).getNoteText().equals("2"));
					assertTrue(sire.getNote(1).getNoteText().equals("Y"));
					found=true;
				}
			}
			assertTrue(found);
			
			assertEquals(1,invLoader.deleteSemenInventoryUsage(inv.getOrgID(), invBean.getItemSKU(), inv.getInventoryAddDttm()));
			assertEquals(1,invLoader.deleteSemenInventory(inv.getOrgID(), invBean.getItemSKU(), invBean.getItemType()));
			assertEquals(1,animalLoader.deleteSire(sireBean.getAnimalTag()));

			sireBean.setSemenInd("N");
			assertEquals(1,animalLoader.insertSire(sireBean, user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone()), user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone())));
			assertEquals(1,animalLoader.insertAnimal(createTestSire(sireBean.getAnimalTag())));
			assertEquals(1,animalLoader.updateAnimalHerdLeavingDTTM(sireBean.getOrgID(), sireBean.getAnimalTag(), Util.getDateInSQLFormart(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(2)), user));
			sires = invLoader.getSiresWithAvailableInventory(inv.getOrgID());
			it = sires.iterator();
			found = false;
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertTrue(sire.getNote(1).getNoteText().equals("N"));
					found=true;
				}
			}
			assertFalse(found);
		
			assertEquals(1,animalLoader.deleteSire(sireBean.getAnimalTag()));
			assertEquals(1,animalLoader.deleteAnimal(sireBean.getOrgID(), sireBean.getAnimalTag()));
		
		
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	

}








