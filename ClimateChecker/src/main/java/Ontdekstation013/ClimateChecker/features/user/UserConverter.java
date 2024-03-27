package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.endpoint.userDataDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;

import org.springframework.stereotype.Service;

@Service
public class UserConverter {
    public userDataDto userToUserDataDto (User user){
        userDataDto newdto = new userDataDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setFirstName(user.getFirstName());
        newdto.setLastName(user.getLastName());
        newdto.setPassword(user.getPassword());
        return newdto;
    }

    public userDto userToUserDto (User user) {
        userDto newdto = new userDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setLastName(user.getLastName());
        newdto.setFirstName(user.getFirstName());
        newdto.setPassword(user.getPassword());
        return newdto;
    };
}
