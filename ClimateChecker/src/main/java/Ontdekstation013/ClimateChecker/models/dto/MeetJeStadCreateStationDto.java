package Ontdekstation013.ClimateChecker.models.dto;


import java.time.LocalDateTime;

import static java.lang.Float.parseFloat;

public class MeetJeStadCreateStationDto extends Dto{
    public int row;
    public long registrationCode;
    public LocalDateTime  timestamp;
    public int firmware_version;
    public float longitude;
    public float latitude;
    public float temperature;
    public float humidity;
    public float supply;


    public void GetValuesFromJSONString(String input, long registrationCode){
        this.registrationCode = registrationCode;
        String[] splittedString = input.split("[,:{[}]]");
        this.longitude = parseFloat(splittedString[12]);
        this.latitude = parseFloat(splittedString[14]);
    }

}
