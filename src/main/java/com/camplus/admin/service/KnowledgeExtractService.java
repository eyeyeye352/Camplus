package com.camplus.admin.service;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface KnowledgeExtractService {
    // 处理单个上传文件：先落盘，再解析
    List<KnowledgeExtractDTO> saveAndExtractUploadedFile(MultipartFile file) throws Exception;

    // 批量初始化：扫描本地 RawData 目录并解析所有合法文件
    List<KnowledgeExtractDTO> initDataFromRawDataDirectory() throws Exception;
}