package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;

@Repository
public interface PollCollectionRepository extends ReactiveMongoRepository<PollDocument, Long> {
    Mono<PollDocument> findByIdAndClosedAtIsNull(Long pollId);
}
