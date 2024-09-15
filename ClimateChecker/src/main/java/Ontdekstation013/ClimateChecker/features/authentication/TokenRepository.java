package Ontdekstation013.ClimateChecker.features.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Ontdekstation013.ClimateChecker.features.user.User;
import java.util.List;

@Repository

public interface TokenRepository extends JpaRepository<Token, Long> {
    boolean existsByUserid(long userid);
    Token findByUserid(long userid);
    List<Token> findAllByUserid(long userid);
}