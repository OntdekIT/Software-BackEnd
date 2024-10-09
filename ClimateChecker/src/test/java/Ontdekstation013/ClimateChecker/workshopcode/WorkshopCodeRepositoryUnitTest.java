package Ontdekstation013.ClimateChecker.workshopcode;

import Ontdekstation013.ClimateChecker.features.workshopcode.IWorkshopCodeRepository;
import Ontdekstation013.ClimateChecker.features.workshopcode.WorkshopCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class WorkshopCodeRepositoryUnitTest {

    private final IWorkshopCodeRepository workshopCodeRepository;

    public WorkshopCodeRepositoryUnitTest(IWorkshopCodeRepository workshopCodeRepository) {
        this.workshopCodeRepository = workshopCodeRepository;
    }

    @Test
    public void testSaveAndFindWorkshopCodeById() {

        WorkshopCode code = new WorkshopCode(123456L, LocalDateTime.now().plusDays(1));

        WorkshopCode savedCode = workshopCodeRepository.save(code);

        assertThat(savedCode.getCode()).isNotNull();
        assertThat(savedCode.getCode()).isEqualTo(code);

        WorkshopCode foundCode = workshopCodeRepository.findById(savedCode.getCode()).orElse(null);
        assertThat(foundCode).isNotNull();
        assertThat(foundCode.getCode()).isEqualTo(code);
    }
}
