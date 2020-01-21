package fileStorage.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fileStorage.payload.Response;
import fileStorage.service.FileStorageService;

@RestController
public class FileUtilitiesController {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilitiesController.class);

    @Autowired
    private FileStorageService fileStorageService;
    
    ///Delete File
    @GetMapping("/DeleteFile/{fileName:.+}")
    public File[] DeleteFile(@PathVariable String fileName, HttpServletRequest request) {
        
    	File[] deletedFiles = fileStorageService.DeleteFile(fileName);
    	
    	if (deletedFiles.length == 0)
    	{
            logger.info("There was a problem and no files were deleted.");
    	}
    	
    	return deletedFiles;
    }
    
    ///Download File
    @GetMapping("/DownloadFile/{fileName:.+}")
    public ResponseEntity<Resource> DownloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.LoadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    ///List Files
    @GetMapping("/ListFiles")
    public Response[] ListFiles(HttpServletRequest request) {
        
    	//Gets all versions of the file
    	String[] fileNames = fileStorageService.GetAllFileNames();
    	File[] fileArray = fileStorageService.GetAllFiles(fileNames);
    	
    	List<Response> Responses = new ArrayList<>();
    	for (int i = 0; i < fileArray.length; i++)
    	{
    		String fileName = fileNames[i];
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(fileArray[i].getName())
                    .toUriString();
            Path path = fileArray[i].toPath();
            String fileContentType = "";
            try {
				fileContentType = Files.probeContentType(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Responses.add(new Response(fileName, fileDownloadUri,
                    fileContentType, fileArray[i].length()));
    	}
    	Response[] responseArray = Responses.parallelStream().toArray(n -> new Response[n]);

    	return responseArray;
    }
    
    ///UploadFile
    @PostMapping("/UploadFile")
    public Response UploadFile(@RequestParam("file") MultipartFile file) {
    	String fileName = fileStorageService.StoreFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new Response(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }
}

