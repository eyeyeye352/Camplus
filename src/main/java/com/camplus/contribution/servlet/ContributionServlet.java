package com.camplus.contribution.servlet;

import com.camplus.contribution.pojo.UserContribution;
import com.camplus.contribution.service.ContributionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/contribution/*")
public class ContributionServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ContributionService contributionService = new ContributionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        String action = normalizeAction(request.getPathInfo());
        try {
            Integer currentUserId = currentUserId(request);
            switch (action) {
                case "create" -> create(request, response, currentUserId);
                case "list" -> list(request, response, currentUserId);
                case "detail" -> detail(request, response, currentUserId);
                case "update" -> update(request, response, currentUserId);
                case "delete" -> delete(request, response, currentUserId);
                default -> writeError(response, HttpServletResponse.SC_NOT_FOUND, "接口不存在");
            }
        } catch (SecurityException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库操作失败");
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response, Integer currentUserId)
            throws SQLException, IOException {
        UserContribution contribution = fromRequest(request);
        int contributionId = contributionService.create(contribution, currentUserId);
        writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"提交成功\",\"data\":{\"contributionId\":"
                        + contributionId + "}}");
    }

    private void list(HttpServletRequest request, HttpServletResponse response, Integer currentUserId)
            throws SQLException, IOException {
        Integer status = nullableInt(request.getParameter("status"));
        int page = intValue(request.getParameter("page"), 1);
        int pageSize = intValue(request.getParameter("pageSize"), 10);
        List<UserContribution> contributions = contributionService.listMine(currentUserId, status, page, pageSize);
        writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"查询成功\",\"data\":" + toJson(contributions) + "}");
    }

    private void detail(HttpServletRequest request, HttpServletResponse response, Integer currentUserId)
            throws SQLException, IOException {
        Integer contributionId = nullableInt(request.getParameter("contribution_id"));
        UserContribution contribution = contributionService.detail(contributionId, currentUserId);
        writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"查询成功\",\"data\":" + toJson(contribution) + "}");
    }

    private void update(HttpServletRequest request, HttpServletResponse response, Integer currentUserId)
            throws SQLException, IOException {
        UserContribution contribution = fromRequest(request);
        contribution.setContributionId(nullableInt(request.getParameter("contribution_id")));
        contributionService.update(contribution, currentUserId);
        writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"修改成功\",\"data\":null}");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response, Integer currentUserId)
            throws SQLException, IOException {
        Integer contributionId = nullableInt(request.getParameter("contribution_id"));
        contributionService.delete(contributionId, currentUserId);
        writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"撤回成功\",\"data\":null}");
    }

    private UserContribution fromRequest(HttpServletRequest request) {
        UserContribution contribution = new UserContribution();
        contribution.setContributionType(nullableInt(request.getParameter("contribution_type")));
        contribution.setTitle(trim(request.getParameter("title")));
        contribution.setContent(trim(request.getParameter("content")));
        return contribution;
    }

    private Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = firstNonNull(
                session.getAttribute("user_id"),
                session.getAttribute("userId"),
                session.getAttribute("currentUserId")
        );
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return null;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String normalizeAction(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return "";
        }
        return pathInfo.substring(1);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status,
                "{\"success\":false,\"message\":\"" + escapeJson(message) + "\",\"data\":null}");
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }

    private String toJson(List<UserContribution> contributions) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < contributions.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(toJson(contributions.get(i)));
        }
        return builder.append(']').toString();
    }

    private String toJson(UserContribution contribution) {
        if (contribution == null) {
            return "null";
        }
        return "{"
                + "\"contributionId\":" + number(contribution.getContributionId()) + ","
                + "\"userId\":" + number(contribution.getUserId()) + ","
                + "\"contributionType\":" + number(contribution.getContributionType()) + ","
                + "\"title\":\"" + escapeJson(contribution.getTitle()) + "\","
                + "\"content\":\"" + escapeJson(contribution.getContent()) + "\","
                + "\"status\":" + number(contribution.getStatus()) + ","
                + "\"reviewComment\":\"" + escapeJson(contribution.getReviewComment()) + "\","
                + "\"createTime\":\"" + time(contribution.getCreateTime()) + "\","
                + "\"updateTime\":\"" + time(contribution.getUpdateTime()) + "\""
                + "}";
    }

    private String number(Integer value) {
        return value == null ? "null" : value.toString();
    }

    private String time(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    private Integer nullableInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private int intValue(String value, int defaultValue) {
        Integer parsed = nullableInt(value);
        return parsed == null ? defaultValue : parsed;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
