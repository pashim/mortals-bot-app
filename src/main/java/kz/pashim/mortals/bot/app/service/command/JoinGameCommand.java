package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionParticipant;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.TeamRatingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class JoinGameCommand extends AbstractCommand {
    public static final String COMMAND = "/join";

    private static final ReentrantLock LOCK = new ReentrantLock();

    private final GameSessionRepository gameSessionRepository;
    private final RatingRepository ratingRepository;
    private final MortalsMessageSource messageSource;

    public JoinGameCommand(
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
        var userEntity = userService.getUser(chatId, event.getUserId());
        var sessionUuid = BotMessageUtils.extractFirstArgument(event.getCommand());
        if (sessionUuid == null) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.join.session.uuid.not.found"));
            return;
        }

        Optional<GameSessionEntity> gameSessionEntity = Optional.empty();
        try {
            gameSessionEntity = gameSessionRepository.findByUuid(UUID.fromString(sessionUuid));
        } catch (IllegalArgumentException ignored) { }

        if (gameSessionEntity.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.common.game.session.not.found"));
            return;
        }
        if (!gameSessionEntity.get().getState().equals(GameSessionState.PREPARING)) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.join.can.not.join.finished.game"));
            return;
        }
        if (gameSessionEntity.get().getParticipants().stream().map(it -> it.getUser().getSourceUserId()).collect(Collectors.toSet()).contains(userEntity.getSourceUserId())) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.join.user.already.in.lobby", userEntity.getNickname()));
            return;
        }
        if (gameSessionEntity.get().getParticipants().size() >= gameSessionEntity.get().getDiscipline().getMaxSlots()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.join.no.free.slots"));
            return;
        }
        try {
            LOCK.lock();
            var updatedGameSessionEntity = joinGameSession(gameSessionEntity.get(), userEntity);
            var freeSlots = getFreeSlots(updatedGameSessionEntity);
            if (freeSlots == 0) {
                updatedGameSessionEntity = autoStartGame(updatedGameSessionEntity);
                getBotCallback(event).sendMessage(chatId,
                        messageSource.getMessage("bot.message.join.participants.gathered",
                        updatedGameSessionEntity.getDiscipline().getName(),
                        buildDividedTeamsMessage(updatedGameSessionEntity),
                        updatedGameSessionEntity.getUuid(),
                        IntStream.range(1, updatedGameSessionEntity.getDiscipline().getTeamsCount()).boxed().map(Object::toString).collect(Collectors.joining("/"))
                ));
            } else {
                getBotCallback(event).sendMessage(chatId,
                        messageSource.getMessage("bot.message.join.success",
                        userEntity.getNickname(),
                        freeSlots
                ));
            }
        } finally {
            LOCK.unlock();
        }
    }

    private GameSessionEntity autoStartGame(GameSessionEntity gameSessionEntity) {
        TeamRatingUtils.divideTeams(
                gameSessionEntity.getParticipants().stream()
                        .map(it -> Pair.of(it, ratingRepository.findByChannelAndGroupAndDisciplineAndUser(
                                gameSessionEntity.getChannel(),
                                gameSessionEntity.getGroup(),
                                gameSessionEntity.getDiscipline(),
                                it.getUser()
                        )))
                        .collect(Collectors.toList())
        );
        return gameSessionRepository.save(
                gameSessionEntity.toBuilder()
                        .state(GameSessionState.STARTED)
                        .build()
        );
    }

    private GameSessionEntity joinGameSession(GameSessionEntity gameSessionEntity, UserEntity userEntity) {
        gameSessionEntity.getParticipants().add(GameSessionParticipant.builder()
                .gameSession(gameSessionEntity)
                .user(userEntity)
                .build());

        return gameSessionRepository.save(gameSessionEntity);
    }

    private Integer getFreeSlots(GameSessionEntity gameSessionEntity) {
        var discipline = gameSessionEntity.getDiscipline();
        return discipline.getTeamMembersCount() * discipline.getTeamsCount() - gameSessionEntity.getParticipants().size();
    }

    private String buildDividedTeamsMessage(GameSessionEntity gameSessionEntity) {
        StringBuilder stringBuilder = new StringBuilder();
        var teams = gameSessionEntity
                .getParticipants()
                .stream()
                .collect(Collectors.groupingBy(GameSessionParticipant::getTeamNumber));

        teams.forEach((key, value) -> {
            stringBuilder.append(key).append(": ");
            stringBuilder.append(value.stream()
                    .map(gameSessionParticipant -> {
                        var rating = getRatingByChannelAndGroupAndDisciplineAndUser(
                                gameSessionEntity, gameSessionParticipant
                        );
                        return String.format(
                                "%s: [%s]",
                                gameSessionParticipant.getUser().getNickname(),
                                rating.getMmr()
                        );
                    })
                    .collect(Collectors.joining(", ")));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    private RatingEntity getRatingByChannelAndGroupAndDisciplineAndUser(GameSessionEntity gameSessionEntity, GameSessionParticipant it) {
        return ratingRepository.findByChannelAndGroupAndDisciplineAndUser(
                gameSessionEntity.getChannel(),
                gameSessionEntity.getGroup(),
                gameSessionEntity.getDiscipline(),
                it.getUser()
        );
    }
}
