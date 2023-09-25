package Ontdekstation013.ClimateChecker.features.region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    @Autowired
    public RegionService(RegionRepository regionRepository)
    {
        this.regionRepository = regionRepository;
    }

    public List<Region> getAll()
    {
        return regionRepository.findAll();
    }

    public Region getById(long id)
    {return regionRepository.getById(id);}


}
