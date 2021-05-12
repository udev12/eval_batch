package com.ipiecoles.eval_batch.dbExport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Classe qui permet d'afficher la taille du fichier de sortie.
 */
public class OutputFileSize implements Tasklet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Long fileSize = (Files.size(Path.of("target/test.txt")));
        logger.info("The size of the 'test.txt' file is {} bytes", fileSize);
        return RepeatStatus.FINISHED;
    }
}
