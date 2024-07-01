package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;
import ru.sultanyarov.nutpartybot.domain.model.PollType;

@Repository
public interface PollInfoCollectionRepository extends ReactiveMongoRepository<PollInfoDocument, PollType> {
}
