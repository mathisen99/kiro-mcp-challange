package dev.kiro.royale;

import java.util.List;
import java.util.Optional;
import static dev.kiro.royale.Models.*;

/** Internal application port implemented by the official Battle Runner boundary. */
public interface BattleEngine extends AutoCloseable {
    Optional<String> readyEndpoint();
    EngineExecution run(List<ValidatedBot> bots, int rounds, boolean record, boolean showBattle) throws Exception;
    @Override void close();
}
