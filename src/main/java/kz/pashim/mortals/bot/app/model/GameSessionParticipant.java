package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "game_session_participant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GameSessionParticipant extends BaseEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    private GameSessionEntity gameSession;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private Integer teamNumber;
}
