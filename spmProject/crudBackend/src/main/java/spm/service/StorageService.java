package spm.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import spm.exception.MyFileNotFoundException;
 
@Service
public class StorageService {
	
	//private final Path fileStorageLocation = Paths.get("C:\\Users\\loren\\Desktop");
	Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private final Path rootLocation = Paths.get("C:\\Users\\loren\\Desktop");
 
	public void store(MultipartFile file, String folderPath) {
		Path folderLocation = Paths.get(folderPath);
		try {
			Files.copy(file.getInputStream(), folderLocation.resolve(file.getOriginalFilename()));
		} catch (Exception e) {
			throw new RuntimeException("Operation failed");
		}
	}
 
	public Resource loadFile(String filename) {
		
		try {
			Path file = Paths.get("C:\\Users\\loren\\Desktop\\TEST.txt");
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Operation failed!");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Operation failed!");
		}
	}
	
	
	public Resource loadFileAsResource(String fileName, String filePath, String username) throws MalformedURLException {
        	
			filePath = filePath.replace("&", "\\");
			Path fileStorageLocation = Paths.get("C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + username + "\\" + filePath + "\\");
            Path fullFilePath = fileStorageLocation.resolve(fileName).normalize();
            System.out.println(fullFilePath);
            Resource resource = new UrlResource(fullFilePath.toUri());
            
            return resource;
 
	}
	
	
	public Resource loadAsResource(String filePath, String fileName) throws MalformedURLException {
    	
		
		Path fileStorageLocation = Paths.get(filePath);
        Path fullFilePath = fileStorageLocation.resolve(fileName);
        System.out.println("FULLPATH: " + fullFilePath);
        Resource resource = new UrlResource(fullFilePath.toUri());

        return resource;

}
	
	
	public Resource loadPublicFileAsResource(String fileName, String filePath) throws MalformedURLException {
    	
		filePath = filePath.replace("&", "\\");
		Path fileStorageLocation = Paths.get("C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + filePath + "\\");
        Path fullFilePath = fileStorageLocation.resolve(fileName).normalize();
        System.out.println(fullFilePath);
        Resource resource = new UrlResource(fullFilePath.toUri());
        
        return resource;

}
	
	
	
 
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}
 
	public void init() {
		try {
			Files.createDirectory(rootLocation);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage!");
		}
	}
}