package ru.sultanyarov.nutpartybot.domain.model;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sultanyarov.nutpartybot.domain.exception.NotFoundException;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum PollType {
    DATE_POLL_DOCUMENT_NAME("datePoll"),
    ACTIVITY_POLL_DOCUMENT_NAME("activityPoll");

    private static final ImmutableMap<String, PollType> pollTypeCodeImmutableMap =
            ImmutableMap.<String, PollType>builder()
                    .putAll(
                            Arrays.stream(PollType.values())
                                    .collect(Collectors.toMap(PollType::getValue, Function.identity()))
                    )
                    .build();

    public static PollType of(String name) {
        return Optional.ofNullable(pollTypeCodeImmutableMap.get(name))
                .orElseThrow(() -> new NotFoundException("Not found  for code={}", name));
    }

    private final String value;
}
