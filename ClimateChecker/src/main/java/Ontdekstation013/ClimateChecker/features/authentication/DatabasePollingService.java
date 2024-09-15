package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.services.MJSValidationService;
import Ontdekstation013.ClimateChecker.models.dto.MeetJeStadDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;

@Service
public class DatabasePollingService {

    private MJSValidationService validator;

    @Autowired
    public DatabasePollingService(MJSValidationService validatorService){
        this.validator = validatorService;
    }

    public String BuildQueryString(List<Long> registrationCodes, Boolean latestResults, Boolean registration){
        String query = new String();

        query = query.concat("https://meetjestad.net/data/?type=sensors&ids=");

        for(Long registrationCode : registrationCodes)
        {
            query = query.concat(registrationCode.toString() + ",");
        }

        if(!latestResults && !registration)
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
            fifteenMinutesAgo = fifteenMinutesAgo.minusHours(3);
            String beginFrame = fifteenMinutesAgo.format(formatter);

            beginFrame = beginFrame.replace(" ", ",");

            //Retrieve and format the current time correctly for the query.
            LocalDateTime currentTime = LocalDateTime.now().minusHours(3);
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

    public MeetJeStadDto GetStationToRegister(Long registrationCode){

        RestTemplate restTemplate = new RestTemplate();

        List<Long> codes = new ArrayList<>();
        codes.add(registrationCode);

        String query = BuildQueryString(codes, true,true);

        ResponseEntity<MeetJeStadDto[]> response = restTemplate.getForEntity(query, MeetJeStadDto[].class);
        MeetJeStadDto newFound = response.getBody()[0];

        return newFound;
    }

    private List<MeetJeStadDto> GetStationsForData(List<Long> registrationCodes, boolean latestResults){
        List<MeetJeStadDto> found = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        String query = BuildQueryString(registrationCodes,latestResults,false);

        ResponseEntity<MeetJeStadDto[]> response = restTemplate.getForEntity(query, MeetJeStadDto[].class);
        MeetJeStadDto[] results = response.getBody();

        for(MeetJeStadDto result : results){
            found.add(result);
        }
        return found;
    }

    public MeetJeStadDto GetStation(Long registrationCode){

        //If the registration code presented is incorrect, escape the function.
        if(registrationCode < 1  || registrationCode ==  null){
            return null;
        }

        MeetJeStadDto found = GetStationToRegister(registrationCode);
        return found;
    }

    public List<MeetJeStadDto> GetAllRecentStations(List<Long> registrationCodes){
        var dtos = GetStationsForData(registrationCodes, true);

        for(MeetJeStadDto MJSdto : dtos){
            try{
                validator.ValidateDTO(MJSdto);
            }
            catch(Exception e){
                MJSdto.errorMessage = e.getMessage();
            }
        }
        return dtos;
    }

}
