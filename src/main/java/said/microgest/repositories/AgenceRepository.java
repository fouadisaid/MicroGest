package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Agence;

import java.util.List;
import java.util.Optional;

public class AgenceRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Agence> findAll() {
        return em.createQuery(
                "SELECT a FROM Agence a ORDER BY a.nom",
                Agence.class
        ).getResultList();
    }

    public Optional<Agence> findById(int id) {
        return Optional.ofNullable(em.find(Agence.class, id));
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

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de l'enregistrement de l'agence.", e);
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

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de la suppression de l'agence.", e);
        }
    }

    public long count() {
        return em.createQuery(
                "SELECT COUNT(a) FROM Agence a",
                Long.class
        ).getSingleResult();
    }
}