package ru.sultanyarov.nutpartybot.service.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class PollUtility {
    public static @NotNull List<String> getMappedVotes(List<Integer> votes, List<String> answers) {
        return votes.stream()
                .map(answers::get)
                .toList();
    }
}
