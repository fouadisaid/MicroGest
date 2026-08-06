package said.microgest.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Agence extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false, length = 150)
    private String adresse;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(unique = true, length = 100)
    private String email;
}