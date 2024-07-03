package ru.sultanyarov.nutpartybot.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.application.config.ChannelsProperties;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.exception.NotFoundException;
import ru.sultanyarov.nutpartybot.domain.exception.TooManyBooksException;
import ru.sultanyarov.nutpartybot.domain.model.ActivityType;
import ru.sultanyarov.nutpartybot.domain.model.TabletopInfo;
import ru.sultanyarov.nutpartybot.service.facade.TelegramFacade;
import ru.sultanyarov.nutpartybot.service.service.BookService;
import ru.sultanyarov.nutpartybot.service.service.FilmService;
import ru.sultanyarov.nutpartybot.service.service.PollService;
import ru.sultanyarov.nutpartybot.service.service.TabletopService;
import ru.sultanyarov.nutpartybot.service.service.TelegramMessagingService;
import ru.sultanyarov.nutpartybot.service.utils.PollUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static ru.sultanyarov.nutpartybot.domain.model.PollType.DATE_POLL_DOCUMENT_NAME;
import static ru.sultanyarov.nutpartybot.service.utils.AnswersConstants.PARTY_SKIP_ANSWER;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.FILM_LIST_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.MENU_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.REMOVE_BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.TABLETOP_LIST_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFacadeImpl implements TelegramFacade {
    @Value("${bot.count-of-participants-to-party-start}")
    private Integer countOfParticipantsToPartyStart;

    private final TelegramMessagingService telegramMessagingService;
    private final FilmService filmService;
    private final BookService bookService;
    private final PollService pollService;
    private final ChannelsProperties channelsProperties;
    private final TabletopService tabletopService;

    @Override
    public void sendStartMessage(Long chatId) {
        log.info("Sending start message to chat {}", chatId);
        telegramMessagingService.sendMessage(getMenuMessage(chatId));
    }

    private @NotNull SendMessage getMenuMessage(Long chatId) {
        var message = new SendMessage(chatId.toString(), "Выбирай");
        message.setChatId(chatId);
        message.enableMarkdown(true);

        var replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(getKeyboardRows())
                .selective(true)
                .build();
        message.setReplyMarkup(replyKeyboardMarkup);

        return message;
    }

    private @NotNull List<KeyboardRow> getKeyboardRows() {
        return List.of(
                new KeyboardRow(
                        KeyboardButton.builder()
                                .text(FILM_LIST_MESSAGE)
                                .build(),
                        KeyboardButton.builder()
                                .text(TABLETOP_LIST_MESSAGE)
                                .build(),
                        KeyboardButton.builder()
                                .text(BOOK_PLACE_MESSAGE)
                                .build(),
                        KeyboardButton.builder()
                                .text(REMOVE_BOOK_PLACE_MESSAGE)
                                .build()
                ),
                new KeyboardRow(
                        KeyboardButton.builder()
                                .text(MENU_MESSAGE)
                                .build()
                )
        );
    }

    @Override
    public void addFilm(String filmName, Long chatId) {
        log.info("Adding film {}", filmName);
        filmService.addFilmWithNotification(filmName, chatId);
    }

    @Override
    public void getFilms(Long chatId) {
        log.info("Getting films");
        filmService.getActualFilms()
                .collectList()
                .subscribe(strings -> {
                    List<String> formattedFilmsList = new ArrayList<>(strings.size());
                    for (int i = 1; i <= strings.size(); i++) {
                        formattedFilmsList.add(String.format("%d. %s", i, strings.get(i - 1)));
                    }
                    var message = String.join("\n", formattedFilmsList);
                    telegramMessagingService.createAndSendMessage(chatId, message);
                });
    }

    @Override
    public void bookPlace(Long chatId, String userName) {
        log.info("Booking place for {}", userName);
        bookService.bookPlace(userName)
                .doOnError(throwable -> {
                    if (throwable instanceof TooManyBooksException) {
                        telegramMessagingService.createAndSendMessage(chatId, "Мест нет!");
                    } else {
                        telegramMessagingService.createAndSendMessage(chatId, "Непредвиденная ошибка. Пиши @eltgm");
                    }
                })
                .subscribe(bookingDocument -> telegramMessagingService.createAndSendMessage(chatId, "Место успешно забронировано"));
    }

    @Override
    public void unBookPlace(Long chatId, String userName) {
        log.info("Unbooking place for {}", userName);
        bookService.unBookPlace(userName)
                .doOnError(throwable -> {
                    if (throwable instanceof NotFoundException) {
                        telegramMessagingService.createAndSendMessage(chatId, "У тебя нет броней, не парься");
                    } else {
                        telegramMessagingService.createAndSendMessage(chatId, "Непредвиденная ошибка. Пиши @eltgm");
                    }
                })
                .doOnSuccess(bookingDocument -> telegramMessagingService.createAndSendMessage(chatId, "Бронь снята!"))
                .subscribe();
    }

    @Override
    public void showMenu(Long chatId) {
        log.info("Showing menu");
        telegramMessagingService.createAndSendMessage(chatId,
                """
                        /start - запуск бота
                        /addFilm название_фильма - добавить фильм в список
                        /tabletop - список настольных игр
                        """
        );
    }

    @Override
    public void sendError(Long chatId) {
        log.info("Sending error message to chat {}", chatId);
        telegramMessagingService.createAndSendMessage(chatId, "Неизвестная команда!");
    }

    @Override
    public void processDatePollResults(PollAnswer pollAnswer) {
        log.info("Processing date-poll results from admin");
        var pollId = Long.valueOf(pollAnswer.getPollId());
        var optionIds = pollAnswer.getOptionIds();

        //End admin poll
        pollService.updatePollResults(pollId, optionIds);
        pollService.closePoll(pollId, false);

        pollService.getPollAnswers(pollId)
                .map(answers -> checkSkipParty(answers, optionIds))
                .subscribe(skipParty -> createDatePollToChannels(skipParty, optionIds));
    }

    @Override
    public void sendEndPollNotification() {
        log.info("Sending end poll notification");
        pollService.getActivePolls()
                .filter(pollDocument -> !pollDocument.getIsAdmin())
                .subscribe(pollDocument -> telegramMessagingService.createAndSendMessageWithReply(
                        pollDocument.getChatId(),
                        "Внимание! Attention! Warning! Alarm! Uwaga! Achtung! Опрос завершится через 5 минут",
                        pollDocument.getMessageId())
                );
    }

    @Override
    public void collectPollResults() {
        log.info("Collecting poll results");
        pollService.getActivePolls()
                .filter(pollDocument -> !pollDocument.getIsAdmin())
                .subscribe(
                        pollDocument -> {
                            var chatId = pollDocument.getChatId();
                            var votesStream = pollDocument.getVotes()
                                    .entrySet()
                                    .stream();

                            boolean isPartyStart = false;
                            switch (pollDocument.getType()) {
                                case DATE_POLL_DOCUMENT_NAME -> {
                                    isPartyStart = isPartyStart(votesStream);
                                    sendDatePollResult(chatId, isPartyStart);
                                }
                                case ACTIVITY_POLL_DOCUMENT_NAME -> sendActivityType(votesStream, chatId);
                            }

                            pollService.closePoll(pollDocument.getId(), isPartyStart);
                        }
                );
    }

    private void sendDatePollResult(Long chatId, Boolean isPartyStart) {
        if (isPartyStart) {
            telegramMessagingService.createAndSendMessage(chatId, "Балдеж, завтра приду с новым опросом");
        } else {
            telegramMessagingService.createAndSendMessage(chatId, "Ну и фиг с вами, буду плакать один...");
        }
    }

    private @NotNull Boolean isPartyStart(Stream<Map.Entry<String, Integer>> votesStream) {
        Optional<Boolean> isPartyStart = votesStream
                .filter(stringIntegerEntry -> !stringIntegerEntry.getKey().equals(PARTY_SKIP_ANSWER))
                .map(stringIntegerEntry -> stringIntegerEntry.getValue() >= countOfParticipantsToPartyStart)
                .filter(aBoolean -> aBoolean)
                .findFirst();
        return isPartyStart.isPresent();
    }

    private void sendActivityType(Stream<Map.Entry<String, Integer>> votesStream, Long chatId) {
        votesStream
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .ifPresent(activity -> {
                    var activityType = ActivityType.of(activity);
                    switch (activityType) {
                        case FILM -> filmService.getRandomFilmName()
                                .subscribe(filmDocument -> telegramMessagingService.createAndSendMessage(chatId, "Смотрим кино - " + filmDocument.getName()));
                        case TABLETOP ->
                                telegramMessagingService.createAndSendMessage(chatId, "Играем в настолку. Посмотреть список /tabletop");
                    }
                });
    }

    @Override
    public void updateDatePollResults(PollAnswer pollAnswer) {
        log.info("Updating date-poll results");
        var pollId = Long.valueOf(pollAnswer.getPollId());
        var optionIds = pollAnswer.getOptionIds();

        pollService.getPollAnswers(pollId)
                .map(answers -> checkSkipParty(answers, optionIds))
                .subscribe(skipParty -> {
                    if (!skipParty) {
                        pollService.updatePollResults(pollId, optionIds);
                    }
                });
    }

    @Override
    public void updateActivityPollResults(PollAnswer pollAnswer) {
        log.info("Updating activity-poll results");
        var pollId = Long.valueOf(pollAnswer.getPollId());
        var optionIds = pollAnswer.getOptionIds();

        pollService.getPollAnswers(pollId)
                .subscribe(answers -> pollService.updatePollResults(pollId, optionIds));
    }

    @Override
    public Mono<PollDocument> getPoll(Long pollId) {
        log.info("Getting poll {}", pollId);
        return pollService.getPoll(pollId);
    }

    @Override
    public void getTabletops(Long chatId) {
        log.info("Getting tabletops");

        var tabletopFormattedString = getTabletopFormattedString();
        telegramMessagingService.createAndSendMessage(chatId, tabletopFormattedString);
    }

    private @NotNull String getTabletopFormattedString() {
        var stringBuilder = new StringBuilder();
        List<TabletopInfo> allTabletops = tabletopService.getAllTabletops();
        int tabletopsCount = 1;
        for (TabletopInfo tabletopInfo : allTabletops) {
            if (!tabletopInfo.inStock()) {
                continue;
            }

            stringBuilder.append(String.format("%-4s〽%-4.1f:  %s", (tabletopsCount++) + ".", tabletopInfo.mark(), tabletopInfo.name()));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private void createDatePollToChannels(Boolean skipParty, List<Integer> optionIds) {
        if (skipParty) {
            log.info("Admin skipped meeting.");
            return;
        }
        log.info("Creating poll");
        //Create poll to admin
        optionIds.add(3); //hardcoded skip value
        pollService.createPolls(DATE_POLL_DOCUMENT_NAME, channelsProperties.getGroups(), optionIds, false);
    }

    private boolean checkSkipParty(List<String> answers, List<Integer> optionIds) {
        var mappedVotes = PollUtility.getMappedVotes(optionIds, answers);
        return mappedVotes.contains(PARTY_SKIP_ANSWER);
    }
}
