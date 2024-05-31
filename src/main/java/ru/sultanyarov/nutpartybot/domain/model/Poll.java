package ru.sultanyarov.nutpartybot.domain.model;

import java.time.LocalDateTime;

public class Poll {
    private Long id;
    private PollType type;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
