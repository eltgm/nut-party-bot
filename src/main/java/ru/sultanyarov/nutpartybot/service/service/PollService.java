package ru.sultanyarov.nutpartybot.service.service;

import reactor.core.publisher.Mono;
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
    void createPoll(PollType type, Long chatId);

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
     * @param pollId id of poll to close
     */
    void closePoll(Long pollId);

    /**
     * Get poll results
     *
     * @param pollId id of poll
     * @return poll results
     */
    Mono<Map<String, Integer>> getPollResult(Long pollId);
}
