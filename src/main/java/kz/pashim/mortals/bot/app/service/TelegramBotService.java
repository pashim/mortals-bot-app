package kz.pashim.mortals.bot.app.service;

import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final UserRepository userRepository;

    private static ConcurrentHashMap<Long, Set<UserEntity>> GAME_SESSION = new ConcurrentHashMap<>();

    public void createGameSession(Long who, String what){

    }
}
