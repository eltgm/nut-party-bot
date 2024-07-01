package ru.sultanyarov.nutpartybot.service.service;

import ru.sultanyarov.nutpartybot.domain.model.TabletopInfo;

import java.util.List;

/**
 * Service for work with tabletop games
 */
public interface TabletopService {

    /**
     * Getting all table tops
     *
     * @return {@link List} of {@link TabletopInfo}
     */
    List<TabletopInfo> getAllTabletops();
}
