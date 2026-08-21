package said.microgest.services;

import said.microgest.entities.Pret;
import said.microgest.entities.Remboursement;
import said.microgest.repositories.RemboursementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RemboursementService {

    private final RemboursementRepository remboursementRepository = new RemboursementRepository();
    private final PretService pretService = new PretService();


    public List<Remboursement> findAll() {
        return remboursementRepository.findAll();
    }

    public List<Remboursement> findByPret(int pretId) {
        return remboursementRepository.findByPret(pretId);
    }

    public List<Remboursement> findPaginated(int page, int size) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        return remboursementRepository.findPaginated(page, size);
    }

    public long count() {
        return remboursementRepository.count();
    }

    public List<Remboursement> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return remboursementRepository.search(keyword);
    }

    // Prêts pouvant recevoir un nouveau remboursement (statut VALIDE)
    public List<Pret> findPretsRemboursables() {
        return pretService.findValides();
    }

    public BigDecimal getMontantRestant(int pretId) {
        return pretService.getMontantRestant(pretId);
    }


    public boolean estModifiable(Remboursement remboursement) {

        if (remboursement == null || remboursement.getNumeroEcheance() == null) {
            return false;
        }

        List<Remboursement> tousLesRemboursements =
                findByPret(remboursement.getPret().getId());

        int maxEcheance = tousLesRemboursements.stream()
                .mapToInt(r -> r.getNumeroEcheance() != null ? r.getNumeroEcheance() : 0)
                .max()
                .orElse(0);

        return remboursement.getNumeroEcheance().equals(maxEcheance);
    }


    public void rembourser(int pretId, BigDecimal montant, LocalDate datePaiement) {
        pretService.rembourser(pretId, montant, datePaiement);
    }


    // MISE A JOUR — uniquement le dernier remboursement
    public void update(int remboursementId, BigDecimal montant, LocalDate datePaiement) {
        pretService.updateRemboursement(remboursementId, montant, datePaiement);
    }

    public BigDecimal getMontantRestantApres(Remboursement remboursement) {

        Pret pret = remboursement.getPret();

        if (remboursement.getNumeroEcheance() == null) {
            return getMontantRestant(pret.getId());
        }

        List<Remboursement> tousLesRemboursements = findByPret(pret.getId());

        BigDecimal cumuleJusquIci = tousLesRemboursements.stream()
                .filter(r -> r.getNumeroEcheance() != null &&
                        r.getNumeroEcheance() <= remboursement.getNumeroEcheance())
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return pret.getMontant().subtract(cumuleJusquIci);
    }
}