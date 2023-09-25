package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;

public class MeetJeStadDto extends Dto{
    public int row;
    public long id;
    public String timestamp;
    public int firmware_version;
    public float longitude;
    public float latitude;
    public float temperature;
    public float humidity;
    public float supply;
    public String errorMessage;

}
