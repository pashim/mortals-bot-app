package kz.pashim.mortals.bot.app.exception;

public class UserNotFoundException extends BotException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
        super("Пользователь не найден");
    }
}
