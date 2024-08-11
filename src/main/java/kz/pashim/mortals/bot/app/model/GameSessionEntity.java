package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "game_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionEntity extends BaseEntity {
    private GameSessionState state;
    private ZonedDateTime time;

    @ManyToOne
    private ChannelEntity channel;
    @ManyToOne
    private GroupEntity group;
    @ManyToOne
    private DisciplineEntity discipline;
    @ManyToMany
    @JoinTable(
            name = "game_session_participant",
            joinColumns = @JoinColumn(name = "game_session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> participants;
}
