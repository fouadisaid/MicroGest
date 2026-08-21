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

    // =========================================================
    // LECTURE
    // =========================================================

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

    public List<Pret> findPaginated(int page, int size) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        return pretRepository.findPaginated(page, size);
    }

    public long count() {
        return pretRepository.count();
    }

    public List<Pret> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return pretRepository.search(keyword);
    }

    public List<Pret> findByStatut(StatutPret statut) {
        return pretRepository.findByStatut(statut);
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

    // =========================================================
    // CREATION
    // =========================================================

    public Pret create(Pret pret) {

        validate(pret);

        Adherent adherent = adherentRepository.findById(pret.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        pret.setAdherent(adherent);

        if (pret.getDatePret() == null) {
            pret.setDatePret(LocalDate.now());
        }

        // Un nouveau prêt est toujours EN_ATTENTE, quel que soit ce qui a été saisi.
        pret.setStatut(StatutPret.EN_ATTENTE);

        return pretRepository.save(pret);
    }

    // =========================================================
    // MODIFICATION
    // =========================================================

    public Pret update(Pret pret) {

        Pret old = pretRepository.findById(pret.getId())
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        if (old.getStatut() != StatutPret.EN_ATTENTE) {
            throw new RuntimeException(
                    "Seul un prêt en attente peut être modifié."
            );
        }

        validate(pret);

        Adherent adherent = adherentRepository.findById(pret.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        pret.setAdherent(adherent);
        pret.setStatut(old.getStatut());
        pret.setCreatedAt(old.getCreatedAt());
        pret.setCreatedBy(old.getCreatedBy());

        return pretRepository.save(pret);
    }

    // =========================================================
    // VALIDATION / REJET
    // =========================================================

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

    // =========================================================
    // MENSUALITE
    // =========================================================

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

    // =========================================================
    // REMBOURSEMENT
    // =========================================================

    public BigDecimal getMontantRestant(int pretId) {

        Pret pret = findById(pretId);

        BigDecimal totalRembourse = pret.getRemboursements().stream()
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return pret.getMontant().subtract(totalRembourse);
    }

    public void rembourser(int pretId, BigDecimal montant) {
        rembourser(pretId, montant, LocalDate.now());
    }

    public void rembourser(int pretId, BigDecimal montant, LocalDate datePaiement) {

        Pret pret = findById(pretId);

        if (pret.getStatut() != StatutPret.VALIDE) {
            throw new RuntimeException("Seul un prêt validé peut être remboursé.");
        }

        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du remboursement doit être supérieur à zéro.");
        }

        if (datePaiement == null) {
            datePaiement = LocalDate.now();
        }

        if (datePaiement.isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de remboursement ne peut pas être dans le futur.");
        }

        BigDecimal restant = getMontantRestant(pretId);

        if (montant.compareTo(restant) > 0) {
            throw new RuntimeException(
                    "Le montant du remboursement (" + String.format("%,.0f FCFA", montant)
                            + ") dépasse le montant restant ("
                            + String.format("%,.0f FCFA", restant) + ")."
            );
        }

        Remboursement remboursement = Remboursement.builder()
                .pret(pret)
                .montant(montant)
                .datePaiement(datePaiement)
                .numeroEcheance(pret.getRemboursements().size() + 1)
                .build();

        remboursementRepository.save(remboursement);

        if (getMontantRestant(pretId).compareTo(BigDecimal.ZERO) <= 0) {
            pret.setStatut(StatutPret.REMBOURSE);
            pretRepository.save(pret);
        }
    }

    // =========================================================
    // SUPPRESSION
    // =========================================================

    public boolean delete(int id) {

        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable."));

        if (pret.getStatut() == StatutPret.VALIDE || pret.getStatut() == StatutPret.REMBOURSE) {
            throw new RuntimeException("Impossible de supprimer un prêt validé ou remboursé.");
        }

        return pretRepository.delete(id);
    }

    // =========================================================
    // VALIDATION DES DONNEES
    // =========================================================

    private void validate(Pret pret) {

        if (pret.getMontant() == null ||
                pret.getMontant().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Le montant du prêt doit être supérieur à zéro."
            );
        }

        if (pret.getTaux() == null ||
                pret.getTaux().compareTo(BigDecimal.ZERO) < 0 ||
                pret.getTaux().compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new RuntimeException(
                    "Le taux doit être compris entre 0 et 100."
            );
        }

        if (pret.getDuree() == null ||
                pret.getDuree() <= 0 ||
                pret.getDuree() > 360) {

            throw new RuntimeException(
                    "La durée doit être comprise entre 1 et 360 mois."
            );
        }

        if (pret.getAdherent() == null || pret.getAdherent().getId() == 0) {

            throw new RuntimeException(
                    "L'adhérent est obligatoire."
            );
        }

        if (pret.getDatePret() != null &&
                pret.getDatePret().isAfter(LocalDate.now())) {

            throw new RuntimeException(
                    "La date du prêt ne peut pas être dans le futur."
            );
        }
    }

    public Remboursement updateRemboursement(
            int remboursementId,
            BigDecimal nouveauMontant,
            LocalDate nouvelleDate
    ) {

        Remboursement remboursement =
                remboursementRepository.findById(remboursementId)
                        .orElseThrow(() ->
                                new RuntimeException("Remboursement introuvable.")
                        );

        Pret pret = remboursement.getPret();

        int maxEcheance = pret.getRemboursements().stream()
                .mapToInt(r -> r.getNumeroEcheance() != null ? r.getNumeroEcheance() : 0)
                .max()
                .orElse(0);

        if (remboursement.getNumeroEcheance() == null ||
                !remboursement.getNumeroEcheance().equals(maxEcheance)) {

            throw new RuntimeException(
                    "Seul le dernier remboursement enregistré sur ce prêt peut être modifié."
            );
        }

        if (nouveauMontant == null || nouveauMontant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être supérieur à zéro.");
        }

        if (nouvelleDate == null) {
            nouvelleDate = LocalDate.now();
        }

        if (nouvelleDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("La date ne peut pas être dans le futur.");
        }

        BigDecimal totalAutresRemboursements = pret.getRemboursements().stream()
                .filter(r -> r.getId() != remboursement.getId())
                .map(Remboursement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal restantSansCetteEcheance =
                pret.getMontant().subtract(totalAutresRemboursements);

        if (nouveauMontant.compareTo(restantSansCetteEcheance) > 0) {

            throw new RuntimeException(
                    "Le montant dépasse le montant restant du prêt ("
                            + String.format("%,.0f FCFA", restantSansCetteEcheance) + ")."
            );
        }

        remboursement.setMontant(nouveauMontant);
        remboursement.setDatePaiement(nouvelleDate);

        remboursementRepository.save(remboursement);

        BigDecimal nouveauRestant =
                restantSansCetteEcheance.subtract(nouveauMontant);

        if (nouveauRestant.compareTo(BigDecimal.ZERO) <= 0) {

            pret.setStatut(StatutPret.REMBOURSE);

        } else if (pret.getStatut() == StatutPret.REMBOURSE) {

            pret.setStatut(StatutPret.VALIDE);
        }

        pretRepository.save(pret);

        return remboursement;
    }
}