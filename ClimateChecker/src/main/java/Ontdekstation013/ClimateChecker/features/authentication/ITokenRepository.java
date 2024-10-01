package Ontdekstation013.ClimateChecker.features.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ITokenRepository extends JpaRepository<Token, Long> {
    Token findByUserid(long userid);
    List<Token> findAllByUserid(long userid);
}