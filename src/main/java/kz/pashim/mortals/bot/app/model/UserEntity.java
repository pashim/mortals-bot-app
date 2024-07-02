package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "m_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    private String nickname;
    private Long mmr;
    @Enumerated(EnumType.STRING)
    private Source source;
    private Long sourceId;
    private Long channelId;
}
