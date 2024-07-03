package ru.sultanyarov.nutpartybot.service.service.impl;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.application.config.ChannelsProperties;
import ru.sultanyarov.nutpartybot.application.config.GoogleSpreadSheetProperties;
import ru.sultanyarov.nutpartybot.domain.entity.FilmDocument;
import ru.sultanyarov.nutpartybot.domain.model.MovieInfo;
import ru.sultanyarov.nutpartybot.domain.repository.FilmCollectionRepository;
import ru.sultanyarov.nutpartybot.service.service.FilmService;
import ru.sultanyarov.nutpartybot.service.service.GoogleService;
import ru.sultanyarov.nutpartybot.service.service.TelegramMessagingService;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmCollectionRepository filmCollectionRepository;
    private final TelegramMessagingService telegramMessagingService;
    private final ChannelsProperties channelsProperties;
    private final GoogleService googleService;
    private final GoogleSpreadSheetProperties googleSpreadSheetProperties;

    @Override
    public void addFilmWithNotification(String filmName, Long chatId) {
        log.info("Add film with name: {}", filmName);
        filmCollectionRepository.save(new FilmDocument(filmName))
                .subscribe(filmDocument -> {
                    telegramMessagingService.createAndSendMessage(channelsProperties.getAdmin(), String.format("Добавь '%s' в список фильмов!", filmDocument.getName()));
                    telegramMessagingService.createAndSendMessage(chatId, "Запрос на добавление фильма отправлен!");
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

    @Override
    public Mono<FilmDocument> getRandomFilmName() {
        return filmCollectionRepository.randomFilmName();
    }
}
