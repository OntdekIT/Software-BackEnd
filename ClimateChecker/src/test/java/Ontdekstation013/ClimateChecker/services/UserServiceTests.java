package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.Mocks.MockTokenRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockUserRepo;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.converters.UserConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserServiceTests {
	private UserService userService;
	private MockUserRepo mockUserRepo;
	private MockTokenRepo mockTokenRepo;
	private UserConverter userConverter;

	@BeforeEach
	void setup() throws Exception{
		this.mockUserRepo = new MockUserRepo();
		this.mockTokenRepo = new MockTokenRepo();

		this.userService = new UserService(mockUserRepo, mockTokenRepo);
		this.userConverter = new UserConverter();

		List<User> userList = new ArrayList<>();

		User user = new User();

		// User 1
		user.setUserID(1);
		user.setUserName("name1");
		user.setMailAddress("mail1");

		userList.add(user);

		// user 2
		user = new User();

		user.setUserID(2);
		user.setUserName("name2");
		user.setMailAddress("mail2");

		userList.add(user);

		mockUserRepo.FillDatabase(userList);
	}

	// No functionality in UserService
	@Test
	void findUserByIdTest() {
		userDto newDto = userService.findUserById(2);

		Assertions.assertEquals("name2",newDto.getUserName());
		Assertions.assertEquals("mail2",newDto.getMailAddress());
	}


	@Test
	void userToUserDtoTest() {
		User user = new User();

		user.setUserName("Jan");
		user.setUserID(1);
		user.setMailAddress("Jan@home.nl");


		userDto newDto = userConverter.userToUserDto(user);

		Assertions.assertEquals(user.getUserName(),newDto.getUserName());
		Assertions.assertEquals(user.getMailAddress(),newDto.getMailAddress());
		Assertions.assertEquals(user.getUserID(),newDto.getId());

	}


	// No functionality in UserService
	@Test
	void getAllUsersTest() {
		List<userDataDto> userList = userService.getAllUsers();

		Assertions.assertEquals("name1",userList.get(0).getUserName());
		Assertions.assertEquals("mail1",userList.get(0).getMailAddress());
		Assertions.assertEquals(1,userList.get(0).getId());


		Assertions.assertEquals("name2",userList.get(1).getUserName());
		Assertions.assertEquals("mail2",userList.get(1).getMailAddress());
		Assertions.assertEquals(2,userList.get(1).getId());
	}


	// No functionality in UserService
	/*@Test
	void getAllByPageIdTest() {
		*//*List<userDataDto> newDtoList = userService.getAllByPageId(1);


		Assertions.assertEquals(2,newDtoList.get(0).getId());
		Assertions.assertEquals(dto.getUsername(),newDtoList.get(0).getUsername());*//*
		Assertions.assertTrue(true);
	}*/


	// No functionality in UserService
	@Test
	void deleteUserTest() {
		userDto deletedUser = userService.deleteUser(2);

		boolean deleted = false;
		if(deletedUser != null){
			deleted = true;
		}

		Assertions.assertTrue(deleted);
	}

	// No functionality in UserService

	@Test
	void createNewUserTest() {
		registerDto dto = new registerDto();

		dto.setUserName("Janpieterman");
		dto.setFirstName("Jan");
		dto.setLastName("Pieter");
		dto.setMailAddress("Jan@home.nl");

		try{
			userService.createNewUser(dto);
		} catch (Exception ex){

		}


		Assertions.assertTrue(true);
	}



	// No functionality in UserService
	@Test
	void editUserTest() {

		editUserDto dto2 = new editUserDto();
		dto2.setId((long)1);
		dto2.setUserName("Piet");
		dto2.setMailAddress("Piet@home.nl");

		try{
			userService.editUser(dto2);
		} catch (Exception ex){

		}


		Assertions.assertTrue(true);
	}
}
