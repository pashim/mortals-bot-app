package kz.pashim.mortals.bot.app.controller;

import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    @GetMapping
    public String hello() {
        return "Hello world!";
    }

    @GetMapping("/create")
    public String create(@RequestParam String nickname) {
        return userRepository.save(UserEntity.builder().nickname(nickname).mmr(1000L).build()).getId().toString();
    }
}
