package Ontdekstation013.ClimateChecker.utility;

import java.security.SecureRandom;

public class StringGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateRandomNumericCode(int length) {
        char[] validCharacters ="0123456789".toCharArray();
        StringBuilder string = new StringBuilder();

        for(int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(validCharacters.length);
            string.append(validCharacters[index]);
        }
        return string.toString();
    }
}