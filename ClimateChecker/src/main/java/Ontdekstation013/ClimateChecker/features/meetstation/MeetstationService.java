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

    public void ClaimMeetstation(Meetstation meetstation){
        Meetstation existingMeetstation = meetstationRepository.getMeetstationByStationid(meetstation.getStationid());
        existingMeetstation.setUserid(meetstation.getUserid());
        existingMeetstation.setName(meetstation.getName());
        existingMeetstation.setDatabase_tag(meetstation.getDatabase_tag());
        existingMeetstation.setIs_public(meetstation.getIs_public());
        existingMeetstation.setRegistrationCode(meetstation.getStationid());
        meetstationRepository.save(existingMeetstation);
    }

    public Boolean IsAvailable(Long stationId){
        Optional<Meetstation> optionalMeetstation = meetstationRepository.findById(stationId);
        if (optionalMeetstation.isPresent()) {
            Meetstation meetstation = optionalMeetstation.get();
            if (meetstation.getUserid() == null) {
                return true;
            }
        }
        return false;
    }
}
