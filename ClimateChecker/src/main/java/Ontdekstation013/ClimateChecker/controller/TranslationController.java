package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Translation;
import Ontdekstation013.ClimateChecker.models.TranslationPage;
import Ontdekstation013.ClimateChecker.services.TranslationService;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/Translation")
public class TranslationController {

    private TranslationService _translationService;

    @GetMapping({"_language", "_pageID"})
    public TranslationPage getTranslationPage(@RequestParam String _language, @RequestParam String _pageID){
        return _translationService.getTranslationPage(_language, _pageID);
    }
}
