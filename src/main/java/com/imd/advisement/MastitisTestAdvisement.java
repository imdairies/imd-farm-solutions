package com.imd.advisement;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Note;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

/**
 *  
 * @author kashif.manzoor
 *
 */
public class MastitisTestAdvisement extends AdvisementRule {
	

	private String testName;

	public MastitisTestAdvisement(){
		setAdvisementID(Util.AdvisementRules.MASTITIS);
		testName = Util.AdvisementRules.MASTITIS;
	}

	@Override
	public List<Animal> getAdvisementRuleAddressablePopulation(String orgId) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving cows that have been tested for: " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActiveLactatingAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
//						LocalDate startDate = LocalDate.now(IMDProperties.getServerTimeZone()).minusDays((int)ruleDto.getThirdThreshold());
//						LocalDate endDate = LocalDate.now(IMDProperties.getServerTimeZone()).plusDays(1);
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,null,
								Util.LifeCycleEvents.MEDICALTST, null,this.testName,null,null,null);
						String ruleNote = "";
						String animalNote = ""; 						
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							int daysSinceTested= getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							IMDLogger.log(animal.getAnimalTag() + " was given " + testName + " test on " + lifeEvents.get(0).getEventTimeStamp() + "(" + daysSinceTested + " days ago)", Util.INFO);
							animalNote = "This animal was given " + testName + " test " + daysSinceTested + " days ago. ";
								if (ruleDto.getThirdThreshold() > 0 && daysSinceTested >= ruleDto.getThirdThreshold()) {
									ruleNote = ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += "You should immediately administer " + testName + " test as it is now over-due";
								} else if (ruleDto.getSecondThreshold() > 0 && daysSinceTested >= ruleDto.getSecondThreshold()) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += "You should administer " + testName + " test as soon as possible";
								} else if (ruleDto.getFirstThreshold() > 0 && daysSinceTested >= ruleDto.getFirstThreshold()) {
									ruleNote = ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += "You should plan to administer " + testName + " test soon";
								}
						} else {
							ruleNote = ruleDto.getThirdThresholdMessage();
							animal.setThreshold3Violated(true);
							animalNote += "You should immediately administer " + testName + " test as it is now long over-due";
						}
						if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
							if (lifeEvents != null && !lifeEvents.isEmpty())
								animal.addLifecycleEvent(lifeEvents.get(0));
							ArrayList<Note> notesList = new ArrayList<Note>();
							notesList.add(new Note(1,ruleNote));
							notesList.add(new Note(2,animalNote));
							animal.setNotes(notesList);
							eligiblePopulation.add(animal);
						}
						
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eligiblePopulation;
	}

	private int getDaysBetween(DateTime endTimeStamp, DateTime startTimeStamp) {
		if (endTimeStamp == null || startTimeStamp == null) 
			return 0;
		return (new Period(startTimeStamp, endTimeStamp, PeriodType.days()).getDays());
	}

	@Override
	public HashMap<Animal, String> applyAdvisementRule(List<Animal> addressablePopulation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Animal, Integer> channelAdvisementRuleOutcome(List<Animal> addressablePopulation) {
		// TODO Auto-generated method stub
		return null;
	}

}
