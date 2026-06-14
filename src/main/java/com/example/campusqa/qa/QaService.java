package com.example.campusqa.qa;

import org.springframework.stereotype.Service;

@Service
public class QaService {

    private final CampusAssistant campusAssistant;

    public QaService(CampusAssistant campusAssistant) {
        this.campusAssistant = campusAssistant;
    }

    public String generateAnswer(String question) {


        return campusAssistant.answer(question);
    }
}