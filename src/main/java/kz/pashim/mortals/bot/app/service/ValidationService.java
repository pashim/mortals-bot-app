package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.model.event.BotEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidationService {

    public boolean validate(BotEvent event) {
        if (event.getMessage() == null) {
            log.error("Пустой message в событии: {}", event);
            return false;
        }

        if (event.getUserId() == null) {
            log.error("Не найден ID пользователя: {}", event);
            return false;
        }

        if (event.getNickname() == null) {
            log.error("Не найден nickname пользователя: {}", event);
            return false;
        }

        return true;
    }
}
