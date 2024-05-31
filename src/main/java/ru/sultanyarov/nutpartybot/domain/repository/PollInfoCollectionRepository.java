package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;

@Repository
public interface PollInfoCollectionRepository extends ReactiveMongoRepository<PollInfoDocument, String> {
}
