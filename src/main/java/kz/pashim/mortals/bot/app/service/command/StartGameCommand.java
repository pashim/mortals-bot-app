package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.exception.GroupNotFoundException;
import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.ChannelRepository;
import kz.pashim.mortals.bot.app.repository.DisciplineRepository;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.GroupRepository;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.UserRoleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class StartGameCommand extends AbstractCommand {
    public static final String COMMAND = "/startgame";

    private final GameSessionRepository gameSessionRepository;
    private final DisciplineRepository disciplineRepository;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;

    public StartGameCommand(
            BotCallbackStrategy botCallbackStrategy,
            ValidationService validationService,
            UserService userService,
            GameSessionRepository gameSessionRepository,
            DisciplineRepository disciplineRepository,
            ChannelRepository channelRepository,
            GroupRepository groupRepository
    ) {
        super(botCallbackStrategy, validationService, userService);
        this.gameSessionRepository = gameSessionRepository;
        this.disciplineRepository = disciplineRepository;
        this.channelRepository = channelRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public void handle(BotEvent event) {
        var chatId = event.getChatId();

        var userEntity = userService.getUser(chatId, event.getUserId(), getBotCallback(event));
        if (!UserRoleUtils.hasPermission(userEntity, UserRole.MODERATOR)) {
            getBotCallback(event).sendMessage(chatId, "Нет прав начать игровую сессию");
            return;
        }

        var disciplineName = BotMessageUtils.extractFirstArgument(event.getCommand());
        if (disciplineName == null) {
            getBotCallback(event).sendMessage(chatId, "Дисциплина не найдена");
            return;
        }

        var discipline = disciplineRepository.findByNameIgnoreCase(disciplineName);
        if (discipline.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, String.format("Дисциплина %s не найден", disciplineName));
            return;
        }

        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        var group = groupRepository.findByChannelAndSourceId(
                channelRepository.findByName(event.getChannel().name()),
                chatId
        );
        if (group.isEmpty()) {
            log.error("Группа (channel={}, chatId={}) не найдена", channel.getId(), chatId);
            throw new GroupNotFoundException();
        }
        var gameSession = gameSessionRepository.findByChannelAndGroupAndDisciplineAndStateIn(
            channel, group.get(), discipline.get(), List.of(GameSessionState.PREPARING)
        );

        if (gameSession.isPresent()) {
            getBotCallback(event).sendMessage(chatId, String.format("Игровая сессия по %s уже имеется", discipline.get().getName()));
            return;
        }

        var gameSessionEntity = createGameSession(channel, group.get(), discipline.get(), userEntity);
        getBotCallback(event).sendMessage(chatId, String.format(
                "Игровая сессия по %s начинается! \n\nИгроки могут присоединиться к сессии командой",
                discipline.get().getName()
        ));
        getBotCallback(event).sendMessage(chatId, String.format(
                "/join %s",
                gameSessionEntity.getUuid()
        ));
    }

    private GameSessionEntity createGameSession(ChannelEntity channelEntity, GroupEntity groupEntity, DisciplineEntity disciplineEntity, UserEntity userEntity) {
        return gameSessionRepository.save(
                GameSessionEntity.builder()
                        .channel(channelEntity)
                        .group(groupEntity)
                        .discipline(disciplineEntity)
                        .state(GameSessionState.PREPARING)
                        .date(ZonedDateTime.now())
                        .uuid(UUID.randomUUID())
                        .initiator(userEntity)
                        .build()
        );
    }
}
