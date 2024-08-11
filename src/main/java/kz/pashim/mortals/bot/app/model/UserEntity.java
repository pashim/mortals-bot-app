package kz.pashim.mortals.bot.app.model;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "_user")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SuperBuilder(toBuilder = true)
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class UserEntity extends BaseEntity {

    private String nickname;
    private String title;
    private String sourceUserId;
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private UserRole role;
    @ManyToOne
    private GroupEntity group;
}
