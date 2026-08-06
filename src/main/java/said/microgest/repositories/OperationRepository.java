package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Operation;

import java.util.List;
import java.util.Optional;

public class OperationRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<Operation> findAll() {
        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                ORDER BY o.dateOperation DESC
                """, Operation.class).getResultList();
    }

    public List<Operation> findByAdherent(int adherentId) {

        return em.createQuery("""
                SELECT o FROM Operation o
                WHERE o.adherent.id=:id
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("id", adherentId)
                .getResultList();
    }

    public Optional<Operation> findById(int id) {
        return Optional.ofNullable(em.find(Operation.class, id));
    }

    public Operation save(Operation operation) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            Operation result;

            if (operation.getId() == 0) {
                em.persist(operation);
                result = operation;
            } else {
                result = em.merge(operation);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de l'opération.", e);
        }
    }

    public boolean delete(int id) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            Operation operation = em.find(Operation.class, id);

            if (operation == null) {
                transaction.rollback();
                return false;
            }

            em.remove(operation);

            transaction.commit();

            return true;

        } catch (Exception e) {

            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException("Erreur lors de la suppression.", e);
        }
    }
}