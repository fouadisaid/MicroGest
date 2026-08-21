package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Epargne;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpargneRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Epargne> findAll() {
        try {
            em.clear();
            return em.createQuery("""
                    SELECT e FROM Epargne e
                    LEFT JOIN FETCH e.adherent
                    ORDER BY e.id
                    """, Epargne.class).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Epargne> findPaginated(int page, int size) {
        try {
            em.clear();
            return em.createQuery("""
                    SELECT e FROM Epargne e
                    LEFT JOIN FETCH e.adherent
                    ORDER BY e.id
                    """, Epargne.class)
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
            return em.createQuery("SELECT COUNT(e) FROM Epargne e", Long.class)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Epargne> search(String keyword) {
        try {
            em.clear();
            String pattern = "%" + keyword.toLowerCase() + "%";

            return em.createQuery("""
                    SELECT e FROM Epargne e
                    LEFT JOIN FETCH e.adherent a
                    WHERE LOWER(a.nom) LIKE :q
                       OR LOWER(a.prenom) LIKE :q
                       OR LOWER(a.numeroAdherent) LIKE :q
                    ORDER BY e.id
                    """, Epargne.class)
                    .setParameter("q", pattern)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Epargne> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Epargne.class, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Epargne> findByAdherent(int adherentId) {

        try {
            em.clear();
            return Optional.of(
                    em.createQuery("""
                        SELECT e FROM Epargne e
                        LEFT JOIN FETCH e.adherent
                        WHERE e.adherent.id = :id
                        """, Epargne.class)
                            .setParameter("id", adherentId)
                            .getSingleResult());

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Epargne save(Epargne epargne) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            Epargne result;

            if (epargne.getId() == 0) {
                em.persist(epargne);
                result = epargne;
            } else {
                result = em.merge(epargne);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de l'enregistrement.", e);
        }
    }

    public boolean delete(int id) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            Epargne epargne = em.find(Epargne.class, id);

            if (epargne == null) {
                transaction.rollback();
                return false;
            }

            em.remove(epargne);

            transaction.commit();

            return true;

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de la suppression.", e);
        }
    }

    public BigDecimal getTotalEpargne() {
        try {
            em.clear();
            BigDecimal sum = em.createQuery(
                    "SELECT SUM(e.solde) FROM Epargne e",
                    BigDecimal.class
            ).getSingleResult();
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}