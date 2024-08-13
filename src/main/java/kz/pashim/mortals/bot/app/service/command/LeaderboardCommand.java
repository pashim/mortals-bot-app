package kz.pashim.mortals.bot.app.service.command;

import kz.pashim.mortals.bot.app.configuration.properties.MortalsBotProperties;
import kz.pashim.mortals.bot.app.exception.GroupNotFoundException;
import kz.pashim.mortals.bot.app.listener.TelegramBotHandler;
import kz.pashim.mortals.bot.app.model.Channel;
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
import kz.pashim.mortals.bot.app.service.UserService;
import kz.pashim.mortals.bot.app.service.ValidationService;
import kz.pashim.mortals.bot.app.util.BotMessageUtils;
import kz.pashim.mortals.bot.app.util.UserRoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("telegram.api.bot.enabled")
@Slf4j
public class LeaderboardCommand extends Command {


    @Autowired
    private DisciplineRepository disciplineRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MortalsBotProperties mortalsBotProperties;
    @Autowired
    @Lazy
    private TelegramBotHandler telegramClient;
    @Autowired
    private ValidationService validationService;

    @Override
    public String command() {
        return "/leaderboard";
    }

    @Override
    public void execute(Update update) {
        if (!validationService.validate(update, command(), telegramClient)) {
            return;
        }

        var user = update.getMessage().getFrom();
        var chatId = update.getMessage().getChatId();

        var disciplineName = BotMessageUtils.extractFirstArgument(update.getMessage().getText());
        if (disciplineName == null) {
            telegramClient.sendText(chatId, "Дисциплина не найдена");
            return;
        }

        var discipline = disciplineRepository.findByNameIgnoreCase(disciplineName);
        if (discipline.isEmpty()) {
            telegramClient.sendText(chatId, String.format("Дисциплина %s не найден", disciplineName));
            return;
        }

        var channel = channelRepository.findByName(Channel.TELEGRAM.name());
        var group = groupRepository.findByChannelAndSourceId(channel, chatId.toString());
        if (group.isEmpty()) {
            log.error("Группа (channel={}, chatId={}) не найдена", channel.getId(), chatId);
            throw new GroupNotFoundException();
        }

        telegramClient.sendText(chatId, String.format(
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
