package kz.pashim.mortals.bot.app.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ValidationService {

    public boolean validate(Update update, String command) {
        var message = update.getMessage();

        if (message == null) {
            log.error("Пустой message в событии: {}", update.getUpdateId());
            return false;
        }

        if (message.getChatId() == null) {
            log.warn("Пустой chatId, обработка команды {} пропущена, user: [{}]",
                    command,
                    message.getFrom().getUserName());
            return false;
        }

        return true;
    }
}
