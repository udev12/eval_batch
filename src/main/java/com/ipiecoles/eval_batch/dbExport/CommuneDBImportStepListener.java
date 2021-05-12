package com.ipiecoles.eval_batch.dbExport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Classe qui implémente l'interface "StepExecutionListener".
 * On distingue les méthodes "beforeStep()" et "afterStep()".
 */
public class CommuneDBImportStepListener implements StepExecutionListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Before step DB Import");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("After step DB Import");
        logger.info(stepExecution.getSummary());
        return ExitStatus.COMPLETED;
    }
}
