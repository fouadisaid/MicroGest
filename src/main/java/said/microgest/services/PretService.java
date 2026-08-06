package said.microgest.services;

import said.microgest.entities.Adherent;
import said.microgest.entities.Pret;
import said.microgest.entities.Remboursement;
import said.microgest.enums.StatutPret;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.PretRepository;
import said.microgest.repositories.RemboursementRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class PretService {

    private final PretRepository pretRepository = new PretRepository();
    private final AdherentRepository adherentRepository = new AdherentRepository();
    private final RemboursementRepository remboursementRepository = new RemboursementRepository();

    public List<Pret> findAll() {
        return pretRepository.findAll();
    }

    public Pret findById(int id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));
    }

    public List<Pret> findByAdherent(int adherentId) {
        adherentRepository.findById(adherentId)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));
        return pretRepository.findByAdherent(adherentId);
    }

    public Pret create(Pret pret) {
        validate(pret);

        Adherent adherent = adherentRepository.findById(pret.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        pret.setAdherent(adherent);

        if (pret.getDatePret() == null) {
            pret.setDatePret(LocalDate.now());
        }

        if (pret.getStatut() == null) {
            pret.setStatut(StatutPret.EN_ATTENTE);
        }

        if (pret.getTaux() == null) {
            pret.setTaux(BigDecimal.valueOf(5.5));
        }

        return pretRepository.save(pret);
    }

    public Pret update(Pret pret) {
        pretRepository.findById(pret.getId())
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        validate(pret);

        return pretRepository.save(pret);
    }

    public Pret valider(int pretId) {
        Pret pret = findById(pretId);

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new RuntimeException("Seul un prêt en attente peut être validé.");
        }

        pret.setStatut(StatutPret.VALIDE);
        return pretRepository.save(pret);
    }

    public Pret rejeter(int pretId) {
        Pret pret = findById(pretId);

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new RuntimeException("Seul un prêt en attente peut être rejeté.");
        }

        pret.setStatut(StatutPret.REJETE);
        return pretRepository.save(pret);
    }

    public BigDecimal calculerMensualite(Pret pret) {
        if (pret.getDuree() == null || pret.getDuree() == 0) {
            throw new RuntimeException("La durée du prêt est invalide.");
        }

        BigDecimal tauxMensuel = pret.getTaux()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal unPlusTaux = BigDecimal.ONE.add(tauxMensuel);
        BigDecimal puissance = unPlusTaux.pow(pret.getDuree());

        BigDecimal numerateur = pret.getMontant().multiply(tauxMensuel).multiply(puissance);
        BigDecimal denominateur = puissance.subtract(BigDecimal.ONE);

        return numerateur.divide(denominateur, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMontantRestant(int pretId) {
        Pret pret = findById(pretId);

        BigDecimal totalRembourse = pret.getRemboursements().stream()
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return pret.getMontant().subtract(totalRembourse);
    }

    public void rembourser(int pretId, BigDecimal montant) {
        Pret pret = findById(pretId);

        if (pret.getStatut() != StatutPret.VALIDE) {
            throw new RuntimeException("Seul un prêt validé peut être remboursé.");
        }

        BigDecimal restant = getMontantRestant(pretId);

        if (montant.compareTo(restant) > 0) {
            throw new RuntimeException("Le montant du remboursement dépasse le montant restant.");
        }

        Remboursement remboursement = Remboursement.builder()
                .pret(pret)
                .montant(montant)
                .datePaiement(LocalDate.now())
                .numeroEcheance(pret.getRemboursements().size() + 1)
                .build();

        remboursementRepository.save(remboursement);

        if (getMontantRestant(pretId).compareTo(BigDecimal.ZERO) <= 0) {
            pret.setStatut(StatutPret.REMBOURSE);
            pretRepository.save(pret);
        }
    }

    public List<Pret> findEnAttente() {
        return pretRepository.findByStatut(StatutPret.EN_ATTENTE);
    }

    public List<Pret> findValides() {
        return pretRepository.findByStatut(StatutPret.VALIDE);
    }

    public List<Pret> findRembourses() {
        return pretRepository.findByStatut(StatutPret.REMBOURSE);
    }

    public List<Pret> findRejetes() {
        return pretRepository.findByStatut(StatutPret.REJETE);
    }

    public BigDecimal getTotalPretsValides() {
        List<Pret> prets = pretRepository.findByStatut(StatutPret.VALIDE);
        return prets.stream()
                .map(Pret::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean delete(int id) {
        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        if (pret.getStatut() == StatutPret.VALIDE || pret.getStatut() == StatutPret.REMBOURSE) {
            throw new RuntimeException("Impossible de supprimer un prêt validé ou remboursé.");
        }

        return pretRepository.delete(id);
    }

    private void validate(Pret pret) {
        if (pret.getMontant() == null || pret.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du prêt doit être supérieur à zéro.");
        }
        if (pret.getDuree() == null || pret.getDuree() <= 0) {
            throw new RuntimeException("La durée du prêt est obligatoire.");
        }
        if (pret.getAdherent() == null || pret.getAdherent().getId() == 0) {
            throw new RuntimeException("L'adhérent est obligatoire.");
        }
    }
}