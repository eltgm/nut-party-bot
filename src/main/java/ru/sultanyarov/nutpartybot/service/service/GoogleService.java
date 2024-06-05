package ru.sultanyarov.nutpartybot.service.service;

import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;

import java.util.List;

/**
 * The service for work with Google API
 */
public interface GoogleService {
    /**
     * Getting all films from tables
     * @param tablesId of {@link List<String>} ids of tables
     * @return films list
     */
    List<MovieInfo> getFilmsFromTables(List<String> tablesId);
}
