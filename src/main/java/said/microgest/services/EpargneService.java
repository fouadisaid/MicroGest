package said.microgest.services;

import said.microgest.entities.Adherent;
import said.microgest.entities.Epargne;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.EpargneRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EpargneService {

    private final EpargneRepository epargneRepository = new EpargneRepository();
    private final AdherentRepository adherentRepository = new AdherentRepository();

    public List<Epargne> findAll() {
        return epargneRepository.findAll();
    }

    public Epargne findById(int id) {
        return epargneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épargne introuvable."));
    }

    public Epargne findByAdherent(int adherentId) {
        return epargneRepository.findByAdherent(adherentId)
                .orElseThrow(() -> new RuntimeException("Aucune épargne trouvée pour cet adhérent."));
    }

    public Epargne create(Epargne epargne) {
        validate(epargne);

        if (epargneRepository.findByAdherent(epargne.getAdherent().getId()).isPresent()) {
            throw new RuntimeException("Cet adhérent a déjà une épargne.");
        }

        if (epargne.getSolde() == null) {
            epargne.setSolde(BigDecimal.ZERO);
        }

        if (epargne.getDateOuverture() == null) {
            epargne.setDateOuverture(LocalDate.now());
        }

        return epargneRepository.save(epargne);
    }

    public Epargne update(Epargne epargne) {
        epargneRepository.findById(epargne.getId())
                .orElseThrow(() -> new RuntimeException("Épargne introuvable."));

        validate(epargne);

        return epargneRepository.save(epargne);
    }

    public BigDecimal calculerInterets(int adherentId) {
        Epargne epargne = findByAdherent(adherentId);
        return calculerInterets(epargne);
    }

    public BigDecimal calculerInterets(Epargne epargne) {
        LocalDate aujourdHui = LocalDate.now();
        LocalDate dateDernierCalcul = epargne.getDateOuverture();

        long jours = ChronoUnit.DAYS.between(dateDernierCalcul, aujourdHui);

        if (jours <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tauxJournalier = BigDecimal.valueOf(3.5)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);

        BigDecimal interets = epargne.getSolde()
                .multiply(tauxJournalier)
                .multiply(BigDecimal.valueOf(jours))
                .setScale(2, RoundingMode.HALF_UP);

        return interets;
    }

    public BigDecimal ajouterInterets(int adherentId) {
        Epargne epargne = findByAdherent(adherentId);
        BigDecimal interets = calculerInterets(epargne);

        if (interets.compareTo(BigDecimal.ZERO) > 0) {
            epargne.setSolde(epargne.getSolde().add(interets));
            epargneRepository.save(epargne);
        }

        return interets;
    }

    public BigDecimal getTotalEpargne() {
        List<Epargne> epargnes = epargneRepository.findAll();
        return epargnes.stream()
                .map(Epargne::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean delete(int id) {
        Epargne epargne = epargneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épargne introuvable."));

        if (epargne.getSolde().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Impossible de supprimer une épargne avec un solde positif.");
        }

        return epargneRepository.delete(id);
    }

    private void validate(Epargne epargne) {
        if (epargne.getAdherent() == null || epargne.getAdherent().getId() == 0) {
            throw new RuntimeException("L'adhérent est obligatoire.");
        }
        if (epargne.getSolde() == null || epargne.getSolde().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Le solde ne peut pas être négatif.");
        }
    }
}