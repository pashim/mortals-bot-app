package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionParticipant;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.TeamRatingUtils;
import kz.pashim.mortals.bot.app.util.UserRoleUtils;
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
import java.util.stream.Collectors;

import static kz.pashim.mortals.bot.app.util.TeamRatingUtils.RATING_DIFF;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class GameResultCommand extends Command {

    public static String COMMAND = "/result";

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
        return COMMAND;
    }

    @Override
    public void execute(Update update) {
        if (!validationService.validate(update, command(), telegramClient)) {
            return;
        }

        var user = update.getMessage().getFrom();
        var chatId = update.getMessage().getChatId();

        var userEntity = userService.getUser(chatId.toString(), user.getId().toString(), telegramClient);
        if (!UserRoleUtils.hasPermission(userEntity, UserRole.MODERATOR)) {
            telegramClient.sendText(chatId, "Нет прав завершить игровую сессию");
            return;
        }

        var arguments = BotMessageUtils.extractArguments(update.getMessage().getText());
        Optional<GameSessionEntity> gameSessionEntity = gameSessionRepository.findByUuid(UUID.fromString(arguments[0]));

        if (gameSessionEntity.isEmpty()) {
            telegramClient.sendText(chatId, "Игровая сессия не найдена");
            return;
        }

        if (!gameSessionEntity.get().getState().equals(GameSessionState.STARTED)) {
            telegramClient.sendText(chatId, String.format("Нельзя выводить результат для игровой сессии cо статусом: %s", gameSessionEntity.get().getState()));
            return;
        }

        var result = Integer.parseInt(arguments[1]);
        var updatedGameSessionEntity = finishGameSession(gameSessionEntity.get(), result);
        telegramClient.sendText(chatId, String.format(
                "Игровая сессия по %s завершена! \n\n %s",
                gameSessionEntity.get().getDiscipline().getName(),
                buildGameSessionResultMessage(updatedGameSessionEntity, result)
        ));
    }

    private GameSessionEntity finishGameSession(GameSessionEntity gameSessionEntity, Integer result) {
        TeamRatingUtils.result(gameSessionEntity.getParticipants().stream()
                .map(it -> Pair.of(it, getRatingByChannelAndGroupAndDisciplineAndUser(gameSessionEntity, it)))
                .collect(Collectors.toList()), result);

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
