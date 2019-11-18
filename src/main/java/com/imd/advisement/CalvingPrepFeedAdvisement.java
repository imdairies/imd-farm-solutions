package com.imd.advisement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

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

public class CalvingPrepFeedAdvisement extends AdvisementRule {
	

	public CalvingPrepFeedAdvisement(){
		setAdvisementID(Util.AdvisementRules.CALVINGPREPFEED);
	}


	@Override
	public List<Animal> applyAdvisementRule(String orgId, String languageCd) {
		List<Animal> eligiblePopulation = new ArrayList<Animal>();
		try {
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Animal> animalPopulation = null;
			IMDLogger.log("Retrieving pergnant animals : " + getAdvisementID(), Util.INFO);
			Advisement ruleDto =  advLoader.retrieveAdvisementRule(orgId, getAdvisementID(), true);
			if (ruleDto == null) {
				return null;
			} else {				
				AnimalLoader animalLoader = new AnimalLoader();
				LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
				animalPopulation = animalLoader.retrieveActivePregnantAnimals(orgId);
				if (animalPopulation != null && !animalPopulation.isEmpty()) {
					Iterator<Animal> it = animalPopulation.iterator();
					while (it.hasNext()) {
						Animal animal = it.next();
						List<LifecycleEvent> lifeEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
								orgId,animal.getAnimalTag(),
								null,
								null,
								Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
						if (lifeEvents != null && !lifeEvents.isEmpty()) {
							IMDLogger.log("Insemination/Mating Date: " + lifeEvents.get(0).getEventTimeStamp(), Util.INFO);
							int daysSinceInseminated= Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()), lifeEvents.get(0).getEventTimeStamp());
							
							List<LifecycleEvent> preCalvingFeedEvents = eventsLoader.retrieveSpecificLifeCycleEventsForAnimal(
									orgId,animal.getAnimalTag(),
									new LocalDate(lifeEvents.get(0).getEventTimeStamp(),IMDProperties.getServerTimeZone()),
									null,
									Util.LifeCycleEvents.PRECAVNGFD, null,null,null,null,null);
							boolean item1Found = ruleDto.getAuxInfo1() == null || ruleDto.getAuxInfo1().isEmpty();
							boolean item2Found = ruleDto.getAuxInfo2() == null || ruleDto.getAuxInfo2().isEmpty();
							boolean item3Found = ruleDto.getAuxInfo3() == null || ruleDto.getAuxInfo3().isEmpty();
							boolean item4Found = ruleDto.getAuxInfo4() == null || ruleDto.getAuxInfo4().isEmpty();
							boolean item5Found = ruleDto.getAuxInfo5() == null || ruleDto.getAuxInfo5().isEmpty();
							int daysToParturition = Util.getDaysBetween(DateTime.now(IMDProperties.getServerTimeZone()).plusDays(Util.LACTATION_DURATION-daysSinceInseminated), 
									DateTime.now(IMDProperties.getServerTimeZone()));
							
							if (preCalvingFeedEvents != null) {
								Iterator<LifecycleEvent> calvingIt = preCalvingFeedEvents.iterator();
								while (calvingIt.hasNext()) {
									LifecycleEvent feedEvt = calvingIt.next();
									String feedItemCode = feedEvt.getAuxField1Value();
									if (!item1Found && feedItemCode.equalsIgnoreCase(this.getFeedItemCode(ruleDto.getAuxInfo1()))) {
										// feed item 1 should be given and there is an event that indicates it was in fact given.
										item1Found = true;
									} else if (!item2Found && feedItemCode.equalsIgnoreCase(this.getFeedItemCode(ruleDto.getAuxInfo2()))) {
										// feed item 2 should be given and there is an event that indicates it was in fact given.
										item2Found = true;
									} else if (!item3Found && feedItemCode.equalsIgnoreCase(this.getFeedItemCode(ruleDto.getAuxInfo3()))) {
										// feed item 2 should be given and there is an event that indicates it was in fact given.
										item3Found = true;
									} else if (!item4Found && feedItemCode.equalsIgnoreCase(this.getFeedItemCode(ruleDto.getAuxInfo4()))) {
										// feed item 2 should be given and there is an event that indicates it was in fact given.
										item4Found = true;
									} else if (!item5Found && feedItemCode.equalsIgnoreCase(this.getFeedItemCode(ruleDto.getAuxInfo5()))) {
										// feed item 2 should be given and there is an event that indicates it was in fact given.
										item5Found = true;
									} else {
										IMDLogger.log("The Feed Item: " + feedItemCode + " was administered during pre-calving feeding even though there is no advisement for this. While this is all well and good, you may be wasting money unnecessarily.", Util.WARNING);
									}
//										float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo1());
//										float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo1());
//										float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo1());
//										if (th3 > 0 && daysToParturition)
								}
								
							}
							
							String ruleNote = "";
							String animalNote = "This cow is expected to parturate after " + daysToParturition + " days and ";						

							if (!item1Found) {
								IMDLogger.log("The Feed Item : " + this.getFeedItemCode(ruleDto.getAuxInfo1()) + " have not yet been given to the animal in its current pregnancy. We will now check whether the threshold is violated or not !", Util.INFO);
								float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo1());
								float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo1());
								float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo1());
								if (th3 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo1()) + ". Immediately start " + this.getFeedItemCode(ruleDto.getAuxInfo1());
								} else if (th2 > 0.0f && (daysToParturition <= th2)) {
									ruleNote += ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo1()) + ". Please start " + this.getFeedItemCode(ruleDto.getAuxInfo1());
								} else if (th1 > 0.0f && (daysToParturition <= th1)) {
									ruleNote += ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo1()) + ". Start your preparation for giving " + this.getFeedItemCode(ruleDto.getAuxInfo1());
								}
							}
							if (!item2Found) {
								IMDLogger.log("The Feed Item : " + this.getFeedItemCode(ruleDto.getAuxInfo2()) + " have not yet been given to the animal in its current pregnancy. We will now check whether the threshold is violated or not !", Util.INFO);
								float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo2());
								float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo2());
								float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo2());
								if (th3 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo2()) + ". Immediately start " + this.getFeedItemCode(ruleDto.getAuxInfo2());
								} else if (th2 > 0.0f && (daysToParturition <= th2)) {
									ruleNote += ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo2()) + ". Please start " + this.getFeedItemCode(ruleDto.getAuxInfo2());
								} else if (th1 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo2()) + ". Start your preparation for giving " + this.getFeedItemCode(ruleDto.getAuxInfo2());
								}
							}							
							if (!item3Found) {
								IMDLogger.log("The Feed Item : " + this.getFeedItemCode(ruleDto.getAuxInfo3()) + " have not yet been given to the animal in its current pregnancy. We will now check whether the threshold is violated or not !", Util.INFO);
								float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo3());
								float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo3());
								float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo3());
								if (th3 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo3()) + ". Immediately start " + this.getFeedItemCode(ruleDto.getAuxInfo3());
								} else if (th2 > 0.0f && (daysToParturition <= th2)) {
									ruleNote += ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo3()) + ". Please start " + this.getFeedItemCode(ruleDto.getAuxInfo3());
								} else if (th1 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo3()) + ". Start your preparation for giving " + this.getFeedItemCode(ruleDto.getAuxInfo3());
								}
							}								
							if (!item4Found) {
								IMDLogger.log("The Feed Item : " + this.getFeedItemCode(ruleDto.getAuxInfo4()) + " have not yet been given to the animal in its current pregnancy. We will now check whether the threshold is violated or not !", Util.INFO);
								float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo4());
								float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo4());
								float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo4());
								if (th3 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo4()) + ". Immediately start " + this.getFeedItemCode(ruleDto.getAuxInfo4());
								} else if (th2 > 0.0f && (daysToParturition <= th2)) {
									ruleNote = ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo4()) + ". Please start " + this.getFeedItemCode(ruleDto.getAuxInfo4());
								} else if (th1 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo4()) + ". Start your preparation for giving " + this.getFeedItemCode(ruleDto.getAuxInfo4());
								}
							}								
							if (!item5Found) {
								IMDLogger.log("The Feed Item : " + this.getFeedItemCode(ruleDto.getAuxInfo5()) + " have not yet been given to the animal in its current pregnancy. We will now check whether the threshold is violated or not !", Util.INFO);
								float th1 = ruleDto.getFirstThreshold() + this.getFeedItemThreshold1(ruleDto.getAuxInfo5());
								float th2 = ruleDto.getSecondThreshold() + this.getFeedItemThreshold2(ruleDto.getAuxInfo5());
								float th3 = ruleDto.getThirdThreshold() + this.getFeedItemThreshold3(ruleDto.getAuxInfo5());
								if (th3 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getThirdThresholdMessage();
									animal.setThreshold3Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo5()) + ". Immediately start " + this.getFeedItemCode(ruleDto.getAuxInfo5());
								} else if (th2 > 0.0f && (daysToParturition <= th2)) {
									ruleNote += ruleDto.getSecondThresholdMessage();
									animal.setThreshold2Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo5()) + ". Please start " + this.getFeedItemCode(ruleDto.getAuxInfo5());
								} else if (th1 > 0.0f && (daysToParturition <= th3)) {
									ruleNote += ruleDto.getFirstThresholdMessage();
									animal.setThreshold1Violated(true);
									animalNote += " it hasn't yet been fed " + this.getFeedItemCode(ruleDto.getAuxInfo5()) + ". Start your preparation for giving " + this.getFeedItemCode(ruleDto.getAuxInfo5());
								}
							}
							if (animal.isThreshold1Violated() || animal.isThreshold2Violated() || animal.isThreshold3Violated()) {
								// This cow has pending pre-calving feed items.
								animal.addLifecycleEvent(lifeEvents.get(0));
								ArrayList<Note> notesList = new ArrayList<Note>();
								notesList.add(new Note(1,ruleNote));
								notesList.add(new Note(2,animalNote));
								animal.setNotes(notesList);
								eligiblePopulation.add(animal);
							}
						} else {
							IMDLogger.log("This pregnant cow does not have an insemination event. The user should have added an insemination event !", Util.ERROR);
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


	
	private String getFeedItemCode(String auxInfo) {
		String code = "";
		try {
			code = auxInfo.substring(0,auxInfo.indexOf("["));
		} catch (Exception ex) {
			IMDLogger.log("Exception occurred while extracting the FEED ITEM CODE from the event Aux Info: " + auxInfo, Util.WARNING);
			ex.printStackTrace();
		}
		return code;
	}
	
	private float getFeedItemThreshold(String auxInfo, int thresholdNum) {
		float threshold = -999f;
		try {
			int startIndex = auxInfo.indexOf("[TH" + thresholdNum + "=");
			int endIndex = auxInfo.indexOf("]",startIndex);
			threshold = new Float(auxInfo.substring(startIndex + ("[TH" + thresholdNum + "=").length(),endIndex));
		} catch (Exception ex) {
			IMDLogger.log("Exception occurred while extracting the Threshold Value from the event Aux Info: " + auxInfo, Util.WARNING);
			ex.printStackTrace();
		}
		return threshold;
	}
	private float getFeedItemThreshold1(String auxInfo) {
		
		return getFeedItemThreshold(auxInfo,1);
	}

	private float getFeedItemThreshold2(String auxInfo) {
		
		return getFeedItemThreshold(auxInfo,2);
	}
	private float getFeedItemThreshold3(String auxInfo) {
		
		return getFeedItemThreshold(auxInfo,3);
	}


}
