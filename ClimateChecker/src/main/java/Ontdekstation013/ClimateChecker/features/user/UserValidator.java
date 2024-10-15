package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.IStationRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.RegisterDto;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Objects;

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

    public ValidationResult validate(RegisterDto registerDto, IUserRepository userRepository, IStationRepository stationRepository, WorkshopCodeService workshopCodeService) {
        ValidationResult result = new ValidationResult(true, null);

        if (registerDto.email().isEmpty() || registerDto.firstName().isEmpty() || registerDto.lastName().isEmpty() || registerDto.password().isEmpty()) {
            result = new ValidationResult(false, "Invalid information");
        } else if (userRepository.existsUserByEmail(registerDto.email())) {
            result = new ValidationResult(false, "Email already in use");
        } else {
            Station station = stationRepository.getByRegistrationCode(registerDto.stationCode());
            if (station == null) {
                result = new ValidationResult(false, "Meetstation bestaat niet");
            } else if (station.getUserid() != null) {
                result = new ValidationResult(false, "Meetstation is al in gebruik");
            } else if (!workshopCodeService.VerifyWorkshopCode(registerDto.workshopCode())) {
                result = new ValidationResult(false, "Workshopcode is niet geldig");
            }
        }
        return result;
    }

    private void validateRequiredFields(RegisterDto registerDto) throws Exception {
        if (registerDto.firstName().isEmpty() || registerDto.lastName().isEmpty() || registerDto.email().isEmpty() || registerDto.password().isEmpty() || registerDto.confirmPassword().isEmpty() || registerDto.stationCode() == null) {
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

    private void validateConfirmPassword(String password, String confirmPassword) throws Exception {
        if (!Objects.equals(password, confirmPassword)) {
            throw new Exception("Password and confirmation password do not match");
        }
    }

    private void validateMeetstationCode(String meetstationCode) throws Exception {
        if (meetstationCode.length() != STATION_CODE_LENGTH) {
            throw new Exception("Code has to contain " + STATION_CODE_LENGTH + " characters");
        }
    }

    public boolean checkPassword(String password) throws Exception {
        boolean containsNumber = password.matches(".*\\d.*");
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
