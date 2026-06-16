package com.camplus.problem.service;

import com.camplus.problem.entity.FaqProblem;
import java.util.List;

public interface FaqProblemService {
    List<FaqProblem> getProblemsByCategory(Long categoryId);
    List<FaqProblem> searchProblems(String keyword);
    FaqProblem getProblemDetail(Long id);
}