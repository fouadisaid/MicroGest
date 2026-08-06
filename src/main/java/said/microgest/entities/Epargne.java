package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "epargnes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "adherent")
public class Epargne extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal solde;

    private LocalDate dateOuverture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;
}