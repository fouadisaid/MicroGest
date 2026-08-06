package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;
import said.microgest.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "adherent")
public class Operation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOperation type;

    @Column(nullable = false)
    private BigDecimal montant;

    private LocalDateTime dateOperation;

    @Column(length = 255)
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;
}