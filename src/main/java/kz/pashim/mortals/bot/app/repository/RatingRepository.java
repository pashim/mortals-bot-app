package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import kz.pashim.mortals.bot.app.model.RatingEntity;
import kz.pashim.mortals.bot.app.model.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends CrudRepository<RatingEntity, Long> {
    RatingEntity findByChannelAndGroupAndDisciplineAndUser(ChannelEntity channel, GroupEntity group, DisciplineEntity discipline, UserEntity user);
}
