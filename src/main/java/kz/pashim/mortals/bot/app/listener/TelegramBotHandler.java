package kz.pashim.mortals.bot.app.listener;

import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher applicationEventPublisher;

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
        log.debug("Received telegram event with message: {}", msg);
        applicationEventPublisher.publishEvent(new BotEvent(
                this,
                getChannel(),
                update,
                msg.getChatId() == null ? null : msg.getChatId().toString(),
                msg.getFrom() == null ? null : msg.getFrom().getId().toString(),
                msg.getText(),
                msg.getFrom() == null ? null : msg.getFrom().getUserName()
        ));
    }

    public void sendText(Long chatId, String message) {
        try {
            execute(
                    SendMessage.builder()
                            .chatId(chatId.toString())
                            .parseMode("MarkdownV2")
                            .text(message)
                            .build()
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(String chatId, String message) {
        sendText(Long.parseLong(chatId), message);
    }

    @Override
    public Channel getChannel() {
        return Channel.TELEGRAM;
    }
}
