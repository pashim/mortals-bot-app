package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GameSessionEntity;
import kz.pashim.mortals.bot.app.model.GameSessionState;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionRepository extends CrudRepository<GameSessionEntity, Long> {
    Optional<GameSessionEntity> findByChannelAndGroupAndDisciplineAndState(ChannelEntity channel, GroupEntity group, DisciplineEntity discipline, GameSessionState gameSessionState);
}
