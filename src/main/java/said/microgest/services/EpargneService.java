package said.microgest.services;

import said.microgest.entities.Epargne;
import said.microgest.entities.Operation;
import said.microgest.repositories.EpargneRepository;

import java.math.BigDecimal;
import java.util.List;

public class EpargneService {

    private final EpargneRepository epargneRepository = new EpargneRepository();
    private final OperationService operationService = new OperationService();

    public List<Epargne> findAll() {
        return epargneRepository.findAll();
    }

    public List<Epargne> findPaginated(int page, int size) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        return epargneRepository.findPaginated(page, size);
    }

    public long count() {
        return epargneRepository.count();
    }

    public List<Epargne> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return epargneRepository.search(keyword);
    }

    public BigDecimal getTotalEpargne() {
        return epargneRepository.getTotalEpargne();
    }

    // Historique des opérations (dépôts/retraits) liées au compte d'un adhérent
    public List<Operation> getHistoriqueOperations(int adherentId) {
        return operationService.findByAdherent(adherentId);
    }
}