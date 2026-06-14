package com.example.campusqa.qa;

import org.springframework.stereotype.Service;

@Service
public class QaService {

    private final CampusAssistant campusAssistant;

    public QaService(CampusAssistant campusAssistant) {
        this.campusAssistant = campusAssistant;
    }

    public String generateAnswer(String question) {
        // 你什么都不用做，直接调用接口！
        // 底层黑魔法：提取问题 -> 向量化 -> 去数据库搜索相关片段 -> 拼成 Prompt -> 发给 API -> 返回结果

        return campusAssistant.answer(question);
    }
}