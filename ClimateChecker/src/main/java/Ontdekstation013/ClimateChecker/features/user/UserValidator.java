package Ontdekstation013.ClimateChecker.features.user;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class UserValidator {
    private static final int MIN_FIRST_NAME_LENGTH = 2;
    private static final int MAX_FIRST_NAME_LENGTH = 30;
    private static final int MIN_LAST_NAME_LENGTH = 2;
    private static final int MAX_LAST_NAME_LENGTH = 30;
    private static final int MIN_EMAIL_LENGTH = 6;
    private static final int MAX_EMAIL_LENGTH = 60;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 50;
    private static final int STATION_CODE_LENGTH = 3;

    public ValidationResult validate(User user) {
        ValidationResult result = new ValidationResult(true, null);

        try {
            validateRequiredFields(user);
            validateFirstName(user.getFirstName());
            validateLastName(user.getLastName());
            validateEmail(user.getEmail());
            validatePassword(user.getPassword());
        } catch (Exception e) {
            return new ValidationResult(false, e.getMessage());
        }
        return result;
    }

    private void validateRequiredFields(User user) throws Exception {
        if (user.getFirstName().isEmpty() || user.getLastName().isEmpty() || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            throw new Exception("Please fill out all fields");
        }
    }

    private void validateFirstName(String firstName) throws Exception {
        if (firstName.length() < MIN_FIRST_NAME_LENGTH || firstName.length() > MAX_FIRST_NAME_LENGTH) {
            throw new Exception("First name has to be between " + MIN_FIRST_NAME_LENGTH + " and " + MAX_FIRST_NAME_LENGTH + " characters");
        }
        if (!firstName.matches("^[A-Za-z0-9]*$")) {
            throw new Exception("First name can only include letters and numbers");
        }
    }

    private void validateLastName(String lastName) throws Exception {
        if (lastName.length() < MIN_LAST_NAME_LENGTH || lastName.length() > MAX_LAST_NAME_LENGTH) {
            throw new Exception("Last name has to be between " + MIN_LAST_NAME_LENGTH + " and " + MAX_LAST_NAME_LENGTH + " characters");
        }
        if (!lastName.matches("^[A-Za-z0-9]*$")) {
            throw new Exception("Last name can only include letters and numbers");
        }
    }

    private void validateEmail(String email) throws Exception {
        if (email.length() < MIN_EMAIL_LENGTH || email.length() > MAX_EMAIL_LENGTH) {
            throw new Exception("Email address has to be between " + MIN_EMAIL_LENGTH + " and " + MAX_EMAIL_LENGTH + " characters");
        }
        if (!checkEmail(email)) {
            throw new Exception("Invalid email address");
        }
    }

    private void validatePassword(String password) throws Exception {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new Exception("Password has to be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters");
        }
        if (!checkPassword(password)) {
            throw new Exception("Password has to contain a lowercase letter, uppercase letter and a number");
        }
    }

    public boolean checkPassword(String password) {
        boolean containsNumber = password.matches(".*[0-9].*");
        boolean containsUppercase = password.matches(".*[A-Z].*");
        boolean containsLowercase = password.matches(".*[a-z].*");

        return containsNumber && containsUppercase && containsLowercase;
    }

    public static boolean checkEmail(String email) {
        try {
            InternetAddress.parse(email, false);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}