package com.imd.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.imd.controller.LactationInformationManager;
import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LactationInformation;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;;

@Path("/animals")
public class AnimalSrvc {

	private static final int INSEMINATION_SEARCH_WINDOW_DAYS = 290;


	
	
	/**
	 * Retrieves ALL the active animals in a farm
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getAllActiveAnimals() {
		// TODO: Incorporate security in GET methods.
		String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	List<Animal> animals = loader.retrieveActiveAnimals((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    		
			if (animals == null || animals.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active animal found\"}").build();
			}
	    	Iterator<Animal> animalIt = animals.iterator();
	    	while (animalIt.hasNext()) {
	    		Animal aimal = animalIt.next();
	    		animalsJson += "{\n" + aimal.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	animalsJson = "[" + animalsJson.substring(0,animalsJson.lastIndexOf(",\n")) + "]";
	    	IMDLogger.log(animalsJson, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.getAllActiveAnimals() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(Util.HTTPCodes.OK).entity(animalsJson).build(); 
    }
	
	@POST
	@Path("/getgrowthdata")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getGrowthData(AnimalBean searchBean){
		String methodName = "getGrowthData";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.NO_MATCHING_RECORD_FOUND).getMessageText();
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
			} else if (animalValues.size() > 1) {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.MULTIPLE_RECORDS_FOUND_ERROR).getMessageText();
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
			}
						
    		Animal animalValue = animalValues.get(0);
    		LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();
    		List<LifecycleEvent> weights = evtLoader.retrieveSpecificLifeCycleEventsForAnimal(animalValue.getOrgId(), animalValue.getAnimalTag(), Util.LifeCycleEvents.WEIGHT);

    		if (weights == null || weights.isEmpty()) {
    			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.WEIGHT_NEVER_MEASURED).getMessageText();
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
			}
    		
    		IMDLogger.log("Total Weight Events " + weights.size(), Util.INFO);
    		String days = "";
    		String idealWeight = "";
    		String animalWeight = "";
    		int recProcessed = weights.size()-1;
    		String lastMeasuredWeight = "0";  
    		double dailyWeightGain = 0;
    		double offset = Util.BIRTH_WEIGHT;
    		double daysInCategory = 0;
    		double largestYAxisValue = 0;
    		double idealWtAtAge = 0;
			int ageAtWeightMeasurement = Util.getDaysBetween(weights.get(recProcessed).getEventTimeStamp(), animalValue.getDateOfBirth());
    		
			for (int i=0; i<= animalValue.getCurrentAgeInDays(); i++) {
				days += i;
				if (i == 0) {
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR1;
				} else if (i == ((365*1) + 1)) {
					offset = offset + ((daysInCategory) * dailyWeightGain);
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR2;
					daysInCategory = 0;
				} else if (i == ((365*2) + 1)) {
					offset = offset + ((daysInCategory) * dailyWeightGain);
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR3;
					daysInCategory = 0;
				} else if (i == ((365*3) + 1)) {
					offset = offset + ((daysInCategory) * dailyWeightGain);
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR4;
					daysInCategory = 0;
				} else if (i == ((365*4) + 1)) {
					offset = offset + ((daysInCategory) * dailyWeightGain);
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR5;
					daysInCategory = 0;
				} else if (i == ((365*5) + 1)) {
					offset = offset + ((daysInCategory) * dailyWeightGain);
					dailyWeightGain = Util.DAILY_WEIGHT_GAIN_YEAR5;
					daysInCategory = 0;
				}
				idealWtAtAge = Math.min(( offset + (daysInCategory * dailyWeightGain)), Util.MAX_BODY_WEIGHT);

				idealWeight += Util.formatToSpecifiedDecimalPlaces(idealWtAtAge,1);
				Double extrapolatedWeight = getExtrapolatedWeight(weights, animalValue.getDateOfBirth(),i);

				if (extrapolatedWeight == null)
					extrapolatedWeight = Double.parseDouble(lastMeasuredWeight);
				else
					lastMeasuredWeight = Double.toString(extrapolatedWeight);
				
				if (ageAtWeightMeasurement == i) {
					lastMeasuredWeight = weights.get(recProcessed).getAuxField1Value();
					animalWeight += Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(lastMeasuredWeight), 1);
					recProcessed = recProcessed == 0 ? 0 : recProcessed-1;
				} else {
					animalWeight += lastMeasuredWeight.isEmpty() ? "" : Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(lastMeasuredWeight), 1);
				}	
				
				if (i < animalValue.getCurrentAgeInDays()) {
					days += ",";
					idealWeight += ",";
//					animalWeight +=  (i >= ageAtLastWeightMeasurement ? "" : ",");
					animalWeight +=  ",";
				}
				if (!lastMeasuredWeight.isEmpty() && largestYAxisValue < Math.max(Double.parseDouble(lastMeasuredWeight), idealWtAtAge))
					largestYAxisValue = Math.max(Double.parseDouble(lastMeasuredWeight), idealWtAtAge);				

				daysInCategory++;
			}
			animalValueResult += "{\n" +
    	    		"\"largestYAxisValue\":" + largestYAxisValue + ",\n" +			
    	    		"\"ageInDays\":[" + days + "],\n" +
    	    		"\"idealWeight\":[" + idealWeight + "],\n" +
    	    		"\"actualWeight\":[" + animalWeight + "]" +
    				"\n}";

    		if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.getGrowthData() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Following exception occurred while processing AnimalSrvc.getGrowthData(): " +  e.getClass().getName() + " " + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	
	public Double getExtrapolatedWeight(List<LifecycleEvent> reverseSortedWeightEvents, DateTime dateOfBirth, int age) {
		Double extrapolatedWeight = null;
		int beforeIndex = reverseSortedWeightEvents.size()-1;
		int afterIndex = beforeIndex;
		
		if (reverseSortedWeightEvents != null && !reverseSortedWeightEvents.isEmpty()) {
			for (int i=reverseSortedWeightEvents.size()-1; i >= 0; i--) {
				LifecycleEvent wtEvent = reverseSortedWeightEvents.get(i);
				int ageAtEvent = Util.getDaysBetween(wtEvent.getEventTimeStamp(), dateOfBirth);
				double weight = Double.parseDouble(wtEvent.getAuxField1Value());
				if (ageAtEvent == age) {
					extrapolatedWeight = weight;
					break;
				} else if (ageAtEvent < age){
					beforeIndex = i;
				} else {
					// we have two indices that we can now use to calculate extrapolated weight.
					afterIndex = i;
					double w1 = Util.BIRTH_WEIGHT;
					double w2 = weight;
					double age1 = 0;
					double age2 = ageAtEvent;
					if (afterIndex != beforeIndex) {
						w1 =  Double.parseDouble(reverseSortedWeightEvents.get(beforeIndex).getAuxField1Value());
						age1 =  Util.getDaysBetween(reverseSortedWeightEvents.get(beforeIndex).getEventTimeStamp(), dateOfBirth);;
					}
//					IMDLogger.log(" ((" + age + " - " + age1 + ")*((" + w2 + " - " + w1 + ")/(" + age2 + " - " + age1 + "))) + " + w1, Util.INFO);
					extrapolatedWeight = ((age - age1)*((w2 - w1)/(age2 - age1))) + w1;
					break;
				}
			}
		}
		IMDLogger.log("At Age " + age + " Extrapolated weight = " + extrapolatedWeight, Util.INFO);
		return extrapolatedWeight;
	}
	
	/**
	 * This API adds a new animal.
	 * Sample Use Case: Call this API to add a new animal.
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/addanimal")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addAnimal(AnimalBean animalBean){
		String methodName = "addAnimal";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		animalBean.setOrgId(orgID);
		IMDLogger.log(animalBean.toString(), Util.INFO);

		Animal animal = null;
		String tag = animalBean.getAnimalTag();
		String alias  = animalBean.getAlias();
		String typeCD = animalBean.getAnimalType();
		String dob = animalBean.getDateOfBirthStr();
		String breed = animalBean.getBreed();
		String gender = "" + animalBean.getGender();
		String damTag = animalBean.getDam();
		String sireTag = animalBean.getSire();
		String dobAccuracyInd = animalBean.getDobAccuracyInd();
		String herdJoiningDate = animalBean.getHerdJoiningDttmStr();
		String aiInd = (animalBean.getAiInd() == null || animalBean.getAiInd().trim().isEmpty()? "N" : "" + animalBean.getAiInd().charAt(0));
		
		if (tag == null || tag.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Animal Tag").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid Animal Tag.\"}").build();
		} else if (typeCD == null || typeCD.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Animal Type").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal Type.\"}").build();
		} else if (dob == null || dob.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Date of Birth").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal date of birth. If you do not know the date of birth then provide an estimated date and set the date of birth accuracy indicator to \"N\".\"}").build();
		} else if (gender == "" || gender.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Gender").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal gender.\"}").build();
		} else if (dobAccuracyInd == null || dobAccuracyInd.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Date of Birth Accuracy").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify if Date of Birth is accurate or not.\"}").build();
		} else if (breed == null || breed.trim().isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Breed").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify animal breed.\"}").build();
		} else if (herdJoiningDate == null || herdJoiningDate.isEmpty()) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.SPECIFIC_VALUE_MISSING,"Herd Joining Date").getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify herd joining date.\"}").build();
		} else if (typeCD.equalsIgnoreCase("CULLED") || typeCD.equalsIgnoreCase("DEAD")) {
			String message = MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.CANT_SET_CULL_STATUS,typeCD).getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + typeCD + " indcates an inactive animal status. You can not set an inacitve animal status at the time of animal addition. Instead, add an event that results in an inactive status.\"}").build();			
		}
		
		int result = -1;
		String frontPose = animalBean.getFrontPoseImage() == null || animalBean.getFrontPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/1.png": animalBean.getFrontPoseImage();
		String backPose =  animalBean.getBackPoseImage() == null || animalBean.getBackPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/2.png": animalBean.getBackPoseImage();
		String rightPose = animalBean.getRightPoseImage() == null || animalBean.getRightPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/3.png": animalBean.getRightPoseImage();
		String leftPose =  animalBean.getLeftPoseImage() == null || animalBean.getLeftPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/4.png": animalBean.getLeftPoseImage();

		try {
			AnimalLoader loader = new AnimalLoader();
			if (gender.equalsIgnoreCase(Util.Gender.MALE) || gender.equalsIgnoreCase(Util.GENDER_CHAR.MALE + "") )
				animal = new Sire(orgID,tag);
			else 
				animal = new Dam(orgID,tag);
			animal.setFrontSideImageURL(frontPose);
			animal.setBackSideImageURL(backPose);
			animal.setRightSideImageURL(rightPose);
			animal.setLeftSideImageURL(leftPose);
			animal.setOrgId(orgID);
			if (alias != null && !alias.trim().isEmpty())
				animal.setAlias(animalBean.getAlias());
			animal.setAnimalType(animalBean.getAnimalType());
			animal.setBreed(animalBean.getBreed());
			animal.setDateOfBirth(animalBean.getDateOfBirth("yyyy-MM-dd HH:mm"));
			animal.setHerdJoiningDate(animalBean.getHerdJoiningDate("yyyy-MM-dd HH:mm"));
			animal.setDateOfBirthEstimated(!(dobAccuracyInd.equalsIgnoreCase(Util.YES) || dobAccuracyInd.equalsIgnoreCase(Util.Y)));
			animal.setBornThroughAI(aiInd.equalsIgnoreCase(Util.YES)||aiInd.equalsIgnoreCase(Util.Y));
			if (damTag != null && !damTag.trim().isEmpty())
				animal.setAnimalDam(new Dam(orgID,damTag));
			if (sireTag != null && !sireTag.trim().isEmpty())
				animal.setAnimalSire(new Sire(orgID,sireTag));
			animal.setFrontSideImageURL(frontPose);
			animal.setBackSideImageURL(backPose);
			animal.setRightSideImageURL(rightPose);
			animal.setLeftSideImageURL(leftPose);
			animal.setCreatedBy(user);
			animal.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			animal.setUpdatedBy(user);
			animal.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.insertAnimal(animal);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addAnimal() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result == 1) {
			animal.setOperatorAtBirth(animalBean.getOperatorAtBirth());
			String message = performPostInsertionSteps(animal);
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"New Animal has been created successfully. "+ message + "\"}").build();
		}
		else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION) {
			String message = MessageCatalogLoader.getMessage(orgID, langCd, Util.MessageCatalog.ALREADY_EXISTS).getMessageText();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + message + "\"}").build();
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Another animal with the same tag already exists. Please use a different tag number.\"}").build();
		} else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Animal  '" + tag+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Animal '" + tag + "' could not be added. Please submit a bug report.\"}").build();
		else
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An unknown error occurred during animal addition\"}").build();

	}

	
	
	@POST
	@Path("/updateanimal")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateAnimal(AnimalBean animalBean){
		String methodName = "updateAnimal";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,animalBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		animalBean.setOrgId(orgID);
		IMDLogger.log(animalBean.toString(), Util.INFO);
		
		
		Animal animal = null;			
		String tag = animalBean.getAnimalTag();
		String alias  = animalBean.getAlias();
		String typeCD = animalBean.getAnimalType();
		String dob = animalBean.getDateOfBirthStr();
		String gender = animalBean.getGender();
//		String breed = animalBean.getBreed();
//		String gender = "" + animalBean.getGender();
//		String damTag = animalBean.getDam();
//		String sireTag = animalBean.getSire();
//		String dobAccuracyInd = animalBean.getDobAccuracyInd();
//		String herdJoiningDate = animalBean.getHerdJoiningDttmStr();
//		String aiInd = (animalBean.getAiInd() == null || animalBean.getAiInd().trim().isEmpty()? "N" : "" + animalBean.getAiInd().charAt(0));
		
		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid Animal Tag.\"}").build();
		}
		else if (typeCD == null || typeCD.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal Type.\"}").build();
		}
		else if (dob == null || dob.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Animal date of birth. If you do not know the date of birth then provide an estimated date and set the date of birth accuracy indicator to \"N\".\"}").build();
		}
		else if (!gender.trim().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "") && 
				!gender.trim().equalsIgnoreCase(Util.GENDER_CHAR.MALE + "")) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid value for Animal gender (M or F).\"}").build();
		}
//		else if (dobAccuracyInd == null || dobAccuracyInd.trim().isEmpty()) {
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify if Date of Birth is accurate or not.\"}").build();
//		}
//		else if (breed == null || breed.trim().isEmpty()) {
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify animal breed.\"}").build();
//		}
//		else if (herdJoiningDate == null || herdJoiningDate.isEmpty()) {
//			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify herd joining date.\"}").build();
//		}
		else if (typeCD.equalsIgnoreCase("CULLED") || typeCD.equalsIgnoreCase("DEAD")) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" + typeCD + " indcates an inactive animal status. You can not set an inacitve animal status. Instead, please add an event that results in an inactive status.\"}").build();			
		}
		
		int result = -1;

		try {
			AnimalLoader loader = new AnimalLoader();
			if (gender.equalsIgnoreCase(Util.GENDER_CHAR.FEMALE+""))
				animal = new Dam(orgID,tag);
			else
				animal = new Sire(orgID,tag);
//			animal = new Animal(tag);
//			animal.setFrontSideImageURL(frontPose);
//			animal.setBackSideImageURL(backPose);
//			animal.setRightSideImageURL(rightPose);
//			animal.setLeftSideImageURL(leftPose);
			animal.setOrgId(orgID);
			if (alias != null && !alias.trim().isEmpty())
				animal.setAlias(alias);
			animal.setAnimalTypeCD(typeCD);
//			animal.setBreed(animalBean.getBreed());
//			animal.setDateOfBirth(animalBean.getDateOfBirth("MM/dd/yyyy, hh:mm:ss aa"));
//			animal.setHerdJoiningDate(animalBean.getHerdJoiningDate("MM/dd/yyyy, hh:mm:ss aa"));
//			animal.setDateOfBirthEstimated(!dobAccuracyInd.equalsIgnoreCase("Y"));
//			animal.setBornThroughAI(aiInd.equalsIgnoreCase("Y"));
//			if (damTag != null && !damTag.trim().isEmpty())
//				animal.setAnimalDam(new Dam(damTag));
//			if (sireTag != null && !sireTag.trim().isEmpty())
//				animal.setAnimalSire(new Sire(sireTag));
//			animal.setFrontSideImageURL(frontPose);
//			animal.setBackSideImageURL(backPose);
//			animal.setRightSideImageURL(rightPose);
//			animal.setLeftSideImageURL(leftPose);
			animal.setUpdatedBy(user);
			animal.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.updateAnimal(animal);
//			IMDLogger.loggingMode = originalMode;
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addAnimal() service method: " + e.getMessage(),  Util.ERROR);
//			IMDLogger.loggingMode = originalMode;
		}
		if (result == 1) {
//			String message = performPostInsertionSteps(animal);
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Animal has been updated successfully.\"}").build();
		}
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Animal  '" + tag+ "' could not be updated. Please reduce the field length and try again.\"}").build();
		} else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Animal '" + tag + "' could not be updated. Please submit a bug report.\"}").build();
		} else
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An unknown error occurred during animal update\"}").build();

	}
	
	
	@POST
	@Path("/addsire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addSire(SireBean sireBean){
		String methodName = "addSire";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,sireBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		sireBean.setOrgID(orgID);
		IMDLogger.log(sireBean.toString(), Util.INFO);
		
			
		
		
		String tag = sireBean.getAnimalTag();
		String alias  = sireBean.getAlias();
		String breed = sireBean.getBreed();
		String semenInd = sireBean.getSemenInd();
		String recordUrl = sireBean.getRecordURL();
		String controller = sireBean.getController();
		String semenCompany = sireBean.getSemenCompany();


		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide a valid Sire Tag/Code.\"}").build();
		}
		else if (alias == null || alias.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Sire Alias.\"}").build();
		}
		else if (breed == null || breed.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must provide Sire Breed .\"}").build();
		}
		else if (semenInd == "" || semenInd.trim().isEmpty() || semenInd.trim().length() != 1) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must indicate whether this is a farm sire (semenInd=N) or a sire whose semens you are going to purchase (semenInd=Y).\"}").build();
		}
		else if (recordUrl == null || recordUrl.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify Sire Data Sheet URL.\"}").build();
		}
		else if (controller == null || controller.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify controller.\"}").build();
		}
		else if (semenCompany == null || semenCompany.trim().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify sire/semen marketing company.\"}").build();
		}
		int result = -1;

		try {
			result = 1;
			AnimalLoader loader = new AnimalLoader();
			result = loader.insertSire(sireBean,user.getUserId(),DateTime.now(IMDProperties.getServerTimeZone()),user.getUserId(),DateTime.now(IMDProperties.getServerTimeZone()));
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addSire() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result == 1) {
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Sire has been added successfully.\"}").build();
		}
		else if (result == Util.ERROR_CODE.KEY_INTEGRITY_VIOLATION)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Sire with the same tag/code already exists. Please use a different tag/code.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Sire  '" + tag+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Sire '" + tag + "' could not be added. Please submit a bug report.\"}").build();
		else
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"An unknown error occurred during Sire addition\"}").build();
	}	
	
	private String performPostInsertionSteps(Animal animalDto) {
		LifeCycleEventBean eventBean = new LifeCycleEventBean();
		eventBean.setOrgID(animalDto.getOrgId());
		eventBean.setAnimalTag(animalDto.getAnimalTag());
		eventBean.setEventComments("This birth event was automatically created during creation of the new animal");
		eventBean.setEventCode(Util.LifeCycleEvents.BIRTH);
		eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(animalDto.getDateOfBirth()));
		eventBean.setAuxField1Value(animalDto.getAnimalSire() == null ? "UNKNOWN" : animalDto.getAnimalSire().getAnimalTag());
		eventBean.setAuxField2Value(animalDto.getAnimalDam() == null ? "" : animalDto.getAnimalDam().getAnimalTag());
		eventBean.setOperatorID(animalDto.getOperatorAtBirth());
		//TODO: May be also add a parturition event automatically for the Dam ?
		
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		LifecycleEvent event;
		int result = -1;
		try {
			event = new LifecycleEvent(eventBean, "yyyy-MM-dd HH:mm:ss");
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			event.setCreatedBy(animalDto.getCreatedBy());
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(animalDto.getUpdatedBy());
			event.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.insertLifeCycleEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.performPostInsertionSteps() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result > 0)
			return "A birth event was automatically created for this animal";
		else
			return "A birth event COULD NOT be created automatically for this animal. Please remember to add it manually.";
	}


	@POST
	@Path("/adultfemalecows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getAdultFemaleCows(AnimalBean searchBean){
		String methodName = "getAdultFemaleCows";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	String animalValueResult = "";
		DateTimeFormatter fmt = null;
    	String prefix = "  ";
    	List<String> sortedJsonArray = new ArrayList<String>();
    	String noInseminationRecordJson = "";
    	try {
    		AnimalLoader animalLoader = new AnimalLoader();
    		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
			List<Animal> animalValues = animalLoader.retrieveAdultFemaleCows(searchBean.getOrgId(),270);
			List<LifecycleEvent> animalEvents = null;
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
    		double largestInseminatedHours = -999999;
    		String additionalInfo = "";
	    	while (animalValueIt.hasNext()) {
	    		additionalInfo = "";
	    		Animal animalValue = animalValueIt.next();
		    	String strInseminationTimeInfo = ""; 
	    		if (animalValue.isPregnant() || animalValue.isInseminated()) {
	    			animalEvents = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(animalValue.getOrgId(),
	    					animalValue.getAnimalTag(), DateTime.now(IMDProperties.getServerTimeZone()).minusDays(INSEMINATION_SEARCH_WINDOW_DAYS), null, Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null, null, null);
	    			if (animalEvents != null && !animalEvents.isEmpty()) {
	    				DateTime inseminatedDate =  animalEvents.get(0).getEventTimeStamp();
		    			double daysSinceInseminated = Util.getDaysBetween( DateTime.now(IMDProperties.getServerTimeZone()), inseminatedDate);
		    			double hoursSinceInseminated = Util.getHoursBetween( DateTime.now(IMDProperties.getServerTimeZone()), inseminatedDate);
	    				
		    			if (animalEvents.get(0).getEventTimeStamp().getHourOfDay() == 0 && 
	    						animalEvents.get(0).getEventTimeStamp().getMinuteOfDay() == 0)
	    					fmt = DateTimeFormat.forPattern("d MMM yyyy");
	    				else 
	    					fmt = DateTimeFormat.forPattern("d MMM yyyy h:mm a");
	    				String inseminationSireCode = animalEvents.get(0).getAuxField1Value();
	    				String sexedIndicator = animalEvents.get(0).getEventType().getEventCode().equals(Util.LifeCycleEvents.MATING) ? "N" : animalEvents.get(0).getAuxField2Value();
	    				Sire sireInfo = null;
	    				if (inseminationSireCode != null && !inseminationSireCode.isEmpty()) {
	    					sireInfo = animalLoader.retrieveSire(inseminationSireCode);
	    				}
	    				int inseminationAttempts = eventLoader.determineInseminationAttemptCountInCurrentLactation(animalValue.getOrgId(),animalValue.getAnimalTag());
	    				strInseminationTimeInfo = ",\n" + prefix + "\"lastInseminationTimeStamp\":\"" + fmt.print(animalEvents.get(0).getEventTimeStamp()) +"\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"eventTransactionID\":\"" + animalEvents.get(0).getEventTransactionID() + "\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"sireInformation\":\"" + (sireInfo == null ? "ERROR Could not find the sire (" +  inseminationSireCode + ")" : sireInfo.getAlias() + " (" + sireInfo.getAnimalTag() + ")") + "\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"isPregnant\":\"" + (animalValue.isPregnant() ? "YES" : "UNKNOWN") + "\"";
						strInseminationTimeInfo += ",\n" + prefix + "\"sexed\":\"" + sexedIndicator + "\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"inseminationAttempts\":\"" + inseminationAttempts +"\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"daysSinceInsemination\":\"" + daysSinceInseminated +"\"";
	    				strInseminationTimeInfo += ",\n" + prefix + "\"hoursSinceInsemination\":\"" + hoursSinceInseminated +"\"";
			    		animalValueResult = "{\n" + animalValue.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + strInseminationTimeInfo + "\n},\n";
			    		//IMDLogger.log("Previous oldest " + largestInseminatedDays + " current value " + daysSinceInseminated, Util.ERROR);
			    		if (hoursSinceInseminated > largestInseminatedHours /*daysSinceInseminated > largestInseminatedDays*/) {
							sortedJsonArray.add(animalValueResult);
							//largestInseminatedDays = daysSinceInseminated;
							largestInseminatedHours = hoursSinceInseminated;
						} else {
							//sortedJsonArray.add(sortedJsonArray.size()-1,animalValueResult);
			    			int j = sortedJsonArray.size()-1;
							for (;j>=0; j--){
								//String days = sortedJsonArray.get(j);
								String hours = sortedJsonArray.get(j);
//								days = days.substring(days.indexOf("daysSinceInsemination\":") + "daysSinceInsemination\":\"".length(),days.lastIndexOf('"'));		
								hours = hours.substring(hours.indexOf("hoursSinceInsemination\":") + "hoursSinceInsemination\":\"".length(),hours.lastIndexOf('"'));		
								//int iDays = Integer.parseInt(days);
								double iHours = Double.parseDouble(hours);
//								if (daysSinceInseminated >= iDays) {
//									sortedJsonArray.add(j+1,animalValueResult);
//									break;
//								}	
								if (hoursSinceInseminated >= iHours) {
									sortedJsonArray.add(j+1,animalValueResult);
									break;
								}	
							}
							if (j < 0)
								sortedJsonArray.add(0,animalValueResult);
						}
	    			} else {
		    			additionalInfo = ",\n" + prefix + "\"isPregnant\":\"\",\n" + prefix + "\"eventTransactionID\":\"\",\n" + prefix + "\"sexed\":\"\",\n" + prefix + "\"sireInformation\":\"\"";
		    			noInseminationRecordJson += "{\n" + animalValue.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + additionalInfo + "\n},\n";
	    			}
	    		} else {
	    			additionalInfo = ",\n" + prefix + "\"isPregnant\":\"\",\n" + prefix + "\"eventTransactionID\":\"\",\n"+ prefix + "\"sexed\":\"\",\n" + prefix + "\"sireInformation\":\"\"";
	    			noInseminationRecordJson += "{\n" + animalValue.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + additionalInfo + "\n},\n";
	    		}
	    	}
	    	animalValueResult = "";
	    	for (int i=sortedJsonArray.size()-1; i>=0 ; i--) {
	    		animalValueResult += sortedJsonArray.get(i) + "\n";
	    	}
	    	animalValueResult += noInseminationRecordJson;
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.getAdultFemaleCows() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }

	/**
	 * 
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/search")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response searchAnimals(AnimalBean searchBean){
		String methodName = "searchAnimals";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName, searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
		if (isValueProvided(searchBean.getGender()) && !searchBean.getGender().trim().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "") && 
			!searchBean.getGender().trim().equalsIgnoreCase(Util.GENDER_CHAR.MALE + "")) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  
			MessageCatalogLoader.getDynamicallyPopulatedMessage(orgID, langCd, Util.MessageCatalog.INVALID_GENDER, "('M' or 'F')") +
			"\"}").build();
		}
		
		
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		List<Animal> animalValues = null;
    		if (searchBean.getAnimalTag() != null && !searchBean.getAnimalTag().isEmpty()) {
    			
    			String[] tags = searchBean.getAnimalTag().split(",");
    			String tagInClause = "";
    			for (int i=0; i<tags.length; i++) {
    				tagInClause += (i==0 ? "'" : ",'") + tags[i].trim() + "'";
    			}
    			tagInClause = "(" + tagInClause + ")";
    			searchBean.setAnimalTag(tagInClause);
    			
    		} 
			animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.searchAnimals() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }



	private boolean isValueProvided(String fieldValue) {
		return fieldValue != null && !fieldValue.trim().isEmpty();
	}
	/**
	 * Returns all the cows who have calved at least once at our farm and which are still in the farm
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/getactivedams")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getActiveFemale(AnimalBean searchBean){
		String methodName = "getActiveFemale";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveDams(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active dam found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.getActiveFemale() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	
	
	
	/**
	 * Returns all the cows who have calved at least once at our farm irrespective of the cows current status (active or inactive)
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/getalldams")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getAllDams(AnimalBean searchBean){
		String methodName = "getAllDams";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveAllDams(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No active dam found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.getActiveFemale() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }	
	

	@POST
	@Path("/lactatingcows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimals(AnimalBean searchBean){
		String methodName = "retrieveLactatingAnimals";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
		String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveLactatingAnimals(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveLactatingAnimals() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }	
	@POST
	@Path("/drycows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveDryCows(AnimalBean searchBean){
		String methodName = "retrieveDryCows";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveDryAnimals(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveHeifers() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	@POST
	@Path("/femalecalves")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFemaleCalves(AnimalBean searchBean){
		String methodName = "retrieveFemaleCalves";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
    	String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveFemaleCalves(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveFemaleCalves() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	@POST
	@Path("/pregnantcows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrievePregnantAnimals(AnimalBean searchBean){
		String methodName = "retrievePregnantAnimals";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

		String animalValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActivePregnantAnimals(searchBean.getOrgId());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrievePregnantAnimals() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }
	

	@POST
	@Path("/monthlymilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMonthlyMilkingRecord(MilkingDetailBean searchBean){
		String methodName = "";
		IMDLogger.log("retrieveMonthlyMilkingRecord Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);

    	String milkingInformation = "";
    	List<MilkingDetail> dailyRecords = new ArrayList<MilkingDetail>(3);
    	LocalDate currentDate = null;
    	try {
			if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		AnimalLoader animalLoader = new AnimalLoader();
    		AnimalBean animalBean = new AnimalBean();
    		animalBean.setOrgId(searchBean.getOrgID());
    		animalBean.setAnimalTag(searchBean.getAnimalTag());    		
    		List<Animal> animals = animalLoader.getAnimalRawInfo(animalBean);
    		if (animals.size() != 1) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Unable to find the animal. foud " + animals.size() + " records\"}").build();    			    			
    		}
    		Animal animal = animals.get(0);
    		IMDLogger.log(animal.getAnimalType(), Util.INFO);
    		if (!animal.getGender().equalsIgnoreCase(Util.GENDER_CHAR.FEMALE + "")) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information only applies to female animals\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.FEMALECALF)) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to female calves\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.HEIFER)) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to heifers\"}").build();    			
    		}    		
			List<MilkingDetail> animalValues = loader.retrieveMonthlyMilkingRecordsOfCow(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<MilkingDetail> recValuesIt = animalValues.iterator();
	    	while (recValuesIt.hasNext()) {
	    		MilkingDetail recValue = recValuesIt.next();
	    		if (currentDate == null) {
	    			currentDate = recValue.getRecordDate();
	    		} else if (!currentDate.isEqual(recValue.getRecordDate())) {
		    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + ",\n";
		    		dailyRecords.clear();
	    			currentDate = recValue.getRecordDate();
	    			IMDLogger.log(currentDate.toString(), Util.INFO);
	    		}
	    		dailyRecords.add(recValue);
	    	}
    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + "\n";
	    	if (milkingInformation != null && !milkingInformation.trim().isEmpty() )
	    		milkingInformation = "[" + milkingInformation + "]";
	    	else
	    		milkingInformation = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveMonthlyMilkingRecord() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingInformation, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(milkingInformation).build();
    }	
	
	private String consolidateDailyMilkingRecord(List<MilkingDetail> dailyRecords) {
		String json = "";
		String prefix = "   ";
		MilkingDetail recValue1 = null;
		MilkingDetail recValue2 = null;
		MilkingDetail recValue3 = null;
		LocalDate recDate = null;
		
		if (dailyRecords != null && !dailyRecords.isEmpty()) {
			
				for (int i=0; i < dailyRecords.size(); i++) {
					recDate = dailyRecords.get(i).getRecordDate();
					if (dailyRecords.get(i).getMilkingEventNumber() == 1) {
						recValue1 = dailyRecords.get(i);
					}
					else if (dailyRecords.get(i).getMilkingEventNumber() == 2) {
						recValue2 = dailyRecords.get(i);
					}
					else if (dailyRecords.get(i).getMilkingEventNumber() == 3) {
						recValue3 = dailyRecords.get(i);
					}
				}
	    		json = "{\n" + prefix + "\"milkingDate\":\""+ recDate + "\",\n";
	    		
	    		json += formatMilkingEventJson(recDate, prefix, recValue1,1,",");
	    		json += formatMilkingEventJson(recDate, prefix, recValue2,2,",");
	    		json += formatMilkingEventJson(recDate, prefix, recValue3,3,"}");
	    	}
		return json;
	}

	private String formatMilkingEventJson(LocalDate recDate, String prefix, MilkingDetail recValue, int postFix, String commaOrBracket) {
		String json = "";
		IMDLogger.log("[" + recDate + "] milkVol" + postFix + " = " + (recValue == null ? "" : recValue.getMilkVolume()), Util.INFO);

		json += prefix + "\"milkVol"+ postFix + "\":" + 
						(recValue == null || recValue.getMilkVolume() == null ? "\"\"" : recValue.getMilkVolume()) + ",\n";
		json += prefix + "\"event"+ postFix + "Time\":\"" + 
						(recValue == null || recValue.getRecordTime() == null ? "" : Util.getTimeInSQLFormart(recValue.getRecordTime())) + "\",\n";
		json += prefix + "\"event"+ postFix + "Temperature\":" +
						(recValue == null ? "\"\"" : recValue.getTemperatureInCentigrade())  + ",\n";
		json += prefix + "\"event"+ postFix + "Humidity\":" + 
						(recValue == null ? "\"\"" : recValue.getHumidity()) + ",\n";
		json += prefix + "\"event"+ postFix + "Comments\":\"" + 
						(recValue == null || recValue.getComments()== null ? "" : recValue.getComments()) + "\"" + commaOrBracket+ "\n";
		return json;
	}	
	
	/**
	 * This API returns the Sire which are included in Farm Herd as opposed to the AI Sire whose semens the farm purchases.
	 * @param searchBean
	 * @return
	 */
	@POST
	@Path("/retrievefarmsire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFarmSire(AnimalBean searchBean){
		String methodName = "retrieveFarmSire";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() +  "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		String sireValueResult = "";
    	searchBean.setGender(Util.GENDER_CHAR.MALE);
    	searchBean.setActiveOnly(true);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Farm doesn't have any Sire in its herd.\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		sireValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (sireValueResult != null && !sireValueResult.trim().isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveFarmSire() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(sireValueResult).build();
    }	
	
	
	@POST
	@Path("/retrieveaisire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAISires(AnimalBean searchBean){
		String methodName = "retrieveAISires";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() +  "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		String sireValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Sire> inseminateSireAndFarmSireValues = loader.retrieveAISire();
			if (inseminateSireAndFarmSireValues == null || inseminateSireAndFarmSireValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();

			}
	    	Iterator<Sire> sireValueIt = inseminateSireAndFarmSireValues.iterator();
	    	while (sireValueIt.hasNext()) {
	    		Sire sireValue = sireValueIt.next();
	    		sireValueResult += "{\n" + sireValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (sireValueResult != null && !sireValueResult.trim().isEmpty() )
	    		sireValueResult = "[" + sireValueResult.substring(0,sireValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		sireValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + this.getClass().getName() +  "." + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(sireValueResult).build();
    }

	
	@POST
	@Path("/retrieveprogney")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveProgney(AnimalBean searchBean){
		String methodName = "retrieveProgney";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + methodName + ".",searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
    	String animalValueResult = "";
    	try {
    		if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify the animal tag\"}").build();

    		
    		AnimalLoader loader = new AnimalLoader();
    		List<Animal> animalValues = loader.retrieveSpecifiedAnimalProgney(searchBean.getOrgId(), searchBean.getAnimalTag());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"Our farm does not have any progney of this animal\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		animalValueResult += "{\n" + animalValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveProgney() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Error occurred while retrieving animal progney: (" +  e.getMessage() + ")\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalValueResult).build();
    }	

	
	@POST
	@Path("/lactationinformation")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactationInformation(AnimalBean searchBean){
		String methodName = "retrieveLactationInformation";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + methodName + ".",searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
    	String lacDetailResult = "";
    	try {
    		if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
    			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Please specify the animal tag\"}").build();
    		
    		LactationInformationManager lacMgr = new LactationInformationManager();
    		List<LactationInformation> lacInfo = lacMgr.getAnimalLactationInformation(orgID, searchBean.getAnimalTag());

			if (lacInfo == null || lacInfo.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"No lactation information could be found for this animal\"}").build();
			}
	    	Iterator<LactationInformation> lacInfoIt = lacInfo.iterator();
	    	while (lacInfoIt.hasNext()) {
	    		LactationInformation lacDetail = lacInfoIt.next();
	    		lacDetailResult += "{\n" + lacDetail.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (lacDetailResult != null && !lacDetailResult.trim().isEmpty() )
	    		lacDetailResult = "[" + lacDetailResult.substring(0,lacDetailResult.lastIndexOf(",\n")) + "]";
	    	else
	    		lacDetailResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveProgney() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Error occurred while retrieving animal lactation information: (" +  e.getMessage() + ")\"}").build();
		}
    	IMDLogger.log(lacDetailResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(lacDetailResult).build();
    }	

	@POST
	@Path("/validateanimaltags")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response validateAnimalTags(AnimalBean searchBean){
		String methodName = "validateAnimalTags";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName, searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName, Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
		if (searchBean.getAnimalTagsList() == null && searchBean.getAnimalTagsList().isEmpty()) {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"No Animal Tags were provided for validation. Please specify at least one animal tag and then try again.\"}").build();
		}
		
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		List<Animal> animalValues = null;
    		String tagInClause = "";
    		HashMap<String,String> inputTagsMap = new HashMap<String, String>();
    		if (searchBean.getAnimalTagsList() != null && !searchBean.getAnimalTagsList().isEmpty()) {
    			Iterator<String> tagsToBeValidateIt = searchBean.getAnimalTagsList().iterator();
    			int index = 0;
    			while (tagsToBeValidateIt.hasNext()) {
    				String tag = tagsToBeValidateIt.next().trim();
    				inputTagsMap.put(tag, tag);
    				tagInClause += (index == 0 ? "'" : ",'") + tag + "'";
    				index++;
    			}
    			searchBean.setAnimalTag( "(" + tagInClause + ")");
    		} 
			animalValues = loader.retrieveMatchingAnimals(searchBean);
			String validTags = "";
			String invalidTags = "";
			if(animalValues == null || animalValues.isEmpty()) {
				invalidTags = tagInClause;
			} else {
		    	Iterator<Animal> animalValueIt = animalValues.iterator();
		    	while (animalValueIt.hasNext()) {
		    		Animal animalValue = animalValueIt.next();
		    		if (inputTagsMap.get(animalValue.getAnimalTag()) != null) {
		    			validTags   += (validTags.isEmpty() ? "" : ", ")  + animalValue.getAnimalTag();
		    			inputTagsMap.remove(animalValue.getAnimalTag());
		    		}
		    	}
		    	Iterator<String> inValidValuesIt = inputTagsMap.keySet().iterator();
		    	while (inValidValuesIt.hasNext()) {
		    		invalidTags   += (invalidTags.isEmpty() ? "" : ", ")  + inputTagsMap.get(inValidValuesIt.next());		    		
		    	}
			}
			IMDLogger.log("Valid Tags:[" + validTags + "]", Util.INFO);
			IMDLogger.log("Invalid Tags:[" + invalidTags + "]", Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"validTags\":\"" + validTags + "\",\n   "
					+ "\"invalidTags\":\"" + invalidTags + "\"\n}").build();				
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.searchAnimals() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    }

}






