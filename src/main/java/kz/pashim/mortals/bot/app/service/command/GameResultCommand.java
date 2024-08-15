package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionParticipant;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.TeamRatingUtils;
import kz.pashim.mortals.bot.app.util.UserRoleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static kz.pashim.mortals.bot.app.util.TeamRatingUtils.RATING_DIFF;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class GameResultCommand extends AbstractCommand {
    public static final String COMMAND = "/result";

    private static final ReentrantLock LOCK = new ReentrantLock();
    private final GameSessionRepository gameSessionRepository;
    private final RatingRepository ratingRepository;
    private final MortalsMessageSource messageSource;

    public GameResultCommand(
            BotCallbackStrategy botCallbackStrategy,
            ValidationService validationService,
            UserService userService,
            GameSessionRepository gameSessionRepository,
            RatingRepository ratingRepository,
            MortalsMessageSource messageSource
    ) {
        super(botCallbackStrategy, validationService, userService);
        this.gameSessionRepository = gameSessionRepository;
        this.ratingRepository = ratingRepository;
        this.messageSource = messageSource;
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public void handle(BotEvent event) {
        var chatId = event.getChatId();
        var userEntity = userService.getUser(event.getChatId(), event.getUserId());
        if (!UserRoleUtils.hasPermission(userEntity, UserRole.MODERATOR)) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.result.unauthorized"));
            return;
        }

        var arguments = BotMessageUtils.extractArguments(event.getCommand());
        Optional<GameSessionEntity> gameSessionEntity = gameSessionRepository.findByUuid(UUID.fromString(arguments[0]));

        if (gameSessionEntity.isEmpty()) {
            getBotCallback(event).sendMessage(chatId,messageSource.getMessage("bot.message.common.game.session.not.found"));
            return;
        }

        if (!gameSessionEntity.get().getState().equals(GameSessionState.STARTED)) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.result.game.status.invalid", gameSessionEntity.get().getState()));
            return;
        }

        try {
            LOCK.lock();
            var result = Integer.parseInt(arguments[1]);
            var updatedGameSessionEntity = finishGameSession(gameSessionEntity.get(), result);
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.result.success",
                    gameSessionEntity.get().getDiscipline().getName(),
                    buildGameSessionResultMessage(updatedGameSessionEntity, result)
            ));
        } finally {
            LOCK.unlock();
        }
    }

    private GameSessionEntity finishGameSession(GameSessionEntity gameSessionEntity, Integer result) {
        var gameParticipants = TeamRatingUtils.result(gameSessionEntity.getParticipants().stream()
                .map(it -> Pair.of(it, getRatingByChannelAndGroupAndDisciplineAndUser(gameSessionEntity, it)))
                .collect(Collectors.toList()), result);

        ratingRepository.saveAll(gameParticipants.stream().map(Pair::getSecond).collect(Collectors.toList()));
        return gameSessionRepository.save(
                gameSessionEntity.toBuilder()
                        .state(GameSessionState.FINISHED)
                        .build()
        );
    }

    private RatingEntity getRatingByChannelAndGroupAndDisciplineAndUser(GameSessionEntity gameSessionEntity, GameSessionParticipant it) {
        return ratingRepository.findByChannelAndGroupAndDisciplineAndUser(
                gameSessionEntity.getChannel(),
                gameSessionEntity.getGroup(),
                gameSessionEntity.getDiscipline(),
                it.getUser()
        );
    }

    private String buildGameSessionResultMessage(GameSessionEntity gameSessionEntity, Integer winner) {
        StringBuilder stringBuilder = new StringBuilder();
        var teams = gameSessionEntity
                .getParticipants()
                .stream()
                .collect(Collectors.groupingBy(GameSessionParticipant::getTeamNumber));

        teams.forEach((key, value) -> {
            stringBuilder.append(value.stream()
                    .map(gameSessionParticipant -> {
                        var rating = getRatingByChannelAndGroupAndDisciplineAndUser(
                                gameSessionEntity, gameSessionParticipant
                        );
                        return String.format(
                                "%s: [%s](%s)",
                                gameSessionParticipant.getUser().getNickname(),
                                rating.getMmr(),
                                gameSessionParticipant.getTeamNumber().equals(winner) ? "+" + RATING_DIFF : "-" + RATING_DIFF
                        );
                    })
                    .collect(Collectors.joining(", ")));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }
}
