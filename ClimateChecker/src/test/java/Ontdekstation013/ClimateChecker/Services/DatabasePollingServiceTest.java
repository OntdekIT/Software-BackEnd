package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.dto.MeetJeStadDto;
import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.services.DatabasePollingService;
import Ontdekstation013.ClimateChecker.services.MJSValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Tag("Services")
class DatabasePollingServiceTest {
    private DatabasePollingService service;
    private MJSValidationService validator;
    private List<Long> registrationCodes;

    @BeforeEach
    void setUp(){
        this.validator = new MJSValidationService();
        this.service = new DatabasePollingService(validator);

        registrationCodes = new ArrayList<>();
        registrationCodes.add(378L);
    }

    @Test
    void BuildQuery_LastFifteenMinutes_Pass(){
        //Arrange
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

        String expected = "https://meetjestad.net/data/?type=sensors&ids=378,&begin=" + beginFrame + "&end=" + endFrame + "&format=json&limit=500";

        //Act
        var result = service.BuildQueryString(registrationCodes, true, false);



        //Assert
        assertEquals(result, expected);
    }

    @Test
    void BuildQuery_Registration_Pass(){
        //Arrange
        String expected = "https://meetjestad.net/data/?type=sensors&ids=378,&format=json&limit=1";

        //Act
        var result = service.BuildQueryString(registrationCodes, true, true);

        //Assert
        assertEquals(expected, result);
    }

    @Test
    void BuildQuery_GetAllData_Pass(){
        //Arrange
                    //Grab data of the last 15 minutes.
                    //Proper format: 2017-11-16,12:00
                    // yyyy-mm-dd,hh,mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                    //Retrieve and format the current time correctly for the query.
        LocalDateTime currentTime = LocalDateTime.now();
        String endFrame = currentTime.format(formatter);

        endFrame = endFrame.replace(" ", ",");

        String expected = "https://meetjestad.net/data/?type=sensors&ids=378,&begin=2017-11-16,12:00&end=" + endFrame + "&format=json&limit=50000";

        //Act
        var result = service.BuildQueryString(registrationCodes, false, false);

        //Assert
        assertEquals(expected, result);
    }

    @Test
    void GetStationToRegister_Pass(){
        //Act
        MeetJeStadDto result = service.GetStation(registrationCodes.get(0));
        //Assert
        assertEquals(378L, result.id);
    }

    @Test
    void GetRecentStations_Pass(){
        //Act
        var stations = service.GetAllRecentStations(registrationCodes);

        assertEquals(378L, stations.get(0).id);
    }

}