package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEntity extends BaseEntity {
    private String sourceId;
    @ManyToOne
    private ChannelEntity channel;
}
