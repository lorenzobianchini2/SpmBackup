package spm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import spm.entities.BpmnModel;
import spm.entities.PublicFolder;

@CrossOrigin(origins = "http://localhost:8080")
public interface ModelsMongoRepository extends MongoRepository<BpmnModel,Integer> {
	public List<BpmnModel> findBymName(String mName);
	public List<BpmnModel> findByAuthor(String author);
}
