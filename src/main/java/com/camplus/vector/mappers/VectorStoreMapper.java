package com.camplus.vector.mappers;

import com.camplus.vector.pojo.VectorSearchResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface VectorStoreMapper {

    List<Map<String, Object>> searchFaqVectors();

    List<Map<String, Object>> searchKnowledgeVectors();

    List<Map<String, Object>> getAllVectorsFromTable(@Param("tableName") String tableName);

    int insertFaqVector(@Param("faqId") Integer faqId,
                        @Param("question") String question,
                        @Param("answer") String answer,
                        @Param("questionEmbedding") byte[] questionEmbedding,
                        @Param("answerEmbedding") byte[] answerEmbedding,
                        @Param("combinedEmbedding") byte[] combinedEmbedding,
                        @Param("sparseEmbedding") String sparseEmbedding);

    int insertKnowledgeVector(@Param("docId") Integer docId,
                              @Param("chunkIndex") Integer chunkIndex,
                              @Param("chunkContent") String chunkContent,
                              @Param("chunkEmbedding") byte[] chunkEmbedding,
                              @Param("chunkMetadata") String chunkMetadata,
                              @Param("sparseEmbedding") String sparseEmbedding);

    int deleteFaqVector(@Param("faqId") Integer faqId);

    int deleteKnowledgeVector(@Param("docId") Integer docId);
}
