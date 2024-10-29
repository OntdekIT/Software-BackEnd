package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.User;

public class TokenService {
    private final ITokenRepository tokenRepository;

    public TokenService(ITokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createVerifyToken(User user){
        throw new UnsupportedOperationException();
    }

    public void saveToken(Token token){
        throw new UnsupportedOperationException();
    }

    public boolean verifyToken(String linkHash, String email) {
        throw new UnsupportedOperationException();
    }
}
