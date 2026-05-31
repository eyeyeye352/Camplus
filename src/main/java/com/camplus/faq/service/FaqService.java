package com.camplus.faq.service;

import com.camplus.faq.pojo.Faq;

import java.util.List;

public interface FaqService {

    List<Faq> getHotFaqs(Integer limit);

    Faq getFaqById(Integer faqId);

    boolean recordClick(Integer faqId);

    void updateHotScores();
}
