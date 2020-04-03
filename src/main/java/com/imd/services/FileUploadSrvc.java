package com.imd.services;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.imd.dto.Animal;
import com.imd.dto.AnimalPhoto;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

@Path("/fileupload")
public class FileUploadSrvc {
	
	@POST
	@Path("/retrieveanimalphotos")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getAnimalPhotos(AnimalBean searchBean){
		
		String methodName = "getAnimalPhotos";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken());
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgID();
		String langCd = user.getPreferredLanguage();
		searchBean.setOrgID(orgID);
		IMDLogger.log(searchBean.toString(), Util.INFO);
		
		if (searchBean.getAnimalTag() == null || searchBean.getAnimalTag().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Invalid animal tag\"}").build();
		
    	String animalPhotoResult = "";
    	try {
    		AnimalLoader loader = new AnimalLoader();
    		List<AnimalPhoto> animalValues = null;
    			
			animalValues = loader.retrieveAnimalPhotos(orgID, searchBean.getAnimalTag());
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The Animal " + searchBean.getAnimalTag() + " does not have any photos\"}").build();

			}
	    	Iterator<AnimalPhoto> animalValueIt = animalValues.iterator();
	    	while (animalValueIt.hasNext()) {
	    		AnimalPhoto animalPhoto = animalValueIt.next();
	    		animalPhotoResult += "{\n" + animalPhoto.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
	    	}
	    	if (animalPhotoResult != null && !animalPhotoResult.trim().isEmpty() )
	    		animalPhotoResult = "[" + animalPhotoResult.substring(0,animalPhotoResult.lastIndexOf(",\n")) + "]";
	    	else
	    		animalPhotoResult = "[]";
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
    	IMDLogger.log(animalPhotoResult, Util.INFO);
		return Response.status(Util.HTTPCodes.OK).entity(animalPhotoResult).build();
    }
	
	
	@POST
	@Path("/uploadphoto")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response uploadImageFile( @FormDataParam("animalTag") String animalTag,
									 @FormDataParam("loginToken") String authToken,
									 @FormDataParam("comments") String comments,
									 @FormDataParam("photoTimestamp") String photoTimeStampStr,
									 @FormDataParam("file") InputStream fileInputStream,
	                                 @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		String methodName = "uploadImageFile";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,authToken);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgID();
		String langCd = user.getPreferredLanguage();
		
		if (animalTag == null || animalTag.isEmpty()) {
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"You must specify an animal tag against which you wish to store the image\"}").build();			
		} else {
			AnimalLoader animalLoader = new AnimalLoader();
			List<Animal> animals = animalLoader.getAnimalRawInfo(orgID, animalTag);
			if (animals == null || animals.isEmpty())
				return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"The specified animal does not exist in the record. The image could not be saved.\"}").build();							
		}
		
		IMDLogger.log("--- Inside " + methodName + " API ---", Util.INFO);
		IMDLogger.log("animalTag: " + animalTag, Util.INFO);
		IMDLogger.log("fileName: " + fileMetaData.getFileName(), Util.INFO);
		IMDLogger.log("fileSize: " + fileMetaData.getSize(), Util.INFO);
		IMDLogger.log("loginToken: " + authToken, Util.INFO);
		IMDLogger.log("comments: " + comments, Util.INFO);
		IMDLogger.log("photoTimestamp: " + photoTimeStampStr, Util.INFO);
	    try
	    {
	        int bytesRead = 0;
	        int fileSize = 0;
	        byte[] bytes = new byte[(int) Util.MAX_PHOTO_SIZE_IN_BYTES /*(int) fileMetaData.getSize()*/];
	        
	        String fileID = animalTag + "-" + DateTime.now(IMDProperties.getServerTimeZone()).getMillis();
	        String fileURI = Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" +  animalTag + "/" + fileID;
	        fileID += (fileMetaData.getFileName().lastIndexOf(".") >=0 ? fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")) : "");
	        fileURI += (fileMetaData.getFileName().lastIndexOf(".") >=0 ? fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")) : "");
	        OutputStream out = Files.newOutputStream(Paths.get(fileURI), StandardOpenOption.CREATE);
	        while ((bytesRead = fileInputStream.read(bytes)) != -1) 
	        {
	        	fileSize += bytesRead;
	            out.write(bytes, 0, bytesRead);
	        }
	        
	    	if (fileSize > Util.MAX_PHOTO_SIZE_IN_BYTES) {
		        out.flush();
		        out.close();
		        Files.delete(Paths.get(fileURI));
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The file size (" + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + " MB) can not be greater than " + (Util.MAX_PHOTO_SIZE_IN_BYTES / (1024*1024)) + " MB \"}").build();
	    	}
	    	else if (fileSize <= 0) {
		        out.flush();
		        out.close();
		        Files.delete(Paths.get(fileURI));
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The image file (" + fileMetaData.getFileName() + ") for the tag #" + animalTag + " seems to be empty \"}").build();
	    	}
	        out.flush();
	        out.close();
	        File newFile = new File(fileURI);
	        if (newFile.exists()) {
		        IMDLogger.log("Photo File uploaded for the animal #" + animalTag + " as file " + fileURI + " and size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + " MB", Util.INFO);
		        AnimalPhoto photo = new AnimalPhoto(orgID, animalTag);
				photo.setPhotoID(fileID);
				photo.setPhotoURI(fileURI);
				photo.setComments(comments);
				photo.setCreatedBy(user);
				photo.setPhotoTimeStamp(Util.parseDateTime(photoTimeStampStr, "yyyy-MM-dd HH:mm"));
				photo.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
				photo.setUpdatedBy(photo.getCreatedBy());
				photo.setUpdatedDTTM(photo.getCreatedDTTM());
				AnimalLoader loader = new AnimalLoader();
				int insertedRecordCount = loader.insertAnimalPhoto(photo);
				if (insertedRecordCount > 0)
					return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"The file " + photo.getPhotoURI() + " of size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + " MB for the tag #" + animalTag + " has been successfully uploaded. \"}").build();
				else {
					newFile.delete();
					return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"The file " + photo.getPhotoURI() + " of size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + ") MB for the tag #" + animalTag + " was successfully received but there was an error associating the photo information with the animal. The file has been discarded, please try again. \"}").build();
				}
	        } else {
	        	IMDLogger.log("The uploaded file was not saved successfully: " + newFile.getName(), Util.ERROR);	        	
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The image file (" + fileURI + ") for the tag #" + animalTag + " could not be saved for unknown reasons. Try again \"}").build();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Exception  [" + e.getClass().getCanonicalName() + "] ) occurred while uploading file \"}").build();
	    } finally {
	    	;
	    }
	}	
}
