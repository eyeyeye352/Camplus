package com.camplus.problem.controller;

import com.camplus.problem.entity.FaqProblem;
import com.camplus.problem.service.FaqProblemService;
import com.camplus.problem.service.FaqProblemServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/faq")
public class FaqProblemServlet extends HttpServlet {

    // 使用 final 关键字，让代码更健壮（这也是 IDEA 刚才建议的）
    private final FaqProblemService faqProblemService = new FaqProblemServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 设置响应编码，防止中文乱码
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        if ("list".equals(action)) {
            // 处理按分类查询列表
            String categoryIdStr = request.getParameter("categoryId");
            if (categoryIdStr != null) {
                Long categoryId = Long.parseLong(categoryIdStr);
                List<FaqProblem> list = faqProblemService.getProblemsByCategory(categoryId);
                // 暂时打印一下，证明我们拿到了数据，同时也消除了“未使用”警告
                System.out.println("成功获取分类ID为 " + categoryId + " 的问题列表，数量：" + list.size());
            }
        } else if ("detail".equals(action)) {
            // 处理查询问题详情
            String idStr = request.getParameter("id");
            if (idStr != null) {
                Long id = Long.parseLong(idStr);
                FaqProblem problem = faqProblemService.getProblemDetail(id);
                // 暂时打印一下详情内容
                if (problem != null) {
                    System.out.println("成功获取问题详情：" + problem.getQuestion());
                }
            }
        }
    }
}