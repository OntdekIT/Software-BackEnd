package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(Long id) {
        User user = userRepository.findById(id).get();
        return user;
    }
}
