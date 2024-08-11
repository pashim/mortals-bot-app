package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionParticipant;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.TeamRatingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class JoinGameCommand extends Command {

    private static ReentrantLock LOCK = new ReentrantLock();

    @Autowired
    private GameSessionRepository gameSessionRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    @Lazy
    private TelegramBotHandler telegramClient;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private UserService userService;

    @Override
    public String command() {
        return "/join";
    }

    @Override
    public void execute(Update update) {
        if (!validationService.validate(update, command(), telegramClient)) {
            return;
        }

        var user = update.getMessage().getFrom();
        var chatId = update.getMessage().getChatId();

        var userEntity = userService.getUser(chatId.toString(), user.getId().toString(), telegramClient);

        var sessionUuid = BotMessageUtils.extractFirstArgument(update.getMessage().getText());
        if (sessionUuid == null) {
            telegramClient.sendText(chatId, "UUID сессии не найден");
            return;
        }

        Optional<GameSessionEntity> gameSessionEntity = Optional.empty();
        try {
            gameSessionEntity = gameSessionRepository.findByUuid(UUID.fromString(sessionUuid));
        } catch (IllegalArgumentException ignored) { }

        if (gameSessionEntity.isEmpty()) {
            telegramClient.sendText(chatId, "Игровая сессия не найдена");
            return;
        }
        if (!gameSessionEntity.get().getState().equals(GameSessionState.PREPARING)) {
            telegramClient.sendText(chatId, "Нельзя присоединиться к завершенной игровой сессии");
            return;
        }
        if (gameSessionEntity.get().getParticipants().stream().map(GameSessionParticipant::getUser).collect(Collectors.toSet()).contains(userEntity)) {
            telegramClient.sendText(chatId, String.format("Пользователь %s уже находится в лобби", userEntity.getNickname()));
            return;
        }
        LOCK.lock();
        var updatedGameSessionEntity = joinGameSession(gameSessionEntity.get(), userEntity);
        var freeSlots = getFreeSlots(updatedGameSessionEntity);
        if (freeSlots == 0) {
            updatedGameSessionEntity = autoStartGame(updatedGameSessionEntity);
            telegramClient.sendText(chatId, String.format(
                    "Все участники собрались, игра по %s начинается, игроки были автоматом распределены на команды \n\n %s \n\n прошу всех игроков зайти в лобби. \n\n по окончанию игры участники должны внести результат командой /result %s (1 или 2)",
                    updatedGameSessionEntity.getDiscipline().getName(),
                    buildDividedTeamsMessage(updatedGameSessionEntity),
                    updatedGameSessionEntity.getUuid()

            ));
        } else {
            telegramClient.sendText(chatId, String.format(
                    "Игрок %s присоединяется к лобби, осталось %d свободных слотов",
                    userEntity.getNickname(),
                    freeSlots
            ));
        }
        LOCK.unlock();
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
        return gameSessionEntity.getDiscipline().getTeamMembersCount() - gameSessionEntity.getParticipants().size();
    }

    private String buildDividedTeamsMessage(GameSessionEntity gameSessionEntity) {
        StringBuilder stringBuilder = new StringBuilder();
        var teams = gameSessionEntity
                .getParticipants()
                .stream()
                .collect(Collectors.groupingBy(GameSessionParticipant::getTeamNumber));

        teams.forEach((key, value) -> {
            stringBuilder.append(value.stream()
                    .map(it -> it.getUser().getNickname())
                    .collect(Collectors.joining(", ")));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }
}
