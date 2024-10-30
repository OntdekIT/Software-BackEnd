package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.utility.StringGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService {
    private final ITokenRepository tokenRepository;

    public TokenService(ITokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createVerifyToken(long userId){
        Token token = new Token();
        token.setUserId(userId);
        token.setCreationTime(LocalDateTime.now());
        token.setNumericCode(StringGenerator.generateRandomNumericCode(6));
        saveToken(token);
        return token;
    }

    public void saveToken(Token token){
        List<Token> tokensToRemove = tokenRepository.findAllByUserId(token.getUserId());
        tokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserId());
        tokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, String email) {
        throw new UnsupportedOperationException();
    }
}
