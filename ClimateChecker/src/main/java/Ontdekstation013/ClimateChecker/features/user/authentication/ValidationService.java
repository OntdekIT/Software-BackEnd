package Ontdekstation013.ClimateChecker.features.user.authentication;

import org.modelmapper.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    public boolean validateStringLength(String input, int minLength, int maxLength){
        boolean validated = true;

        if (input.length() < minLength ){
            throw new IllegalArgumentException("The string cannot be smaller than " + minLength);
        }
        if(input.length() > maxLength){
            throw new IllegalArgumentException("The string cannot be longer than " + maxLength);
        }
        return validated;
    }

    public boolean validateStringContains(String input, String contains){
        boolean validated = true;
        if (!input.contains(contains)){
            throw new IllegalArgumentException("The string did not contain the required body");
        }
        return validated;
    }

    public boolean validateLongValue(long input, int minValue, int maxValue) throws ValidationException {
        boolean validated = true;
        String upperParameter = Integer.toString(maxValue);
        String lowerParameter = Integer.toString(minValue);

        if(input < minValue) {
            throw new IllegalArgumentException("The input cannot be below " + lowerParameter);
        }
        if(input > maxValue && maxValue > 0)
        {
            throw new IllegalArgumentException("The input cannot be above " + upperParameter);
        }
        return validated;
    }

    public boolean validateFloatValue(float input, float minValue, float maxValue){
        boolean validated = true;
        String upperParameter = Float.toString(maxValue);
        String lowerParameter = Float.toString(minValue);

        if(input < minValue ){
            throw new IllegalArgumentException("The input cannot be below " + lowerParameter);
        }
        if(input > maxValue && maxValue > 0){
            throw new IllegalArgumentException("The input cannot exceed " + upperParameter);
        }
        return validated;
    }

    public boolean validateIntValue(int input, int minValue, int maxValue){
        boolean validated = false;
        String upperParameter = Integer.toString(maxValue);
        String lowerParameter = Integer.toString(minValue);
        if(input < minValue ){
            throw new IllegalArgumentException("The input cannot be below " + lowerParameter);
        }
        if(input > maxValue && maxValue > 0){
            throw new IllegalArgumentException("The input cannot exceed " + upperParameter);
        }
        return validated;
    }
}
