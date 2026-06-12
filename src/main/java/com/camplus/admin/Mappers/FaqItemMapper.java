package com.camplus.admin.Mappers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FaqItemMapper {
    // 严格适配 v2 表字段：移除了 category_id 和 user_id，改用 display_status
    @Insert("INSERT INTO faq_items (question, answer, display_status) " +
            "VALUES (#{question}, #{answer}, 1)")
    int insertFaq(@Param("question") String question,
                  @Param("answer") String answer);
}