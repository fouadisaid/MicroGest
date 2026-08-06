package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Agence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgenceRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Agence> findAll() {
        try {
            return em.createQuery(
                    "SELECT a FROM Agence a ORDER BY a.id",
                    Agence.class
            ).getResultList();
        } catch (PersistenceException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Agence> findById(int id) {
        try {
            return Optional.ofNullable(em.find(Agence.class, id));
        } catch (PersistenceException e) {
            return Optional.empty();
        }
    }

    public Agence save(Agence agence) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Agence result;
            if (agence.getId() == 0) {
                em.persist(agence);
                result = agence;
            } else {
                result = em.merge(agence);
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
            Agence agence = em.find(Agence.class, id);
            if (agence == null) {
                transaction.rollback();
                return false;
            }
            em.remove(agence);
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