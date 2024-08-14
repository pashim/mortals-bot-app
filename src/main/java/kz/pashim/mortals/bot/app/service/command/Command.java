package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.model.event.BotEvent;

public abstract class Command {
    public abstract void execute(BotEvent event);
    public abstract String command();
}