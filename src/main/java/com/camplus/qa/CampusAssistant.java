package com.camplus.qa;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CampusAssistant {

    @SystemMessage({
            "你是一个幽默、专业的校园生活助手。",
            "请严格根据检索到的校园规章制度片段来回答问题。",
            "如果提供的片段中找不到答案，请直接回复\"暂无相关方面的信息\"，不要添加任何其他内容，绝对不要自己编造（防止大模型幻觉）。",
            "如果可以回答，请用自然、友好的语言详细解答用户的问题。"
    })
    String answer(@UserMessage String userMessage);
}