package fileStorage.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import fileStorage.exception.FileNotFoundException;
import fileStorage.exception.FileStorageException;
import fileStorage.property.FileStorageProperties;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try 
        {
            Files.createDirectories(this.fileStorageLocation);
        } 
        catch (Exception ex) 
        {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    // Save or Edit a file
    public String StoreFile(MultipartFile file) {
   	  	
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Get any exiting versions of the file
        String[] fileNames = GetAllFileNames(fileName);
        int iNextIndex = fileNames.length + 1;
        
        // Increment the version of the file.  All files start with 1.
        String fileNameIndexed = Integer.toString(iNextIndex) + "_" + fileName;
       
        try 
        {
            // Check if the file's name contains invalid characters.  ('_' if not valid because it's used to track version.)
            if(fileName.contains("..") || fileName.contains("_")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileNameIndexed);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } 
        catch (IOException ex) 
        {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource LoadFileAsResource(String fileName) {
        try 
        {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } 
        catch (MalformedURLException ex) 
        {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }
    
    // Get the name(s) of all unique filenames
    public String[] GetAllFileNames() {
    	File dir = new File(fileStorageLocation.toString());
    	String[] matchingFiles = dir.list(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.startsWith("1_");
    	    }
    	});
    	
    	for (int i=0; i < matchingFiles.length; i++)
    	{
    		matchingFiles[i] = matchingFiles[i].replaceFirst("1_", "");
    	}
    	
        return matchingFiles;
        }
    
    //  Get the name(s) of all versions of a file
    public String[] GetAllFileNames(String sfileName) {
    	File dir = new File(fileStorageLocation.toString());
    	String[] matchingFiles = dir.list(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith("_" + sfileName);
    	    }
    	});
    	
    	Arrays.sort(matchingFiles);
        return matchingFiles;
        }
    
    // Get the latest files in upload directory
    public File[] GetAllFiles(String[] fileNames) {
    	List<File> fileList = new ArrayList<>();
    	File dir = new File(fileStorageLocation.toString());

    	
    	for (int i = 0; i < fileNames.length; i++) 
    	{
    		String sSearchName = fileNames[i];
        	File[] matchingFile = dir.listFiles(new FilenameFilter() {
        	    public boolean accept(File dir, String name) {
        	        return name.endsWith("_" + sSearchName);
        	    }
        	});
        	
        	fileList.add(matchingFile[matchingFile.length-1]);
    	}
    	File[] matchingFiles = fileList.parallelStream().toArray(n -> new File[n]);
    	return matchingFiles;
    }
    
    // Delete file and all versions of the file
    public File[] DeleteFile(String sfileName) {
	    	File dir = new File(fileStorageLocation.toString());
	    	File[] matchingFiles = dir.listFiles(new FilenameFilter() {
	    	    public boolean accept(File dir, String name) {
	    	        return name.endsWith("_" + sfileName);
	    	    }
	    	});
	    	
	    	for (int i = 0; i < matchingFiles.length; i++)
	    	{
	    		matchingFiles[i].delete();
	    	}
    	
        return matchingFiles;
        }
    
}
