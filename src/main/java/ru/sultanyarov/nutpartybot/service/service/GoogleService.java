package ru.sultanyarov.nutpartybot.service.service;

import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;
import ru.sultanyarov.nutpartybot.domain.model.TabletopInfo;

import java.util.List;
import java.util.Set;

/**
 * The service for work with Google API
 */
public interface GoogleService {
    /**
     * Getting all films from tables
     *
     * @param tablesId of {@link Set<String>} ids of tables
     * @return films list
     */
    Set<MovieInfo> getFilmsFromTables(List<String> tablesId);

    /**
     * Getting all tabletop from table
     *
     * @return {@link Set} of {@link TabletopInfo}
     */
    Set<TabletopInfo> getTabletopsFromTable(String tableId);
}
