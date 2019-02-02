package spm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import spm.entities.ShareCode;

public interface ShareCodesMongoRepository  extends MongoRepository<ShareCode, Integer> {
    public List<ShareCode> findByshareCode(String shareCode);
}
