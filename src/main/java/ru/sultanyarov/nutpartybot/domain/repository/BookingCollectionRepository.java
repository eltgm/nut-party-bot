package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.BookingDocument;

public interface BookingCollectionRepository extends ReactiveMongoRepository<BookingDocument, String> {
    Mono<BookingDocument> findTopByUserName(String userName);
}
