package com.camplus.faq.mappers;

import com.camplus.faq.pojo.Faq;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface FaqMapper {

    List<Faq> selectHotFaqs(@Param("limit") Integer limit);

    Faq selectById(@Param("faqId") Integer faqId);

    int incrementQuestionCount(@Param("faqId") Integer faqId);

    int updateHotScore(@Param("faqId") Integer faqId, @Param("hotScore") Integer hotScore);

    List<Faq> selectAllDisplayed();

    int insertFaq(Map<String, Object> params);

    @Select("SELECT COUNT(*) FROM faq_items")
    int countAll();
}
