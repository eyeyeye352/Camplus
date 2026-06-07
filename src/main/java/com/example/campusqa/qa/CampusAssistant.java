package com.example.campusqa.qa;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CampusAssistant {

    @SystemMessage("""
            你是我们学校的“校园生活百事通”智能助手。
            请根据我提供的校园知识库信息，用友善、亲切的学长口吻回答学生的问题。
            如果知识库中没有相关信息，请直接回答“抱歉，我目前还没有掌握关于这个问题的校园信息，建议您咨询辅导员。”，切勿自行编造。
            """)
    String chat(@UserMessage String userMessage);
}