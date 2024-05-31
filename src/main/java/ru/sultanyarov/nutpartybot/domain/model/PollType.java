package ru.sultanyarov.nutpartybot.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PollType {
    DATE_POLL_DOCUMENT_NAME("datePoll"),
    ACTIVITY_POLL_DOCUMENT_NAME("activityPoll");

    private final String value;
}
