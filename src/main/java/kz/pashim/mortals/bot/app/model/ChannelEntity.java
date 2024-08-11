package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "channel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelEntity extends BaseEntity {
    private String name;
}
