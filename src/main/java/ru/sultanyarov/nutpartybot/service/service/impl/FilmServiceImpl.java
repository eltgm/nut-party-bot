package ru.sultanyarov.nutpartybot.service.service.impl;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.application.config.ChannelsProperties;
import ru.sultanyarov.nutpartybot.application.config.GoogleSpreadSheetProperties;
import ru.sultanyarov.nutpartybot.domain.entity.FilmDocument;
import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;
import ru.sultanyarov.nutpartybot.domain.repository.FilmCollectionRepository;
import ru.sultanyarov.nutpartybot.service.service.FilmService;
import ru.sultanyarov.nutpartybot.service.service.GoogleService;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmCollectionRepository filmCollectionRepository;
    private final TelegramClient telegramClient;
    private final ChannelsProperties channelsProperties;
    private final GoogleService googleService;
    private final GoogleSpreadSheetProperties googleSpreadSheetProperties;

    @Override
    public void addFilmWithNotification(String filmName) {
        log.info("Add film with name: {}", filmName);
        filmCollectionRepository.save(new FilmDocument(filmName))
                .subscribe(filmDocument -> {
                    try {
                        telegramClient.execute(SendMessage.builder()
                                .chatId(channelsProperties.getAdmin())
                                .text(String.format("Добавь '%s' в список фильмов!", filmDocument.getName()))
                                .build());
                    } catch (TelegramApiException e) {
                        log.error("Error while save film", e);
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Flux<String> getActualFilms() {
        log.info("Get actual films");
        return filmCollectionRepository.findAll()
                .map(FilmDocument::getName);
    }

    @Override
    public void actualizeFilms() {
        log.info("Start actual film collection");
        Set<String> filmsFromTables = googleService.getFilmsFromTables(googleSpreadSheetProperties.getTables())
                .stream()
                .filter(movieInfo -> !movieInfo.hasViewed())
                .map(MovieInfo::name)
                .collect(Collectors.toSet());
        Flux<String> allFilms = filmCollectionRepository.findAll()
                .map(FilmDocument::getName);

        allFilms.collect(Collectors.toSet())
                .flatMap((Function<Set<String>, Mono<Set<String>>>) filmDocuments -> {
                    Sets.SetView<String> oldFilmsToRemove = Sets.difference(filmDocuments, filmsFromTables);
                    return Mono.just(oldFilmsToRemove);
                })
                .flatMap(filmCollectionRepository::deleteAllById)
                .subscribe();

        allFilms.collect(Collectors.toSet())
                .flatMap((Function<Set<String>, Mono<Set<String>>>) filmDocuments -> {
                    Sets.SetView<String> newFilmsToAdd = Sets.difference(filmsFromTables, filmDocuments);
                    return Mono.just(newFilmsToAdd);
                })
                .flatMapMany(Flux::fromIterable)
                .map(FilmDocument::new)
                .flatMap(filmCollectionRepository::save)
                .subscribe();
    }
}
