package spm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import spm.entities.PublicFolder;

@CrossOrigin(origins = "http://localhost:8080")
public interface PublicMongoRepository extends MongoRepository<PublicFolder,Integer> {
    public List<PublicFolder> findByfName(String fName);
    
}