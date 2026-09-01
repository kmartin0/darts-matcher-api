package nl.kmartin.dartsmatcherapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Configures MongoDB transaction support.
 */
@Configuration
public class MongoConfig {

    /**
     * Creates the transaction manager used for MongoDB transactions.
     *
     * @param databaseFactory the MongoDB database factory
     * @return the MongoDB transaction manager
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }
}