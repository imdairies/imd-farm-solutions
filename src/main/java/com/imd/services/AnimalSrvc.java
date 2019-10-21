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

import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;;

@Path("/animals")
public class AnimalSrvc {

	private static final int INSEMINATION_SEARCH_WINDOW_DAYS = 285;


	/**
	 * Retrieves ALL the active animals in a farm
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getAllActiveAnimals() {

		String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	List<Animal> animals = loader.retrieveActiveAnimals((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    		
			if (animals == null || animals.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No active animal found\"}").build();
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
		return Response.status(200).entity(animalsJson).build(); 
    }

//	/**
//	 * Retrieves ALL the active animals in a farm
//	 * @return
//	 */
//	@GET
//	@Path("/animalpopulationdistribution")
//	@Produces(MediaType.APPLICATION_JSON)
//    public Response getAnimalPopulationDistribution() {
//
//		String animalsJson = "";
//    	try {
//			AnimalLoader loader = new AnimalLoader();
//		 	List<Animal> animals = loader.retrieveActiveAnimals((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
//    		
//			if (animals == null || animals.size() == 0)
//			{
//				return Response.status(200).entity("{ \"error\": true, \"message\":\"No active animal found\"}").build();
//			}
//	    	Iterator<Animal> animalIt = animals.iterator();
//	    	while (animalIt.hasNext()) {
//	    		Animal aimal = animalIt.next();
//	    		animalsJson += "{\n" + aimal.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
//	    	}
//	    	animalsJson = "[" + animalsJson.substring(0,animalsJson.lastIndexOf(",\n")) + "]";
//	    	IMDLogger.log(animalsJson, Util.INFO);
//		} catch (Exception e) {
//			e.printStackTrace();
//			IMDLogger.log("Exception in AnimalSrvc.getAnimalPopulationDistribution() service method: " + e.getMessage(),  Util.ERROR);
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
//		}
//		return Response.status(200).entity(animalsJson).build(); 
//    }
//	
	
	
	@POST
	@Path("/getgrowthdata")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getGrowthData(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log("Inside AnimalSrvc.getGrowthData " + searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			} else if (animalValues.size() > 1) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Multiple matching records found for this animal. Please report a product bug\"}").build();				
			}
						
    		Animal animalValue = animalValues.get(0);
    		LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();
    		List<LifecycleEvent> weights = evtLoader.retrieveSpecificLifeCycleEventsForAnimal(animalValue.getOrgID(), animalValue.getAnimalTag(), Util.LifeCycleEvents.WEIGHT);

    		if (weights == null || weights.isEmpty()) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You have never measured the weight of this animal. Growth information can't be displayed.\"}").build();				
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
				int ageAtWeightMeasurement = Util.getDaysBetween(weights.get(recProcessed).getEventTimeStamp(), animalValue.getDateOfBirth());
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
					animalWeight += Util.formatToSpecifiedDecimalPlaces(Float.parseFloat(lastMeasuredWeight), 1);;
				}	
				
				if (i < animalValue.getCurrentAgeInDays()) {
					days += ",";
					idealWeight += ",";
					animalWeight += ",";
				}
				if (largestYAxisValue < Math.max(Double.parseDouble(lastMeasuredWeight), idealWtAtAge))
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Following exception occurred while processing AnimalSrvc.getGrowthData(): " +  e.getClass().getName() + " " + e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
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
		IMDLogger.log("Add Animal Called with following input values", Util.INFO);
		IMDLogger.log(animalBean.toString(), Util.INFO);
		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid Animal Tag.\"}").build();
		}
		else if (typeCD == null || typeCD.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal Type.\"}").build();
		}
		else if (dob == null || dob.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal date of birth. If you do not know the date of birth then provide an estimated date and set the date of birth accuracy indicator to \"N\".\"}").build();
		}
		else if (gender == "" || gender.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal gender.\"}").build();
		}
		else if (dobAccuracyInd == null || dobAccuracyInd.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify if Date of Birth is accurate or not.\"}").build();
		}
		else if (breed == null || breed.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify animal breed.\"}").build();
		}
		else if (herdJoiningDate == null || herdJoiningDate.isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify herd joining date.\"}").build();
		}
		else if (typeCD.equalsIgnoreCase("CULLED") || typeCD.equalsIgnoreCase("DEAD")) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + typeCD + " indcates an inactive animal status. You can not set an inacitve animal status at the time of animal addition. Instead, add an event that results in an inactive status.\"}").build();			
		}
		
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		String frontPose = animalBean.getFrontPoseImage() == null || animalBean.getFrontPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/1.png": animalBean.getFrontPoseImage();
		String backPose =  animalBean.getBackPoseImage() == null || animalBean.getBackPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/2.png": animalBean.getBackPoseImage();
		String rightPose = animalBean.getRightPoseImage() == null || animalBean.getRightPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/3.png": animalBean.getRightPoseImage();
		String leftPose =  animalBean.getLeftPoseImage() == null || animalBean.getLeftPoseImage().trim().isEmpty() ? com.imd.util.Util.COW_PHOTOS_URI_PREFIX + tag + "/4.png": animalBean.getLeftPoseImage();

		try {
			AnimalLoader loader = new AnimalLoader();
			if (gender.equalsIgnoreCase("M"))
				animal = new Sire(tag);
			else 
				animal = new Dam(tag);
			animal.setFrontSideImageURL(frontPose);
			animal.setBackSideImageURL(backPose);
			animal.setRightSideImageURL(rightPose);
			animal.setLeftSideImageURL(leftPose);
			animal.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
			if (alias != null && !alias.trim().isEmpty())
				animal.setAlias(animalBean.getAlias());
			animal.setAnimalType(animalBean.getAnimalType());
			animal.setBreed(animalBean.getBreed());
			animal.setDateOfBirth(animalBean.getDateOfBirth("MM/dd/yyyy, hh:mm:ss aa"));
			animal.setHerdJoiningDate(animalBean.getHerdJoiningDate("MM/dd/yyyy, hh:mm:ss aa"));
			animal.setDateOfBirthEstimated(!dobAccuracyInd.equalsIgnoreCase("Y"));
			animal.setBornThroughAI(aiInd.equalsIgnoreCase("Y"));
			if (damTag != null && !damTag.trim().isEmpty())
				animal.setAnimalDam(new Dam(damTag));
			if (sireTag != null && !sireTag.trim().isEmpty())
				animal.setAnimalSire(new Sire(sireTag));
			animal.setFrontSideImageURL(frontPose);
			animal.setBackSideImageURL(backPose);
			animal.setRightSideImageURL(rightPose);
			animal.setLeftSideImageURL(leftPose);
			animal.setCreatedBy(new User(userID));
			animal.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			animal.setUpdatedBy(new User(userID));
			animal.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.insertAnimal(animal);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addAnimal() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result == 1) {
			String message = performPostInsertionSteps(animal);
			return Response.status(200).entity("{ \"error\": false, \"message\":\"New Animal has been created successfully. "+ message + "\"}").build();
		}
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Another animal with the same tag already exists. Please use a different tag number.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Animal  '" + tag+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Animal '" + tag + "' could not be added. Please submit a bug report.\"}").build();
		else
			return Response.status(400).entity("{ \"error\": true, \"message\":\"An unknown error occurred during animal addition\"}").build();

	}

	
	
	@POST
	@Path("/updateanimal")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response updateAnimal(AnimalBean animalBean){
		Animal animal = null;			
		String tag = animalBean.getAnimalTag();
		String alias  = animalBean.getAlias();
		String typeCD = animalBean.getAnimalType();
		String dob = animalBean.getDateOfBirthStr();
//		String breed = animalBean.getBreed();
//		String gender = "" + animalBean.getGender();
//		String damTag = animalBean.getDam();
//		String sireTag = animalBean.getSire();
//		String dobAccuracyInd = animalBean.getDobAccuracyInd();
//		String herdJoiningDate = animalBean.getHerdJoiningDttmStr();
//		String aiInd = (animalBean.getAiInd() == null || animalBean.getAiInd().trim().isEmpty()? "N" : "" + animalBean.getAiInd().charAt(0));
		IMDLogger.log("Update Animal Called with following input values", Util.INFO);
		IMDLogger.log(animalBean.toString(), Util.INFO);
		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid Animal Tag.\"}").build();
		}
		else if (typeCD == null || typeCD.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal Type.\"}").build();
		}
		else if (dob == null || dob.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal date of birth. If you do not know the date of birth then provide an estimated date and set the date of birth accuracy indicator to \"N\".\"}").build();
		}
//		else if (gender == "" || gender.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Animal gender.\"}").build();
//		}
//		else if (dobAccuracyInd == null || dobAccuracyInd.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify if Date of Birth is accurate or not.\"}").build();
//		}
//		else if (breed == null || breed.trim().isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify animal breed.\"}").build();
//		}
//		else if (herdJoiningDate == null || herdJoiningDate.isEmpty()) {
//			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify herd joining date.\"}").build();
//		}
		else if (typeCD.equalsIgnoreCase("CULLED") || typeCD.equalsIgnoreCase("DEAD")) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" + typeCD + " indcates an inactive animal status. You can not set an inacitve animal status. Instead, please add an event that results in an inactive status.\"}").build();			
		}
		
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;

		try {
			AnimalLoader loader = new AnimalLoader();
			animal = new Animal(tag);
//			animal.setFrontSideImageURL(frontPose);
//			animal.setBackSideImageURL(backPose);
//			animal.setRightSideImageURL(rightPose);
//			animal.setLeftSideImageURL(leftPose);
			animal.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
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
			animal.setUpdatedBy(new User(userID));
			animal.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			result = loader.updateAnimal(animal);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addAnimal() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result == 1) {
//			String message = performPostInsertionSteps(animal);
			return Response.status(200).entity("{ \"error\": false, \"message\":\"Animal has been updated successfully.\"}").build();
		}
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Animal  '" + tag+ "' could not be updated. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Animal '" + tag + "' could not be updated. Please submit a bug report.\"}").build();
		else
			return Response.status(400).entity("{ \"error\": true, \"message\":\"An unknown error occurred during animal update\"}").build();

	}
	
	
	@POST
	@Path("/addsire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addSire(SireBean sireBean){
		String tag = sireBean.getAnimalTag();
		String alias  = sireBean.getAlias();
		String breed = sireBean.getBreed();
		String semenInd = sireBean.getSemenInd();
		String recordUrl = sireBean.getRecordURL();
		String controller = sireBean.getController();
		String semenCompany = sireBean.getSemenCompany();


		IMDLogger.log("Add Sire Called with following input values", Util.INFO);		
		IMDLogger.log(sireBean.toString(), Util.INFO);
		
		if (tag == null || tag.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide a valid Sire Tag/Code.\"}").build();
		}
		else if (alias == null || alias.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Sire Alias.\"}").build();
		}
		else if (breed == null || breed.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must provide Sire Breed .\"}").build();
		}
		else if (semenInd == "" || semenInd.trim().isEmpty() || semenInd.trim().length() != 1) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must indicate whether this is a farm sire (semenInd=N) or a sire whose semens you are going to purchase (semenInd=Y).\"}").build();
		}
		else if (recordUrl == null || recordUrl.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify Sire Data Sheet URL.\"}").build();
		}
		else if (controller == null || controller.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify controller.\"}").build();
		}
		else if (semenCompany == null || semenCompany.trim().isEmpty()) {
			return Response.status(400).entity("{ \"error\": true, \"message\":\"You must specify sire/semen marketing company.\"}").build();
		}
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;

		try {
			result = 1;
			AnimalLoader loader = new AnimalLoader();
			result = loader.insertSire(sireBean,userID,DateTime.now(IMDProperties.getServerTimeZone()),userID,DateTime.now(IMDProperties.getServerTimeZone()));
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addSire() service method: " + e.getMessage(),  Util.ERROR);
		}
		if (result == 1) {
			return Response.status(200).entity("{ \"error\": false, \"message\":\"Sire has been added successfully.\"}").build();
		}
		else if (result == Util.ERROR_CODE.ALREADY_EXISTS)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Sire with the same tag/code already exists. Please use a different tag/code.\"}").build();
		else if (result == Util.ERROR_CODE.DATA_LENGTH_ISSUE)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"At least one of the fields is longer than the allowed length. Sire  '" + tag+ "' could not be added. Please reduce the field length and try again.\"}").build();
		else if (result == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an error in the SQL format. This indicates a lapse on the developer's part. Sire '" + tag + "' could not be added. Please submit a bug report.\"}").build();
		else
			return Response.status(400).entity("{ \"error\": true, \"message\":\"An unknown error occurred during Sire addition\"}").build();
	}	
	
	private String performPostInsertionSteps(Animal animalDto) {
		LifeCycleEventBean eventBean = new LifeCycleEventBean();
		eventBean.setOrgID(animalDto.getOrgID());
		eventBean.setAnimalTag(animalDto.getAnimalTag());
		eventBean.setEventComments("This birth event was automatically created during creation of the new animal");
		eventBean.setEventCode(Util.LifeCycleEvents.BIRTH);
		eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormart(animalDto.getDateOfBirth()));
		eventBean.setAuxField1Value(animalDto.getAnimalSire() == null ? "UNKNOWN" : animalDto.getAnimalSire().getAnimalTag());
		eventBean.setAuxField2Value(animalDto.getAnimalDam() == null ? "" : animalDto.getAnimalDam().getAnimalTag());
		//TODO: May be also add a parturition event automatically for the Dam ?
		
		IMDLogger.log(eventBean.toString(), Util.INFO);
		
		LifecycleEvent event;
		String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
		int result = -1;
		try {
			event = new LifecycleEvent(eventBean, "yyyy-MM-dd HH:mm:ss");
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			event.setCreatedBy(new User(userID));
			event.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			event.setUpdatedBy(new User(userID));
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
    	String animalValueResult = "";
		DateTimeFormatter fmt = null;
    	String prefix = "  ";
    	List<String> sortedJsonArray = new ArrayList<String>();
    	String noInseminationRecordJson = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader animalLoader = new AnimalLoader();
    		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
			List<Animal> animalValues = animalLoader.retrieveAdultFemaleCows(searchBean.getOrgID(),270);
			List<LifecycleEvent> animalEvents = null;
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
    		double largestInseminatedHours = -999999;
    		String additionalInfo = "";
	    	while (animalValueIt.hasNext()) {
	    		additionalInfo = "";
	    		Animal animalValue = animalValueIt.next();
		    	String strInseminationTimeInfo = ""; 
	    		if (animalValue.isPregnant() || animalValue.isInseminated()) {
	    			animalEvents = eventLoader.retrieveSpecificLifeCycleEventsForAnimal(animalValue.getOrgID(),
	    					animalValue.getAnimalTag(), LocalDate.now(IMDProperties.getServerTimeZone()).minusDays(INSEMINATION_SEARCH_WINDOW_DAYS), null, Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null, null, null);
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
	    				int inseminationAttempts = eventLoader.determineInseminationAttemptCountInCurrentLactation(animalValue.getOrgID(),animalValue.getAnimalTag());
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
			    			//IMDLogger.log("New oldest found. Will simply Add", Util.ERROR);
							sortedJsonArray.add(animalValueResult);
							//largestInseminatedDays = daysSinceInseminated;
							largestInseminatedHours = hoursSinceInseminated;
						} else {
			    			//IMDLogger.log("New value is smaller. Will swap", Util.ERROR);
							//sortedJsonArray.add(sortedJsonArray.size()-1,animalValueResult);
			    			int j = sortedJsonArray.size()-1;
							for (;j>=0; j--){
								//String days = sortedJsonArray.get(j);
								String hours = sortedJsonArray.get(j);
//								days = days.substring(days.indexOf("daysSinceInsemination\":") + "daysSinceInsemination\":\"".length(),days.lastIndexOf('"'));		
								hours = hours.substring(hours.indexOf("hoursSinceInsemination\":") + "hoursSinceInsemination\":\"".length(),hours.lastIndexOf('"'));		
								//IMDLogger.log("Previous days value: " + days, Util.ERROR);
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
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
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
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
    			
       			animalValues = loader.retrieveSpecifiedAnimalTags(searchBean.getOrgID(), tagInClause);   			
    		} else {
    			animalValues = loader.retrieveMatchingAnimals(searchBean);
    		}
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	/**
	 * 
	 * @param AnimalBean
	 * @return
	 */
	@POST
	@Path("/getactivedams")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getActiveFemale(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveDams(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No active dam found\"}").build();
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }

	@POST
	@Path("/lactatingcows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimals(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveLactatingAnimals(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }	
	@POST
	@Path("/drycows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveDryCows(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveDryAnimals(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	@POST
	@Path("/femalecalves")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveFemaleCalves(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActiveFemaleCalves(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	@POST
	@Path("/pregnantcows")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrievePregnantAnimals(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveActivePregnantAnimals(searchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	@POST
	@Path("/lactatingcowsmilkrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveLactatingAnimalsMilkRecord(MilkingDetailBean selectedDateSearchBean) {
		//TODO: Improve the implementation of this service. It is inefficient and counter intuitive. 
		// TODO: The monthly average averages across the month in all of the lifetime of the cow instead of only for one month i.e.
		// Feb average will be for Febs of all the years, instead of only the Feb for a given year. This is not the right way to deduce monthly average.
		String animalValueResult = "";
    	selectedDateSearchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(selectedDateSearchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		MilkingDetailLoader milkingLoader = new MilkingDetailLoader();
			List<Animal> animalValues = loader.retrieveActiveLactatingAnimals(selectedDateSearchBean.getOrgID());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();

			}
	    	Iterator<Animal> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		Animal animalValue = animalValueIt.next();
	    		selectedDateSearchBean.setAnimalTag(animalValue.getAnimalTag());
	    		MilkingDetailBean previousDateSearchBean = new MilkingDetailBean(selectedDateSearchBean);
	    		previousDateSearchBean.setRecordDate(selectedDateSearchBean.getRecordDate().minusDays(1));
	    		List <MilkingDetail> selectedDateMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(selectedDateSearchBean, true);
	    		List <MilkingDetail> prevDayMilkingInfo = null;
	    		if (selectedDateMilkingInfo == null || selectedDateMilkingInfo.size() == 0)
	    			prevDayMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(previousDateSearchBean, true);
	    		else
	    			prevDayMilkingInfo = milkingLoader.retrieveSingleMilkingRecordsOfCow(previousDateSearchBean, false);
	    		
	    		animalValueResult += appendMilkingDetails(selectedDateMilkingInfo,prevDayMilkingInfo,selectedDateSearchBean);
	    	}
	    	if (animalValueResult != null && !animalValueResult.trim().isEmpty() )
	    		animalValueResult = "[" + animalValueResult.substring(0,animalValueResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalValueResult = "[]";
	    	IMDLogger.log(animalValueResult, Util.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveLactatingAnimalsMilkRecord() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }
	private String appendMilkingDetails(List<MilkingDetail> selectedDaysMilkingRecords, List<MilkingDetail> previousDaysMilkingRecords, MilkingDetailBean searchBean) throws IMDException {
		String milkingDetail = "";
		String prefix = "  ";
		if (selectedDaysMilkingRecords == null || selectedDaysMilkingRecords.size() == 0) {
	    	MilkingDetail recordDetail = new MilkingDetail(searchBean.getOrgID(), searchBean.getAnimalTag(), 
	    			(short) 0, true, null, null, null, (short) 0);
	    	recordDetail.setAdditionalStatistics(getPreviousDaysVolumeForAnimal(searchBean.getAnimalTag(),previousDaysMilkingRecords));
			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.DAYS_IN_MILKING, getDaysInMilking(searchBean.getOrgID(), searchBean.getAnimalTag(),searchBean.getRecordDate()));
	    	milkingDetail = "{\n" + recordDetail.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
		} else {
	    	Iterator<MilkingDetail> recordsIt = selectedDaysMilkingRecords.iterator();
	    	while (recordsIt.hasNext()) {
	    		MilkingDetail recordDetail = recordsIt.next();
	    		HashMap<String, Float> prevDayValues = getPreviousDaysVolumeForAnimal(recordDetail.getAnimalTag(),previousDaysMilkingRecords);
				recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.DAYS_IN_MILKING, getDaysInMilking(searchBean.getOrgID(), searchBean.getAnimalTag(),searchBean.getRecordDate()));
	    		if (prevDayValues != null) {
	    			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL, prevDayValues.get(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL));
	    		}
	    		milkingDetail += "{\n" + recordDetail.dtoToJson(prefix, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";
	    		IMDLogger.log(milkingDetail, Util.INFO);
	    	}
		}
		return milkingDetail;
	}
	private Float getDaysInMilking(String orgID, String animalTag, LocalDate toDateForDaysInMilking) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		return new Float(loader.getDaysInMilkingOfCow(orgID, animalTag, true, toDateForDaysInMilking));
	}
	private HashMap <String, Float> getPreviousDaysVolumeForAnimal(String animalTag, List<MilkingDetail> previousDaysMilkingRecords) {
		HashMap <String, Float> values = null;
    	Iterator<MilkingDetail> recordsIt = previousDaysMilkingRecords.iterator();
    	while (recordsIt.hasNext()) {
    		MilkingDetail recordDetail = recordsIt.next();
    		if (recordDetail.getAnimalTag().equalsIgnoreCase(animalTag)) {
    			recordDetail.addToAdditionalStatistics(Util.MilkingDetailStatistics.YESTERDAY_SEQ_NBR_VOL, recordDetail.getMilkVolume());
    			values = recordDetail.getAdditionalStatistics();
    			break;
    		}
    	}
    	return values;
	}

	@POST
	@Path("/addmilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response addCowMilkingRecord(MilkingDetailBean milkingRecord){
    	int responseCode = 0;
    	milkingRecord.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(milkingRecord.toString(), Util.INFO);
    	try {
			if (milkingRecord.getAnimalTag() == null || milkingRecord.getAnimalTag().isEmpty())
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
			if (milkingRecord.getMilkingEventNumber() <1)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid milking event number\"}").build();
			}
			if (!(milkingRecord.getMilkVolume() > 0))
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid milking volume\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		responseCode = loader.insertMilkRecord(milkingRecord);
    		if (responseCode == Util.ERROR_CODE.ALREADY_EXISTS)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"This milking record already exists. Please edit the record instead of trying to add it again\"}").build();
    		else if (responseCode == Util.ERROR_CODE.SQL_SYNTAX_ERROR)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There is an error in your add request. Please consult the system administrator\"}").build();
    		else if (responseCode == Util.ERROR_CODE.UNKNOWN_ERROR)
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. Please consult the system administrator\"}").build();
    		else
    			return Response.status(200).entity("{ \"error\": false, \"message\":\"" + responseCode + " record added" + "\"}").build();
    	} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.addCowMilkingRecord() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"There was an unknown error in trying to add the milkiing record. " +  e.getMessage() + "\"}").build();
		}
    }	

	@POST
	@Path("/monthlymilkingrecord")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveMonthlyMilkingRecord(MilkingDetailBean searchBean){
    	String milkingInformation = "";
    	List<MilkingDetail> dailyRecords = new ArrayList<MilkingDetail>(3);
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	LocalDate currentDate = null;
    	try {
			if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"You must specify a valid animal tag\"}").build();
			}
    		MilkingDetailLoader loader = new MilkingDetailLoader();
    		AnimalLoader animalLoader = new AnimalLoader();
    		AnimalBean animalBean = new AnimalBean();
    		animalBean.setOrgID(searchBean.getOrgID());
    		animalBean.setAnimalTag(searchBean.getAnimalTag());    		
    		List<Animal> animals = animalLoader.getAnimalRawInfo(animalBean);
    		if (animals.size() != 1) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Unable to find the animal. foud " + animals.size() + " records\"}").build();    			    			
    		}
    		Animal animal = animals.get(0);
    		IMDLogger.log(animal.getAnimalType(), Util.INFO);
    		if (animal.getGender() != Util.GENDER.FEMALE) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information only applies to female animals\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.FEMALECALF)) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to female calves\"}").build();    			
    		}
    		if (animal.getAnimalType().equalsIgnoreCase(Util.AnimalTypes.HEIFER)) {
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Milking information applies to only female animals of age, it does not apply to heifers\"}").build();    			
    		}    		
			List<MilkingDetail> animalValues = loader.retrieveMonthlyMilkingRecordsOfCow(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No matching record found\"}").build();
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
//	    		milkingInformation += "{\n" + recValue.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
    		milkingInformation += consolidateDailyMilkingRecord(dailyRecords) + "\n";
	    	if (milkingInformation != null && !milkingInformation.trim().isEmpty() )
	    		milkingInformation = "[" + milkingInformation + "]";
	    	else
	    		milkingInformation = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in AnimalSrvc.retrieveMonthlyMilkingRecord() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(milkingInformation, Util.INFO);
		return Response.status(200).entity(milkingInformation).build();
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
		String sireValueResult = "";
    	searchBean.setGender(Util.GENDER.MALE);
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	searchBean.setActiveOnly(true);
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Animal> animalValues = loader.retrieveMatchingAnimals(searchBean);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Farm doesn't have any Sire in its herd.\"}").build();

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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(200).entity(sireValueResult).build();
    }	
	
	
	@POST
	@Path("/retrieveaisire")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveAISires(AnimalBean searchBean){
		String sireValueResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
			List<Sire> sireValues = loader.retrieveAISire();
			if (sireValues == null || sireValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"No Sire record found\"}").build();

			}
	    	Iterator<Sire> sireValueIt = sireValues.iterator();
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
			IMDLogger.log("Exception in AnimalSrvc.retrieveAISires() service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(400).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(sireValueResult, Util.INFO);
		return Response.status(200).entity(sireValueResult).build();
    }

	
	@POST
	@Path("/retrieveprogney")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response retrieveProgney(AnimalBean searchBean){
    	String animalValueResult = "";
    	searchBean.setOrgID((String)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.ORG_ID));
    	IMDLogger.log(searchBean.toString(), Util.INFO);
    	try {
    		if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
    			return Response.status(400).entity("{ \"error\": true, \"message\":\"Please specify the animal tag\"}").build();

    		
    		AnimalLoader loader = new AnimalLoader();
    		List<Animal> animalValues = loader.retrieveSpecifiedAnimalProgney(searchBean.getOrgID(), searchBean.getAnimalTag());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(200).entity("{ \"error\": true, \"message\":\"Our farm does not have any progney of this animal\"}").build();
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
			return Response.status(400).entity("{ \"error\": true, \"message\":\"Error occurred while retrieving animal progney: (" +  e.getMessage() + ")\"}").build();
		}
    	IMDLogger.log(animalValueResult, Util.INFO);
		return Response.status(200).entity(animalValueResult).build();
    }	
	
	
	
}



