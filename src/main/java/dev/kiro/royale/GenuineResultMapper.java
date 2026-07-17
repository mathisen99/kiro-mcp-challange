package dev.kiro.royale;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static dev.kiro.royale.Models.*;

/** Validates and maps only successful official completion data without score calculation. */
public final class GenuineResultMapper {
    public List<BattleResult> map(EngineCompletion completion, List<ValidatedBot> expectedBots) {
        if (completion == null || !completion.successful() || !completion.officialCompletionObserved()
                || completion.provenance() != CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION) {
            throw new IllegalStateException("Official successful completion was not observed");
        }
        if (completion.roundsPlayed() < 1 || completion.results() == null || completion.results().size() != 2) {
            throw new IllegalStateException("Official completion did not contain exactly two valid results");
        }
        Set<String> expected = expectedBots.stream()
                .map(bot -> identity(bot.descriptor().name(), bot.descriptor().version()))
                .collect(Collectors.toUnmodifiableSet());
        Set<String> actual = completion.results().stream()
                .map(result -> identity(result.name(), result.version()))
                .collect(Collectors.toSet());
        if (expected.size() != 2 || !actual.equals(expected)) throw new IllegalStateException("Official result identities do not match selected bots");
        var ranks = completion.results().stream().map(EngineResult::rank).collect(Collectors.toSet());
        if (!ranks.equals(Set.of(1, 2))) throw new IllegalStateException("Official result ranks are invalid");
        return completion.results().stream()
                .sorted(Comparator.comparingInt(EngineResult::rank))
                .map(result -> new BattleResult(result.rank(), result.name(), result.version(), result.totalScore(),
                        result.survivalScore(), result.bulletDamage(), result.ramDamage(), result.firstPlaces(), completion.roundsPlayed()))
                .toList();
    }

    private static String identity(String name, String version) { return name + "\u0000" + version; }
}
