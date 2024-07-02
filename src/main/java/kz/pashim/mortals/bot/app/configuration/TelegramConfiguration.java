package kz.pashim.mortals.bot.app.configuration;

import kz.pashim.mortals.bot.app.listener.TelegramBotListener;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfiguration {

//    public TelegramBotsApi telegramBotsApi() throws Exception {
//        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//        botsApi.registerBot(new TelegramBotListener());
//        return botsApi;
//    }
}
