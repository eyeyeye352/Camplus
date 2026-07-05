package com.camplus.faq.mappers;

import com.camplus.faq.pojo.Faq;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface FaqMapper {

    List<Faq> selectHotFaqs(@Param("limit") Integer limit);

    Faq selectById(@Param("faqId") Integer faqId);

    Map<String, Object> selectByIdMap(@Param("faqId") Integer faqId);

    int incrementQuestionCount(@Param("faqId") Integer faqId);

    int updateHotScore(@Param("faqId") Integer faqId, @Param("hotScore") Integer hotScore);

    List<Faq> selectAllDisplayed();

    int insertFaq(Map<String, Object> params);

    int insertFaqWithZeroStats(Map<String, Object> params);

    int updateDisplayStatus(@Param("faqId") Integer faqId, @Param("displayStatus") Integer displayStatus);

    int resetDailyStats();

    @Select("SELECT * FROM faq_items ORDER BY hot_score DESC")
    List<Faq> selectAllByHotScore();

    @Select("SELECT COUNT(*) FROM faq_items")
    int countAll();
}
