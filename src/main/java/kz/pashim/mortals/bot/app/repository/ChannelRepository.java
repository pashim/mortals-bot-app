package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {
    ChannelEntity findByName(String name);
}
