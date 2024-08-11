package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserEntity extends BaseEntity {
    private String nickname;
    private String title;
    private String sourceUserId;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @ManyToOne
    private GroupEntity group;
}
