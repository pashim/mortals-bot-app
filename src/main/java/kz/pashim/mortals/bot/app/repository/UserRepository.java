package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
}
