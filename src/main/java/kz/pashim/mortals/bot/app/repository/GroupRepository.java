package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.ChannelEntity;
import kz.pashim.mortals.bot.app.model.GroupEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<GroupEntity, Long> {
    Optional<GroupEntity> findByChannelAndSourceId(ChannelEntity channel, String sourceId);
}
