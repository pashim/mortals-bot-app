package kz.pashim.mortals.bot.app.listener;

public interface BotCallback {
    void sendMessage(String chatId, String message);
}
