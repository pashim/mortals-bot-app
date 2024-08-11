package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "game_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GameSessionEntity extends BaseEntity {
    private GameSessionState state;
    private ZonedDateTime time;
    @Column(name = "session_uuid")
    private UUID uuid;
    private Integer result;

    @ManyToOne
    private UserEntity initiator;
    @ManyToOne
    private ChannelEntity channel;
    @ManyToOne
    private GroupEntity group;
    @ManyToOne
    private DisciplineEntity discipline;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gameSession", orphanRemoval = true)
    private Set<GameSessionParticipant> participants = new HashSet<>();
}
