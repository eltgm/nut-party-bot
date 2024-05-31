package ru.sultanyarov.nutpartybot.domain.migration;

import com.mongodb.reactivestreams.client.MongoDatabase;
import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.BaseSubscriber;

@ChangeUnit(id = "poll-change-unit", order = "002", author = "v.sultanyarov")
public class PollChangeUnit {
    @BeforeExecution
    public void before(MongoDatabase mongoDatabase) {
        mongoDatabase.createCollection("poll")
                .subscribe(new BaseSubscriber<>() {
                });
    }

    @RollbackBeforeExecution
    public void rollbackBefore(MongoDatabase mongoDatabase) {
        mongoDatabase.getCollection("poll")
                .drop()
                .subscribe(new BaseSubscriber<>() {
                });
    }

    @Execution
    public void migrationMethod(ReactiveMongoTemplate template) {
    }

    @RollbackExecution
    public void rollback() {
    }
}
