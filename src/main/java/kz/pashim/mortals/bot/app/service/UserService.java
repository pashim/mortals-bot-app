package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.exception.UserNotFoundException;
import kz.pashim.mortals.bot.app.listener.BotCallback;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity getUser(Long groupId, Long sourceId, BotCallback callback) {
        var user = userRepository.findByGroupSourceIdAndSourceUserId(
                groupId, sourceId
        );

        if (user.isEmpty()) {
            var errorMessage = "Пользователь не найден";
            callback.sendMessage(groupId.toString(), errorMessage);
            throw new UserNotFoundException();
        }

        return user.get();
    }
}
