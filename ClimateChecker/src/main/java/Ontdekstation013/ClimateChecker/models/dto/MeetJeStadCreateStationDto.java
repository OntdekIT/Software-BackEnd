package Ontdekstation013.ClimateChecker.models.dto;


import java.time.LocalDateTime;

public class MeetJeStadCreateStationDto extends Dto{
    public String row;
    public String id;
    public LocalDateTime  timestamp;
    public String firmware_version;
    public String longitude;
    public String latitude;
    public String temperature;
    public String humidity;
    public String supply;


    public void GetValuesFromJSONString(String input){
        String[] splittedString = input.split("[,:]");

    }

}
