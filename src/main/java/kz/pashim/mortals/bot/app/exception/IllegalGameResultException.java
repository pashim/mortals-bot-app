package kz.pashim.mortals.bot.app.exception;

public class IllegalGameResultException extends BotException {
    public IllegalGameResultException(String message) {
        super(message);
    }

    public IllegalGameResultException() {
        super("Ошибка завершения игровой сессии");
    }
}
