package kz.pashim.mortals.bot.app.listener;

import kz.pashim.mortals.bot.app.exception.BotException;
import kz.pashim.mortals.bot.app.service.command.CommandRegistry;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@RequiredArgsConstructor
public class TelegramBotHandler extends TelegramLongPollingBot implements BotCallback {

    private final CommandRegistry commandRegistry;

    @Value("${telegram.api.bot.name}")
    private String botName;

    @Value("${telegram.api.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    @Transactional
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        log.info("Received telegram event with message: {}", msg);
        var user = msg.getFrom();

        var command = commandRegistry.getCommand(BotMessageUtils.extractCommand(msg.getText()));
        if (command == null) {
            sendText(user.getId(), "хз че ты высрал...");
            return;
        }
        try {
            command.execute(update);
        } catch (BotException ignore) { }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(String chatId, String message) {
        sendText(Long.parseLong(chatId), message);
    }
}
