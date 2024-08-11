package kz.pashim.mortals.bot.app.util;

import kz.pashim.mortals.bot.app.exception.IllegalGameResultException;
import kz.pashim.mortals.bot.app.model.GameSessionParticipant;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

@Slf4j
public class TeamRatingUtils {

    public static final Integer RATING_DIFF = 25;

    public static void divideTeams(List<Pair<GameSessionParticipant, RatingEntity>> participants) {
        if (CollectionUtils.isEmpty(participants)) {
            return;
        }

        participants.sort(Comparator.comparing(it -> it.getSecond().getMmr()));
        for (int i = 0; i < participants.size(); i++) {
            if (i % 2 == 0) {
                participants.get(i).getFirst().setTeamNumber(1);
            } else {
                participants.get(i).getFirst().setTeamNumber(2);
            }
        }
    }

    public static void result(List<Pair<GameSessionParticipant, RatingEntity>> participants, Integer winner) {
        if (winner == null) {
            throw new IllegalGameResultException();
        }

        participants.forEach(entry -> {
            int diff = entry.getFirst().getTeamNumber().equals(winner) ? RATING_DIFF : -RATING_DIFF;
            entry.getSecond().setMmr(entry.getSecond().getMmr() + diff);
        });
    }
}
