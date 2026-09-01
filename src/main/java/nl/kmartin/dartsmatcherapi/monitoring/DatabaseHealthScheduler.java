package nl.kmartin.dartsmatcherapi.monitoring;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically checks whether the MongoDB database is reachable.
 */
@Component
public class DatabaseHealthScheduler {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthScheduler.class);

    private final MongoTemplate mongoTemplate;

    public DatabaseHealthScheduler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Pings the MongoDB database every day at 3 AM and 3 PM.
     */
    @Scheduled(cron = "0 0 3,15 * * *")
    public void pingDatabase() {
        try {
            // Get the configured MongoDB database.
            MongoDatabase database = mongoTemplate.getDb();

            // Execute MongoDB's ping command to verify that the database is reachable.
            database.runCommand(new Document("ping", 1));

            logger.info("MongoDB ping successful.");
        } catch (Exception exception) {
            // Log the failure without interrupting future scheduled health checks.
            logger.error("MongoDB ping failed.", exception);
        }
    }
}