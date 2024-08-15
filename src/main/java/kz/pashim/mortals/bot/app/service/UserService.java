package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.exception.UserNotFoundException;
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
    private final MortalsMessageSource messageSource;

    public UserEntity getUser(String groupId, String sourceId) {
        var user = userRepository.findByGroupSourceIdAndSourceUserId(groupId, sourceId);
        if (user.isEmpty()) {
            var errorMessage = messageSource.getMessage("bot.message.common.user.not.found");
            throw new UserNotFoundException(errorMessage);
        }
        return user.get();
    }
}
