package Ontdekstation013.ClimateChecker.features.workshopCode;

import Ontdekstation013.ClimateChecker.features.user.User;
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

    // TODO: refactor by adding creationTime
    public Workshop createWorkshop(LocalDateTime expirationDate) {
        Long uniqueRandomCode = generateUniqueRandomCode();
        Workshop workshop = new Workshop(uniqueRandomCode, expirationDate);
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
        throw new UnsupportedOperationException();
    }

    public List<User> getAllUsersByWorkshop(Long code) {
        throw new UnsupportedOperationException();
    }

    // TODO: Clean up nested if statements
    public boolean VerifyWorkshopCode(Long code) {
        Workshop officialCode = workshopRepository.findByCode(code);
        if (officialCode != null) {
            if (officialCode.getCode().equals(code)) {
                if (officialCode.getExpirationDate().isAfter(LocalDateTime.now())) {
                    return true;
                }
                else {
                    workshopRepository.delete(officialCode);
                }
            }
        }
        return false;
    }

    private Long generateUniqueRandomCode() {
        Long randomCode = ThreadLocalRandom.current().nextLong(100000, 999999);
        if (workshopRepository.existsById(randomCode)) {
            return generateUniqueRandomCode();
        } else {
            return randomCode;
        }
    }
}
