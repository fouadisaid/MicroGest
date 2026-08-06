package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Adherent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdherentRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Adherent> findAll() {
        try {
            return em.createQuery(
                    "SELECT a FROM Adherent a LEFT JOIN FETCH a.agence ORDER BY a.id",
                    Adherent.class
            ).getResultList();
        } catch (PersistenceException e) {
            return new ArrayList<>();
        }
    }

    public List<Adherent> search(String query) {
        try {
            String pattern = "%" + query.toLowerCase() + "%";
            return em.createQuery("""
                    SELECT a FROM Adherent a
                    LEFT JOIN FETCH a.agence
                    WHERE LOWER(a.nom) LIKE :q
                       OR LOWER(a.prenom) LIKE :q
                       OR LOWER(a.email) LIKE :q
                       OR LOWER(a.telephone) LIKE :q
                    ORDER BY a.nom
                    """, Adherent.class)
                    .setParameter("q", pattern)
                    .getResultList();
        } catch (PersistenceException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Adherent> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Adherent.class, id));
        } catch (PersistenceException e) {
            return Optional.empty();
        }
    }

    public Adherent save(Adherent adherent) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Adherent result;
            if (adherent.getId() == 0) {
                em.persist(adherent);
                result = adherent;
            } else {
                result = em.merge(adherent);
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
            Adherent adherent = em.find(Adherent.class, id);
            if (adherent == null) {
                transaction.rollback();
                return false;
            }
            em.remove(adherent);
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