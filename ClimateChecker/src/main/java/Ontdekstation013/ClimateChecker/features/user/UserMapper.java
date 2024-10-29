package Ontdekstation013.ClimateChecker.features.user;

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
}
