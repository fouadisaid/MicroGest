package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "remboursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "pret")
public class Remboursement extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal montant;

    private LocalDate datePaiement;

    private Integer numeroEcheance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    private Pret pret;
}