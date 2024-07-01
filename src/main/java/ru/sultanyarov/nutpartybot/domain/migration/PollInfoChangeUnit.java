package ru.sultanyarov.nutpartybot.domain.migration;

import com.mongodb.reactivestreams.client.MongoDatabase;
import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.BaseSubscriber;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;

import java.util.List;

import static ru.sultanyarov.nutpartybot.domain.model.PollType.ACTIVITY_POLL_DOCUMENT_NAME;
import static ru.sultanyarov.nutpartybot.domain.model.PollType.DATE_POLL_DOCUMENT_NAME;

@ChangeUnit(id = "poll-info-change-unit", order = "001", author = "v.sultanyarov")
public class PollInfoChangeUnit {

    @BeforeExecution
    public void before(MongoDatabase mongoDatabase) {
        mongoDatabase.createCollection("poll_info")
                .subscribe(new BaseSubscriber<>() {
                });
    }

    @RollbackBeforeExecution
    public void rollbackBefore(MongoDatabase mongoDatabase) {
        mongoDatabase.getCollection("poll_info")
                .drop()
                .subscribe(new BaseSubscriber<>() {
                });
    }

    @Execution
    public void migrationMethod(ReactiveMongoTemplate template) {
        template.insertAll(getPollInfoDocuments()).blockLast();
    }

    @RollbackExecution
    public void rollback() {
    }

    private List<PollInfoDocument> getPollInfoDocuments() {
        var dateDocument = new PollInfoDocument();
        dateDocument.setName(DATE_POLL_DOCUMENT_NAME);
        dateDocument.setQuestion("Когда собираемся?");
        dateDocument.setAnswers(List.of("ПТ", "СБ", "ВС", "СКИП"));

        var activityDocument = new PollInfoDocument();
        activityDocument.setName(ACTIVITY_POLL_DOCUMENT_NAME);
        activityDocument.setQuestion("Что делаем?");
        activityDocument.setAnswers(List.of("Кино", "Настолки"));

        return List.of(dateDocument, activityDocument);
    }
}
