package ru.sultanyarov.nutpartybot.application.config;

import com.mongodb.reactivestreams.client.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.reactivestreams.ReactiveStreamsMongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@ConditionalOnProperty(prefix = "bot.scheduling", name = "enable-scheduled-task", havingValue = "true")
public class SchedulingConfiguration {
    @Bean
    public LockProvider lockProvider(MongoClient mongo) {
        return new ReactiveStreamsMongoLockProvider(mongo.getDatabase("nut-party-db"));
    }
}
