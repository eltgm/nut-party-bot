package ru.sultanyarov.nutpartybot.domain.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.model.PollType;

import java.time.LocalDate;

@Repository
public interface PollCollectionRepository extends ReactiveMongoRepository<PollDocument, Long> {
    Mono<PollDocument> findByIdAndClosedAtIsNull(Long pollId);

    Flux<PollDocument> getAllByClosedAtIsNull();

    @Aggregation(value =
            {
                    """
                        {
                            $addFields: {"creationDate": {$dateToString: {format: "%Y-%m-%d", date: "$createdAt"}}}
                        }
                    """,
                    """
                        {
                            $match: {
                                $and: [
                                    {creationDate: {$eq: ?0}},
                                    {type: {$eq: ?2}},
                                    {isAdmin: {$eq: ?1}},
                                    {closedAt: {$ne: null}},
                                    {isPartyStart: {$eq: true}},
                                ]
                            }
                        }
                                """
            }
    )
    Flux<PollDocument> findAllByCreatedAtAndIsAdminAndPollTypeAndClosed(LocalDate createdAt, Boolean isAdmin, PollType pollType);
}
