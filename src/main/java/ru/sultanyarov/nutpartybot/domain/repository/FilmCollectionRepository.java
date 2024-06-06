package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import ru.sultanyarov.nutpartybot.domain.entity.FilmDocument;

public interface FilmCollectionRepository extends ReactiveMongoRepository<FilmDocument, String> {
}
