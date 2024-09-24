package Ontdekstation013.ClimateChecker.features.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AdminService {

    private final IWorkshopCodeRepository workshopCodeRepository;
    @Autowired
    public AdminService(IWorkshopCodeRepository workshopCodeRepository) {
        this.workshopCodeRepository = workshopCodeRepository;
    }

    public WorkshopCode createWorkshopCode(Long minutes, Long length){
        WorkshopCode workshopCode = new WorkshopCode();
        Long code = null;
        boolean isUnique = false;
        while (!isUnique){
            code = randomCode(length);
            WorkshopCode existingWorkshopCode = workshopCodeRepository.findByCode(code);
            if (existingWorkshopCode == null){
                isUnique = true;
            }
            else{
                if (!VerifyWorkshopCode(existingWorkshopCode.getCode())){
                    isUnique = true;
                }
            }
        }

        workshopCode.setExpirationDate(LocalDateTime.now().plusMinutes(minutes));
        workshopCode.setCode(code);
        workshopCodeRepository.save(workshopCode);
        return workshopCode;
    }

    public List<WorkshopCode> getWorkshopCodes() {
        List<WorkshopCode> workshopCodeList = workshopCodeRepository.findAll();
        List<WorkshopCode> returnWorkshopCodeList = new ArrayList<>();

        for (WorkshopCode workshopCode : workshopCodeList) {
            if (VerifyWorkshopCode(workshopCode.getCode())) {
                returnWorkshopCodeList.add(workshopCode);
            }
        }

        return returnWorkshopCodeList;
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
