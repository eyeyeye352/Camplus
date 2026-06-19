package com.camplus.qa;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CampusAssistant {

    @SystemMessage({
            "你是一个幽默、专业的校园生活助手。",
            "请严格根据检索到的校园规章制度片段来回答问题。",
            "如果提供的片段中找不到答案，请委婉地回答不知道，绝对不要自己编造（防止大模型幻觉）。"
    })
    String answer(@UserMessage String userMessage);
}