package com.camplus.problem.service;

import com.camplus.problem.dao.FaqProblemDao;
import com.camplus.problem.dao.FaqProblemDaoImpl;
import com.camplus.problem.entity.FaqProblem;
import java.util.List;

public class FaqProblemServiceImpl implements FaqProblemService {


    private final FaqProblemDao faqProblemDao = new FaqProblemDaoImpl();

    @Override
    public List<FaqProblem> getProblemsByCategory(Long category_id) {
        if (category_id == null || category_id <= 0) return null;
        return faqProblemDao.findItemsByCategoryId(category_id);
    }

    @Override
    public FaqProblem getProblemDetail(Long faq_id) {
        FaqProblem p = faqProblemDao.findItemById(faq_id);
        if (p != null) {
            faqProblemDao.incrementViewCount(faq_id);
        }
        return p;
    }


    @Override
    public List<FaqProblem> searchProblems(String keyword) {
        if (keyword == null || keyword.isEmpty()) return null;
        return faqProblemDao.searchItemsByKeyword(keyword);
    }
}