package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.Source;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class RegisterCommand extends Command {

    private final UserRepository userRepository;
    private final TelegramBotHandler telegramClient;

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

        if (userRepository.findBySourceAndChannelIdAndSourceId(Source.TELEGRAM, chatId, user.getId()).isPresent()) {
            telegramClient.sendText(chatId, "Пользователь уже зарегистрирован");
            return;
        }

        userRepository.save(UserEntity.builder()
                .nickname(user.getUserName())
                .channelId(chatId)
                .source(Source.TELEGRAM)
                .sourceId(user.getId())
                .mmr(1000L)
                .build()
        );
    }
}
