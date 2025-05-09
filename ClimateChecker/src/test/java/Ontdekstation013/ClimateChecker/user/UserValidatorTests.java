package Ontdekstation013.ClimateChecker.user;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.UserValidator;
import Ontdekstation013.ClimateChecker.features.user.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTests {

    private UserValidator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        validator = new UserValidator();
        validUser = new User(1L, "John", "Doe", "john.doe@email.com", UserRole.USER, "ValidPass1");
    }

    @Test
    public void validate_ValidUser_ReturnsSuccess() {
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void validateRequiredFields_WhenEmpty_ThrowsException() {
        validUser.setFirstName("");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Please fill out all fields", result.getErrorMessage());
    }

    @Test
    public void validateFirstName_WhenValid_Succeeds() {
        validUser.setFirstName("John");
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }

    @Test
    public void validateFirstName_WhenTooShort_Fails() {
        validUser.setFirstName("J");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("First name has to be between"));
    }


    @Test
    public void validateFirstName_WithSpecialCharacters_Fails() {
        validUser.setFirstName("J@hn");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("First name can only include letters and numbers", result.getErrorMessage());
    }

    @Test
    public void validateLastName_WhenValid_Succeeds() {
        validUser.setLastName("Doe123");
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }


    @Test
    public void validateLastName_WhenTooLong_Fails() {
        validUser.setLastName("D".repeat(31));
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Last name has to be between"));
    }

    @Test
    public void validateLastName_WithSpecialCharacters_Fails() {
        validUser.setLastName("Doe!");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Last name can only include letters and numbers", result.getErrorMessage());
    }

    @Test
    public void validateEmail_WhenValidFormat_Succeeds() {
        validUser.setEmail("valid.email@example.com");
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }


    @Test
    public void validateEmail_WhenInvalidFormat_Fails() {
        validUser.setEmail("john.doe@");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Invalid email address", result.getErrorMessage());
    }

    @Test
    public void validateEmail_WhenMinAndMaxLength_Succeeds() {
        validUser.setEmail("a".repeat(50) + "@mail.com"); // Length = 60
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }

    @Test
    public void validateEmail_WhenTooLong_Fails() {
        validUser.setEmail("a".repeat(61) + "@mail.com");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Email address has to be between"));
    }

    @Test
    public void validatePassword_WhenLengthAtMin_Succeeds() {
        validUser.setPassword("Abcdef1g"); // length = 8, meets all rules
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }

    @Test
    public void validatePassword_WhenTooShort_Fails() {
        validUser.setPassword("Ab1");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Password has to be between 8 and 50 characters", result.getErrorMessage());
    }

    @Test
    public void validatePassword_WithoutNumber_Fails() {
        validUser.setPassword("Password");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Password has to contain a lowercase letter, uppercase letter and a number", result.getErrorMessage());
    }

    @Test
    public void validatePassword_WithoutUppercase_Fails() {
        validUser.setPassword("password1");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Password has to contain a lowercase letter, uppercase letter and a number", result.getErrorMessage());
    }

    @Test
    public void validatePassword_WithoutLowercase_Fails() {
        validUser.setPassword("PASSWORD1");
        ValidationResult result = validator.validate(validUser);
        assertFalse(result.isValid());
        assertEquals("Password has to contain a lowercase letter, uppercase letter and a number", result.getErrorMessage());
    }

    @Test
    public void validatePassword_WithAllCriteria_Succeeds() {
        validUser.setPassword("Valid123");
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }

    @Test
    public void validatePassword_WhenLengthAtMax_Succeeds() {
        validUser.setPassword("A1" + "a".repeat(47));
        ValidationResult result = validator.validate(validUser);
        assertTrue(result.isValid());
    }
}
