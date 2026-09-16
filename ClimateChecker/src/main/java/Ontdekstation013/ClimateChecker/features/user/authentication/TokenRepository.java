package Ontdekstation013.ClimateChecker.features.user.authentication;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByUserIdAndTokenType(long userId, TokenType tokenType);
    List<Token> findAllByUserIdAndTokenType(long userId, TokenType tokenType);
}