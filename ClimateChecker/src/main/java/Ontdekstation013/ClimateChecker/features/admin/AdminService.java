package Ontdekstation013.ClimateChecker.features.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AdminService {

    private final WorkshopCodeRepository workshopCodeRepository;
    @Autowired
    public AdminService(WorkshopCodeRepository workshopCodeRepository) {
        this.workshopCodeRepository = workshopCodeRepository;
    }

    public WorkshopCode createWorkshopCode(Long minutes, Long length){
        WorkshopCode workshopCode = new WorkshopCode();
        workshopCode.setExpirationDate(LocalDateTime.now().plusMinutes(minutes));
        workshopCode.setCode(randomCode(length));
        workshopCodeRepository.save(workshopCode);
        return workshopCode;
    }

    public boolean verifyWorkshopCode(Long code) {
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


    private Long randomCode(float length){
        char[] CODE ="0123456789".toCharArray();

        StringBuilder string = new StringBuilder();

        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(CODE.length);

            string.append(CODE[index]);
        }
        return Long.parseLong(string.toString());
    }
}
