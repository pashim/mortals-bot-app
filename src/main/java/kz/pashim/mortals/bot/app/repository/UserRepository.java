package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByGroupSourceIdAndSourceUserId(Long groupId, Long sourceUserId);
    Optional<UserEntity> findByGroupSourceIdAndNickname(Long groupId, String nickname);
}
