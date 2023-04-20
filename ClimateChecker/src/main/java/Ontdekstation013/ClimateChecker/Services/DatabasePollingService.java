package Ontdekstation013.ClimateChecker.Services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class DatabasePollingService {

    public String BuildQueryString(List<Long> registrationCodes, Boolean latestResults, Boolean registration){
        String query = new String();

        query = query.concat("https://meetjestad.net/data/?type=sensors&ids=");

        for(Long registrationCode : registrationCodes)
        {
            query = query.concat(registrationCode.toString() + ",");
        }

        if(!latestResults)
        {
            //Grab data of the last 15 minutes.
            //Proper format: 2017-11-16,12:00
            // yyyy-mm-dd,hh,mm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            //Retrieve and format the current time correctly for the query.
            LocalDateTime currentTime = LocalDateTime.now();
            String endFrame = currentTime.format(formatter);

            endFrame = endFrame.replace(" ", ",");
            //This should get all of them.
            query = query.concat("&begin=2017-11-16,12:00&end=" + endFrame);
            //Possible issue where the query volume is too high
            query = query.concat("&format=json&limit=50000");
        }
        else if(!registration)
        {
            //Grab data of the last 15 minutes.
            //Proper format: 2017-11-16,12:00
            // yyyy-mm-dd,hh,mm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            //Format the starting date time correctly for the query.
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now();
            fifteenMinutesAgo = fifteenMinutesAgo.minusMinutes(15);
            String beginFrame = fifteenMinutesAgo.format(formatter);

            beginFrame = beginFrame.replace(" ", ",");

            //Retrieve and format the current time correctly for the query.
            LocalDateTime currentTime = LocalDateTime.now();
            String endFrame = currentTime.format(formatter);

            endFrame = endFrame.replace(" ", ",");

            query = query.concat("&begin=" + beginFrame +"&end=" + endFrame);
            query = query.concat("&format=json&limit=500");
        }
        else{
            var registrationCode = registrationCodes.get(0);
            query = query.concat("&format=json&limit=1");
        }
        return query;
    }

}
