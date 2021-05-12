package com.ipiecoles.eval_batch.csvImport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Classe qui implémente l'interface "StepExecutionListener".
 * On compte deux méthodes : la méthode "beforeStep()" et la méthode "afterStep()".
 */
public class CommuneCSVImportStepListener implements StepExecutionListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Before step CSV Import");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("After step CSV Import");
        logger.info(stepExecution.getSummary());
        return ExitStatus.COMPLETED;
    }
}
