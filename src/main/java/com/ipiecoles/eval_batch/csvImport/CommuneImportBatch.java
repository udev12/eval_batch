package com.ipiecoles.eval_batch.csvImport;

import com.ipiecoles.eval_batch.dto.CommuneCSV;
import com.ipiecoles.eval_batch.exception.CommuneCSVException;
import com.ipiecoles.eval_batch.exception.NetworkException;
import com.ipiecoles.eval_batch.model.Commune;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.retry.backoff.FixedBackOffPolicy;

import javax.persistence.EntityManagerFactory;

/**
 * Cette classe de configuration permet d'importer des données concernant les communes de FRANCE à partir d'un fichier CSV.
 * On remplit ensuite la table "commune" avec ces données.
 */
@Configuration
@EnableBatchProcessing
@PropertySource("classpath:application.properties")
public class CommuneImportBatch {
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    // Déclaration de constantes dont les valeurs sont définies dans le fichier "application.properties"

    @Value("${importFile.jobName}")
    private String JOB_NAME;

    @Value("${importFile.stepHelloWorld}")
    private String STEP_HELLO_WORLD;

    @Value("${importFile.stepImportCSV}")
    private String STEP_IMPORT_CSV;

    @Value("${importFile.csvItemReader}")
    private String CSV_ITEM_READER;

    @Value("${importFile.stepGetMissingCoordinates}")
    private String STEP_GET_MISSING_COORDINATES;

    @Value("${importFile.missingCoordinateJpaItemReader}")
    private String MISSING_COORDINATE_JPA_ITEM_READER;

    @Value("${importFile.chunkSize}")
    private Integer CHUNK_SIZE;

    @Value("${importFile.skipLimit}")
    private Integer SKIP_LIMIT;

    @Value("${importFile.linesToSkip}")
    private Integer LINES_TO_SKIP;

    @Value("${importFile.pageSize}")
    private Integer PAGE_SIZE;

    @Value("${importFile.retryLimit}")
    private Integer RETRY_LIMIT;

    @Value("${importFile.setBackOffPeriod}")
    private Integer SET_BACK_OFF_PERIOD;

    /**
     * Méthode principale de notre classe car, c'est le job d'importation du fichier CSV.
     * Elle est constituée d'une step orientée Tasklet pour l'affichage de "Hello World", mais aussi d'une step orientée chunk.
     * Cette dernière copie le contenu du fichier CSV, puis écrit en base de données.
     *
     * @param stepHelloWorld            : step passée en paramètre
     * @param stepImportCSV             : step passée en paramètre
     * @param stepGetMissingCoordinates : step passée en paramètre
     * @return le job construit.
     */
    @Bean
    public Job importCsvJob(Step stepHelloWorld, Step stepImportCSV, Step stepGetMissingCoordinates) {
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(stepHelloWorld)
                .next(stepImportCSV)
//                .next(stepGetMissingCoordinates)
                .end().build();
    }

    /**
     * Step qui appelle la méthode "helloWorldTasklet()".
     *
     * @return la step construite.
     */
    @Bean
    public Step stepHelloWorld() {
        return stepBuilderFactory.get(STEP_HELLO_WORLD)
                .tasklet(helloWorldTasklet())
                .build();
    }

    /**
     * Méthode qui appelle la méthode "execute" de la classe "HelloWorldTasklet".
     *
     * @return un objet de type "Tasklet".
     */
    @Bean
    public Tasklet helloWorldTasklet() {
        return new HelloWorldTasklet();
    }

    /**
     * Cette step permet d'importer les données du fchier CSV.
     * Elle prend en entrée le type "CommuneCSV" (dto) et en sortie le type "Commune" (model).
     * La lecture du fichier CSV se fait grâce à la méthode "communeCSVItemReader()".
     * Les données sont formatées avec la méthode "communeCSVToCommuneProcessor()".
     * L'écriture dans la table "commune" est réalisée grâce à la méthode "writerJPA()".
     * Il y a des listeners pour exécuter des instructions à des moments clés du batch.
     * On prévoit des exceptions en cas de problème au moment de la lecture.
     *
     * @return la step construite.
     */
    @Bean
    public Step stepImportCSV() {
        return stepBuilderFactory.get(STEP_IMPORT_CSV)
                .<CommuneCSV, Commune>chunk(CHUNK_SIZE)
                .reader(communeCSVItemReader())
                .processor(communeCSVToCommuneProcessor())
                .writer(writerJPA())
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .skip(CommuneCSVException.class)
//                .skip(IncorrectTokenCountException.class) // exception copiée dan les logs
                .skip(FlatFileParseException.class) // exception copiée dans les logs
                .listener(communeCSVImportSkipListener())
                .listener(communeCSVImportStepListener())
                .listener(communeCSVImportChunkListener())
                .listener(communeCSVItemReadListener())
                .listener(communeCSVItemWriteListener())
                .build();
    }

    /**
     * Méthode qui permet de lire le fichier CSV et de stocker son contenu.
     *
     * @return un objet de type "FlatFileItemReader".
     */
    @Bean
    public FlatFileItemReader<CommuneCSV> communeCSVItemReader() {
        return new FlatFileItemReaderBuilder<CommuneCSV>()
                .name(CSV_ITEM_READER)
                .linesToSkip(LINES_TO_SKIP)
                .resource(new ClassPathResource("laposte_hexasmal_small.csv"))
                .delimited()
                .delimiter(";")
                .names("codeInsee", "nom", "codePostal", "ligne5", "libelleAcheminement", "coordonneesGPS")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(CommuneCSV.class);
                    }
                })
                .build();
    }

    /**
     * Méthode qui appelle la méthode "process" de la classe "CommuneCSVItemProcessor".
     *
     * @return un objet de type "CommuneCSVItemProcessor".
     */
    @Bean
    public CommuneCSVItemProcessor communeCSVToCommuneProcessor() {
        return new CommuneCSVItemProcessor();
    }

    /**
     * Méthode qui permet d'écire en base de données avec "JpaItemWriterBuilder".
     *
     * @return un objet de type "JpaItemWriter".
     */
    @Bean
    public JpaItemWriter<Commune> writerJPA() {
        return new JpaItemWriterBuilder<Commune>().entityManagerFactory(entityManagerFactory).build();
    }

    /**
     * On instancie la classe "CommuneCSVImportStepListener" qui implémente l'interface "StepExecutionListener".
     *
     * @return un objet de type "StepExecutionListener".
     */
    @Bean
    public StepExecutionListener communeCSVImportStepListener() {
        return new CommuneCSVImportStepListener();
    }

    /**
     * On instancie la classe "CommuneCSVImportChunkListener" qui implémente l'interface "ChunkListener".
     *
     * @return un objet de type "ChunkListener".
     */
    @Bean
    public ChunkListener communeCSVImportChunkListener() {
        return new CommuneCSVImportChunkListener();
    }

    /**
     * On instancie la classe "CommuneCSVImportSkipListener" qui implémente l'interface "SkipListener".
     *
     * @return un objet de type "CommuneCSVImportSkipListener".
     */
    @Bean
    public CommuneCSVImportSkipListener communeCSVImportSkipListener() {
        return new CommuneCSVImportSkipListener();
    }

//    @Bean
//    public CommuneCSVItemListener communeCSVItemListener(){
//        return new CommuneCSVItemListener();
//    }

    /**
     * On instancie la classe "CommuneCSVItemListener" qui implémente les interfaces "ItemReadListener" et "ItemWriteListener".
     *
     * @return un objet de type "ItemReadListener".
     */
    @Bean
    public ItemReadListener<CommuneCSV> communeCSVItemReadListener() {
        return new CommuneCSVItemListener();
    }

    /**
     * On instancie la classe "CommuneCSVItemListener" qui implémente les interfaces "ItemReadListener" et "ItemWriteListener".
     *
     * @return un objet de type "ItemWriteListener".
     */
    @Bean
    public ItemWriteListener<Commune> communeCSVItemWriteListener() {
        return new CommuneCSVItemListener();
    }

    /**
     * Step qui permet d'écrire dans la base de données, plus précisément dans la table "commune".
     * On a le type "Commune" (model) en entrée et en sortie de notre step.
     * Notre step lit grâce à la méthode "communeMissingCoordinateJpaItemReader()", et écrit avec la méthode "writerJPA2()".
     * Le données sont formatées grâce à la méthode "communeMissingCoordinateItemProcessor()".
     * On prévoit des exceptions en cas de problème de communication avec la bdd.
     *
     * @return la step construite.
     */
    @Bean
    public Step stepGetMissingCoordinates() {
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(SET_BACK_OFF_PERIOD);
        return stepBuilderFactory.get(STEP_GET_MISSING_COORDINATES)
                .<Commune, Commune>chunk(CHUNK_SIZE)
                .reader(communeMissingCoordinateJpaItemReader())
                .processor(communeMissingCoordinateItemProcessor())
                .writer(writerJPA2())
                .faultTolerant()
                .retryLimit(RETRY_LIMIT)
                .retry(NetworkException.class)
                .backOffPolicy(policy)
                .build();
    }

    /**
     * Méthode qui permet de lire la base de données.
     *
     * @return un objet de type "JpaPagingItemReader".
     */
    @Bean
    public JpaPagingItemReader<Commune> communeMissingCoordinateJpaItemReader() {
        return new JpaPagingItemReaderBuilder<Commune>()
                .name(MISSING_COORDINATE_JPA_ITEM_READER)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(PAGE_SIZE)
                .queryString("from Commune c where c.latitude is null or c.longitude is null")
                .build();
    }

    /**
     * Méthode qui appelle la méthode "process" de la classe "CommuneMissingCoordinateItemProcessor".
     *
     * @return un objet de type "CommuneMissingCoordinateItemProcessor".
     */
    @Bean
    public CommuneMissingCoordinateItemProcessor communeMissingCoordinateItemProcessor() {
        return new CommuneMissingCoordinateItemProcessor();
    }

    /**
     * Classe qui permet d'écrire dans la base de données avec "JpaItemWriterBuilder".
     *
     * @return un objet de type "JpaItemWriter".
     */
    @Bean
    public JpaItemWriter<Commune> writerJPA2() {
        return new JpaItemWriterBuilder<Commune>().entityManagerFactory(entityManagerFactory).build();
    }
}
