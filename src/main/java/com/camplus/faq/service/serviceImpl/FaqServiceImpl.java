package com.camplus.faq.service.serviceImpl;

import com.camplus.faq.mappers.FaqMapper;
import com.camplus.faq.pojo.Faq;
import com.camplus.faq.service.FaqService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FaqServiceImpl implements FaqService {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public List<Faq> getHotFaqs(Integer limit) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            return faqMapper.selectHotFaqs(limit);
        }
    }

    @Override
    public Faq getFaqById(Integer faqId) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            return faqMapper.selectById(faqId);
        }
    }

    @Override
    public boolean recordClick(Integer faqId) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            int rows = faqMapper.incrementQuestionCount(faqId);
            session.commit();
            return rows > 0;
        }
    }

    @Override
    public void updateHotScores() {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            List<Faq> faqs = faqMapper.selectAllDisplayed();

            for (Faq faq : faqs) {
                long days = ChronoUnit.DAYS.between(faq.getCreateTime(), LocalDateTime.now());
                int timeDecay = Math.max(0, (7 - (int) days) * 2);
                int hotScore = faq.getQuestionCount() * 10 + faq.getLikeCount() * 5 + timeDecay;
                faqMapper.updateHotScore(faq.getFaqId(), hotScore);
            }
            session.commit();
        }
    }
}
