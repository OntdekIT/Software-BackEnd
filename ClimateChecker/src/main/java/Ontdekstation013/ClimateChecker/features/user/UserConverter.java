package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDto;
import org.springframework.stereotype.Service;

@Service
public class UserConverter {
    public UserDataDto userToUserDataDto (User user){
        UserDataDto dto = new UserDataDto();
        dto.setId(user.getUserID());
        dto.setMailAddress(user.getMailAddress());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPassword(user.getPassword());
        return dto;
    }

    public UserDto userToUserDto (User user) {
        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStations()
        );
    }
}
