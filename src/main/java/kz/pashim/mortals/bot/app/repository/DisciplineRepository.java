package kz.pashim.mortals.bot.app.repository;

import kz.pashim.mortals.bot.app.model.DisciplineEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisciplineRepository extends CrudRepository<DisciplineEntity, Long> {
    Optional<DisciplineEntity> findByNameIgnoreCase(String name);
}
