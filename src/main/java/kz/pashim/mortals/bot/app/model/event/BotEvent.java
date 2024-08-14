package kz.pashim.mortals.bot.app.model.event;

import kz.pashim.mortals.bot.app.model.Channel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
@Setter
public class BotEvent extends ApplicationEvent {
    private Object message;
    private Channel channel;
    private String chatId;
    private String userId;
    private String command;
    private String nickname;

    public BotEvent(
            Object source,
            Channel channel,
            Object message,
            String chatId,
            String userId,
            String command,
            String nickname
    ) {
        super(source);
        this.channel = channel;
        this.message = message;
        this.chatId = chatId;
        this.userId = userId;
        this.command = command;
        this.nickname = nickname;
    }

    public BotEvent(Object source, Clock clock) {
        super(source, clock);
    }

    public String getCallbackId() {
        return this.chatId != null ? this.chatId : this.userId;
    }
}
