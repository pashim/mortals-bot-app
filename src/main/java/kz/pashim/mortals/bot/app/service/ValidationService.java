package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.listener.BotCallback;
import kz.pashim.mortals.bot.app.service.command.GameResultCommand;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ValidationService {

    private static final Set<String> VALID_TEAM_RESULT_SET = Set.of("1", "2");

    public boolean validate(Update update, String command, BotCallback callback) {
        var message = update.getMessage();

        if (message == null) {
            log.error("Пустой message в событии: {}", update.getUpdateId());
            return false;
        }

        if (message.getChatId() == null) {
            log.warn("Пустой chatId, обработка команды {} пропущена, user: [{}]",
                    command,
                    message.getFrom().getUserName());
            return false;
        }

        var groupId = message.getChatId().toString();
        if (GameResultCommand.COMMAND.equals(command)) {
            return validateResultCommand(message.getText(), groupId, callback);
        }

        return true;
    }

    private boolean validateResultCommand(String text, String groupId, BotCallback callback) {
        var args = BotMessageUtils.extractArguments(text);
        var errorMessage = "Пожалуйста введите команду по формату: /result {sessionId} {result}";
        if (args == null || args.length != 2) {
            callback.sendMessage(groupId, errorMessage);
            return false;
        }
        try {
            UUID.fromString(args[0]);
        } catch (Exception ex) {
            callback.sendMessage(groupId, errorMessage);
            return false;
        }

        if (!VALID_TEAM_RESULT_SET.contains(args[1])) {
            callback.sendMessage(groupId, errorMessage);
        }
        return true;
    }
}
