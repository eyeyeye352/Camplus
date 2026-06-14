package com.camplus.problem.service;

import com.camplus.problem.dao.FaqProblemDao;
import com.camplus.problem.dao.FaqProblemDaoImpl;
import com.camplus.problem.entity.FaqProblem;
import java.util.List;

public class FaqProblemServiceImpl implements FaqProblemService {
    private FaqProblemDao faqProblemDao = new FaqProblemDaoImpl();

    @Override
    public List<FaqProblem> getProblemsByCategory(Long categoryId) {
        return faqProblemDao.findItemsByCategoryId(categoryId);
    }

    @Override
    public List<FaqProblem> searchProblems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        return faqProblemDao.searchItemsByKeyword(keyword);
    }

    @Override
    public FaqProblem getProblemDetail(Long id) {
        faqProblemDao.incrementViewCount(id);
        return faqProblemDao.findItemById(id);
    }
}