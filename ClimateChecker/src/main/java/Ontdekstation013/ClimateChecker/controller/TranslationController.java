package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.TranslationPage;
import Ontdekstation013.ClimateChecker.services.TranslationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Translation")
public class TranslationController {

    private TranslationService _translationService;

    @GetMapping
    public TranslationPage getTranslationPage(String _language, String _pageID){
        return _translationService.getTranslationPage(_language, _pageID);
    }
}
