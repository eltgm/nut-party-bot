package ru.sultanyarov.nutpartybot.application.config;

import com.mongodb.reactivestreams.client.MongoClient;
import io.mongock.driver.mongodb.reactive.driver.MongoReactiveDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfiguration {

    @Bean
    public MongoReactiveDriver connectionDriver(MongoClient mongoClient) {
        return MongoReactiveDriver.withDefaultLock(mongoClient, "nut-party-db");
    }
}
