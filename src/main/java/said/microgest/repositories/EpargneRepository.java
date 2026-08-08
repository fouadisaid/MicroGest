package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Epargne;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class EpargneRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Epargne> findAll() {
        return em.createQuery("""
                SELECT e FROM Epargne e
                LEFT JOIN FETCH e.adherent
                ORDER BY e.id
                """, Epargne.class).getResultList();
    }

    public Optional<Epargne> findById(int id) {
        return Optional.ofNullable(em.find(Epargne.class, id));
    }

    public Optional<Epargne> findByAdherent(int adherentId) {

        try {

            return Optional.of(
                    em.createQuery("""
                        SELECT e FROM Epargne e
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