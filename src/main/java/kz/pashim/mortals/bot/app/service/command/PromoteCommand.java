package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.UserRepository;
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
public class PromoteCommand extends AbstractCommand {
    public static final String COMMAND = "/promote";

    private final UserRepository userRepository;
    private final MortalsMessageSource messageSource;

    public PromoteCommand(
            BotCallbackStrategy botCallbackStrategy,
            ValidationService validationService,
            UserService userService,
            UserRepository userRepository,
            @Qualifier("messageSource") MortalsMessageSource messageSource) {
        super(botCallbackStrategy, validationService, userService);
        this.userRepository = userRepository;
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
        if (!UserRoleUtils.hasPermission(userEntity, UserRole.ADMIN)) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.promote.unauthorized"));
            return;
        }

        var userName = BotMessageUtils.extractFirstArgument(event.getCommand());
        if (userName == null) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.common.user.not.found"));
            return;
        }

        var userToPromote = userRepository.findByGroupSourceIdAndNickname(chatId, userName);
        if (userToPromote.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.common.user.not.found.with.name", userName));
            return;
        }

        promote(userToPromote.get(), UserRole.MODERATOR);
        getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.promote.success", userToPromote.get().getNickname(), UserRole.MODERATOR.displayName));
    }

    private void promote(UserEntity user, UserRole userRole) {
        userRepository.save(user.toBuilder().role(userRole).build());
    }
}
