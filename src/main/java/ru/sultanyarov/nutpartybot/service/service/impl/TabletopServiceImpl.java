package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sultanyarov.nutpartybot.domain.model.TabletopInfo;
import ru.sultanyarov.nutpartybot.service.service.GoogleService;
import ru.sultanyarov.nutpartybot.service.service.TabletopService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TabletopServiceImpl implements TabletopService {
    @Value("${bot.google.tabletop-table}")
    private String tabletopTableName;

    private final GoogleService googleService;

    @Override
    public List<TabletopInfo> getAllTabletops() {
        log.info("Get all tabletops from google");
        return googleService.getTabletopsFromTable(tabletopTableName).stream().toList();
    }
}
