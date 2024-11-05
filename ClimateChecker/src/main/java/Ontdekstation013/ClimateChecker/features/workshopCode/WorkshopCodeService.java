package Ontdekstation013.ClimateChecker.features.workshopCode;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class WorkshopCodeService {
    private final IWorkshopCodeRepository workshopCodeRepository;

    public WorkshopCodeService(IWorkshopCodeRepository workshopCodeRepository) {
        this.workshopCodeRepository = workshopCodeRepository;
    }


    public WorkshopCode createWorkshopCode(LocalDateTime expirationDate) {
        Long uniqueRandomCode = generateUniqueRandomCode();
        WorkshopCode workshopCode = new WorkshopCode(uniqueRandomCode, expirationDate);
        return workshopCodeRepository.save(workshopCode);
    }

    public List<WorkshopCode> getAllWorkshopCodes() {
        LocalDateTime now = LocalDateTime.now();
        return workshopCodeRepository.findAll()
                .stream()
                .filter(code -> code.getExpirationDate().isAfter(now))
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void deleteExpiredWorkshopCodes() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkshopCode> expiredCodes = workshopCodeRepository.findByExpirationDateBefore(now);
        workshopCodeRepository.deleteAll(expiredCodes);
        System.out.println("Deleted workshop codes at " + now);
    }

    private Long generateUniqueRandomCode() {
        Long randomCode = ThreadLocalRandom.current().nextLong(100000, 999999);
        if (workshopCodeRepository.existsById(randomCode)) {
            return generateUniqueRandomCode();
        } else {
            return randomCode;
        }
    }

    public boolean VerifyWorkshopCode(Long code) {
        WorkshopCode officialCode = workshopCodeRepository.findByCode(code);
        if (officialCode != null){
            if (officialCode.getCode().equals(code)) {
                if (officialCode.getExpirationDate().isAfter(LocalDateTime.now())) {
                    return true;
                }
                else {
                    workshopCodeRepository.delete(officialCode);
                }
            }
        }
        return false;
    }
}
