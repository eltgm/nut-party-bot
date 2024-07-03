package ru.sultanyarov.nutpartybot.service.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.FilmDocument;

/**
 * The service to work with request from tg bot
 */
public interface FilmService {

    /**
     * Adding film to DB and send notification to admin
     *
     * @param filmName film name
     * @param chatId   chat id to result message
     */
    void addFilmWithNotification(String filmName, Long chatId);

    /**
     * Getting list of actual films
     *
     * @return type of {@link Flux<String>}
     */
    Flux<String> getActualFilms();

    /**
     * Updating films in DB
     */
    void actualizeFilms();

    /**
     * Getting random film name
     *
     * @return {@link Mono} of {@link FilmDocument}
     */
    Mono<FilmDocument> getRandomFilmName();
}
