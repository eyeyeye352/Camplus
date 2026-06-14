package com.camplus.admin.pojo;

/**
 * 纯文本提取数据传输对象
 * 用于将解析后的内容统一格式化，交给向量化大模型处理
 */
public class KnowledgeExtractDTO {
    private String sourceFileName; // 来源文件名，方便溯源
    private String dataType;       // 数据类型（TYPE_FAQ: 现成问答; TYPE_DOC: 纯文档片段）
    private String title;          // 标题或问题（如果有）
    private String plainText;      // 提取出的纯净文本（核心段落或答案）

    public KnowledgeExtractDTO(String sourceFileName, String dataType, String title, String plainText) {
        this.sourceFileName = sourceFileName;
        this.dataType = dataType;
        this.title = title;
        this.plainText = plainText;
    }

    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
}