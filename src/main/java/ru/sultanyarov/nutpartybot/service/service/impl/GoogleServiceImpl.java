package ru.sultanyarov.nutpartybot.service.service.impl;

import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sultanyarov.nutpartybot.application.config.GoogleSpreadSheetProperties;
import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;
import ru.sultanyarov.nutpartybot.service.service.GoogleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleServiceImpl implements GoogleService {
    private final Integer WATCHED_FILM_FLAG_ID = 0;
    private final Integer FILM_NAME_ID = 2;

    private final Sheets sheets;
    private final GoogleSpreadSheetProperties googleSpreadSheetProperties;

    @Override
    public List<MovieInfo> getFilmsFromTables(List<String> tablesName) {
        log.info("Getting films from tables: {}", tablesName);

        List<MovieInfo> movies = new ArrayList<>();
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

    private List<MovieInfo> getMoviesInfos(String tableName, Integer rowNumber) {
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
                .map(filmInfo -> new MovieInfo(Boolean.parseBoolean((String) filmInfo.get(WATCHED_FILM_FLAG_ID)), (String) filmInfo.get(FILM_NAME_ID)))
                .toList();
    }
}
