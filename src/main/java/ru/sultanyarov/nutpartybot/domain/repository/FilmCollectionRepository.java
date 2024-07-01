package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.FilmDocument;

public interface FilmCollectionRepository extends ReactiveMongoRepository<FilmDocument, String> {

    @Aggregation(
            value = """
                        {$sample: {size: 1}}
                    """
    )
    Mono<FilmDocument> randomFilmName();
}
