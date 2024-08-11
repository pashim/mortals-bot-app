package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
public class RegisterCommand extends Command {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private TelegramBotHandler telegramClient;

    @Override
    public String command() {
        return "/register";
    }

    @Override
    public void execute(Update update) {
        var user = update.getMessage().getFrom();
        var chatId = update.getMessage().getChatId();

        if (chatId == null) {
            telegramClient.sendText(user.getId(), "Что бы зарегистрироваться в лиге, вам нужно запустить эту же команду из группового чата");
            return;
        }

        if (userRepository.findBySourceAndChannelIdAndSourceId(GameSessionState.TELEGRAM, chatId, user.getId()).isPresent()) {
            telegramClient.sendText(chatId, String.format("Пользователь %s уже зарегистрирован", user.getId()));
            return;
        }

        var userEntity = userRepository.save(UserEntity.builder()
                .nickname(user.getUserName())
                .channelId(chatId)
                .source(GameSessionState.TELEGRAM)
                .sourceId(user.getId())
                .mmr(1000L)
                .build()
        );
        telegramClient.sendText(chatId, String.format("Пользователь %s успешно зареган, ммр [%s]",
                userEntity.getNickname(), userEntity.getMmr()));
    }
}
