package ru.sultanyarov.nutpartybot.service.facade;

import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;

/**
 * The service for working with tg
 */
public interface TelegramFacade {
    /**
     * Sending start message
     *
     * @param chatId chat to sending keyboard
     */
    void sendStartMessage(Long chatId);

    /**
     * Adding new film
     *
     * @param filmName name of adding film
     */
    void addFilm(String filmName);

    /**
     * Getting all films
     *
     * @param chatId id of chat in which films list will be sent
     */
    void getFilms(Long chatId);

    /**
     * Booking sleep place
     *
     * @param chatId   id of chat for booking result
     * @param userName name of user which booked place
     */
    void bookPlace(Long chatId, String userName);

    /**
     * Reset booking sleep place
     *
     * @param chatId   id of chat for unbooking result
     * @param userName name of user which unbooked place
     */
    void unBookPlace(Long chatId, String userName);

    /**
     * Send message with menu
     *
     * @param chatId id of chat for menu
     */
    void showMenu(Long chatId);

    /**
     * Send error
     *
     * @param chatId id of chat for sending error
     */
    void sendError(Long chatId);

    /**
     * Process date poll result
     *
     * @param pollAnswer poll result
     */
    void processDatePollResults(PollAnswer pollAnswer);

    /**
     * Send end poll notification
     */
    void sendEndPollNotification();

    /**
     * Collect poll results
     */
    void collectPollResults();

    /**
     * Update date poll results
     *
     * @param pollAnswer poll result
     */
    void updateDatePollResults(PollAnswer pollAnswer);

    /**
     * Update activity poll results
     *
     * @param pollAnswer poll results
     */
    void updateActivityPollResults(PollAnswer pollAnswer);

    /**
     * Get poll
     *
     * @param pollId poll id
     * @return {@link Mono} of {@link PollDocument}
     */
    Mono<PollDocument> getPoll(Long pollId);

    /**
     * Getting all tabletop games
     *
     * @param chatId id of chat for sending tabletop
     */
    void getTabletops(Long chatId);
}
