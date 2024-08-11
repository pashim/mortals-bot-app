package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.exception.GroupNotFoundException;
import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.repository.ChannelRepository;
import kz.pashim.mortals.bot.app.repository.DisciplineRepository;
import kz.pashim.mortals.bot.app.repository.GameSessionRepository;
import kz.pashim.mortals.bot.app.repository.GroupRepository;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.UserRoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class StartGameCommand extends Command {

    @Autowired
    private GameSessionRepository gameSessionRepository;
    @Autowired
    private DisciplineRepository disciplineRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    @Lazy
    private TelegramBotHandler telegramClient;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private UserService userService;

    @Override
    public String command() {
        return "/startgame";
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
            telegramClient.sendText(chatId, "Нет прав начать игровую сессию");
            return;
        }

        var disciplineName = BotMessageUtils.extractFirstArgument(update.getMessage().getText());
        if (disciplineName == null) {
            telegramClient.sendText(chatId, "Дисциплина не найдена");
            return;
        }

        var discipline = disciplineRepository.findByNameIgnoreCase(disciplineName);
        if (discipline.isEmpty()) {
            telegramClient.sendText(chatId, String.format("Дисциплина %s не найден", discipline));
            return;
        }

        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        var group = groupRepository.findByChannelAndSourceId(
                channelRepository.findByName(Channel.TELEGRAM.name()),
                chatId.toString()
        );
        if (group.isEmpty()) {
            log.error("Группа (channel={}, chatId={}) не найдена", channel.getId(), chatId);
            throw new GroupNotFoundException();
        }
        var gameSession = gameSessionRepository.findByChannelAndGroupAndDisciplineAndStateIn(
            channel, group.get(), discipline.get(), List.of(GameSessionState.PREPARING)
        );

        if (gameSession.isPresent()) {
            telegramClient.sendText(chatId, String.format("Игровая сессия по %s уже имеется", discipline.get().getName()));
            return;
        }

        var gameSessionEntity = createGameSession(channel, group.get(), discipline.get(), userEntity);
        telegramClient.sendText(chatId, String.format(
                "Игровая сессия по %s начинается! \n\nИгроки могут присоединиться к сессии командой \n/join %s",
                discipline.get().getName(),
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
