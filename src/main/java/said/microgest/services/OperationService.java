package said.microgest.services;

import said.microgest.entities.Adherent;
import said.microgest.entities.Epargne;
import said.microgest.entities.Operation;
import said.microgest.enums.TypeOperation;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.EpargneRepository;
import said.microgest.repositories.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OperationService {

    private final OperationRepository operationRepository = new OperationRepository();
    private final AdherentRepository adherentRepository = new AdherentRepository();
    private final EpargneRepository epargneRepository = new EpargneRepository();

    public List<Operation> findAll() {
        return operationRepository.findAll();
    }

    public List<Operation> findByAdherent(int adherentId) {
        adherentRepository.findById(adherentId)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));
        return operationRepository.findByAdherent(adherentId);
    }

    public Operation findById(int id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opération introuvable."));
    }

    public Operation create(Operation operation) {
        validate(operation);

        Adherent adherent = adherentRepository.findById(operation.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        operation.setAdherent(adherent);

        if (operation.getDateOperation() == null) {
            operation.setDateOperation(LocalDateTime.now());
        }

        Operation saved = operationRepository.save(operation);

        updateEpargne(operation);

        return saved;
    }

    public Operation depot(int adherentId, BigDecimal montant, String observation) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du dépôt doit être supérieur à zéro.");
        }

        Adherent adherent = adherentRepository.findById(adherentId)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        Operation operation = Operation.builder()
                .type(TypeOperation.DEPOT)
                .montant(montant)
                .dateOperation(LocalDateTime.now())
                .observation(observation)
                .adherent(adherent)
                .build();

        return create(operation);
    }

    public Operation retrait(int adherentId, BigDecimal montant, String observation) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du retrait doit être supérieur à zéro.");
        }

        Adherent adherent = adherentRepository.findById(adherentId)
                .orElseThrow(() -> new RuntimeException("Adhérent introuvable."));

        Epargne epargne = epargneRepository.findByAdherent(adherentId)
                .orElseThrow(() -> new RuntimeException("Aucune épargne trouvée pour cet adhérent."));

        if (epargne.getSolde().compareTo(montant) < 0) {
            throw new RuntimeException("Solde d'épargne insuffisant.");
        }

        Operation operation = Operation.builder()
                .type(TypeOperation.RETRAIT)
                .montant(montant)
                .dateOperation(LocalDateTime.now())
                .observation(observation)
                .adherent(adherent)
                .build();

        return create(operation);
    }

    private void updateEpargne(Operation operation) {
        Epargne epargne = epargneRepository.findByAdherent(operation.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Aucune épargne trouvée pour cet adhérent."));

        if (operation.getType() == TypeOperation.DEPOT) {
            epargne.setSolde(epargne.getSolde().add(operation.getMontant()));
        } else if (operation.getType() == TypeOperation.RETRAIT) {
            epargne.setSolde(epargne.getSolde().subtract(operation.getMontant()));
        }

        epargneRepository.save(epargne);
    }

    public boolean delete(int id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opération introuvable."));

        Epargne epargne = epargneRepository.findByAdherent(operation.getAdherent().getId())
                .orElseThrow(() -> new RuntimeException("Aucune épargne trouvée."));

        if (operation.getType() == TypeOperation.DEPOT) {
            epargne.setSolde(epargne.getSolde().subtract(operation.getMontant()));
        } else if (operation.getType() == TypeOperation.RETRAIT) {
            epargne.setSolde(epargne.getSolde().add(operation.getMontant()));
        }

        epargneRepository.save(epargne);

        return operationRepository.delete(id);
    }

    public BigDecimal getSoldeAdherent(int adherentId) {
        Epargne epargne = epargneRepository.findByAdherent(adherentId)
                .orElseThrow(() -> new RuntimeException("Aucune épargne trouvée pour cet adhérent."));
        return epargne.getSolde();
    }

    private void validate(Operation operation) {
        if (operation.getType() == null) {
            throw new RuntimeException("Le type d'opération est obligatoire.");
        }
        if (operation.getMontant() == null || operation.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être supérieur à zéro.");
        }
        if (operation.getAdherent() == null || operation.getAdherent().getId() == 0) {
            throw new RuntimeException("L'adhérent est obligatoire.");
        }
    }
}