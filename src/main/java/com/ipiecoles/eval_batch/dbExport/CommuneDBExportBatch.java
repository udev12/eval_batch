package com.ipiecoles.eval_batch.dbExport;

import com.ipiecoles.eval_batch.dto.CommuneTXT;
import com.ipiecoles.eval_batch.exception.CommuneCSVException;
import com.ipiecoles.eval_batch.exception.NetworkException;
import com.ipiecoles.eval_batch.model.Commune;
import com.ipiecoles.eval_batch.repository.CommuneRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe de configuration permet de récupérer les données contenues dans la table "commune" (base "testdb" pour H2 / test_batch pour MySQL).
 * Ces données sont ensuite formatées, puis écrites dans le fichier "test.txt" (cf. /target/test.txt).
 * La classe "CommuneDBExportBatch" comprend principalement :
 * - un job : le job "exportCommunes",
 * - trois steps : la step "stepCountCodePostalAndCommune", la step "stepGetDataFromDB" et la step "stepOutputFileSize".
 */
@Configuration
@PropertySource("classpath:application.properties")
public class CommuneDBExportBatch {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public CommuneRepository communeRepository;

    // Déclaration de constantes dont les valeurs sont définies dans le fichier "application.properties"

    @Value("${exportFile.jobName}")
    private String JOB_NAME;

    @Value("${exportFile.stepCountCodePostalAndCommune}")
    private String STEP_COUNT_CP_AND_COMMUNE;

    @Value("${exportFile.stepGetDataFromDB}")
    private String STEP_GET_DATA_FROM_DB;

    @Value("${exportFile.chunkSize}")
    private Integer CHUNK_SIZE;

    @Value("${exportFile.skipLimit}")
    private Integer SKIP_LIMIT;

    @Value("${exportFile.mapId}")
    private String MAP_ID;

    @Value("${exportFile.targetPath}")
    public String TARGET_PATH;

    /**
     * La méthode "exportCommunes" est la méthode principale de notre classe.
     * Notre job va, via deux steps, calculer et stocker le nombre de codes postaux et de communes, récupérer les données de la bdd,
     * puis écrire ces données dans un fichier texte après formatage.
     * Le job est stocké dans le "JobRepository".
     * Pour pouvoir utiliser le "jobBuilderFactory" une deuxième fois (en plus de la classe "CommuneImportBatch"),
     * on ajoute l'annotation "@Qualifier("exportCommunes")".
     *
     * @param stepCountCodePostalAndCommune : première sep du job.
     * @param stepGetDataFromDB             : deuxième step du job.
     * @return le job construit.
     */
    @Bean
    @Qualifier("exportCommunes")
    public Job exportCommunes(Step stepCountCodePostalAndCommune, Step stepGetDataFromDB) {
//        return jobBuilderFactory.get("exportCommunes")
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(stepCountCodePostalAndCommune)
                .next(stepGetDataFromDB)
                .next(stepOutputFileSize())
                .end().build();
    }

    /**
     * Cette step est orientée Tasklet, afin de réaliser un traitement unitaire.
     * Elle appelle la méthode "countCodePostalAndCommuneTasklet()".
     *
     * @return la step construite.
     */
    @Bean
    public Step stepCountCodePostalAndCommune() {
        return stepBuilderFactory.get(STEP_COUNT_CP_AND_COMMUNE)
                .tasklet(countCodePostalAndCommuneTasklet())
                .build();
    }

    /**
     * Ici, on va faire appel à la méthode "exécute" de la classe "CountCodePostalAndCommuneTasklet"
     * qui implémente l'interface "Tasklet".
     *
     * @return un objet de type Tasklet.
     */
    @Bean
    public Tasklet countCodePostalAndCommuneTasklet() {
        return new CountCodePostalAndCommuneTasklet();
    }

    /**
     * La step "stepGetDataFromDB" est la step maîtresse de notre classe. C'est elle qui va lire dans la base de données (table
     * "commune"), puis écrire dans le fichier "test.txt" après formatage.
     * Elle prend en entrée le type "Commune" (model) et en sortie le type "CommuneTXT" (dto).
     * Notre step lit grâce à la méthode "repositoryItemReader()", et écrit avec la méthode "sendDataToFile()".
     * Il y a des listeners pour exécuter des instructions à des moments clés du batch.
     * On prévoit des exceptiosn en cas de problème de lecture, d'écriture ou de communication avec la bdd.
     *
     * @return la step construite.
     */
    @Bean
    public Step stepGetDataFromDB() {
        return stepBuilderFactory.get(STEP_GET_DATA_FROM_DB)
                .<Commune, CommuneTXT>chunk(CHUNK_SIZE)
                .reader(repositoryItemReader())
                .writer(sendDataToFile())
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .skip(CommuneCSVException.class)
                .skip(FlatFileParseException.class)
                .retry(NetworkException.class)
                .listener(communeDBImportStepListener())
                .listener(communeDBImportChunkListener())
                .listener(communeDBItemReadListener())
                .listener(communeDBItemWriteListener())
                .build();
    }

    /**
     * Notre méthode lit dans la table "commune", puis stocke les données lues.
     * On fait appel à la méthode "findAll()" de "CommuneRepository".
     * Les codes INSEE sont triés par ordre croissant.
     *
     * @return un objet de type "RepositoryItemReader".
     */
    @Bean
    public RepositoryItemReader<Commune> repositoryItemReader() {
        Map<String, Sort.Direction> map = new HashMap<>();
        map.put(MAP_ID, Sort.Direction.ASC);
        RepositoryItemReader<Commune> repositoryItemReader = new RepositoryItemReader<>();
        repositoryItemReader.setRepository(communeRepository);
        repositoryItemReader.setPageSize(1);
        repositoryItemReader.setMethodName("findAll");
        repositoryItemReader.setSort(map);
        return repositoryItemReader;

    }

    /**
     * L'écriture dans notre fichier texte (target/test.txt) va se faire ici.
     * Les données lues par la méthode "repositoryItemReader()" sont formatées avant d'être écrites.
     * On écrit en entête, le nombre de codes postayx et en pied de page, le nombre de communes.
     *
     * @return un objet de type "FlatFileItemWriter".
     */
    @Bean
    public FlatFileItemWriter<CommuneTXT> sendDataToFile() {
        // On crée l'instance writer
        FlatFileItemWriter<CommuneTXT> writer = new FlatFileItemWriter<>();

        // On définit l'emplacement du fichier de sortie
        writer.setResource(new FileSystemResource(TARGET_PATH));

        // Le fichier de sortie est intégralement réécrit à chaque exécution de la méthode "sendDataToFile()"
        writer.setAppendAllowed(false);

        // On formate les données à écrire ici
        writer.setLineAggregator(new FormatterLineAggregator<CommuneTXT>() {
            {
                setFormat("%5s - %5s - %s : %.5f %.5f");
                setFieldExtractor(new BeanWrapperFieldExtractor<CommuneTXT>() {
                    {
                        setNames(new String[]{"codePostal", "codeInsee", "nom", "latitude", "longitude"});
                    }
                });
            }
        });

        // On écrit l'entête
        writer.setHeaderCallback(headerCallBack(null));

        // On écrit le pied de page
        writer.setFooterCallback(footerCallBack(null));

        return writer;
    }

    /**
     * Permet d'écire l'entête.
     *
     * @param nbZipCodes : nombre de codes postayx récupéré grâce à la méthode "execute" de la classe "CountCodePostalAndCommuneTasklet".
     * @return un objet de type "FlatFileHeaderCallback".
     */
    @Bean
    @StepScope
    public FlatFileHeaderCallback headerCallBack(@Value("#{jobExecutionContext['nbZipCodes']}") Long nbZipCodes) {
        return writer -> writer.write("Total codes postaux : " + nbZipCodes);
    }

    /**
     * Permet d'écire le pied de page
     *
     * @param nbCities : nombre de communes récupéré grâce à la méthode "execute" de la classe "CountCodePostalAndCommuneTasklet".
     * @return un objet de  type "FlatFileHeaderCallback".
     */
    @Bean
    @StepScope
    public FlatFileFooterCallback footerCallBack(@Value("#{jobExecutionContext['nbCities']}") Long nbCities) {
        return writer -> writer.write("Total communes : " + nbCities);
    }

    /**
     * On instancie la classe "CommuneDBImportStepListener" qui implémente l'interface "StepExecutionListener".
     *
     * @return un objet de type "StepExecutionListener".
     */
    @Bean
    public StepExecutionListener communeDBImportStepListener() {
        return new CommuneDBImportStepListener();
    }

    /**
     * On instancie la classe "CommuneDBImportChunkListener" qui implémente l'interface "ChunkListener".
     *
     * @return un objet de type "ChunkListener".
     */
    @Bean
    public ChunkListener communeDBImportChunkListener() {
        return new CommuneDBImportChunkListener();
    }

//    @Bean
//    public CommuneDBItemListener communeDBItemListener() {
//        return new CommuneDBItemListener();
//    }

    /**
     * On instancie la classe "CommuneDBItemListener" qui implémente les interfaces "ItemReadListener" et "ItemWriteListener"
     *
     * @return un objet de type "ItemReadListener"
     */
    @Bean
    public ItemReadListener<Commune> communeDBItemReadListener() {
        return new CommuneDBItemListener();
    }

    /**
     * On instancie la classe "CommuneDBItemListener" qui implémente les interfaces "ItemReadListener" et "ItemWriteListener"
     *
     * @return un objet de type "ItemWriteListener"
     */
    @Bean
    public ItemWriteListener<CommuneTXT> communeDBItemWriteListener() {
        return new CommuneDBItemListener();
    }

    /**
     * Cette step est orientée Tasklet, afin de réaliser un traitement unitaire.
     * Elle appelle la méthode "outputFileSize".
     *
     * @return la step construite.
     */
    public Step stepOutputFileSize() {
        return stepBuilderFactory.get("stepFileSize")
                .tasklet(outputFileSize())
                .build();
    }

    /**
     * Ici, on va faire appel à la méthode "exécute" de la classe "OutputFileSize" qui implémente l'interface "Tasklet".
     *
     * @return un objet de type Tasklet.
     */
    public Tasklet outputFileSize() {
        return new OutputFileSize();
    }

}
