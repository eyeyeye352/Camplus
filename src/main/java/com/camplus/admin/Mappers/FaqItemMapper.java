package com.camplus.admin.Mappers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FaqItemMapper {
    // 将审核通过的数据正式入库到 FAQ 表
    @Insert("INSERT INTO faq_items (category_id, user_id, question, answer, status) " +
            "VALUES (#{categoryId}, #{userId}, #{question}, #{answer}, 1)")
    int insertFaq(@Param("categoryId") Long categoryId,
                  @Param("userId") Long userId,
                  @Param("question") String question,
                  @Param("answer") String answer);
}
