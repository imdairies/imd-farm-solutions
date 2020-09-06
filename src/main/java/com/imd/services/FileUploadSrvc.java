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

import com.imd.dto.AnimalPhoto;
import com.imd.dto.LookupValues;
import com.imd.dto.LookupValuesPhoto;
import com.imd.dto.User;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LookupValuesLoader;
import com.imd.loader.MessageCatalogLoader;
import com.imd.services.bean.PhotoBean;
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
	public Response getAnimalPhotos(PhotoBean searchBean){
		
		String methodName = "getAnimalPhotos";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(this.getClass().getName() + "." + methodName + " >> " + searchBean.toString(), Util.INFO);
		
		if (searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1().isEmpty() || 
				searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1Value().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"No file identification information provided\"}").build();
		
		if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.ANIMAL)) {
	    	return retrieveAnimalPhoto(searchBean, methodName, orgID);
		} else if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.LOOKUP) && 
				searchBean.getPhotoIdentifier2() != null && searchBean.getPhotoIdentifier2().equalsIgnoreCase(Util.PhotoIdentifiers.CODE) &&
				searchBean.getPhotoIdentifier1Value() != null && !searchBean.getPhotoIdentifier1Value().isEmpty() &&
				searchBean.getPhotoIdentifier2Value() != null && !searchBean.getPhotoIdentifier2Value().isEmpty()
				){
			return retrieveLookupPhoto(searchBean, methodName, orgID);
		} else {		
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Unknown or Unhandled file identification information provided\"}").build();			
		}
    }

	@POST
	@Path("/removephotos")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response deletePhoto(PhotoBean searchBean){
		
		String methodName = "deletePhoto";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(this.getClass().getName() + "." + methodName + " >> " + searchBean.toString(), Util.INFO);
		
		if (searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1().isEmpty() || 
				searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1Value().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"No file identification information provided\"}").build();
		
		if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.ANIMAL) && 
				searchBean.getPhotoIdentifier3() != null && searchBean.getPhotoIdentifier3().equalsIgnoreCase(Util.PhotoIdentifiers.PHOTO_ID) &&
				searchBean.getPhotoIdentifier3Value() != null && !searchBean.getPhotoIdentifier3Value().isEmpty()
			) {
	    	return deleteAnimalPhoto(searchBean, methodName, orgID);
		} else if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.LOOKUP) && 
				searchBean.getPhotoIdentifier2() != null && searchBean.getPhotoIdentifier2().equalsIgnoreCase(Util.PhotoIdentifiers.CODE) &&
				searchBean.getPhotoIdentifier1Value() != null && !searchBean.getPhotoIdentifier1Value().isEmpty() &&
				searchBean.getPhotoIdentifier2Value() != null && !searchBean.getPhotoIdentifier2Value().isEmpty() &&
				searchBean.getPhotoIdentifier3() != null && searchBean.getPhotoIdentifier3().equalsIgnoreCase(Util.PhotoIdentifiers.PHOTO_ID) &&
				searchBean.getPhotoIdentifier3Value() != null && !searchBean.getPhotoIdentifier3Value().isEmpty()
				){
			return deleteLookupPhoto(searchBean, methodName, orgID);
		} else {		
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"One or more required input paramaeters are incorrect or missing.\"}").build();			
		}
    }


	@POST
	@Path("/retrievephotocount")
	@Consumes (MediaType.APPLICATION_JSON)
	public Response getPhotoCount(PhotoBean searchBean){
		
		String methodName = "getPhotoCount";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName ,searchBean.getLoginToken(),/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName()  + "." + methodName , Util.INFO);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		searchBean.setOrgId(orgID);
		IMDLogger.log(this.getClass().getName() + "." + methodName + " >> " + searchBean.toString(), Util.INFO);
		
		if (searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1().isEmpty() || 
				searchBean.getPhotoIdentifier1() == null || searchBean.getPhotoIdentifier1Value().isEmpty())
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"No file identification information provided\"}").build();
		
		if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.ANIMAL)) {
			AnimalLoader anmlLoader = new AnimalLoader();
			int photoCount = 0;
			List<AnimalPhoto> photos;
			try {
				photos = anmlLoader.retrieveAnimalPhotos(searchBean.getOrgId(), searchBean.getPhotoIdentifier1Value(),null);
				if (photos != null) {
					photoCount = photos.size();
				}
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"photoCount\":\"" + photoCount + "\"}").build();			
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Exception occurred while retrieving file count\"}").build();
			}
		} else if (searchBean.getPhotoIdentifier1().equalsIgnoreCase(Util.PhotoIdentifiers.LOOKUP) && 
				searchBean.getPhotoIdentifier2() != null && searchBean.getPhotoIdentifier2().equalsIgnoreCase(Util.PhotoIdentifiers.CODE) &&
				searchBean.getPhotoIdentifier1Value() != null && !searchBean.getPhotoIdentifier1Value().isEmpty() &&
				searchBean.getPhotoIdentifier2Value() != null && !searchBean.getPhotoIdentifier2Value().isEmpty()
				){
			LookupValuesLoader luLoader = new LookupValuesLoader();
			int photoCount = 0;
			LookupValues luValue;
			try {
				luValue = luLoader.retrieveLookupValuesPhotos(searchBean.getPhotoIdentifier1Value(), searchBean.getPhotoIdentifier2Value(),null);
				if (luValue != null && luValue.getLookupValuesPhotoList() != null) {
					photoCount = luValue.getLookupValuesPhotoList().size();
				}
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"photoCount\":\"" + photoCount + "\"}").build();			
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Exception occurred while retrieving lookup values file count\"}").build();
			}
		} else {		
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Unknown or Unhandled file identification information provided\"}").build();			
		}
    }

	
	private Response deleteAnimalPhoto(PhotoBean searchBean, String methodName, String orgID) {
		try {
			AnimalLoader loader = new AnimalLoader();
			List<AnimalPhoto> photos = loader.retrieveAnimalPhotos(orgID, searchBean.getPhotoIdentifier1Value(), searchBean.getPhotoIdentifier3Value());
			if (photos == null || photos.isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Non-existent photo(s) can not be deleted\"}").build();
			String fileURI = photos.get(0).getPhotoURI();
			String fileID = photos.get(0).getPhotoID();

			int recordDeletedCount = loader.deleteSpecificAnimalPhotos(orgID, searchBean.getPhotoIdentifier1Value(),fileID);
			if (recordDeletedCount <= 0 )
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An error occurred while deleting the photo: " + fileID + "\"}").build();
			}
			boolean isDeletedFromStorageSystem = deleteFileFromStorageSystem(fileURI, fileID);
			if (isDeletedFromStorageSystem) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Photo was successfully deleted\"}").build();				
			} else {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The file was successfully deleted from the record but it could not be deleted from the storage system, probably because it does not exist at the specified storage location.\"}").build();				
			}
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
	}

	private Response deleteLookupPhoto(PhotoBean searchBean, String methodName, String orgID) {
		try {
			LookupValuesLoader loader = new LookupValuesLoader();
			LookupValues luValue = loader.retrieveLookupValuesPhotos(searchBean.getPhotoIdentifier1Value(), searchBean.getPhotoIdentifier2Value(), searchBean.getPhotoIdentifier3Value());
			if (luValue == null || luValue.getLookupValuesPhotoList() == null || luValue.getLookupValuesPhotoList().isEmpty())
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Non-existent photo(s) can not be deleted\"}").build();
			
			String fileURI = luValue.getLookupValuesPhotoList().get(0).getPhotoURI();
			String fileID = luValue.getLookupValuesPhotoList().get(0).getPhotoID();

			int recordDeletedCount = loader.deleteLookupValuesPhotos(searchBean.getPhotoIdentifier1Value(), 
					searchBean.getPhotoIdentifier2Value(),fileID);
			if (recordDeletedCount <= 0 )
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"An error occurred while deleting the photo: " + fileID + "\"}").build();
			}
			boolean isDeletedFromStorageSystem = deleteFileFromStorageSystem(fileURI, fileID);
			if (isDeletedFromStorageSystem) {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"Photo was successfully deleted\"}").build();				
			} else {
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The file was successfully deleted from the record but it could not be deleted from the storage system, probably because it does not exist at the specified storage location.\"}").build();				
			}
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
	}
	
	private Response retrieveAnimalPhoto(PhotoBean searchBean, String methodName, String orgID) {
		String animalPhotoResult = "";
		try {
			AnimalLoader loader = new AnimalLoader();
			List<AnimalPhoto> animalValues = null;
				
			animalValues = loader.retrieveAnimalPhotos(orgID, searchBean.getPhotoIdentifier1Value(),null);
			if (animalValues == null || animalValues.size() == 0)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The Animal " + searchBean.getPhotoIdentifier1Value() + " does not have any photos\"}").build();
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
			IMDLogger.log(animalPhotoResult, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(animalPhotoResult).build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
	}

	private Response retrieveLookupPhoto(PhotoBean searchBean, String methodName, String orgID) {
		String photoResult = "";
		try {
			LookupValuesLoader loader = new LookupValuesLoader();
				
			LookupValues luValue = loader.retrieveLookupValuesPhotos(searchBean.getPhotoIdentifier1Value(), searchBean.getPhotoIdentifier2Value(),null);
			if (luValue == null)
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The lookup Value " + 
						searchBean.getPhotoIdentifier1Value() + "-" + 
						searchBean.getPhotoIdentifier2Value() + " is invalid\"}").build();

			}
			if (luValue.getLookupValuesPhotoList() == null || luValue.getLookupValuesPhotoList().isEmpty())
			{
				return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The lookup Value " + 
						searchBean.getPhotoIdentifier1Value() + "-" + 
						searchBean.getPhotoIdentifier2Value() + " does not have any photos\"}").build();

			}
			Iterator<LookupValuesPhoto> photoIt = luValue.getLookupValuesPhotoList().iterator();
			while (photoIt.hasNext()) {
				LookupValuesPhoto photo = photoIt.next();
				photoResult += "{\n" + photo.dtoToJson("  ", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")) + "\n},\n";	    		
			}
			if (photoResult != null && !photoResult.trim().isEmpty() )
				photoResult = "[" + photoResult.substring(0,photoResult.lastIndexOf(",\n")) + "]";
			else
				photoResult = "[]";
			IMDLogger.log(photoResult, Util.INFO);
			return Response.status(Util.HTTPCodes.OK).entity(photoResult).build();
		} catch (Exception e) {
			e.printStackTrace();
			IMDLogger.log("Exception in " + methodName + " service method: " + e.getMessage(),  Util.ERROR);
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"" +  e.getMessage() + "\"}").build();
		}
	}
	
	@POST
	@Path("/uploadphoto")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response uploadImageFile( @FormDataParam("photoIdentifier1") String identifier1,
									 @FormDataParam("photoIdentifier1Value") String photoIdentifier1Value,
									 @FormDataParam("photoIdentifier2") String identifier2,
									 @FormDataParam("photoIdentifier2Value") String photoIdentifier2Value,
									 @FormDataParam("photoIdentifier3") String identifier3,
									 @FormDataParam("photoIdentifier3Value") String photoIdentifier3Value,
									 @FormDataParam("loginToken") String authToken,
									 @FormDataParam("comments") String comments,
									 @FormDataParam("photoTimestamp") String photoTimeStampStr,
									 @FormDataParam("file") InputStream fileInputStream,
	                                 @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		String methodName = "uploadImageFile";
		IMDLogger.log(methodName + " Called ", Util.INFO);
		User user = Util.verifyAccess(this.getClass().getName() + "." + methodName,authToken,/*renewToken*/ true);
		if (user == null) {
			IMDLogger.log(MessageCatalogLoader.getMessage((String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.ORG_ID), 
					(String)Util.getConfigurations().getGlobalConfigurationValue(Util.ConfigKeys.LANG_CD),Util.MessageCatalog.VERIFY_ACCESS_MESSAGE)  
					+ this.getClass().getName() + "." + methodName, Util.WARNING);
			return Response.status(Util.HTTPCodes.UNAUTHORIZED).entity("{ \"error\": true, \"message\":\"Unauthorized\"}").build();
		}
		String orgID = user.getOrgId();
//		String langCd = user.getPreferredLanguage();
		
		IMDLogger.log("--- Inside " + methodName + " API ---", Util.INFO);
		IMDLogger.log("photoIdentifier1: " + identifier1 + "=" + photoIdentifier1Value, Util.INFO);
		IMDLogger.log("photoIdentifier2: " + identifier2 + "=" + photoIdentifier2Value, Util.INFO);
		IMDLogger.log("photoIdentifier3: " + identifier3 + "=" + photoIdentifier3Value, Util.INFO);
		IMDLogger.log("fileName: " + fileMetaData.getFileName(), Util.INFO);
		IMDLogger.log("fileSize: " + fileMetaData.getSize(), Util.INFO);
		IMDLogger.log("loginToken: " + authToken, Util.INFO);
		IMDLogger.log("comments: " + comments, Util.INFO);
		IMDLogger.log("photoTimestamp: " + photoTimeStampStr, Util.INFO);
		
		
		boolean isUploadingAnimalPhoto =  identifier1 != null && identifier1.equalsIgnoreCase(Util.PhotoIdentifiers.ANIMAL);
		boolean isUploadingLookupPhoto = identifier1 != null && identifier1.equalsIgnoreCase(Util.PhotoIdentifiers.LOOKUP);
		
		if (isUploadingAnimalPhoto) {
			if ((photoIdentifier1Value == null || photoIdentifier1Value.isEmpty()))
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify an animal tag for which you wish to store the image\"}").build();			
		} else if (isUploadingLookupPhoto) { 
			   if ((photoIdentifier1Value == null || photoIdentifier1Value.isEmpty()) || 
			   (photoIdentifier2Value == null || photoIdentifier2Value.isEmpty()))
					return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"You must specify category and lookup codes for which you wish to store the image\"}").build();			
		} else {
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"File upload request has one or more missing or invalid request parameters. Please read the API documentation.\"}").build();			
		}
		
	    try
	    {
	        int bytesRead = 0;
	        int fileSize = 0;
	        byte[] bytes = new byte[(int) Util.MAX_PHOTO_SIZE_IN_BYTES /*(int) fileMetaData.getSize()*/];
	        String fileID = (isUploadingAnimalPhoto ? photoIdentifier1Value : photoIdentifier1Value + "-" + photoIdentifier2Value) +
	        		"-" + DateTime.now(IMDProperties.getServerTimeZone()).getMillis();
	        	
	        
	        String fileURI = (isUploadingAnimalPhoto ? Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" +  photoIdentifier1Value : Util.LOOKUP_PHOTO_UPLOAD_PATH) 
	        		+ "/" + fileID;
	        fileID += (fileMetaData.getFileName().lastIndexOf(".") >=0 ? fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")) : "");
	        fileURI += (fileMetaData.getFileName().lastIndexOf(".") >=0 ? fileMetaData.getFileName().substring(fileMetaData.getFileName().lastIndexOf(".")) : "");
	        if (isUploadingAnimalPhoto && Files.notExists(Paths.get(Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" +  photoIdentifier1Value + "/" ))) {
	        	// this may be the first time a photo is being uploaded for this animal.
	        	IMDLogger.log("The photos destination folder (" + Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" +  photoIdentifier1Value + "/"  + ") does not exit. We will attempt to create it now", Util.WARNING);
	        	Files.createDirectories(Paths.get(Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" +  photoIdentifier1Value + "/" ));
	        }
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
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The file (" + fileMetaData.getFileName() + ") seems to be empty \"}").build();
	    	}
	        out.flush();
	        out.close();
	        File newFile = new File(fileURI);
	        if (newFile.exists()) {
	        	int insertedRecordCount = -1;
		        IMDLogger.log("File uploaded as: " + fileURI + " with the size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + " MB", Util.INFO);
		        if (isUploadingAnimalPhoto) {
			        AnimalPhoto photo = new AnimalPhoto(orgID, photoIdentifier1Value);
					photo.setPhotoID(fileID);
					photo.setPhotoURI(fileURI);
					photo.setComments(comments);
					photo.setCreatedBy(user);
					photo.setPhotoTimeStamp(Util.parseDateTime(photoTimeStampStr, "yyyy-MM-dd HH:mm"));
					photo.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
					photo.setUpdatedBy(photo.getCreatedBy());
					photo.setUpdatedDTTM(photo.getCreatedDTTM());
					AnimalLoader loader = new AnimalLoader();
					insertedRecordCount = loader.insertAnimalPhoto(photo);
		        } else if (isUploadingLookupPhoto) {
		        	// lookup upload
		        	LookupValues luValue = new LookupValues(photoIdentifier1Value, photoIdentifier2Value, "", "", "", "");
		        	LookupValuesPhoto photo = new LookupValuesPhoto();
					photo.setPhotoID(fileID);
					photo.setPhotoURI(fileURI);
					photo.setComments(comments);
					photo.setCreatedBy(user);
					photo.setPhotoTimeStamp(Util.parseDateTime(photoTimeStampStr, "yyyy-MM-dd HH:mm"));
					photo.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
					photo.setUpdatedBy(photo.getCreatedBy());
					photo.setUpdatedDTTM(photo.getCreatedDTTM());
		        	luValue.addPhoto(photo);
		        	LookupValuesLoader loader = new LookupValuesLoader();
					insertedRecordCount = loader.insertLookupValuesPhotos(luValue);
		        } else {
					return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"File upload request has one or more missing or invalid request parameters. Please read the API documentation.\"}").build();
		        }
				if (insertedRecordCount > 0)
					return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": false, \"message\":\"The file " + fileID + " of size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + " MB has been successfully uploaded. \"}").build();
				else {
					newFile.delete();
					return Response.status(Util.HTTPCodes.OK).entity("{ \"error\": true, \"message\":\"The file " + fileURI + " of size " + Util.formatTwoDecimalPlaces((float) (fileSize/ (1024f * 1024f))) + ") MB was successfully received but there was an error associating the file information with the parent record. The file has been discarded, please try again. \"}").build();
				}
	        } else {
				return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"The file (" + fileURI + ") could not be saved for unknown reasons. Try again \"}").build();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
			return Response.status(Util.HTTPCodes.BAD_REQUEST).entity("{ \"error\": true, \"message\":\"Exception  [" + e.getClass().getCanonicalName() + "] ) occurred while uploading file \"}").build();
	    } finally {
	    	;
	    }
	}
	private boolean deleteFileFromStorageSystem(String fileURI, String fileID) {
	    try
	    {
	        if (Files.notExists(Paths.get(fileURI))) {
	        	IMDLogger.log("The file (" + fileURI + ") does not exit. We have already deleted it from the database. Either the file has been manually removed from the storage system or the URI of this file was incorrectly specified in the record.", Util.WARNING);
		    	return false;
	        }
	        Files.delete(Paths.get(fileURI));
	        IMDLogger.log("The File " + fileURI + " with the ID: " + fileID + " has been removed from the storage system", Util.INFO);
			return true;
	    } catch (Exception e) {
        	IMDLogger.log("An exception occurred while attempting to delete the file from the storage system. The file (" + fileURI + ") could not be deleted. ", Util.WARNING);
	    	e.printStackTrace();
	    	return false;
	    } 
	}
}









