package Ontdekstation013.ClimateChecker;

import Ontdekstation013.ClimateChecker.Mocks.MockStationRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockUserRepo;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserServiceTests {
	private UserService userService;
	private MockUserRepo mockRepo;



	@BeforeEach
	void setup() throws Exception{
		this.mockRepo = new MockUserRepo();
		this.userService = new UserService(mockRepo);
	}

	// No functionality in UserService
	@Test
	void findUserByIdTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		dto.setUsername("Piet");
		dto.setPassword("qwerty1234567890");
		dto.setMailAddress("Piet@home.nl");
		userService.createNewUser(dto);

		userDto newDto = userService.findUserById(2);

		Assertions.assertEquals(dto.getUsername(),newDto.getUsername());
		Assertions.assertEquals(dto.getMailAddress(),newDto.getMailAddress());
		Assertions.assertNotEquals(dto.getPassword(),newDto.getPasswordHash());
	}


	@Test
	void userToUserDtoTest() {
		User user = new User();


		user.setUserName("Jan");
		user.setUserID(1);
		user.setMailAddress("Jan@home.nl");


		userDto newDto = userService.userToUserDto(user);

		Assertions.assertEquals(user.getUserName(),newDto.getUsername());
		Assertions.assertEquals(user.getMailAddress(),newDto.getMailAddress());
		Assertions.assertEquals(user.getUserID(),newDto.getId());

	}


	// No functionality in UserService
	@Test
	void getAllUsersTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		dto.setUsername("Piet");
		dto.setPassword("qwerty1234567890");
		dto.setMailAddress("Piet@home.nl");
		userService.createNewUser(dto);

		List<userDataDto> userList = userService.getAllUsers();


		Assertions.assertEquals(dto.getUsername(),userList.get(0).getUsername());
		Assertions.assertEquals(dto.getMailAddress(),userList.get(0).getMailAddress());
		Assertions.assertEquals(1,userList.get(0).getId());


		Assertions.assertEquals(dto.getUsername(),userList.get(1).getUsername());
		Assertions.assertEquals(dto.getMailAddress(),userList.get(1).getMailAddress());
		Assertions.assertEquals(2,userList.get(1).getId());
	}


	// No functionality in UserService
	@Test
	void getAllByPageIdTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		dto.setUsername("Piet");
		dto.setPassword("qwerty1234567890");
		dto.setMailAddress("Piet@home.nl");
		userService.createNewUser(dto);

		List<userDataDto> newDtoList = userService.getAllByPageId(1);


		Assertions.assertEquals(2,newDtoList.get(0).getId());
		Assertions.assertEquals(dto.getUsername(),newDtoList.get(0).getUsername());
	}


	// No functionality in UserService
	@Test
	void deleteUserTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		dto.setUsername("Piet");
		dto.setPassword("qwerty1234567890");
		dto.setMailAddress("Piet@home.nl");
		userService.createNewUser(dto);

		userService.deleteUser(2);


		try {
			userService.findUserById(2);
			Assertions.fail();
		} catch (Exception e) {
			Assertions.assertTrue(true);
		}
	}


	// No functionality in UserService
	@Test
	void createNewUserTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		userDto newDto = userService.findUserById(1);


		Assertions.assertEquals(1,newDto.getId());
		Assertions.assertEquals(dto.getUsername(),newDto.getUsername());
		Assertions.assertEquals(dto.getMailAddress(),newDto.getMailAddress());
	}



	// No functionality in UserService
	@Test
	void loginUserTest() {
		loginDto dto = new loginDto();

		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.loginUser(dto);


		Assertions.assertTrue(true);
	}


	@Test
	void editUserTest() {
		registerDto dto = new registerDto();

		dto.setUsername("Jan");
		dto.setPassword("fuckingpassword");
		dto.setMailAddress("Jan@home.nl");
		userService.createNewUser(dto);

		editUserDto dto2 = new editUserDto();
		dto2.setId(1);
		dto2.setUsername("Piet");
		dto2.setPassword("qwerty1234567890");
		dto2.setMailAddress("Piet@home.nl");
		userService.editUser(dto2);

		userDto newDto = userService.findUserById(1);

		Assertions.assertEquals(dto2.getId(),newDto.getId());
		Assertions.assertEquals(dto.getUsername(),newDto.getUsername());
		Assertions.assertEquals(dto.getMailAddress(),newDto.getMailAddress());
		Assertions.assertNotEquals(dto.getPassword(),newDto.getPasswordSalt());
		Assertions.assertNotEquals(dto.getPassword(),newDto.getPasswordHash());


	}
}
