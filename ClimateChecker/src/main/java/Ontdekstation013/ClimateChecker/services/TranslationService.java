package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Translation;
import Ontdekstation013.ClimateChecker.models.TranslationPage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import java.util.Iterator;



@Service
public class TranslationService {

    String jsonFile = "./Ontdekstation013/ClimateChecker/TranslationFile.Json";


    public TranslationPage getTranslationPage(String _language, String _pageID){
        TranslationPage _translationPage = new TranslationPage();
        _translationPage.setLanguageID(_language);

        ObjectMapper mapper = new ObjectMapper();

        try {
            // Laad het JSON-bestand
            File jsonFile = new File("src/main/java/Ontdekstation013/ClimateChecker/TranslationFile.Json");
            Map<String, List<Map<String, Map<String, String>>>> data = mapper.readValue(jsonFile, Map.class);

            // Haal de gewenste tekst uit de JSON
            Map page = data.get(_translationPage.getLanguageID()).get(0).get(_pageID);

            Iterator<Map.Entry> itr1 = page.entrySet().iterator();
            while (itr1.hasNext()) {
                Map.Entry pair = itr1.next();
                Translation _translation = new Translation();
                _translation.setBoksID((String)pair.getKey());
                _translation.setText((String)pair.getValue());
                _translationPage.Bloks.add(_translation);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return _translationPage;
    }


}