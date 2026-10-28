package io.github.nastiasydorchuk.studentportal.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Fills empty collections from classpath:seed/<collection>.json, so a fresh database (Docker, CI,
// a reviewer's machine) starts with the English content. Collections that already hold data are
// never touched. Translations aren't seeded: they're generated and cached on first request.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedDataLoader implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(SeedDataLoader.class);
    private static final String SEED_FILES = "classpath:seed/*.json";

    private final MongoTemplate mongoTemplate;

    public SeedDataLoader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        for (Resource file : new PathMatchingResourcePatternResolver().getResources(SEED_FILES)) {
            String collection = StringUtils.stripFilenameExtension(file.getFilename());
            if (mongoTemplate.count(new Query(), collection) > 0) {
                continue;
            }
            List<Document> documents = parse(file);
            mongoTemplate.insert(documents, collection);
            logger.info("Seeded empty collection '{}' with {} document(s)", collection, documents.size());
        }
    }

    // Files are JSON arrays as exported by Compass/mongoexport; extended JSON such as {"$oid": ...} is kept
    static List<Document> parse(Resource file) throws IOException {
        String json = file.getContentAsString(StandardCharsets.UTF_8);
        return Document.parse("{\"documents\": " + json + "}").getList("documents", Document.class);
    }
}
