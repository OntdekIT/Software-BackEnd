package Ontdekstation013.ClimateChecker;

import Ontdekstation013.ClimateChecker.models.TranslationPage;
import Ontdekstation013.ClimateChecker.services.TranslationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TranslationServiceTests {
    private TranslationService _translationService = new TranslationService();

    @Test
    void getTranslationPage(){
        //arrange
        String language = "Nederlands";
        String page = "LoginPage";

        //act
        TranslationPage translationPage = _translationService.getTranslationPage(language, page);

        //assert
        Assertions.assertEquals(3, translationPage.Bloks.stream().count());
        for(int i = 0; i < 3; i++){
            System.out.println(translationPage.Bloks.get(i).getBoksID());
            System.out.println(translationPage.Bloks.get(i).getText());
        }

    }
}
