package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.utility.StringGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createVerifyToken(long userId) {
        Token token = new Token();
        token.setUserId(userId);
        token.setCreationTime(LocalDateTime.now());
        token.setNumericCode(StringGenerator.generateRandomNumericCode(6));
        saveToken(token);
        return token;
    }

    public void saveToken(Token token) {
        List<Token> tokensToRemove = tokenRepository.findAllByUserId(token.getUserId());
        tokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserId());
        tokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, long userId) {
        boolean isVerified = false;
        Token token = tokenRepository.findByUserId(userId);
        if (token != null && token.getNumericCode().equals(linkHash) && isTokenInCreationTimeWindow(token.getCreationTime())) {
            isVerified = true;
            tokenRepository.delete(token);
        }

        return isVerified;
    }

    private boolean isTokenInCreationTimeWindow(LocalDateTime tokenCreationTime) {
        return tokenCreationTime.isBefore(LocalDateTime.now().plusMinutes(5));
    }
}
