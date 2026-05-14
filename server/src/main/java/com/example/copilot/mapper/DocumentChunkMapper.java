package com.example.copilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.copilot.entity.DocumentChunk;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {

    @Insert("""
            INSERT INTO document_chunks (
                document_id,
                knowledge_base_id,
                chunk_index,
                content,
                token_count,
                embedding,
                metadata_json,
                created_at
            ) VALUES (
                #{documentId},
                #{knowledgeBaseId},
                #{chunkIndex},
                #{content},
                #{tokenCount},
                CAST(#{embedding} AS vector),
                #{metadataJson},
                #{createdAt}
            )
            """)
    int insertWithEmbedding(DocumentChunk chunk);

    @Select("""
            SELECT id, document_id, knowledge_base_id, chunk_index, content, token_count, embedding, metadata_json, created_at
            FROM document_chunks
            WHERE knowledge_base_id = #{knowledgeBaseId}
              AND embedding IS NOT NULL
            ORDER BY embedding <=> CAST(#{queryVector} AS vector)
            LIMIT #{topK}
            """)
    List<DocumentChunk> selectTopKByEmbedding(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                              @Param("queryVector") String queryVector,
                                              @Param("topK") int topK);
}
