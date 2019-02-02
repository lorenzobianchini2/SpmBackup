package spm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import spm.entities.User;
import spm.repositories.UserMongoRepository;

@SpringBootApplication
public class CrudBackendApplication implements CommandLineRunner {
	@Autowired
	private UserMongoRepository userRepository;
	@Autowired
	private UserMongoRepository PublicRepository;

	public static void main(String[] args) {
		SpringApplication.run(CrudBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
