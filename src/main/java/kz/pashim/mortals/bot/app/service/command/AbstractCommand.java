package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.exception.BotException;
import kz.pashim.mortals.bot.app.listener.BotCallback;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractCommand extends Command {

    protected BotCallbackStrategy botCallbackStrategy;
    protected ValidationService validationService;
    protected UserService userService;

    @Override
    public void execute(BotEvent event) {
        try {
            validationService.validate(event);
            handle(event);
        } catch (BotException exception) {
            getBotCallback(event).sendMessage(event.getCallbackId(), exception.getMessage());
        }
    }

    protected BotCallback getBotCallback(BotEvent event) {
        return botCallbackStrategy.getBotCallback(event.getChannel());
    }

    protected abstract void handle(BotEvent event);
}
