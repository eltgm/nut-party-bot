package ru.sultanyarov.nutpartybot.service.service;

import reactor.core.publisher.Flux;

/**
 * The service to work with request from tg bot
 */
public interface FilmService {

    /**
     * Adding film to DB and send notification to admin
     *
     * @param filmName film name
     */
    void addFilmWithNotification(String filmName);

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
}
