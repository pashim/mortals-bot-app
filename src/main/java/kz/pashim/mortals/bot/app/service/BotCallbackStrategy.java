package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.listener.BotCallback;
import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotCallbackStrategy {

    private final TelegramBotHandler telegramBotHandler;

    public BotCallback getBotCallback(Channel channel) {
        BotCallback callback = null;
        switch (channel) {
            case TELEGRAM -> callback = telegramBotHandler;
            case DISCORD -> {}
        }
        return callback;
    }
}
