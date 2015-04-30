package uk.ac.ebi.pride.proteomes.pipeline.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * @author ntoro
 * @since 13/04/15 11:00
 */
public class ChunkListener extends ChunkListenerSupport {

    private static final Log log = LogFactory.getLog(ChunkListener.class);


    public void beforeChunk(ChunkContext chunkContext) {
        log.debug(chunkContext.toString());
    }

    public void afterChunk(ChunkContext chunkContext) {
        log.debug(chunkContext.toString());
    }

    public void afterChunkError(ChunkContext chunkContext) {
        log.error(chunkContext.toString());
    }
}
