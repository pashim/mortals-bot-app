package kz.pashim.mortals.bot.app.model;

import java.util.List;

public enum GameSessionState {
    PREPARING,
    STARTED,
    FINISHED,
    ABORTED;

    public static List<GameSessionState> liveStates() {
        return List.of(STARTED, PREPARING);
    }
}
