package kz.pashim.mortals.bot.app.model;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
@SuperBuilder(toBuilder = true)
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class GameSessionEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private GameSessionState state;
    private ZonedDateTime date;
    @Column(name = "session_uuid")
    private UUID uuid;

    @ManyToOne
    private UserEntity initiator;
    @ManyToOne
    private ChannelEntity channel;
    @ManyToOne
    private GroupEntity group;
    @ManyToOne
    private DisciplineEntity discipline;
    @OneToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER, mappedBy = "gameSession", orphanRemoval = true)
    private Set<GameSessionParticipant> participants = new HashSet<>();
}
