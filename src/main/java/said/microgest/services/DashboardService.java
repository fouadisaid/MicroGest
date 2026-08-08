package said.microgest.services;

import said.microgest.enums.StatutPret;
import said.microgest.enums.TypeOperation;
import said.microgest.repositories.AdherentRepository;
import said.microgest.repositories.EpargneRepository;
import said.microgest.repositories.OperationRepository;
import said.microgest.repositories.PretRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    private final AdherentRepository adherentRepository = new AdherentRepository();
    private final OperationRepository operationRepository = new OperationRepository();
    private final EpargneRepository epargneRepository = new EpargneRepository();
    private final PretRepository pretRepository = new PretRepository();

    public long getTotalAdherents() {
        return adherentRepository.count();
    }

    public long getTotalAdherentsActifs() {
        return adherentRepository.countActifs();
    }

    public BigDecimal getTotalEpargne() {
        return epargneRepository.getTotalEpargne();
    }

    public BigDecimal getTotalPretsValides() {
        return pretRepository.sumMontantTotal();
    }

    public long getTotalOperationsMois() {
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = LocalDate.now().withDayOfMonth(YearMonth.now().lengthOfMonth()).atTime(23, 59, 59);
        return operationRepository.countByDateRange(debutMois, finMois);
    }

    public BigDecimal getTotalDepotsMois() {
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = LocalDate.now().withDayOfMonth(YearMonth.now().lengthOfMonth()).atTime(23, 59, 59);
        return operationRepository.sumByTypeAndDateRange(TypeOperation.DEPOT, debutMois, finMois);
    }

    public BigDecimal getTotalRetraitsMois() {
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = LocalDate.now().withDayOfMonth(YearMonth.now().lengthOfMonth()).atTime(23, 59, 59);
        return operationRepository.sumByTypeAndDateRange(TypeOperation.RETRAIT, debutMois, finMois);
    }

    public Map<String, Long> getStatistiquesParStatut() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("ACTIF", adherentRepository.countByStatut("ACTIF"));
        stats.put("INACTIF", adherentRepository.countByStatut("INACTIF"));
        stats.put("SUSPENDU", adherentRepository.countByStatut("SUSPENDU"));
        return stats;
    }

    public Map<String, Long> getStatistiquesParAgence() {
        return adherentRepository.countByAgence();
    }

    public Map<String, BigDecimal> getOperationsParMois(int mois, int annee) {
        Map<String, BigDecimal> stats = new HashMap<>();

        LocalDateTime debut = LocalDate.of(annee, mois, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(annee, mois, YearMonth.of(annee, mois).lengthOfMonth()).atTime(23, 59, 59);

        BigDecimal depots = operationRepository.sumByTypeAndDateRange(TypeOperation.DEPOT, debut, fin);
        BigDecimal retraits = operationRepository.sumByTypeAndDateRange(TypeOperation.RETRAIT, debut, fin);

        stats.put("DEPOTS", depots != null ? depots : BigDecimal.ZERO);
        stats.put("RETRAITS", retraits != null ? retraits : BigDecimal.ZERO);

        return stats;
    }

    public long getNbPretsEnAttente() {
        return pretRepository.countByStatut(StatutPret.EN_ATTENTE);
    }

    public long getNbPretsValides() {
        return pretRepository.countByStatut(StatutPret.VALIDE);
    }

    public long getNbPretsRembourses() {
        return pretRepository.countByStatut(StatutPret.REMBOURSE);
    }

    public long getNbPretsRejetes() {
        return pretRepository.countByStatut(StatutPret.REJETE);
    }

    public BigDecimal getMontantTotalPrets() {
        return pretRepository.sumMontantTotal();
    }

    public Map<String, Object> getDashboardIndicateurs() {
        Map<String, Object> indicateurs = new HashMap<>();
        indicateurs.put("totalAdherents", getTotalAdherents());
        indicateurs.put("totalEpargne", getTotalEpargne());
        indicateurs.put("totalPretsValides", getTotalPretsValides());
        indicateurs.put("totalOperationsMois", getTotalOperationsMois());
        indicateurs.put("totalDepotsMois", getTotalDepotsMois());
        indicateurs.put("totalRetraitsMois", getTotalRetraitsMois());
        indicateurs.put("nbPretsEnAttente", getNbPretsEnAttente());
        indicateurs.put("nbPretsValides", getNbPretsValides());
        indicateurs.put("nbPretsRembourses", getNbPretsRembourses());
        indicateurs.put("nbPretsRejetes", getNbPretsRejetes());
        indicateurs.put("statistiquesParStatut", getStatistiquesParStatut());
        indicateurs.put("statistiquesParAgence", getStatistiquesParAgence());
        return indicateurs;
    }
}