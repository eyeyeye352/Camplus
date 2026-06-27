package com.camplus.problem.dao;

import com.camplus.problem.entity.FaqProblem;
import com.camplus.problem.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FaqProblemDaoImpl implements FaqProblemDao {

    @Override
    public List<FaqProblem> findItemsByCategoryId(Long categoryId) {
        String sql = "SELECT * FROM faq_items WHERE category_id = ? AND status = 1";
        List<FaqProblem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToProblem(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<FaqProblem> searchItemsByKeyword(String keyword) {
        // 使用模糊查询，注意这里的 sql 写法
        String sql = "SELECT * FROM faq_items WHERE question LIKE ? AND status = 1";
        List<FaqProblem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToProblem(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public FaqProblem findItemById(Long faq_id) { // 注意参数名也要匹配
        String sql = "SELECT * FROM faq_items WHERE faq_id = ? AND status = 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, faq_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRowToProblem(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void incrementViewCount(Long faq_id) {
        String sql = "UPDATE faq_items SET view_count = view_count + 1 WHERE faq_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, faq_id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 辅助方法：为了避免重复写 rs.get 逻辑，写一个通用的映射方法
    private FaqProblem mapRowToProblem(ResultSet rs) throws SQLException {
        FaqProblem p = new FaqProblem();
        p.setFaq_id(rs.getLong("faq_id"));
        p.setCategory_id(rs.getLong("category_id"));
        p.setQuestion(rs.getString("question"));
        p.setAnswer(rs.getString("answer"));
        p.setStatus(rs.getInt("status"));
        p.setView_count(rs.getInt("view_count"));
        return p;
    }
}