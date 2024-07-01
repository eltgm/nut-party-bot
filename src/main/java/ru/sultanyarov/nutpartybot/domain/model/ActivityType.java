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
public enum ActivityType {
    FILM("Кино"),
    TABLETOP("Настолки");

    private static final ImmutableMap<String, ActivityType> activityTypeCodeImmutableMap =
            ImmutableMap.<String, ActivityType>builder()
                    .putAll(
                            Arrays.stream(ActivityType.values())
                                    .collect(Collectors.toMap(ActivityType::getValue, Function.identity()))
                    )
                    .build();

    public static ActivityType of(String name) {
        return Optional.ofNullable(activityTypeCodeImmutableMap.get(name))
                .orElseThrow(() -> new NotFoundException("Not found  for code={}", name));
    }

    private final String value;
}
