package Ontdekstation013.ClimateChecker.features.user.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByUserIdAndTokenType(long userId, TokenType tokenType);
    List<Token> findAllByUserIdAndTokenType(long userId, TokenType tokenType);
}