package Ontdekstation013.ClimateChecker.features.meetstation;

import Ontdekstation013.ClimateChecker.features.meetstation.endpoint.MeetstationDto;
import org.springframework.stereotype.Service;

@Service
public class MeetstationService {
    private final MeetstationRepository meetstationRepository;

    public MeetstationService(MeetstationRepository meetstationRepository){
        this.meetstationRepository = meetstationRepository;
    }

    public MeetstationDto ReadById(Long id){
        Meetstation meetstation = meetstationRepository.getMeetstationByStationid(id);
        return meetstation.toDto();
    }

    public void UpdateMeetstation(Meetstation meetstation){
        meetstationRepository.save(meetstation);
    }
}
