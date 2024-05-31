package ru.sultanyarov.nutpartybot.domain.migration;

import com.mongodb.reactivestreams.client.MongoDatabase;
import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;
import reactor.core.publisher.BaseSubscriber;

@ChangeUnit(id = "drop-change-unit", order = "000", author = "v.sultanyarov")
public class DropDbChangeUnit {
    @BeforeExecution
    public void before(MongoDatabase mongoDatabase) {
    }

    @RollbackBeforeExecution
    public void rollbackBefore(MongoDatabase mongoDatabase) {
    }

    @Execution
    public void migrationMethod(MongoDatabase mongoDatabase) {
        mongoDatabase.drop().subscribe(new BaseSubscriber<>() {
        });
    }

    @RollbackExecution
    public void rollback() {
    }
}
