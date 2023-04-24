package Ontdekstation013.ClimateChecker.services.converters;

import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.userDataDto;
import Ontdekstation013.ClimateChecker.models.dto.userDto;
import org.springframework.stereotype.Service;

@Service
public class UserConverter {
    public userDataDto userToUserDataDto (User user){
        userDataDto newdto = new userDataDto();
        newdto.setId(user.getUserID());
        newdto.setUserName(user.getUserName());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setFirstName(user.getFirstName());
        newdto.setLastName(user.getLastName());
        newdto.setUserName(user.getUserName());
        return newdto;
    }

    public userDto userToUserDto (User user) {
        userDto newdto = new userDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setLastName(user.getLastName());
        newdto.setFirstName(user.getFirstName());
        newdto.setUserName(user.getUserName());
        return newdto;
    };
}
