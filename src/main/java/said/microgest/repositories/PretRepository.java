package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Pret;

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
                    ORDER BY p.id DESC
                    """, Pret.class)
                    .getResultList();
        } catch (PersistenceException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Pret> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Pret.class, id));
        } catch (PersistenceException e) {
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
            Pret pret = em.find(Pret.class, id);
            if (pret == null) {
                transaction.rollback();
                return false;
            }
            em.remove(pret);
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