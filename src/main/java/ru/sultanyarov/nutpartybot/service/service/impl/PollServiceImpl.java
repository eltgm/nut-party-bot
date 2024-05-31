package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    private final TelegramClient telegramClient;
    private final PollInfoCollectionRepository pollInfoCollectionRepository;
    private final PollCollectionRepository pollCollectionRepository;

    @Override
    public void createPoll(PollType type, Long chatId) {
        log.info("Create poll with type {} for chat {}", type, chatId);
        pollInfoCollectionRepository.findById(type.getValue())
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll info type {}", type)))
                .<PollDocument>handle((pollInfoDocument, sink) -> {
                    Message message;
                    List<String> answers = pollInfoDocument.getAnswers();
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
                        log.error("Error creating poll with type {} for chatId {}", type, chatId, e);
                        sink.error(new ServiceException(e, "Error on telegram while sending poll to chat with id {}", chatId));
                        return;
                    }

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
                    sink.next(pollDocument);
                })
                .flatMap(pollCollectionRepository::save)
                .subscribe();
    }

    @Override
    public void updatePollResults(Long pollId, List<Integer> votes) {
        log.info("Update poll results for pollId {}", pollId);
        pollCollectionRepository.findById(pollId)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll with id {}", pollId)))
                .map(pollDocument -> {
                    List<String> answers = pollDocument.getAnswers();
                    List<String> mappedResults = votes.stream()
                            .map(answers::get)
                            .toList();

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
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll with id {}", pollId)))
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
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found poll with id {}", pollId)))
                .map(PollDocument::getVotes);
    }
}
