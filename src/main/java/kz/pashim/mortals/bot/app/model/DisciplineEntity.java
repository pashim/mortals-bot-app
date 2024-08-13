package kz.pashim.mortals.bot.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "discipline")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisciplineEntity extends BaseEntity {
    private String name;
    private Integer teamMembersCount;
    private Integer teamsCount;

    public Integer getMaxSlots() {
        return teamsCount * teamMembersCount;
    }
}
