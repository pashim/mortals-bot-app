package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.repository.UserRepository;
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

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class PromoteCommand extends Command {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private TelegramBotHandler telegramClient;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private UserService userService;

    @Override
    public String command() {
        return "/promote";
    }

    @Override
    public void execute(Update update) {
        if (!validationService.validate(update, command(), telegramClient)) {
            return;
        }

        var user = update.getMessage().getFrom();
        var chatId = update.getMessage().getChatId();

        var userEntity = userService.getUser(chatId, user.getId(), telegramClient);
        if (!UserRoleUtils.hasPermission(userEntity, UserRole.ADMIN)) {
            telegramClient.sendText(chatId, "Только администраторы канала могут назначать роли");
            return;
        }

        var userName = BotMessageUtils.extractFirstArgument(update.getMessage().getText());
        if (userName == null) {
            telegramClient.sendText(chatId, "Пользователь не найден");
            return;
        }

        var userToPromote = userRepository.findByGroupSourceIdAndNickname(chatId, userName);
        if (userToPromote.isEmpty()) {
            telegramClient.sendText(chatId, String.format("Пользователь %s не найден", userName));
            return;
        }

        promote(userToPromote.get(), UserRole.MODERATOR);
        telegramClient.sendText(chatId, String.format("Пользователю %s назначена роль: %s", user.getUserName(), UserRole.MODERATOR.displayName));
    }

    private void promote(UserEntity user, UserRole userRole) {
        userRepository.save(user.toBuilder().role(userRole).build());
    }
}
