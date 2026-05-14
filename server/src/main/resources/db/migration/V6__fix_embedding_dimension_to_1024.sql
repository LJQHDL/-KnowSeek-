DROP INDEX IF EXISTS idx_document_chunks_embedding_ivfflat;

ALTER TABLE document_chunks
    ALTER COLUMN embedding TYPE vector(1024)
    USING CASE
        WHEN embedding IS NULL THEN NULL
        ELSE CAST(embedding AS vector(1024))
    END;

CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding_ivfflat
    ON document_chunks USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
