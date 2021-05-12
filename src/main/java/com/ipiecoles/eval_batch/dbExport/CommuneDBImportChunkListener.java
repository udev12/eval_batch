package com.ipiecoles.eval_batch.dbExport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * Classe qui implémente l'interface "ChunkListener".
 * On distingue les méthodes "beforeChunk()", "afterChunk()" et "afterChunkError()".
 */
public class CommuneDBImportChunkListener implements ChunkListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
        logger.info("Before Chunk DB Import");
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        logger.info("After Chunk DB Import");
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        logger.error("After Chunk Error DB Import");
    }
}
