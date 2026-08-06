package said.microgest.services;

import said.microgest.entities.Adherent;
import said.microgest.entities.Agence;
import said.microgest.entities.Epargne;
import said.microgest.enums.StatutAdherent;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.AgenceRepository;
import said.microgest.repositories.EpargneRepository;
import said.microgest.utils.SessionContext;

import java.time.LocalDate;
import java.util.List;

public class AdherentService {

    private final AdherentRepository adherentRepository = new AdherentRepository();
    private final AgenceRepository agenceRepository = new AgenceRepository();
    private final EpargneRepository epargneRepository = new EpargneRepository();

    public List<Adherent> findAll() {
        return adherentRepository.findAll();
    }

    public Adherent findById(int id) {
        return adherentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));
    }

    public List<Adherent> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return adherentRepository.search(keyword);
    }

    public List<Adherent> findPaginated(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        return adherentRepository.findPaginated(page, size);
    }

    public long count() {
        return adherentRepository.count();
    }

    public List<Adherent> filter(StatutAdherent statut, Integer agenceId, LocalDate dateDebut, LocalDate dateFin) {
        Agence agence = null;
        if (agenceId != null && agenceId > 0) {
            agence = agenceRepository.findById(agenceId)
                    .orElseThrow(() -> new RuntimeException("Agence introuvable."));
        }
        return adherentRepository.filter(statut, agence, dateDebut, dateFin);
    }

    public Adherent create(Adherent adherent) {
        validate(adherent);

        if (adherent.getNumeroAdherent() == null || adherent.getNumeroAdherent().isBlank()) {
            adherent.setNumeroAdherent(generateNumeroAdherent());
        }

        adherentRepository.findByNumeroAdherent(adherent.getNumeroAdherent())
                .ifPresent(a -> {
                    throw new RuntimeException("Ce numéro d'adhérent existe déjà.");
                });

        if (adherent.getEmail() != null && !adherent.getEmail().isBlank()) {
            adherentRepository.findByEmail(adherent.getEmail())
                    .ifPresent(a -> {
                        throw new RuntimeException("Cet email existe déjà.");
                    });
        }

        adherentRepository.findByTelephone(adherent.getTelephone())
                .ifPresent(a -> {
                    throw new RuntimeException("Ce téléphone existe déjà.");
                });

        if (adherent.getDateAdhesion() == null) {
            adherent.setDateAdhesion(LocalDate.now());
        }

        if (adherent.getStatut() == null) {
            adherent.setStatut(StatutAdherent.ACTIF);
        }

        Adherent saved = adherentRepository.save(adherent);

        Epargne epargne = Epargne.builder()
                .adherent(saved)
                .solde(java.math.BigDecimal.ZERO)
                .dateOuverture(LocalDate.now())
                .build();

        epargneRepository.save(epargne);

        return saved;
    }

    public Adherent update(Adherent adherent) {
        Adherent old = adherentRepository.findById(adherent.getId())
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        validate(adherent);

        if (adherent.getEmail() != null && !adherent.getEmail().isBlank()) {
            adherentRepository.findByEmail(adherent.getEmail())
                    .ifPresent(a -> {
                        if (a.getId() != adherent.getId()) {
                            throw new RuntimeException("Cet email existe déjà.");
                        }
                    });
        }

        adherentRepository.findByTelephone(adherent.getTelephone())
                .ifPresent(a -> {
                    if (a.getId() != adherent.getId()) {
                        throw new RuntimeException("Ce téléphone existe déjà.");
                    }
                });

        adherent.setNumeroAdherent(old.getNumeroAdherent());
        adherent.setCreatedAt(old.getCreatedAt());
        adherent.setCreatedBy(old.getCreatedBy());

        return adherentRepository.save(adherent);
    }

    public boolean delete(int id) {
        Adherent adherent = adherentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        if (adherent.getEpargne() != null && adherent.getEpargne().getSolde().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Impossible de supprimer un adhérent avec un solde d'épargne positif.");
        }

        return adherentRepository.delete(id);
    }

    public void toggleStatut(int id) {
        Adherent adherent = findById(id);
        if (adherent.getStatut() == StatutAdherent.ACTIF) {
            adherent.setStatut(StatutAdherent.SUSPENDU);
        } else {
            adherent.setStatut(StatutAdherent.ACTIF);
        }
        adherentRepository.save(adherent);
    }

    private String generateNumeroAdherent() {
        String prefix = "ADH";
        String year = String.valueOf(LocalDate.now().getYear());
        long count = adherentRepository.count() + 1;
        String sequence = String.format("%05d", count);
        return prefix + year + sequence;
    }

    private void validate(Adherent adherent) {
        if (adherent.getNom() == null || adherent.getNom().isBlank()) {
            throw new RuntimeException("Le nom est obligatoire.");
        }
        if (adherent.getPrenom() == null || adherent.getPrenom().isBlank()) {
            throw new RuntimeException("Le prénom est obligatoire.");
        }
        if (adherent.getSexe() == null || adherent.getSexe().isBlank()) {
            throw new RuntimeException("Le sexe est obligatoire.");
        }
        if (adherent.getTelephone() == null || adherent.getTelephone().isBlank()) {
            throw new RuntimeException("Le téléphone est obligatoire.");
        }
        if (adherent.getAgence() == null || adherent.getAgence().getId() == 0) {
            throw new RuntimeException("L'agence est obligatoire.");
        }
    }
}