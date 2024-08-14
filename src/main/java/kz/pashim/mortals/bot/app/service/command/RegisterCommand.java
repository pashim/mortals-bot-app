package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.configuration.properties.MortalsBotProperties;
import kz.pashim.mortals.bot.app.exception.ChannelNotFoundException;
import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.repository.ChannelRepository;
import kz.pashim.mortals.bot.app.repository.DisciplineRepository;
import kz.pashim.mortals.bot.app.repository.GroupRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class RegisterCommand extends Command {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final RatingRepository ratingRepository;
    private final MortalsMessageSource messageSource;
    private final MortalsBotProperties mortalsBotProperties;
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
            telegramClient.sendText(user.getId(), messageSource.getMessage("bot.message.register.not.in.group"));
            return;
        }

        if (userRepository.findByGroupSourceIdAndSourceUserId(chatId.toString(), user.getId().toString()).isPresent()) {
            telegramClient.sendText(chatId, messageSource.getMessage("bot.message.register.user.already.exists", user.getUserName()));
            return;
        }

        var userEntity = createUser(user, chatId);
        telegramClient.sendText(chatId, messageSource.getMessage("bot.message.register.success", userEntity.getNickname()));
    }

    private UserEntity createUser(User user, Long chatId) {
        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        if (channel == null) {
            throw new ChannelNotFoundException(messageSource.getMessage("error.channel.not.found"));
        }
        var userEntityBuilder = UserEntity.builder()
                .nickname(user.getUserName())
                .sourceUserId(user.getId().toString());

        var group = groupRepository.findByChannelAndSourceId(channel, chatId.toString())
                .orElseGet(() -> {
                    userEntityBuilder.role(UserRole.ADMIN);
                    return createGroup(channel, chatId.toString());
                });


        var createdUser = userRepository.save(
                userEntityBuilder
                        .group(group)
                        .build()
        );

        disciplineRepository.findAll().forEach(discipline -> {
            createUserRatingForDiscipline(createdUser, group, discipline);
        });

        return createdUser;
    }

    private GroupEntity createGroup(ChannelEntity channel, String sourceId) {
        return groupRepository.save(GroupEntity.builder().channel(channel).sourceId(sourceId).build());
    }

    private void createUserRatingForDiscipline(UserEntity user, GroupEntity group, DisciplineEntity discipline) {
        ratingRepository.save(
                RatingEntity.builder()
                        .discipline(discipline)
                        .user(user)
                        .group(group)
                        .mmr(mortalsBotProperties.getDefaultMmrAssigned())
                        .channel(group.getChannel())
                        .build()
        );
    }
}
