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
import com.imd.dto.FoodInventoryItem;
import com.imd.dto.FoodUsage;
import com.imd.dto.Inventory;
import com.imd.dto.LookupValues;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.FoodInventoryItemBean;
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

	@Test
	void testInsertSemenInv() {
		try {
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setReceivedDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setInventoryAddDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			
			assertFalse(invBean.validateValues().isEmpty());
			invBean.setPrice(1000.0f);
			invBean.setDiscount(10.0f);
			invBean.setQuantity(5.0f);
			
			Inventory inv = new Inventory(invBean);
			inv.setOrgId("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
			InventoryLoader loader = new InventoryLoader();
			int result = loader.deleteSemenInventory(inv.getOrgId(),inv.getItemSKU(), inv.getItemType());
			assertTrue(result >= 0);
			assertEquals(1,loader.insertSemenInventory(inv));
	
			assertEquals(1,loader.deleteSemenInventory(inv.getOrgId(),inv.getItemSKU(), inv.getItemType()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	
	@Test
	void testInsertSemenUsageInv() {
		try {
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setReceivedDttm(DateTime.now(IMDProperties.getServerTimeZone()));
			invBean.setInventoryAddDttm(new DateTime(2019,1,1,0,0,0,IMDProperties.getServerTimeZone()));
			
			assertFalse(invBean.validateValues().isEmpty());
			invBean.setQuantity(1.0f);
			invBean.setAuxValue1("-9999");
			
			Inventory inv = new Inventory(invBean);
			inv.setOrgId("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
			InventoryLoader loader = new InventoryLoader();
			int result = loader.deleteSemenInventoryUsage(inv.getOrgId(),inv.getItemSKU(),inv.getInventoryAddDttm());
			assertTrue(result >= 0);
			assertEquals(1,loader.addSemenInventoryUsage(inv));
	
			assertEquals(1,loader.deleteSemenInventoryUsage(inv.getOrgId(),inv.getItemSKU(),inv.getInventoryAddDttm()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	
	@Test
	void testSemenQtyUpdate() {
		try {
			IMDLogger.loggingMode = Util.INFO;
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
			inv.setOrgId("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			
			assertTrue(invLoader.deleteSemenInventoryUsage(inv.getOrgId(), invBean.getItemSKU(),inv.getInventoryAddDttm()) >= 0);
			assertTrue(invLoader.deleteSemenInventory(inv.getOrgId(), invBean.getItemSKU(), invBean.getItemType()) >= 0);
			
			invLoader.insertSemenInventory(inv);
			inv.setQuantity(inv.getQuantity()-2);
			invLoader.addSemenInventoryUsage(inv);
			
			List<Sire> sires = animalLoader.retrieveAISire();
			Iterator<Sire> it = sires.iterator();
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertEquals(null,invLoader.getRemainingSemenInventory(inv.getOrgId(),"-NOTPRESENT",  inv.getItemType()));
					Inventory invOutput = invLoader.getRemainingSemenInventory(inv.getOrgId(),sire.getAnimalTag(),  inv.getItemType());
					assertTrue(invOutput != null);
					assertTrue(invOutput.getQuantity() == 5.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue1()) == 3.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue2()) == 2.0f);
					IMDLogger.log(invOutput.dtoToJson("   "), Util.INFO);
					break;
				}
			}
			sires = invLoader.getSiresWithAvailableInventory(inv.getOrgId());
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
			
			assertEquals(1,invLoader.deleteSemenInventoryUsage(inv.getOrgId(), invBean.getItemSKU(), inv.getInventoryAddDttm()));
			assertEquals(1,invLoader.deleteSemenInventory(inv.getOrgId(), invBean.getItemSKU(), invBean.getItemType()));
			assertEquals(1,animalLoader.deleteSire(sireBean.getAnimalTag()));

			sireBean.setSemenInd("N");
			assertEquals(1,animalLoader.insertSire(sireBean, user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone()), user.getUserId(), DateTime.now(IMDProperties.getServerTimeZone())));
			assertEquals(1,animalLoader.insertAnimal(createTestSire(sireBean.getAnimalTag())));
			assertEquals(1,animalLoader.updateAnimalHerdLeavingDTTM(sireBean.getOrgID(), sireBean.getAnimalTag(), Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(2)), user));
			sires = invLoader.getSiresWithAvailableInventory(inv.getOrgId());
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
	
	@Test
	void testFeedItemUsage() {
		try {
			
			
			String categoryCd = "TST_CTGRY";
			String valueCd = "TST_ITEM";
			String trackingId = "-999";
			FoodInventoryItem invItem1 = new FoodInventoryItem(trackingId,
					"IMD",categoryCd, valueCd,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(4),
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(1),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(30),
					"",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					);
			
			invItem1.setPricePerUnitUnit("Rs.");
			invItem1.setQuantityPerUnitUnit("Kgs.");
			invItem1.setNumberOfUnitsUnit("Bags");

			InventoryLoader invLdr = new InventoryLoader();

			FoodUsage usg = new FoodUsage("IMD",invItem1.getStockTrackingId(),invItem1.getReceivedDttm().plusDays(3),
					10f, null, null,
					DateTime.now(IMDProperties.getServerTimeZone()),
					new User("KASHIF"));

			usg.setUsageCd(Util.LookupValues.NORMAL);
			usg.setComments("Test to be deleted");
			assertTrue(invLdr.deleteFoodInventoryUsage(usg.getOrgId(),usg.getStockTrackingId()) >= 0 );
			assertTrue(invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()) >= 0 );
			
			LookupValuesLoader lvLdr = new LookupValuesLoader();
			assertTrue(lvLdr.deleteLookupValue(categoryCd, valueCd)>=0);
			
			LookupValues lv = new LookupValues(categoryCd, valueCd, "test to be deleted", "test to be deleted", "-999", "-999");
			lv.setCreatedBy(new User("KASHIF"));
			lv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			lv.setUpdatedBy(new User("KASHIF"));
			lv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			assertEquals(1,lvLdr.insertLookupValues(lv));
			assertEquals(1,invLdr.addFoodInventory(invItem1));
						

			FoodInventoryItemBean searchBean = new FoodInventoryItemBean();			
			searchBean.setStockTrackingId(invItem1.getStockTrackingId());

			List<FoodInventoryItem> searchResults = invLdr.retrieveFoodItemInventory(searchBean);
			assertTrue(searchResults != null);
			assertEquals(1,searchResults.size());
			assertEquals(invItem1.getLookupCd(),searchResults.get(0).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getExpiryDttm()));
			assertEquals(invItem1.getNumberOfUnits(),searchResults.get(0).getNumberOfUnits());
			assertEquals(invItem1.getQuantityPerUnit(),searchResults.get(0).getQuantityPerUnit());
			assertEquals(invItem1.getPricePerUnit(),searchResults.get(0).getPricePerUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),searchResults.get(0).getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),searchResults.get(0).getQuantityPerUnitUnit());
			assertEquals(invItem1.getNumberOfUnitsUnit(),searchResults.get(0).getNumberOfUnitsUnit());
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit(),searchResults.get(0).getRemainingQuantity().floatValue());
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit(),searchResults.get(0).getTotalQuantity().floatValue());			
			
			
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit(),invLdr.retrieveRemainingQuantity(searchBean.getOrgId(), searchBean.getStockTrackingId()).floatValue());
			
			assertEquals(1,invLdr.addFoodInventoryUsage(usg));
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit() - usg.getConsumptionQuantity(),invLdr.retrieveRemainingQuantity(searchBean.getOrgId(), searchBean.getStockTrackingId()).floatValue());

			assertEquals(Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION,invLdr.addFoodInventoryUsage(new FoodUsage("IMD","-99999",invItem1.getReceivedDttm().plusDays(3),
					10f,
					null,null,
					DateTime.now(IMDProperties.getServerTimeZone()),new User("KASHIF"))));

			List<FoodUsage> usageList = invLdr.retrieveFeedInventoryUsage(usg.getOrgId(), usg.getStockTrackingId(), null);
			assertEquals(1,usageList.size());
			assertEquals(usg.getStockTrackingId(),usageList.get(0).getStockTrackingId());
			assertEquals(usg.getConsumptionQuantity(),usageList.get(0).getConsumptionQuantity());
			assertEquals(usg.getUsageCd(),usageList.get(0).getUsageCd());
			assertEquals(usg.getComments(),usageList.get(0).getComments());
			assertEquals(Util.getDateTimeInSpecifiedFormat(usg.getConsumptionTimestamp(),"yyyy-MM-dd HH:mm:ss"),
					Util.getDateTimeInSpecifiedFormat(usageList.get(0).getConsumptionTimestamp(),"yyyy-MM-dd HH:mm:ss"));
			
			
			searchResults = invLdr.retrieveFoodItemInventory(searchBean);
			assertTrue(searchResults != null);
			assertEquals(1,searchResults.size());
			assertEquals(invItem1.getLookupCd(),searchResults.get(0).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getExpiryDttm()));
			assertEquals(invItem1.getNumberOfUnits(),searchResults.get(0).getNumberOfUnits());
			assertEquals(invItem1.getQuantityPerUnit(),searchResults.get(0).getQuantityPerUnit());
			assertEquals(invItem1.getPricePerUnit(),searchResults.get(0).getPricePerUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),searchResults.get(0).getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),searchResults.get(0).getQuantityPerUnitUnit());
			assertEquals(invItem1.getNumberOfUnitsUnit(),searchResults.get(0).getNumberOfUnitsUnit());
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit(),searchResults.get(0).getTotalQuantity().floatValue());			
			assertEquals(usg.getConsumptionQuantity(),searchResults.get(0).getUsedQuantity());
			assertEquals(invItem1.getNumberOfUnits() * invItem1.getQuantityPerUnit() - usg.getConsumptionQuantity(),searchResults.get(0).getRemainingQuantity().floatValue());
			
			
			
			assertEquals(1,invLdr.deleteFoodInventoryUsage(usg.getOrgId(),usg.getStockTrackingId()));
			assertEquals(1,invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()));
			assertEquals(1,lvLdr.deleteLookupValue(categoryCd, valueCd));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception ");
		}
	
	}
	
	@Test
	void testFeedInventoryItemAddition() {
		try {
			FoodInventoryItem invItem1 = new FoodInventoryItem("-999",
					"IMD",Util.LookupValues.FEED,Util.FeedItems.VANDA,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(1),
					DateTime.now(IMDProperties.getServerTimeZone()),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(30),
					"",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					);

			FoodInventoryItem invItem2 = new FoodInventoryItem("-998",
					"IMD",Util.LookupValues.FEED,Util.FeedItems.VANDA,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(1),
					DateTime.now(IMDProperties.getServerTimeZone()),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(30),
					"",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					);
			
			InventoryLoader invLdr = new InventoryLoader();
			assertTrue(invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()) >= 0 );
			assertTrue(invLdr.deleteFoodInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()) >= 0 );
			
			assertEquals(1,invLdr.addFoodInventory(invItem1));
			assertEquals(1,invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()));
			assertEquals(1,invLdr.addFoodInventory(invItem2));
			assertEquals(1,invLdr.deleteFoodInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception ");
		}
	
	}	
	
	@Test
	void testFeedInventoryItemUpdate() {
		try {
			FoodInventoryItem invItem1 = new FoodInventoryItem("-999",
					"IMD",Util.LookupValues.FEED,Util.FeedItems.VANDA,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(1),
					DateTime.now(IMDProperties.getServerTimeZone()),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(30),
					"ISHRAT",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone()).minusDays(1)
					);
			invItem1.setNumberOfUnitsUnit("BAGS");
			invItem1.setPricePerUnitUnit("PKR");
			invItem1.setQuantityPerUnitUnit("KGS");

			FoodInventoryItem invItem2 = new FoodInventoryItem("-999",
					"IMD",Util.LookupValues.FEED,Util.FeedItems.VANDA,
					10f,20f,1550f,
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(3),
					DateTime.now(IMDProperties.getServerTimeZone()).minusDays(2),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(3).plusDays(60),
					"GHUMAN",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					); 
			invItem2.setNumberOfUnitsUnit("PACKS");
			invItem2.setPricePerUnitUnit(null);
			invItem2.setQuantityPerUnitUnit(null);

			InventoryLoader invLdr = new InventoryLoader();
			assertTrue(invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()) >= 0 );
			
			assertEquals(1,invLdr.addFoodInventory(invItem1));
			assertEquals(1,invLdr.updateFoodInventoryItem(invItem2));
			
			FoodInventoryItem item = invLdr.retrieveFoodItemInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()).get(0);
			assertEquals(invItem2.getNumberOfUnitsUnit(),item.getNumberOfUnitsUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),item.getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),item.getQuantityPerUnitUnit());
			assertEquals(invItem2.getQuantityPerUnit(),item.getQuantityPerUnit());
			assertEquals(invItem2.getPricePerUnit(),item.getPricePerUnit());
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm()),Util.getDateTimeInSQLFormat(item.getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getReceivedDttm()),Util.getDateTimeInSQLFormat(item.getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getExpiryDttm()),Util.getDateTimeInSQLFormat(item.getExpiryDttm()));
			assertEquals(invItem2.getPurchasedFromCd(),item.getPurchasedFromCd());
			
			assertEquals(1,invLdr.deleteFoodInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception ");
		}
	
	}	

	@Test
	void testFeedInventoryItemSearch() {
		try {
			String categoryCd = "TST_CTGRY";
			String valueCd = "TST_ITEM";
			FoodInventoryItem invItem1 = new FoodInventoryItem("-999",
					"IMD",categoryCd, valueCd,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(50),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(51),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(81),
					"",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					);
			
			invItem1.setPricePerUnitUnit("Rs.");
			invItem1.setQuantityPerUnitUnit("Kgs.");
			invItem1.setNumberOfUnitsUnit("Bags");

			FoodInventoryItem invItem2 = new FoodInventoryItem("-998",
					"IMD",categoryCd, valueCd,
					50f,40f,1580f,
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(45),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(46),
					DateTime.now(IMDProperties.getServerTimeZone()).plusDays(76),
					"",new User("KASHIF"),DateTime.now(IMDProperties.getServerTimeZone())
					);

			invItem2.setPricePerUnitUnit("$");
			invItem2.setQuantityPerUnitUnit("ltr.");
			invItem2.setNumberOfUnitsUnit("Container");
			
			LookupValuesLoader lvLdr = new LookupValuesLoader();
			
			
			LookupValues lv = new LookupValues(categoryCd, valueCd, "test to be deleted", "test to be deleted", "-999", "-999");
			lv.setCreatedBy(new User("KASHIF"));
			lv.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			lv.setUpdatedBy(new User("KASHIF"));
			lv.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			

			InventoryLoader invLdr = new InventoryLoader();
			assertTrue(invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()) >= 0 );
			assertTrue(invLdr.deleteFoodInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()) >= 0 );
			assertTrue(lvLdr.deleteLookupValue(categoryCd, valueCd)>=0);
			assertEquals(1,lvLdr.insertLookupValues(lv));

			assertEquals(1,invLdr.addFoodInventory(invItem1));
			assertEquals(1,invLdr.addFoodInventory(invItem2));
			
			FoodInventoryItemBean searchBean = new FoodInventoryItemBean();
			
			searchBean.setStockTrackingId(invItem1.getStockTrackingId());
			
			List<FoodInventoryItem> searchResults = invLdr.retrieveFoodItemInventory(searchBean);
			assertTrue(searchResults != null);
			assertEquals(1,searchResults.size());
			assertEquals(invItem1.getLookupCd(),searchResults.get(0).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getExpiryDttm()));
			assertEquals(invItem1.getNumberOfUnits(),searchResults.get(0).getNumberOfUnits());
			assertEquals(invItem1.getQuantityPerUnit(),searchResults.get(0).getQuantityPerUnit());
			assertEquals(invItem1.getPricePerUnit(),searchResults.get(0).getPricePerUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),searchResults.get(0).getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),searchResults.get(0).getQuantityPerUnitUnit());
			assertEquals(invItem1.getNumberOfUnitsUnit(),searchResults.get(0).getNumberOfUnitsUnit());

			searchBean.setOrgId(invItem1.getOrgId());
			searchBean.setStockTrackingId(null);
			searchBean.setOrderFromDttmStr(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm().minusDays(1)));
			searchBean.setOrderToDttmStr(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm().plusDays(6)));
			searchResults = invLdr.retrieveFoodItemInventory(searchBean);
			assertTrue(searchResults != null);
			assertEquals(2,searchResults.size());
			assertEquals(invItem1.getLookupCd(),searchResults.get(0).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getExpiryDttm()));
			assertEquals(invItem1.getNumberOfUnits(),searchResults.get(0).getNumberOfUnits());
			assertEquals(invItem1.getQuantityPerUnit(),searchResults.get(0).getQuantityPerUnit());
			assertEquals(invItem1.getPricePerUnit(),searchResults.get(0).getPricePerUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),searchResults.get(0).getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),searchResults.get(0).getQuantityPerUnitUnit());
			assertEquals(invItem1.getNumberOfUnitsUnit(),searchResults.get(0).getNumberOfUnitsUnit());
			
			assertEquals(invItem2.getLookupCd(),searchResults.get(1).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getExpiryDttm()));
			assertEquals(invItem2.getNumberOfUnits(),searchResults.get(1).getNumberOfUnits());
			assertEquals(invItem2.getQuantityPerUnit(),searchResults.get(1).getQuantityPerUnit());
			assertEquals(invItem2.getPricePerUnit(),searchResults.get(1).getPricePerUnit());
			assertEquals(invItem2.getPricePerUnitUnit(),searchResults.get(1).getPricePerUnitUnit());
			assertEquals(invItem2.getQuantityPerUnitUnit(),searchResults.get(1).getQuantityPerUnitUnit());
			assertEquals(invItem2.getNumberOfUnitsUnit(),searchResults.get(1).getNumberOfUnitsUnit());
			
			searchBean = new FoodInventoryItemBean();
			searchBean.setCategoryCd(invItem1.getCategoryCd());
			searchBean.setLookupCd(invItem1.getLookupCd());
			searchResults = invLdr.retrieveFoodItemInventory(searchBean);
			assertTrue(searchResults != null);
			assertEquals(2,searchResults.size());
			assertEquals(invItem1.getLookupCd(),searchResults.get(0).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem1.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(0).getExpiryDttm()));
			assertEquals(invItem1.getNumberOfUnits(),searchResults.get(0).getNumberOfUnits());
			assertEquals(invItem1.getQuantityPerUnit(),searchResults.get(0).getQuantityPerUnit());
			assertEquals(invItem1.getPricePerUnit(),searchResults.get(0).getPricePerUnit());
			assertEquals(invItem1.getPricePerUnitUnit(),searchResults.get(0).getPricePerUnitUnit());
			assertEquals(invItem1.getQuantityPerUnitUnit(),searchResults.get(0).getQuantityPerUnitUnit());
			assertEquals(invItem1.getNumberOfUnitsUnit(),searchResults.get(0).getNumberOfUnitsUnit());
			
			assertEquals(invItem2.getLookupCd(),searchResults.get(1).getLookupCd());
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getOrderDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getOrderDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getReceivedDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getReceivedDttm()));
			assertEquals(Util.getDateTimeInSQLFormat(invItem2.getExpiryDttm()),Util.getDateTimeInSQLFormat(searchResults.get(1).getExpiryDttm()));
			assertEquals(invItem2.getNumberOfUnits(),searchResults.get(1).getNumberOfUnits());
			assertEquals(invItem2.getQuantityPerUnit(),searchResults.get(1).getQuantityPerUnit());
			assertEquals(invItem2.getPricePerUnit(),searchResults.get(1).getPricePerUnit());
			assertEquals(invItem2.getPricePerUnitUnit(),searchResults.get(1).getPricePerUnitUnit());
			assertEquals(invItem2.getQuantityPerUnitUnit(),searchResults.get(1).getQuantityPerUnitUnit());
			assertEquals(invItem2.getNumberOfUnitsUnit(),searchResults.get(1).getNumberOfUnitsUnit());

			
			assertEquals(1,invLdr.deleteFoodInventory(invItem1.getOrgId(), invItem1.getStockTrackingId()));
			assertEquals(1,invLdr.deleteFoodInventory(invItem2.getOrgId(), invItem2.getStockTrackingId()));
			assertEquals(1,lvLdr.deleteLookupValue(categoryCd, valueCd));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception ");
		}
	
	}	
	
}








