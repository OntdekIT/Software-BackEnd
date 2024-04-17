package Ontdekstation013.ClimateChecker.features.admin;

import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.authentication.TokenRepository;
import Ontdekstation013.ClimateChecker.features.user.User;
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
        workshopCode.setRandomCode(randomCode(length));
        workshopCodeRepository.save(workshopCode);
        return workshopCode;
    }

    public boolean verifyWorkshopCode(String code) {
        WorkshopCode officialCode = WorkshopCodeRepository.findByRandomCode(code);
        if (officialCode != null){
            if (officialCode.getRandomCode().equals(code)) {
                if (officialCode.getExpirationDate().isBefore(LocalDateTime.now())) {
                    return true;
                }
                else {
                    WorkshopCodeRepository.delete(code);
                }
            }
        }
        return false;
    }


    private String randomCode(float length){
        char[] CODE ="0123456789".toCharArray();

        StringBuilder string = new StringBuilder();

        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(CODE.length);

            string.append(CODE[index]);
        }
        return string.toString();
    }
}
