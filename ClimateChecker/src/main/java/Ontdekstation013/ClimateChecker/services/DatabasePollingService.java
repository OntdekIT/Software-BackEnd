package Ontdekstation013.ClimateChecker.services;

import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DatabasePollingService {

    public String BuildQueryString(List<Long> registrationCodes, Boolean lastFifteenMinutes){
        String query = new String();

        query.concat("https://meetjestad.net/data/?type=sensor&ids=");

        for(Long registrationCode : registrationCodes)
        {
            query.concat(registrationCode.toString() + ",");
        }

        if(!lastFifteenMinutes)
        {
            //This should get all of them.
            query.concat("&begin=2017-11-16,12:00&end=" + LocalDateTime.now().toString());
            //Possible issue where the query volume is too high
            query.concat("&format=json&limit=50000");
        }
        else
        {
            //Grab data of the last 15 minutes.
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now();
            fifteenMinutesAgo = fifteenMinutesAgo.minusMinutes(15);
            String beginFrame = fifteenMinutesAgo.toString();
            String endFrame = LocalDateTime.now().toString();

            query.concat("&begin=" + beginFrame +"&end=" + endFrame);
            query.concat("&format=json&limit=500");
        }
        return query;
    }


    public String BuildQueryCreateStationFromMeetJeStad(Long registrationCode){
        String query = new String();

        query.concat("https://meetjestad.net/data/?type=sensor&ids=");
        query.concat(registrationCode.toString() + ",");
        query.concat("&format=json&limit=1");

        return query;
    }

}
