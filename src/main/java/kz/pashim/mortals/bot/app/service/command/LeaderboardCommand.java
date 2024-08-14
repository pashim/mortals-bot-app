package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.properties.MortalsBotProperties;
import kz.pashim.mortals.bot.app.exception.GroupNotFoundException;
import kz.pashim.mortals.bot.app.model.Channel;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.event.BotEvent;
import kz.pashim.mortals.bot.app.repository.ChannelRepository;
import kz.pashim.mortals.bot.app.repository.DisciplineRepository;
import kz.pashim.mortals.bot.app.repository.GroupRepository;
import kz.pashim.mortals.bot.app.repository.RatingRepository;
import kz.pashim.mortals.bot.app.service.BotCallbackStrategy;
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class LeaderboardCommand extends AbstractCommand {
    public static final String COMMAND = "/leaderboard";

    private final DisciplineRepository disciplineRepository;
    private final RatingRepository ratingRepository;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final MortalsBotProperties mortalsBotProperties;

    public LeaderboardCommand(
            BotCallbackStrategy botCallbackStrategy,
            ValidationService validationService,
            UserService userService,
            DisciplineRepository disciplineRepository,
            RatingRepository ratingRepository,
            ChannelRepository channelRepository,
            GroupRepository groupRepository,
            MortalsBotProperties mortalsBotProperties
    ) {
        super(botCallbackStrategy, validationService, userService);
        this.disciplineRepository = disciplineRepository;
        this.ratingRepository = ratingRepository;
        this.channelRepository = channelRepository;
        this.groupRepository = groupRepository;
        this.mortalsBotProperties = mortalsBotProperties;
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public void handle(BotEvent event) {
        var chatId = event.getChatId();
        var disciplineName = BotMessageUtils.extractFirstArgument(event.getCommand());
        if (disciplineName == null) {
            getBotCallback(event).sendMessage(chatId, "Дисциплина не найдена");
            return;
        }

        var discipline = disciplineRepository.findByNameIgnoreCase(disciplineName);
        if (discipline.isEmpty()) {
            getBotCallback(event).sendMessage(chatId, String.format("Дисциплина %s не найден", disciplineName));
            return;
        }

        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        var group = groupRepository.findByChannelAndSourceId(channel, chatId);
        if (group.isEmpty()) {
            log.error("Группа (channel={}, chatId={}) не найдена", channel.getId(), chatId);
            throw new GroupNotFoundException();
        }

        getBotCallback(event).sendMessage(chatId, String.format(
                "Таблица лидеров по дисциплине %s\n\n%s",
                discipline.get().getName(),
                buildLeaderBoardMessage(discipline.get(), group.get()))
        );
    }

    private String buildLeaderBoardMessage(DisciplineEntity disciplineEntity, GroupEntity groupEntity) {
        StringBuilder stringBuilder = new StringBuilder();
        var ratings = ratingRepository.findByChannelAndGroupAndDisciplineOrderByMmrDesc(
                groupEntity.getChannel(),
                groupEntity,
                disciplineEntity,
                PageRequest.of(0, mortalsBotProperties.getLeaderboard().getMaxPlayersToDisplay())
        );
        var iterator = ratings.getContent().iterator();
        var place = 1;
        while (iterator.hasNext()) {
            var rating = iterator.next();
            stringBuilder.append(String.format("%d. %s [%s]", place++, rating.getUser().getNickname(), rating.getMmr())).append("\n");
        }
        return stringBuilder.toString();
    }
}
