package ru.sultanyarov.nutpartybot.application.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sultanyarov.nutpartybot.application.config.ChannelsProperties;
import ru.sultanyarov.nutpartybot.domain.model.PollType;
import ru.sultanyarov.nutpartybot.service.facade.TelegramFacade;
import ru.sultanyarov.nutpartybot.service.service.BookService;
import ru.sultanyarov.nutpartybot.service.service.FilmService;
import ru.sultanyarov.nutpartybot.service.service.PollService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final FilmService filmService;
    private final PollService pollService;
    private final ChannelsProperties channelsProperties;
    private final TelegramFacade telegramFacade;
    private final BookService bookService;

    @Scheduled(cron = "${bot.scheduling.films-refresh}")
    @SchedulerLock(name = "updateFilms")
    public void updateFilms() {
        log.info("Update films task");
        filmService.actualizeFilms();
    }

    @Scheduled(cron = "${bot.scheduling.get-activity-days}")
    @SchedulerLock(name = "getActivityDays")
    public void getActivityDays() {
        log.info("Get activity days task");
        pollService.createPoll(PollType.DATE_POLL_DOCUMENT_NAME, channelsProperties.getAdmin(), true);
    }

    @Scheduled(cron = "${bot.scheduling.end-poll-notify}")
    @SchedulerLock(name = "endPollNotification")
    public void endPollNotification() {
        log.info("End poll notification task");
        telegramFacade.sendEndPollNotification();
    }

    @Scheduled(cron = "${bot.scheduling.collect-poll-results}")
    @SchedulerLock(name = "collectPollResults")
    public void collectPollResults() {
        log.info("Collect poll results task");
        telegramFacade.collectPollResults();
    }

    @Scheduled(cron = "${bot.scheduling.clear-bookings}")
    @SchedulerLock(name = "clearBookings")
    public void clearBookings() {
        log.info("Clear bookings task");
        bookService.removeBookings();
    }

    @Scheduled(cron = "${bot.scheduling.get-activity}")
    @SchedulerLock(name = "getActivity")
    public void getActivity() {
        log.info("Get activity task");
        pollService.sendActivityPolls();
    }
}
