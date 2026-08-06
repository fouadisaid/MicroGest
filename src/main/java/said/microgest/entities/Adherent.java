package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;
import said.microgest.enums.StatutAdherent;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "adherents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"agence", "epargne", "operations", "prets"})
public class Adherent extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String numeroAdherent;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String sexe;

    private LocalDate dateNaissance;

    private String adresse;

    @Column(nullable = false, unique = true)
    private String telephone;

    @Column(unique = true)
    private String email;

    private LocalDate dateAdhesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAdherent statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    @OneToOne(mappedBy = "adherent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Epargne epargne;

    @OneToMany(mappedBy = "adherent", fetch = FetchType.LAZY)
    private List<Operation> operations;

    @OneToMany(mappedBy = "adherent", fetch = FetchType.LAZY)
    private List<Pret> prets;
}