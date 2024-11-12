package Ontdekstation013.ClimateChecker.features.workshopCode;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class WorkshopService {
    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    public Workshop createWorkshop(LocalDateTime expirationDate) {
        Workshop workshop = new Workshop();
        workshop.setExpirationDate(expirationDate);
        workshop.setCode(generateUniqueRandomCode());
        workshop.setCreationTime(LocalDateTime.now());
        return workshopRepository.save(workshop);
    }

    public List<Workshop> getAllActiveWorkshops() {
        LocalDateTime now = LocalDateTime.now();
        return workshopRepository.findAll()
                .stream()
                .filter(code -> code.getExpirationDate().isAfter(now))
                .collect(Collectors.toList());
    }

    public List<Workshop> getAllExpiredWorkshops() {
        LocalDateTime now = LocalDateTime.now();
        return workshopRepository.findByExpirationDateBefore(now);
    }

    public Workshop getByCode(long code) {
        return workshopRepository.findByCode(code);
    }

    public void deleteWorkshopCode(long code) {
        Workshop workshop = workshopRepository.findByCode(code);
        workshopRepository.delete(workshop);
    }

    public boolean verifyWorkshopCode(Long code) {
        boolean isValid = false;

        Workshop workshop = workshopRepository.findByCode(code);
        if (workshop != null && workshop.getExpirationDate().isAfter(LocalDateTime.now())) {
            isValid = true;
        }
        return isValid;
    }

    private Long generateUniqueRandomCode() {
        Long randomCode;
        do {
            randomCode = ThreadLocalRandom.current().nextLong(100000, 999999);
        } while (workshopRepository.existsById(randomCode));

        return randomCode;
    }
}
