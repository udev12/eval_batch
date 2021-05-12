package com.ipiecoles.eval_batch;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class EvalBatchApplication {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(EvalBatchApplication.class, args);
    }

    public EvalBatchApplication() throws IOException, XmlPullParserException {
        // On affiche les informations générales du projet
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));
        logger.info("General informations about the project :");
        logger.info("Id : {}", model.getId());
        logger.info("Group Id : {}", model.getGroupId());
        logger.info("Artifact Id : {}", model.getArtifactId());
        logger.info("Version : {}", model.getVersion());
    }


}
