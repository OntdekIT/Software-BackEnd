package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.dto.MeetJeStadDto;
import Ontdekstation013.ClimateChecker.services.MJSValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MJSValidationServiceTest {

    private MJSValidationService validatorService;
    private MeetJeStadDto testDto = new MeetJeStadDto();

    private List<MeetJeStadDto> testList;

    @BeforeEach
    void setUp() {

        this.validatorService = new MJSValidationService();
        testList = new ArrayList<>();

        testDto.id = 1L;
        testDto.temperature = 37.5F;
        testDto.humidity = 77.9F;
        testDto.supply = 40.4F;

        testList.add(testDto);

        testDto = new MeetJeStadDto();

        testDto.id = 2L;
        testDto.temperature = 12.0F;
        testDto.humidity = 10F;
        testDto.supply = 98.0F;

        testList.add(testDto);

    }

    @Test
    void validateDTO_Pass() {

        var result = validatorService.ValidateDTO(testDto);

        assertEquals(testDto, result);

    }

    @Test
    void validateDTO_InvalidID_Failed() {

        testDto.id = -1L;

        var result = validatorService.ValidateDTO(testDto);

        assertFalse(result.errorMessage.isEmpty());

    }

    @Test
    void validateDTO_InvalidTemperature_Failed() {

        testDto.temperature = 5000.000F;

        var result = validatorService.ValidateDTO(testDto);

        assertFalse(result.errorMessage.isEmpty());

    }

    @Test
    void validateDTO_InvalidBattery_Failed() {

        testDto.supply = 101;

        var result = validatorService.ValidateDTO(testDto);

        assertFalse(result.errorMessage.isEmpty());

    }

    @Test
    void validateAll_Pass() {

        var result = validatorService.ValidateAll(testList);

        assertEquals(testList.size(), result.size());

    }

    @Test
    void validateAll_InvalidDTOID_Fail() {
        testList.get(0).id = -1L;
        testList.get(1).id = 2L;

        var result = validatorService.ValidateAll(testList);

        assertEquals(testList.size(), result.size());

    }
}