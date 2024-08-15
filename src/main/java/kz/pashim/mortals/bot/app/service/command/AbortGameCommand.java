package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.exception.GroupNotFoundException;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionState;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class AbortGameCommand extends AbstractCommand {
    public static final String COMMAND = "/abort";

    private final GameSessionRepository gameSessionRepository;
    private final DisciplineRepository disciplineRepository;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final MortalsMessageSource messageSource;

    public AbortGameCommand(
            ValidationService validationService,
            BotCallbackStrategy botCallbackStrategy,
            GameSessionRepository gameSessionRepository,
            DisciplineRepository disciplineRepository,
            ChannelRepository channelRepository,
            GroupRepository groupRepository,
            UserService userService,
            @Qualifier("messageSource") MortalsMessageSource messageSource) {
        super(botCallbackStrategy, validationService, userService);
        this.gameSessionRepository = gameSessionRepository;
        this.disciplineRepository = disciplineRepository;
        this.channelRepository = channelRepository;
        this.groupRepository = groupRepository;
        this.messageSource = messageSource;
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public void handle(BotEvent event) {
        var chatId = event.getChatId();
        var userEntity = userService.getUser(
                event.getChatId(),
                event.getUserId(),
                botCallbackStrategy.getBotCallback(event.getChannel())
        );

        if (!UserRoleUtils.hasPermission(userEntity, UserRole.MODERATOR)) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.abort.game.command.no.rights.to.interrupt.game.session"));
            return;
        }

        var disciplineName = BotMessageUtils.extractFirstArgument(event.getCommand());
        if (disciplineName == null) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.common.discipline.not.found"));
            return;
        }

        var discipline = disciplineRepository.findByNameIgnoreCase(disciplineName);
        if (discipline.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.common.discipline.not.found", disciplineName));
            return;
        }

        var channel = channelRepository.findByName(event.getChannel().name());
        var group = groupRepository.findByChannelAndSourceId(channel, chatId);
        if (group.isEmpty()) {
            log.error(messageSource.getMessage("error.channel.chat.not.found", channel.getId(), chatId));
            throw new GroupNotFoundException();
        }
        var gameSession = gameSessionRepository.findByChannelAndGroupAndDisciplineAndStateIn(
            channel, group.get(), discipline.get(), GameSessionState.liveStates()
        );

        if (gameSession.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message,abort.game.command.not.found", discipline.get().getName()));
            return;
        }

        abortGameSession(gameSession.get());
        getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.abort.game.command.session.interrupted", discipline.get().getName()));
    }

    private void abortGameSession(GameSessionEntity gameSessionEntity) {
        gameSessionRepository.save(
                gameSessionEntity.toBuilder()
                        .state(GameSessionState.ABORTED)
                        .participants(gameSessionEntity.getParticipants())
                        .build()
        );
    }
}
