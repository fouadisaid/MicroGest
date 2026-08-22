package said.microgest.services;

import said.microgest.entities.Adherent;
import said.microgest.entities.Agence;
import said.microgest.entities.Epargne;
import said.microgest.enums.StatutAdherent;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.AgenceRepository;
import said.microgest.repositories.EpargneRepository;
import said.microgest.utils.EmailUtil;
//import said.microgest.utils.EmailUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class AdherentService {

    private final AdherentRepository adherentRepository =
            new AdherentRepository();

    private final AgenceRepository agenceRepository =
            new AgenceRepository();

    private final EpargneRepository epargneRepository =
            new EpargneRepository();

    public List<Adherent> findAll() {
        return adherentRepository.findAll();
    }

    public Adherent findById(int id) {
        return adherentRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Adhérent introuvable."
                        )
                );
    }

    public List<Adherent> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return adherentRepository.search(keyword);
    }

    public List<Adherent> findPaginated(
            int page,
            int size
    ) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        return adherentRepository.findPaginated(
                page,
                size
        );
    }

    public long count() {
        return adherentRepository.count();
    }

    public List<Adherent> filter(
            StatutAdherent statut,
            Integer agenceId,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {

        Agence agence = null;

        if (agenceId != null && agenceId > 0) {

            agence =
                    agenceRepository.findById(agenceId)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Agence introuvable."
                                    )
                            );
        }

        return adherentRepository.filter(
                statut,
                agence,
                dateDebut,
                dateFin
        );
    }

    public Adherent create(
            Adherent adherent
    ) {

        validate(adherent);

        if (adherent.getNumeroAdherent() == null ||
                adherent.getNumeroAdherent().isBlank()) {

            adherent.setNumeroAdherent(
                    generateNumeroAdherent()
            );
        }

        adherentRepository
                .findByNumeroAdherent(
                        adherent.getNumeroAdherent()
                )
                .ifPresent(a -> {
                    throw new RuntimeException(
                            "Ce numéro d'adhérent existe déjà."
                    );
                });

        if (adherent.getEmail() != null &&
                !adherent.getEmail().isBlank()) {

            adherentRepository
                    .findByEmail(
                            adherent.getEmail()
                    )
                    .ifPresent(a -> {
                        throw new RuntimeException(
                                "Cet email existe déjà."
                        );
                    });
        }

        adherentRepository
                .findByTelephone(
                        adherent.getTelephone()
                )
                .ifPresent(a -> {
                    throw new RuntimeException(
                            "Ce téléphone existe déjà."
                    );
                });

        if (adherent.getDateAdhesion() == null) {
            adherent.setDateAdhesion(
                    LocalDate.now()
            );
        }

        if (adherent.getStatut() == null) {
            adherent.setStatut(
                    StatutAdherent.ACTIF
            );
        }

        Adherent saved =
                adherentRepository.save(
                        adherent
                );

        Epargne epargne =
                Epargne.builder()
                        .adherent(saved)
                        .solde(
                                java.math.BigDecimal.ZERO
                        )
                        .dateOuverture(
                                LocalDate.now()
                        )
                        .build();

        epargneRepository.save(epargne);

        // =========================================================
        // ENVOI EMAIL DE BIENVENUE
        // =========================================================
        try {
            EmailUtil.envoyerBienvenue(saved);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'envoi de l'email de bienvenue : " + e.getMessage());
        }

        return saved;
    }

    public Adherent update(
            Adherent adherent
    ) {

        Adherent old =
                adherentRepository
                        .findById(adherent.getId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Adhérent introuvable."
                                )
                        );

        validate(adherent);

        if (adherent.getEmail() != null &&
                !adherent.getEmail().isBlank()) {

            adherentRepository
                    .findByEmail(
                            adherent.getEmail()
                    )
                    .ifPresent(a -> {

                        if (a.getId() != adherent.getId()) {

                            throw new RuntimeException(
                                    "Cet email existe déjà."
                            );
                        }
                    });
        }

        adherentRepository
                .findByTelephone(
                        adherent.getTelephone()
                )
                .ifPresent(a -> {

                    if (a.getId() != adherent.getId()) {

                        throw new RuntimeException(
                                "Ce téléphone existe déjà."
                        );
                    }
                });

        adherent.setNumeroAdherent(
                old.getNumeroAdherent()
        );

        adherent.setCreatedAt(
                old.getCreatedAt()
        );

        adherent.setCreatedBy(
                old.getCreatedBy()
        );

        return adherentRepository.save(
                adherent
        );
    }

    public boolean delete(int id) {

        Adherent adherent =
                adherentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Adhérent introuvable."
                                )
                        );

        if (adherent.getEpargne() != null &&
                adherent.getEpargne()
                        .getSolde()
                        .compareTo(
                                java.math.BigDecimal.ZERO
                        ) > 0) {

            throw new RuntimeException(
                    "Impossible de supprimer un adhérent avec un solde d'épargne positif."
            );
        }

        return adherentRepository.delete(id);
    }

    public void toggleStatut(int id) {

        Adherent adherent =
                adherentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Adhérent introuvable."
                                )
                        );

        if (adherent.getStatut() ==
                StatutAdherent.ACTIF) {

            adherent.setStatut(
                    StatutAdherent.SUSPENDU
            );

        } else {

            adherent.setStatut(
                    StatutAdherent.ACTIF
            );
        }

        adherentRepository.save(
                adherent
        );
    }

    private String generateNumeroAdherent() {

        String prefix = "ADH";

        String year =
                String.valueOf(
                        LocalDate.now().getYear()
                );

        long count =
                adherentRepository.count() + 1;

        String sequence =
                String.format(
                        "%05d",
                        count
                );

        return prefix + year + sequence;
    }

    private void validate(
            Adherent adherent
    ) {

        if (adherent == null) {

            throw new RuntimeException(
                    "Les données de l'adhérent sont invalides."
            );
        }

        String nom =
                adherent.getNom() != null
                        ? adherent.getNom().trim()
                        : "";

        if (nom.isBlank()) {

            throw new RuntimeException(
                    "Le nom est obligatoire."
            );
        }

        if (!Pattern.matches(
                "^[\\p{L}][\\p{L} '\\-]*$",
                nom
        )) {

            throw new RuntimeException(
                    "Le nom contient des caractères invalides."
            );
        }

        String prenom =
                adherent.getPrenom() != null
                        ? adherent.getPrenom().trim()
                        : "";

        if (prenom.isBlank()) {

            throw new RuntimeException(
                    "Le prénom est obligatoire."
            );
        }

        if (!Pattern.matches(
                "^[\\p{L}][\\p{L} '\\-]*$",
                prenom
        )) {

            throw new RuntimeException(
                    "Le prénom contient des caractères invalides."
            );
        }

        if (adherent.getSexe() == null ||
                adherent.getSexe().isBlank()) {

            throw new RuntimeException(
                    "Le sexe est obligatoire."
            );
        }

        if (!adherent.getSexe().equals("M") &&
                !adherent.getSexe().equals("F")) {

            throw new RuntimeException(
                    "Le sexe sélectionné est invalide."
            );
        }

        if (adherent.getDateNaissance() == null) {

            throw new RuntimeException(
                    "La date de naissance est obligatoire."
            );
        }

        if (!adherent.getDateNaissance()
                .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "La date de naissance doit être antérieure à aujourd'hui."
            );
        }

        String telephone =
                adherent.getTelephone() != null
                        ? adherent.getTelephone()
                        .replaceAll("[\\s\\-().]", "")
                        : "";

        if (telephone.startsWith("+221")) {

            telephone =
                    telephone.substring(4);
        }

        if (!telephone.matches(
                "^7[05678]\\d{7}$"
        )) {

            throw new RuntimeException(
                    "Le numéro de téléphone sénégalais est invalide."
            );
        }

        adherent.setTelephone(
                telephone
        );

        String email =
                adherent.getEmail() != null
                        ? adherent.getEmail().trim()
                        : "";

        if (email.isBlank()) {

            throw new RuntimeException(
                    "L'email est obligatoire."
            );
        }

        if (!Pattern.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                email
        )) {

            throw new RuntimeException(
                    "L'adresse email est invalide."
            );
        }

        adherent.setEmail(email);

        String adresse =
                adherent.getAdresse() != null
                        ? adherent.getAdresse().trim()
                        : "";

        if (adresse.isBlank()) {

            throw new RuntimeException(
                    "L'adresse est obligatoire."
            );
        }

        if (adherent.getDateAdhesion() == null) {

            throw new RuntimeException(
                    "La date d'adhésion est obligatoire."
            );
        }

        if (adherent.getDateAdhesion()
                .isAfter(LocalDate.now())) {

            throw new RuntimeException(
                    "La date d'adhésion ne peut pas être dans le futur."
            );
        }

        if (adherent.getDateAdhesion()
                .isBefore(
                        adherent.getDateNaissance()
                )) {

            throw new RuntimeException(
                    "La date d'adhésion doit être postérieure à la date de naissance."
            );
        }

        if (adherent.getStatut() == null) {

            throw new RuntimeException(
                    "Le statut est obligatoire."
            );
        }

        if (adherent.getAgence() == null ||
                adherent.getAgence().getId() <= 0) {

            throw new RuntimeException(
                    "L'agence est obligatoire."
            );
        }

        adherent.setNom(nom);
        adherent.setPrenom(prenom);
        adherent.setAdresse(adresse);
    }
}