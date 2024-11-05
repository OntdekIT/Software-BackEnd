package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDataDto;
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
        dto.setAdmin(user.getAdmin());
        return dto;
    }

    public UserDto userToUserDto (User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getUserID());
        dto.setMailAddress(user.getMailAddress());
        dto.setLastName(user.getLastName());
        dto.setFirstName(user.getFirstName());
        dto.setPassword(user.getPassword());
        dto.setAdmin(user.getAdmin());
        return dto;
    };
}
