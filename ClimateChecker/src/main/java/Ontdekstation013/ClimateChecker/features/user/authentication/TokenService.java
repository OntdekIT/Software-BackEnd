package Ontdekstation013.ClimateChecker.features.user.authentication;

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

    public Token createVerifyToken(long userId, TokenType tokenType) {
        Token token = new Token();
        token.setUserId(userId);
        token.setTokenType(tokenType);
        token.setCreationTime(LocalDateTime.now());
        token.setNumericCode(StringGenerator.generateRandomNumericCode(6));
        saveToken(token);
        return token;
    }

    public void saveToken(Token token) {
        List<Token> tokensToRemove = tokenRepository.findAllByUserIdAndTokenType(token.getUserId(), token.getTokenType());
        tokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserId());
        tokenRepository.save(token);
    }

    public Token getTokenByUserIdAndTokenType(long userId, TokenType tokenType) {
        List<Token> tokensToRemove = tokenRepository.findAllByUserIdAndTokenType(userId, tokenType);

        for (Token token : tokensToRemove) {
            if (!isTokenInCreationTimeWindow(token.getCreationTime())) {
                tokenRepository.delete(token);
            }
        }

        return tokenRepository.findByUserIdAndTokenType(userId, tokenType);
    }

    public boolean verifyToken(String linkHash, long userId, TokenType tokenType) {
        boolean isVerified = false;
        Token token = tokenRepository.findByUserIdAndTokenType(userId, tokenType);

        if (token != null && token.getNumericCode().equals(linkHash)) {
            tokenRepository.delete(token);

            if (isTokenInCreationTimeWindow(token.getCreationTime() ) && token.getTokenType().equals(tokenType)) {
                isVerified = true;
            }
        }

        return isVerified;
    }

    private boolean isTokenInCreationTimeWindow(LocalDateTime tokenCreationTime) {
        return tokenCreationTime.isAfter(LocalDateTime.now().minusMinutes(5));
    }
}
