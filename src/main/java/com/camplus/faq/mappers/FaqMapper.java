package com.camplus.faq.mappers;

import com.camplus.faq.pojo.Faq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FaqMapper {

    List<Faq> selectHotFaqs(@Param("limit") Integer limit);

    Faq selectById(@Param("faqId") Integer faqId);

    int incrementQuestionCount(@Param("faqId") Integer faqId);

    int updateHotScore(@Param("faqId") Integer faqId, @Param("hotScore") Integer hotScore);

    List<Faq> selectAllDisplayed();
}
