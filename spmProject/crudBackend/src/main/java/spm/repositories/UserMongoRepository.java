package spm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import spm.entities.User;

@CrossOrigin(origins = "http://localhost:8080")
public interface UserMongoRepository extends MongoRepository<User,Integer> {
    public List<User> findByName(String name);

}