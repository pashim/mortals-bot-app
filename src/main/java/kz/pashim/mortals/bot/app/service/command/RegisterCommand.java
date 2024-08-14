package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.messages.MortalsMessageSource;
import kz.pashim.mortals.bot.app.configuration.properties.MortalsBotProperties;
import kz.pashim.mortals.bot.app.exception.ChannelNotFoundException;
import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.ChannelRepository;
import kz.pashim.mortals.bot.app.repository.DisciplineRepository;
import kz.pashim.mortals.bot.app.repository.GroupRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.repository.UserRepository;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class RegisterCommand extends AbstractCommand {
    public static final String COMMAND = "/register";

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final RatingRepository ratingRepository;
    private final MortalsMessageSource messageSource;
    private final MortalsBotProperties mortalsBotProperties;

    public RegisterCommand(
            BotCallbackStrategy botCallbackStrategy,
            ValidationService validationService,
            UserService userService,
            UserRepository userRepository,
            ChannelRepository channelRepository,
            GroupRepository groupRepository,
            DisciplineRepository disciplineRepository,
            RatingRepository ratingRepository,
            MortalsMessageSource messageSource,
            MortalsBotProperties mortalsBotProperties
    ) {
        super(botCallbackStrategy, validationService, userService);
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
        this.groupRepository = groupRepository;
        this.disciplineRepository = disciplineRepository;
        this.ratingRepository = ratingRepository;
        this.messageSource = messageSource;
        this.mortalsBotProperties = mortalsBotProperties;
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public void handle(BotEvent event) {
        var chatId = event.getChatId();
        if (chatId == null) {
            getBotCallback(event).sendMessage(event.getUserId(), messageSource.getMessage("bot.message.register.not.in.group"));
            return;
        }

        if (userRepository.findByGroupSourceIdAndSourceUserId(chatId, event.getUserId()).isPresent()) {
            getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.register.user.already.exists", event.getNickname()));
            return;
        }

        var userEntity = createUser(event);
        getBotCallback(event).sendMessage(chatId, messageSource.getMessage("bot.message.register.success", userEntity.getNickname()));
    }

    private UserEntity createUser(BotEvent event) {
        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        if (channel == null) {
            throw new ChannelNotFoundException(messageSource.getMessage("error.channel.not.found"));
        }
        var userEntityBuilder = UserEntity.builder()
                .nickname(event.getNickname())
                .sourceUserId(event.getUserId());

        var group = groupRepository.findByChannelAndSourceId(channel, event.getChatId())
                .orElseGet(() -> {
                    userEntityBuilder.role(UserRole.ADMIN);
                    return createGroup(channel, event.getChatId());
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
