package com.camplus.problem.controller;

import com.camplus.problem.entity.FaqProblem;
import com.camplus.problem.service.FaqProblemService;
import com.camplus.problem.service.FaqProblemServiceImpl;
import com.google.gson.Gson; // 确保你的项目里有 Gson 依赖

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/faq")
public class FaqProblemServlet extends HttpServlet {

    private final FaqProblemService faqProblemService = new FaqProblemServiceImpl();
    private final Gson gson = new Gson(); // 用于将对象转换成前端需要的 JSON 格式

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 设置响应编码为 UTF-8，这是解决中文乱码的关键
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        if ("list".equals(action)) {
            // 处理按分类查询列表
            String categoryIdStr = request.getParameter("categoryId");
            if (categoryIdStr != null) {
                Long categoryId = Long.parseLong(categoryIdStr);
                List<FaqProblem> list = faqProblemService.getProblemsByCategory(categoryId);

                // 将数据转为 JSON 并返回给前端
                String json = gson.toJson(list);
                response.getWriter().write(json);
            }
        } else if ("detail".equals(action)) {
            // 处理查询问题详情
            String idStr = request.getParameter("id");
            if (idStr != null) {
                Long id = Long.parseLong(idStr);
                FaqProblem problem = faqProblemService.getProblemDetail(id);

                // 将对象转为 JSON 并返回给前端
                String json = gson.toJson(problem);
                response.getWriter().write(json);
            }
        }
    }
}