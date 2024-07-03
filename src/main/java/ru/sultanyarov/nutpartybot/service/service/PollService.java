package ru.sultanyarov.nutpartybot.service.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.model.PollType;

import java.util.List;
import java.util.Map;

/**
 * The service for work with polls
 */
public interface PollService {

    /**
     * Create specific poll in concrete chat
     *
     * @param type   value of {@link PollType}
     * @param chatId id of the tg chat
     */
    void createPoll(PollType type, Long chatId, Boolean isAdmin);

    /**
     * @param type    type of poll {@link PollType}
     * @param votes   custom votes if necessary
     * @param chatsId id of chats to message for
     */
    void createPolls(PollType type, List<Long> chatsId, List<Integer> votes, Boolean isAdmin);

    /**
     * Update poll results
     *
     * @param pollId id of th poll
     * @param votes  votes from user
     */
    void updatePollResults(Long pollId, List<Integer> votes);

    /**
     * Close poll
     *
     * @param pollId       id of poll to close
     * @param isPartyStart is party start flag
     */
    void closePoll(Long pollId, boolean isPartyStart);

    /**
     * Get poll results
     *
     * @param pollId id of poll
     * @return poll results
     */
    Mono<Map<String, Integer>> getPollResult(Long pollId);

    /**
     * Get poll
     *
     * @param pollId id of poll
     * @return {@link Mono} of {@link PollDocument}
     */
    Mono<PollDocument> getPoll(Long pollId);

    /**
     * Get all active polls
     *
     * @return {@link Flux} of {@link PollDocument}
     */
    Flux<PollDocument> getActivePolls();

    /**
     * Get poll answers
     *
     * @param pollId poll id
     * @return {@link Mono} of {@link List} {@link String}
     */
    Mono<List<String>> getPollAnswers(Long pollId);

    /**
     * Send activity poll
     */
    void sendActivityPolls();
}
