package com.imd.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.dto.LactationInformation;
import com.imd.dto.LifecycleEvent;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class LactationInformationManager {
	
	public List<LactationInformation> getAnimalLactationInformation(String orgId, String animalTag) {
		
		List<LactationInformation> lacInfo = new ArrayList<LactationInformation>();
		
		LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();
		List<LifecycleEvent> calvingEvts = evtLoader.retrieveSpecificLifeCycleEventsForAnimal(orgId, animalTag, 
				null, null, Util.LifeCycleEvents.ABORTION, Util.LifeCycleEvents.PARTURATE,
				null,null,null,null);
		if (calvingEvts == null || calvingEvts.isEmpty())
			// never calved so doesn't have any lactation
			return lacInfo;

		int lactationCount = calvingEvts.size();
		DateTime endTimestamp = DateTime.now(IMDProperties.getServerTimeZone());
		for (int processedCount = 0; processedCount < calvingEvts.size(); processedCount++) {
			LifecycleEvent evt = calvingEvts.get(processedCount);
			LactationInformation lct = new LactationInformation();
			lct.setLactationStartTimestamp(evt.getEventTimeStamp());
			lct.setLactationEndTimestamp(endTimestamp);
			endTimestamp = evt.getEventTimeStamp();
			lct.setLactationNumber(lactationCount-processedCount);
			if (evt.getAuxField3Value() == null || evt.getAuxField3Value().isEmpty()) {
				List<LifecycleEvent> birthEvts = evtLoader.retrieveSpecificLifeCycleEvents(orgId, 
						lct.getLactationStartTimestamp().minusDays(2),
						lct.getLactationStartTimestamp().plusDays(2),
						Util.LifeCycleEvents.BIRTH, null,
						null,animalTag,null,null);
				
				if (birthEvts != null && !birthEvts.isEmpty()) {
					lct.setCalfTag(birthEvts.get(0).getAnimalTag());
				}
			} else {
				lct.setCalfTag(evt.getAuxField3Value());
			}
			List<LifecycleEvent> insemEvts = evtLoader.retrieveSpecificLifeCycleEventsForAnimal(orgId, animalTag, 
					processedCount == lactationCount-1 /* first lactation */ ? null : calvingEvts.get(processedCount+1).getEventTimeStamp(), 
					lct.getLactationStartTimestamp(),
					Util.LifeCycleEvents.MATING, Util.LifeCycleEvents.INSEMINATE,
					null,null,null,null);
			if (insemEvts == null || insemEvts.isEmpty()) {
				// Data error, a calving event was found but no insemination record was found.
				IMDLogger.log("Error occurred while processing information of Lactation # " + 
				lct.getLactationNumber() + " for the animal " + animalTag +
				". A Calving event was found but no insemination or mating was found for this calving event", Util.ERROR);
			} else {
				lct.setInseminationAttemptCount(insemEvts.size());
				LifecycleEvent successfulInseminationEvenet =  insemEvts.get(0);
				if (successfulInseminationEvenet.getEventType().getEventCode().equals(Util.LifeCycleEvents.MATING)) {
					if (!successfulInseminationEvenet.getAuxField2Value().equals(Util.YES)) {
						IMDLogger.log("The last mating that may have resulted in the successful conception of lactation # " + 
								lct.getLactationNumber() + " of animal " + animalTag + 
								" does not have the correct outcome value. It should have been " + Util.YES + " but is was " +  
								successfulInseminationEvenet.getAuxField2Value(), Util.WARNING);
					}
				} else if (successfulInseminationEvenet.getEventType().getEventCode().equals(Util.LifeCycleEvents.INSEMINATE)) {
					if (!successfulInseminationEvenet.getAuxField3Value().equals(Util.YES)) {
						IMDLogger.log("The last insemination that may have resulted in the successful conception of lactation # " + 
								lct.getLactationNumber() + " of animal " + animalTag + 
								" does not have the "
								+ " correct outcome value. It should have been " + Util.YES + " but is was " +  
								successfulInseminationEvenet.getAuxField3Value(), Util.WARNING);
					}
				}
			}
			
			MilkingDetailLoader milkLoader = new MilkingDetailLoader();
			lct.setMilkingProduction(milkLoader.getTotalProductionOfCow(orgId, animalTag, lct.getLactationStartTimestamp(), lct.getLactationEndTimestamp()));
			lct.setMaxDailyProduction(milkLoader.getMaximumDailyProductionOfCow(orgId, animalTag, lct.getLactationStartTimestamp(), lct.getLactationEndTimestamp()));
			lct.setDaysInMilking((int)milkLoader.getDurationInMilking(orgId, animalTag, lct.getLactationStartTimestamp(), lct.getLactationEndTimestamp(), Util.DurationType.DAYS));
			if (lct.getDaysInMilking() > 0)
				lct.setLpd(lct.getMilkingProduction() / lct.getDaysInMilking());
			else 
				lct.setLpd(0f);
			
			lacInfo.add(lct);
		}
		return lacInfo;
	}
}


