package ru.sultanyarov.nutpartybot.application.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sultanyarov.nutpartybot.service.service.FilmService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    @Value("${bot.films-refresh}")
    private String refreshCron;

    private final FilmService filmService;

    @Scheduled(cron = "${bot.films-refresh}")
    @SchedulerLock(name = "updateFilms")
    public void updateFilms() {
        log.info("Update films tasks with interval - {}", refreshCron);
        filmService.actualizeFilms();
    }
}
