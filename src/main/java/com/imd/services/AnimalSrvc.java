package com.imd.services;


import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.imd.dto.Animal;
import com.imd.loader.AnimalLoader;;

@Path("/animals")
public class AnimalSrvc {

	/**
	 * Retrieves ALL the animals 
	 * @return
	 */
	@GET
	@Path("/all")
	@Produces(MediaType.TEXT_PLAIN)
    public String getAllAnimals() {
    	String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	List<Animal> animals = loader.retrieveAllAnimals("IMD");	    	
	    	Iterator<Animal> animalIt = animals.iterator();
	    	while (animalIt.hasNext()) {
	    		Animal animal = animalIt.next();
	    		animalsJson += "{\n" + animal.dtoToJson("  ") + "\n},\n";	    		
	    	}
	    	animalsJson = animalsJson.substring(0,animalsJson.lastIndexOf(",\n"));
	    	System.out.println(animalsJson);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return animalsJson;
    }
	/**
	 * Retrieves ALL the event codes
	 * @return
	 */
	@GET
	@Path("/allactive")
	@Produces(MediaType.TEXT_PLAIN)
    public String getAllActiveLifecycleEventsLookup() {
	   	String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	List<Animal> animals = loader.retrieveActiveAnimals("IMD");	    	
	    	Iterator<Animal> animalIt = animals.iterator();
	    	while (animalIt.hasNext()) {
	    		Animal animal = animalIt.next();
	    		animalsJson += "{\n" + animal.dtoToJson("  ") + "\n},\n";	    		
	    	}
	    	animalsJson = animalsJson.substring(0,animalsJson.lastIndexOf(",\n"));
	    	System.out.println(animalsJson);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return animalsJson;
    }	
	
	/**
	 * Retrieve a particular life cycle event given its event code. If the event does not exist then return
	 * No Record Found
	 * @param eventcode
	 * @return
	 */
	
	@GET
	@Path("{animaltag}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAnimal(@PathParam("animaltag") String animaltag){
		
	   	String animalsJson = "";
    	try {
			AnimalLoader loader = new AnimalLoader();
		 	Animal animal = loader.retrieveAnimal("IMD", animaltag);
		 	animalsJson = animal == null ? "No Record Found" : "{\n" + animal.dtoToJson("  ") +"\n}\n";
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println(ex.getMessage());
    	}
        return animalsJson;
    }
}
