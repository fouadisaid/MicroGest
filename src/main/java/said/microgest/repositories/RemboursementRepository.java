package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Remboursement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemboursementRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Remboursement> findAll() {
        try {
            em.clear();
            return em.createQuery("""
                    SELECT r FROM Remboursement r
                    LEFT JOIN FETCH r.pret p
                    LEFT JOIN FETCH p.adherent
                    ORDER BY r.datePaiement DESC
                    """, Remboursement.class).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Remboursement> findByPret(int pretId) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT r FROM Remboursement r WHERE r.pret.id = :pretId ORDER BY r.numeroEcheance ASC",
                            Remboursement.class
                    ).setParameter("pretId", pretId)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Remboursement> findPaginated(int page, int size) {
        try {
            em.clear();
            return em.createQuery("""
                    SELECT r FROM Remboursement r
                    LEFT JOIN FETCH r.pret p
                    LEFT JOIN FETCH p.adherent
                    ORDER BY r.datePaiement DESC
                    """, Remboursement.class)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public long count() {
        try {
            em.clear();
            return em.createQuery("SELECT COUNT(r) FROM Remboursement r", Long.class)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Remboursement> search(String keyword) {
        try {
            em.clear();
            String pattern = "%" + keyword.toLowerCase() + "%";

            return em.createQuery("""
                    SELECT r FROM Remboursement r
                    LEFT JOIN FETCH r.pret p
                    LEFT JOIN FETCH p.adherent a
                    WHERE LOWER(a.nom) LIKE :q
                       OR LOWER(a.prenom) LIKE :q
                       OR LOWER(a.numeroAdherent) LIKE :q
                    ORDER BY r.datePaiement DESC
                    """, Remboursement.class)
                    .setParameter("q", pattern)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Remboursement> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Remboursement.class, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Remboursement save(Remboursement remboursement) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Remboursement result;
            if (remboursement.getId() == 0) {
                em.persist(remboursement);
                result = remboursement;
            } else {
                result = em.merge(remboursement);
            }
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors du remboursement.", e);
        }
    }

    public boolean delete(int id) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Remboursement remboursement = em.find(Remboursement.class, id);
            if (remboursement == null) {
                transaction.rollback();
                return false;
            }
            em.remove(remboursement);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression.", e);
        }
    }
}