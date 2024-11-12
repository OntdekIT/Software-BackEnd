package Ontdekstation013.ClimateChecker.utility;

import java.util.Random;

public class StringGenerator {
    public static String generateRandomNumericCode(int length) {
        char[] validCharacters ="0123456789".toCharArray();
        StringBuilder string = new StringBuilder();
        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(validCharacters.length);
            string.append(validCharacters[index]);
        }
        return string.toString();
    }
}