package kz.pashim.mortals.bot.app.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mortals.bot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MortalsBotProperties {
    private Integer defaultMmrAssigned;
    private Leaderboard leaderboard;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leaderboard {
        private Integer maxPlayersToDisplay;
    }
}
