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

    public List<Agence> search(String keyword) {

        String value = "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery(
                        "SELECT a FROM Agence a " +
                                "WHERE LOWER(a.nom) LIKE :keyword " +
                                "OR LOWER(a.adresse) LIKE :keyword " +
                                "OR LOWER(a.telephone) LIKE :keyword " +
                                "OR LOWER(a.email) LIKE :keyword " +
                                "ORDER BY a.nom",
                        Agence.class
                )
                .setParameter("keyword", value)
                .getResultList();
    }

    public List<Agence> findPaginated(int page, int size) {

        int firstResult = (page - 1) * size;

        return em.createQuery(
                        "SELECT a FROM Agence a ORDER BY a.nom",
                        Agence.class
                )
                .setFirstResult(firstResult)
                .setMaxResults(size)
                .getResultList();
    }

    public long count() {

        return em.createQuery(
                "SELECT COUNT(a) FROM Agence a",
                Long.class
        ).getSingleResult();
    }

    public long countSearch(String keyword) {

        String value = "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery(
                        "SELECT COUNT(a) FROM Agence a " +
                                "WHERE LOWER(a.nom) LIKE :keyword " +
                                "OR LOWER(a.adresse) LIKE :keyword " +
                                "OR LOWER(a.telephone) LIKE :keyword " +
                                "OR LOWER(a.email) LIKE :keyword",
                        Long.class
                )
                .setParameter("keyword", value)
                .getSingleResult();
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

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Erreur lors de l'enregistrement de l'agence.",
                    e
            );
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

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Impossible de supprimer cette agence car elle contient déjà des adhérents."
            );
        }
    }
}