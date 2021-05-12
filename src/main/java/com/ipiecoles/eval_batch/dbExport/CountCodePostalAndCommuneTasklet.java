package com.ipiecoles.eval_batch.dbExport;

import com.ipiecoles.eval_batch.repository.CommuneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class CountCodePostalAndCommuneTasklet implements Tasklet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CommuneRepository communeRepository;

    /**
     * La méthode "execute" est la seule méthode de notre Tasklet.
     * Elle interroge la base de données, récupère le nombre de codes postaux et le nombre de communes, puis sauvegarde ces
     * données pour les transférer à la step "stepGetDataFromDB".
     *
     * @param contribution
     * @param chunkContext
     * @return le statut terminé.
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        // On récupère le nombre de codes postaux dans la table "Commune"
        Long nbZipCodes = communeRepository.countDistinctCodePostal();

        // On récupère le nombre de communes dans la table "Commune"
        Long nbCities = communeRepository.countDistinctNom();

        // On sauvegarde la nombre de codes postaux en vue d'une utilisation future dans la step "stepGetDataFromDB"
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("nbZipCodes", nbZipCodes);

        // On sauvegarde la nombre de communes en vue d'une utilisation future dans la step "stepGetDataFromDB"
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("nbCities", nbCities);

        // On informe l'utilisateur : nombre de codes postaux et nombre de communes transmis à la step suivante
        logger.info("Passing '" + nbZipCodes + "' to next step");
        logger.info("Passing '" + nbCities + "' to next step");

        // On renvoie le statut "FINISHED" pour sortir de la Tasklet
        return RepeatStatus.FINISHED;
    }
}
