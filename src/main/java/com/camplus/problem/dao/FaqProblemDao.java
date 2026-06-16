package com.camplus.problem.dao;

import com.camplus.problem.entity.FaqProblem;
import java.util.List;

public interface FaqProblemDao {

    /**
     * 根据分类ID查询该分类下所有状态为正常的固定问题
     * @param categoryId 分类ID
     * @return 问题列表
     */
    List<FaqProblem> findItemsByCategoryId(Long categoryId);

    /**
     * 根据用户输入的关键字进行模糊搜索
     * @param keyword 搜索关键字
     * @return 匹配的问题列表
     */
    List<FaqProblem> searchItemsByKeyword(String keyword);

    /**
     * 根据问题ID查询详情（必须同时检查 status=1）
     * @param id 问题ID
     * @return FaqProblem对象
     */
    FaqProblem findItemById(Long id);

    /**
     * 增加问题的浏览次数
     * @param id 问题ID
     */
    void incrementViewCount(Long id);
}