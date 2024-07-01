package ru.sultanyarov.nutpartybot.service.service.impl;

import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sultanyarov.nutpartybot.application.config.GoogleSpreadSheetProperties;
import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;
import ru.sultanyarov.nutpartybot.domain.model.TabletopInfo;
import ru.sultanyarov.nutpartybot.service.service.GoogleService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleServiceImpl implements GoogleService {
    private final Sheets sheets;
    private final GoogleSpreadSheetProperties googleSpreadSheetProperties;

    @Override
    public Set<MovieInfo> getFilmsFromTables(List<String> tablesName) {
        log.info("Getting films from tables: {}", tablesName);

        Set<MovieInfo> movies = new HashSet<>();
        for (String tableName : tablesName) {
            int rowNumber = 1;
            List<MovieInfo> movieInfos;

            while ((movieInfos = getMoviesInfos(tableName, rowNumber)) != null) {
                movies.addAll(movieInfos);
                rowNumber += 49;
            }
        }

        return movies;
    }

    @Override
    public Set<TabletopInfo> getTabletopsFromTable(String tableName) {
        Set<TabletopInfo> tabletopInfos = new HashSet<>();
        int rowNumber = 5;
        List<TabletopInfo> tabletopsList;

        while ((tabletopsList = getTabletopsInfo(tableName, rowNumber)) != null) {
            tabletopInfos.addAll(tabletopsList);
            rowNumber += 49;
        }

        return tabletopInfos;
    }

    private List<MovieInfo> getMoviesInfos(String tableName, Integer rowNumber) {
        var watchedFilmFlagId = 0;
        var filmNameId = 2;

        List<List<Object>> filmDto;
        try {
            filmDto = sheets.spreadsheets()
                    .values()
                    .batchGet(googleSpreadSheetProperties.getSpreadsheetId())
                    .setRanges(List.of(String.format("%s!A%d:C%d", tableName, rowNumber, rowNumber + 49)))
                    .execute()
                    .getValueRanges()
                    .get(0)
                    .getValues();
        } catch (IOException e) {
            log.error("Error while reading spreadsheet", e);
            throw new RuntimeException(e);
        }

        if (filmDto == null) {
            return null;
        }

        return filmDto.stream()
                .map(filmInfo -> new MovieInfo(Boolean.parseBoolean((String) filmInfo.get(watchedFilmFlagId)), (String) filmInfo.get(filmNameId)))
                .toList();
    }

    private List<TabletopInfo> getTabletopsInfo(String tableName, Integer rowNumber) {
        var tabletopNameId = 0;
        var markId = 5;
        var inStockId = 6;

        List<List<Object>> tabletopDtos;
        try {
            tabletopDtos = sheets.spreadsheets()
                    .values()
                    .batchGet(googleSpreadSheetProperties.getSpreadsheetId())
                    .setRanges(List.of(String.format("%s!A%d:G%d", tableName, rowNumber, rowNumber + 49)))
                    .execute()
                    .getValueRanges()
                    .get(0)
                    .getValues();
        } catch (IOException e) {
            log.error("Error while reading spreadsheet", e);
            throw new RuntimeException(e);
        }

        if (tabletopDtos == null) {
            return null;
        }

        return tabletopDtos.stream()
                .map(tabletopDto -> new TabletopInfo(
                        (String) tabletopDto.get(tabletopNameId),
                        Double.valueOf(((String) tabletopDto.get(markId)).replace(",", ".")),
                        Boolean.parseBoolean((String) tabletopDto.get(inStockId))
                ))
                .toList();
    }
}
