package com.manthau.postservice.infrastructure.language;

import com.github.pemistahl.lingua.api.*;
import org.springframework.stereotype.Service;

import static com.github.pemistahl.lingua.api.Language.*;

@Service
public class LanguageDetectionServiceImpl implements LanguageDetectionService {

    private final LanguageDetector detector;

    public LanguageDetectionServiceImpl() {
        this.detector = LanguageDetectorBuilder
                .fromLanguages(ENGLISH, VIETNAMESE, JAPANESE, CHINESE, KOREAN, FRENCH, GERMAN)
                .build();
    }

    @Override
    public String detect(String text) {
        if (text == null || text.length() < 50) return "en";
        Language language = detector.detectLanguageOf(text);
        if (language == Language.UNKNOWN) return "en";
        return language.getIsoCode639_1().toString().toLowerCase();
    }
}
