package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Pret;
import said.microgest.enums.StatutPret;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PretRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Pret> findAll() {
        try {
            return em.createQuery("""
                    SELECT p FROM Pret p
                    LEFT JOIN FETCH p.adherent
                    ORDER BY p.datePret DESC
                    """, Pret.class).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Pret> findByAdherent(int adherentId) {
        try {
            return em.createQuery(
                            "SELECT p FROM Pret p WHERE p.adherent.id = :adherentId ORDER BY p.datePret DESC",
                            Pret.class
                    ).setParameter("adherentId", adherentId)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Pret> findByStatut(StatutPret statut) {
        try {
            return em.createQuery(
                            "SELECT p FROM Pret p WHERE p.statut = :statut ORDER BY p.datePret DESC",
                            Pret.class
                    ).setParameter("statut", statut)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Pret> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Pret.class, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Pret save(Pret pret) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Pret result;
            if (pret.getId() == 0) {
                em.persist(pret);
                result = pret;
            } else {
                result = em.merge(pret);
            }
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'enregistrement du prêt.", e);
        }
    }

    public boolean delete(int id) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Pret pret = em.find(Pret.class, id);
            if (pret == null) {
                transaction.rollback();
                return false;
            }
            em.remove(pret);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du prêt.", e);
        }
    }

    public long countByStatut(StatutPret statut) {
        try {
            return em.createQuery(
                            "SELECT COUNT(p) FROM Pret p WHERE p.statut = :statut",
                            Long.class
                    ).setParameter("statut", statut)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public BigDecimal sumMontantTotal() {
        try {
            BigDecimal sum = em.createQuery(
                    "SELECT SUM(p.montant) FROM Pret p",
                    BigDecimal.class
            ).getSingleResult();
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


}