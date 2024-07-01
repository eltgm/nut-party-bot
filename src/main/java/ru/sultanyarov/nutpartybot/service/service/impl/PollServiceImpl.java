package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.sultanyarov.nutpartybot.service.utils.PollUtility.getMappedVotes;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    private final TelegramClient telegramClient;
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
        Message message = sendPollMessage(chatId, pollInfoDocument, answers);

        return createPollDocument(pollInfoDocument.getName(), chatId, answers, message, isAdmin);
    }

    private Message sendPollMessage(Long chatId, PollInfoDocument pollInfoDocument, List<String> answers) {
        var pollType = pollInfoDocument.getName();
        Message message;
        try {
            message = telegramClient.execute(
                    SendPoll.builder()
                            .chatId(chatId)
                            .allowMultipleAnswers(true)
                            .question(pollInfoDocument.getQuestion())
                            .options(answers)
                            .isAnonymous(false)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Error creating poll with type {} for chatId {}", pollType, chatId, e);
            throw new ServiceException("Error creating poll with type " + pollType, e);
        }

        return message;
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
    public void closePoll(Long pollId) {
        log.info("Close poll with id {}", pollId);
        pollCollectionRepository.findByIdAndClosedAtIsNull(pollId)
                .switchIfEmpty(getErrorLambda(pollId))
                .<PollDocument>handle((pollDocument, sink) -> {
                    try {
                        telegramClient.execute(
                                StopPoll.builder()
                                        .chatId(pollDocument.getChatId())
                                        .messageId(pollDocument.getMessageId())
                                        .build()
                        );
                    } catch (TelegramApiException e) {
                        log.error("Error closing poll with id {}", pollId, e);
                        sink.error(new ServiceException(e, "Error on telegram while closing poll with id {}", pollId));
                        return;
                    }

                    pollDocument.setClosedAt(LocalDateTime.now());
                    sink.next(pollDocument);
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

    private @NotNull Mono<PollDocument> getErrorLambda(Long pollId) {
        return Mono.error(() -> new NotFoundException("Not found poll with id {}", pollId));
    }
}
