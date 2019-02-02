package spm.test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spm.controllers.*;
import spm.entities.*;
import spm.utilities.RandomGenerator;
class ControllerTest {
	
	UserController userController;
	User user = new User();
	
	
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	
	@Test
	void generatedStringNotNull() {
		
		RandomGenerator randomGenerator = new RandomGenerator(10, ThreadLocalRandom.current());
		
		assertNotNull(randomGenerator.nextString());
		
	}
	
	
	@Test
	public void generatedStringMustHaveTenCharacters() {
		
		RandomGenerator randomGenerator = new RandomGenerator(10, ThreadLocalRandom.current());
		
		assertEquals(10, randomGenerator.nextString().length());
		assertNotEquals(9, randomGenerator.nextString().length());
		assertNotEquals(11, randomGenerator.nextString().length());
		
	}
	
	
	@Test
	public void currentDateNotNull() {
		
		UserController userController = new UserController();
		assertNotNull(userController.getCurrentDate());
		
	}
	
	
	@Test
	public void UserListIsNotEmpty() {
		
		List<User> users = new ArrayList<User>();
		User user = new User("lorenzo.bianchini", "Prova12345");
		users.add(user);
		assertFalse(users.isEmpty());
	}

}
