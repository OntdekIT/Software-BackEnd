package Ontdekstation013.ClimateChecker.features.meetstation;

import Ontdekstation013.ClimateChecker.features.meetstation.endpoint.MeetstationDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MeetstationService {
    private final MeetstationRepository meetstationRepository;

    public MeetstationService(MeetstationRepository meetstationRepository){
        this.meetstationRepository = meetstationRepository;
    }

    public MeetstationDto ReadById(Long id){
        Optional<Meetstation> meetstation = Optional.ofNullable(meetstationRepository.getMeetstationByStationid(id));
        if(meetstation.isPresent())
        {
            return meetstation.get().toDto();
        }

        return null;
    }

    public void UpdateMeetstation(Meetstation meetstation){
        meetstationRepository.save(meetstation);
    }
}
