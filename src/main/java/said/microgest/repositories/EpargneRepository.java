package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Epargne;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpargneRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Epargne> findAll() {
        try {
            return em.createQuery("""
                    SELECT e FROM Epargne e
                    LEFT JOIN FETCH e.adherent
                    ORDER BY e.id
                    """, Epargne.class)
                    .getResultList();
        } catch (PersistenceException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Epargne> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Epargne.class, id));
        } catch (PersistenceException e) {
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
        } catch (PersistenceException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return null;
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
        } catch (PersistenceException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return false;
        }
    }
}