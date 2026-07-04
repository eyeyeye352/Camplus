package com.camplus.vector.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface KnowledgeDocMapper {

    int insertKnowledgeDoc(Map<String, Object> params);

    @Select("SELECT COUNT(*) FROM knowledge_docs")
    int countAll();
}
