package kz.pashim.mortals.bot.app.exception;

public class GroupNotFoundException extends BotException {
    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException() {
        super("Группа не найдена");
    }
}
