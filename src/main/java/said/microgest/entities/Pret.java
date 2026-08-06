package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;
import said.microgest.enums.StatutPret;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "prets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"adherent", "remboursements"})
public class Pret extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(nullable = false)
    private BigDecimal taux;

    @Column(nullable = false)
    private Integer duree;

    private LocalDate datePret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPret statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    @OneToMany(mappedBy = "pret", fetch = FetchType.LAZY)
    private List<Remboursement> remboursements;
}