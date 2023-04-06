package Ontdekstation013.ClimateChecker.services;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    // Validate if string length is in between given parameter
    public boolean validateStringLength(String input, int minLength, int maxLength){
        boolean validated = false;
        if (input.length() > minLength && input.length() < maxLength){
            validated = true;
        }
        return validated;
    }

    // Validate if string contains given parameter
    public boolean validateStringContains(String input, String contains){
        boolean validated = false;
        if (input.contains(contains)){
            validated = true;
        }
        return validated;
    }


    // Validate if value is in between given parameters
    // If max = 0, it only needs to be above minimum value
    public boolean validateLongValue(long input, int minValue, int maxValue){
        boolean validated = false;
        if(input > minValue && input < maxValue){
            validated = true;
        } else if(input > minValue && maxValue == 0){
            validated = true;
        }
        return validated;
    }


    // Validate if value is in between given parameters
    // If max = 0, it only needs to be above minimum value
    public boolean validateFloatValue(float input, int minValue, int maxValue){
        boolean validated = false;
        if(input > minValue && input < maxValue){
            validated = true;
        } else if(input > minValue && maxValue == 0){
            validated = true;
        }
        return validated;
    }

    // Validate if value is in between given parameters
    // If max = 0, it only needs to be above minimum value
    public boolean validateIntValue(int input, int minValue, int maxValue){
        boolean validated = false;
        if(input > minValue && input < maxValue){
            validated = true;
        } else if(input > minValue && maxValue == 0){
            validated = true;
        }
        return validated;
    }
}
