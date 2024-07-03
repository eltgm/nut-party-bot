package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;
import ru.sultanyarov.nutpartybot.domain.exception.NotFoundException;
import ru.sultanyarov.nutpartybot.domain.exception.ServiceException;
import ru.sultanyarov.nutpartybot.domain.model.PollType;
import ru.sultanyarov.nutpartybot.domain.repository.PollCollectionRepository;
import ru.sultanyarov.nutpartybot.domain.repository.PollInfoCollectionRepository;
import ru.sultanyarov.nutpartybot.service.service.PollService;
import ru.sultanyarov.nutpartybot.service.service.TelegramMessagingService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.sultanyarov.nutpartybot.service.utils.PollUtility.getMappedVotes;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    @Value("${bot.minus-days-for-activity-poll}")
    private Duration minusDaysForActivityPoll;

    private final TelegramMessagingService telegramMessagingService;
    private final PollInfoCollectionRepository pollInfoCollectionRepository;
    private final PollCollectionRepository pollCollectionRepository;

    @Override
    public void createPoll(PollType type, Long chatId, Boolean isAdmin) {
        log.info("Create poll with type {} for chat {}", type, chatId);
        pollInfoCollectionRepository.findById(type)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll info type {}", type)))
                .<PollDocument>handle((pollInfoDocument, sink) -> {
                    try {
                        var pollDocument = sendMessageAndCreatePoll(pollInfoDocument, pollInfoDocument.getAnswers(), chatId, isAdmin);
                        sink.next(pollDocument);
                    } catch (ServiceException ex) {
                        sink.error(new ServiceException(ex, "Error on telegram while sending poll to chat with id {}", chatId));
                    }
                })
                .flatMap(pollCollectionRepository::save)
                .subscribe();
    }

    @Override
    public void createPolls(PollType type, List<Long> chatsId, List<Integer> votes, Boolean isAdmin) {
        chatsId.forEach(chatId -> {
            log.info("Create custom poll with type {} for chat {}", type, chatId);
            pollInfoCollectionRepository.findById(type)
                    .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll info type {}", type)))
                    .<PollDocument>handle((pollInfoDocument, sink) -> {
                        List<String> mappedVotes = getMappedVotes(votes, pollInfoDocument.getAnswers());
                        try {
                            var pollDocument = sendMessageAndCreatePoll(pollInfoDocument, mappedVotes, chatId, isAdmin);
                            sink.next(pollDocument);
                        } catch (ServiceException ex) {
                            sink.error(new ServiceException(ex, "Error on telegram while sending poll to chat with id {}", chatId));
                        }
                    })
                    .flatMap(pollCollectionRepository::save)
                    .subscribe();
        });
    }

    private PollDocument sendMessageAndCreatePoll(PollInfoDocument pollInfoDocument, List<String> answers, Long chatId, Boolean isAdmin) {
        log.info("Creating and send poll. Poll type - {}, chat id - {}", pollInfoDocument.getName(), chatId);
        Message message = telegramMessagingService.sendPollMessage(chatId, pollInfoDocument, answers);

        return createPollDocument(pollInfoDocument.getName(), chatId, answers, message, isAdmin);
    }


    private @NotNull PollDocument createPollDocument(PollType type, Long chatId, List<String> answers, Message message, Boolean isAdmin) {
        var answerWithResult = new HashMap<String, Integer>();
        for (String answer : answers) {
            answerWithResult.put(answer, 0);
        }

        var pollDocument = new PollDocument();
        pollDocument.setId(Long.valueOf(message.getPoll().getId()));
        pollDocument.setMessageId(message.getMessageId());
        pollDocument.setType(type);
        pollDocument.setChatId(chatId);
        pollDocument.setCreatedAt(LocalDateTime.now());
        pollDocument.setVotes(answerWithResult);
        pollDocument.setAnswers(answers);
        pollDocument.setIsAdmin(isAdmin);

        return pollDocument;
    }

    @Override
    public void updatePollResults(Long pollId, List<Integer> votes) {
        log.info("Update poll results for pollId {}", pollId);
        pollCollectionRepository.findById(pollId)
                .switchIfEmpty(getErrorLambda(pollId))
                .map(pollDocument -> {
                    var mappedResults = getMappedVotes(votes, pollDocument.getAnswers());

                    Map<String, Integer> documentVotes = pollDocument.getVotes();
                    for (String mappedResult : mappedResults) {
                        documentVotes.compute(mappedResult, (k, count) -> count + 1);
                    }

                    return pollDocument;
                })
                .flatMap(pollCollectionRepository::save)
                .subscribe();
    }

    @Override
    public void closePoll(Long pollId, boolean isPartyStart) {
        log.info("Close poll with id {}", pollId);
        pollCollectionRepository.findByIdAndClosedAtIsNull(pollId)
                .switchIfEmpty(getErrorLambda(pollId))
                .map(pollDocument -> {
                    telegramMessagingService.stopPoll(pollDocument, pollId);
                    pollDocument.setClosedAt(LocalDateTime.now());
                    pollDocument.setIsPartyStart(isPartyStart);
                    return pollDocument;
                })
                .flatMap(pollCollectionRepository::save)
                .subscribe();
    }


    @Override
    public Mono<Map<String, Integer>> getPollResult(Long pollId) {
        log.info("Get poll result for poll with id {}", pollId);
        return pollCollectionRepository.findById(pollId)
                .switchIfEmpty(getErrorLambda(pollId))
                .map(PollDocument::getVotes);
    }

    @Override
    public Mono<PollDocument> getPoll(Long pollId) {
        log.info("Get poll with id {}", pollId);
        return pollCollectionRepository.findById(pollId);
    }

    @Override
    public Flux<PollDocument> getActivePolls() {
        log.info("Get active polls");
        return pollCollectionRepository.getAllByClosedAtIsNull();
    }

    @Override
    public Mono<List<String>> getPollAnswers(Long pollId) {
        log.info("Get poll answers for poll with id {}", pollId);
        return pollCollectionRepository.findById(pollId)
                .map(PollDocument::getAnswers);
    }

    @Override
    public void sendActivityPolls() {
        log.info("Send activity poll");

        LocalDate dateForDaysPoll = LocalDate.now().minus(minusDaysForActivityPoll);
        pollCollectionRepository.findAllByCreatedAtAndIsAdminAndPollTypeAndClosed(dateForDaysPoll, false, PollType.DATE_POLL_DOCUMENT_NAME)
                .subscribe(pollDocument -> {
                    var chatId = pollDocument.getChatId();
                    log.debug("Send activity poll to {}", chatId);
                    createPoll(PollType.ACTIVITY_POLL_DOCUMENT_NAME, chatId, false);
                });
    }

    private @NotNull Mono<PollDocument> getErrorLambda(Long pollId) {
        return Mono.error(() -> new NotFoundException("Not found poll with id {}", pollId));
    }
}
