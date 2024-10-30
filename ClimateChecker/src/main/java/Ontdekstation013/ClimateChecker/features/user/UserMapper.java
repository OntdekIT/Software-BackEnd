package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;

public class UserMapper {
    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isAdmin()
        );
    }

    public static User toUser(UserRequest request) {
        return new User(
                request.firstName(),
                request.lastName(),
                request.email()
        );
    }

    public static User toUpdatedUser(User user, UpdateMyAccountRequest request) {
        return new User(
                user.getUserId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                user.isAdmin(),
                request.password()
        );
    }
}
