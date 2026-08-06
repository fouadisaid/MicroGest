package said.microgest.services;

import said.microgest.entities.Pret;
import said.microgest.entities.Remboursement;
import said.microgest.enums.StatutPret;
import said.microgest.repositories.PretRepository;
import said.microgest.repositories.RemboursementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RemboursementService {

    private final RemboursementRepository remboursementRepository = new RemboursementRepository();
    private final PretRepository pretRepository = new PretRepository();
    private final PretService pretService = new PretService();

    public List<Remboursement> findAll() {
        return remboursementRepository.findAll();
    }

    public Remboursement findById(int id) {
        return remboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Remboursement introuvable."));
    }

    public List<Remboursement> findByPret(int pretId) {
        pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));
        return remboursementRepository.findByPret(pretId);
    }

    public Remboursement create(Remboursement remboursement) {
        validate(remboursement);

        Pret pret = pretRepository.findById(remboursement.getPret().getId())
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        if (pret.getStatut() != StatutPret.VALIDE) {
            throw new RuntimeException("Seul un prêt validé peut recevoir des remboursements.");
        }

        remboursement.setPret(pret);

        if (remboursement.getDatePaiement() == null) {
            remboursement.setDatePaiement(LocalDate.now());
        }

        BigDecimal restant = pretService.getMontantRestant(pret.getId());

        if (remboursement.getMontant().compareTo(restant) > 0) {
            throw new RuntimeException("Le montant du remboursement dépasse le montant restant du prêt.");
        }

        Remboursement saved = remboursementRepository.save(remboursement);

        if (pretService.getMontantRestant(pret.getId()).compareTo(BigDecimal.ZERO) <= 0) {
            pret.setStatut(StatutPret.REMBOURSE);
            pretRepository.save(pret);
        }

        return saved;
    }

    public BigDecimal getTotalRembourse(int pretId) {
        pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        List<Remboursement> remboursements = remboursementRepository.findByPret(pretId);

        return remboursements.stream()
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean delete(int id) {
        Remboursement remboursement = remboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Remboursement introuvable."));

        Pret pret = remboursement.getPret();

        if (pret.getStatut() == StatutPret.REMBOURSE) {
            throw new RuntimeException("Impossible de supprimer un remboursement d'un prêt déjà remboursé.");
        }

        return remboursementRepository.delete(id);
    }

    private void validate(Remboursement remboursement) {
        if (remboursement.getMontant() == null || remboursement.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du remboursement doit être supérieur à zéro.");
        }
        if (remboursement.getPret() == null || remboursement.getPret().getId() == 0) {
            throw new RuntimeException("Le prêt est obligatoire.");
        }
    }
}