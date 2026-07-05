package com.camplus.faq.service.serviceImpl;

import com.camplus.faq.mappers.FaqMapper;
import com.camplus.faq.pojo.Faq;
import com.camplus.faq.service.FaqService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FAQ服务实现类
 * 提供FAQ数据的增删改查和热度计算功能
 */
@Service
public class FaqServiceImpl implements FaqService {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }

    /**
     * 获取热点FAQ列表
     * @param limit 返回数量限制
     * @return FAQ列表
     */
    @Override
    public List<Faq> getHotFaqs(Integer limit) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            return faqMapper.selectHotFaqs(limit);
        }
    }

    /**
     * 根据ID获取FAQ详情
     * @param faqId FAQ ID
     * @return FAQ对象
     */
    @Override
    public Faq getFaqById(Integer faqId) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            return faqMapper.selectById(faqId);
        }
    }

    /**
     * 记录FAQ点击次数
     * @param faqId FAQ ID
     * @return 是否成功
     */
    @Override
    public boolean recordClick(Integer faqId) {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            int rows = faqMapper.incrementQuestionCount(faqId);
            session.commit();
            return rows > 0;
        }
    }

    /**
     * 更新所有FAQ的热度分
     * 热度计算：将今日查询次数累加到热度分中
     */
    @Override
    public void updateHotScores() {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            List<Faq> faqs = faqMapper.selectAllDisplayed();

            for (Faq faq : faqs) {
                int hotScore = faq.getHotScore() + faq.getQuestionCount() * 10;
                faqMapper.updateHotScore(faq.getFaqId(), hotScore);
            }
            session.commit();
        }
    }

    /**
     * 每日重置统计数据
     * 1. 将今日查询次数(question_count)重置为0
     * 2. 将热度分(hot_score)减半
     */
    public void resetDailyStats() {
        try (SqlSession session = getSession()) {
            FaqMapper faqMapper = session.getMapper(FaqMapper.class);
            faqMapper.resetDailyStats();
            session.commit();
        }
    }
}
