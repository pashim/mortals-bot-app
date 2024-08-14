package kz.pashim.mortals.bot.app.listener;

import kz.pashim.mortals.bot.app.exception.BotException;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.command.CommandRegistry;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotEventListener implements ApplicationListener<BotEvent> {

    private final CommandRegistry commandRegistry;
    private final BotCallbackStrategy botCallbackStrategy;

    @Override
    public void onApplicationEvent(BotEvent event) {
        var command = commandRegistry.getCommand(BotMessageUtils.extractCommand(event.getCommand()));
        try {
            command.execute(event);
        } catch (BotException exception) {
            exception.printStackTrace();
            botCallbackStrategy.getBotCallback(event.getChannel()).sendMessage(
                    event.getChatId() != null ? event.getChatId() : event.getUserId(),
                    exception.getMessage()
            );
        }
    }
}
