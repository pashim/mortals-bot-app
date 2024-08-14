package kz.pashim.mortals.bot.app.listener;

import kz.pashim.mortals.bot.app.model.Channel;

public interface BotCallback {
    void sendMessage(String chatId, String message);
    Channel getChannel();
}
