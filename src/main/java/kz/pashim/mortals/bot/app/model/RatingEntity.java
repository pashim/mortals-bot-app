package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rating")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingEntity extends BaseEntity {
    private Integer mmr;

    @ManyToOne
    private ChannelEntity channel;
    @ManyToOne
    private GroupEntity group;
    @ManyToOne
    private DisciplineEntity discipline;
    @ManyToOne
    private UserEntity user;
}
