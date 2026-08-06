package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.User;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<User> findAll() {
        return em.createQuery(
                "SELECT u FROM User u ORDER BY u.nom, u.prenom",
                User.class
        ).getResultList();
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "SELECT u FROM User u WHERE u.username = :username",
                                    User.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "SELECT u FROM User u WHERE u.email = :email",
                                    User.class)
                            .setParameter("email", email)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsernameOrEmail(String login) {

        try {

            User user = em.createQuery("""
                SELECT u
                FROM User u
                WHERE u.username = :login
                   OR u.email = :login
                """, User.class)
                    .setParameter("login", login)
                    .getSingleResult();

            return Optional.of(user);

        } catch (NoResultException e) {

            return Optional.empty();

        }

    }

    public List<User> search(String keyword) {

        String pattern = "%" + keyword.toLowerCase() + "%";

        return em.createQuery("""
                SELECT u FROM User u
                WHERE LOWER(u.nom) LIKE :q
                   OR LOWER(u.prenom) LIKE :q
                   OR LOWER(u.username) LIKE :q
                   OR LOWER(u.email) LIKE :q
                ORDER BY u.nom
                """, User.class)
                .setParameter("q", pattern)
                .getResultList();
    }

    public User save(User user) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            User result;

            if (user.getId() == 0) {
                em.persist(user);
                result = user;
            } else {
                result = em.merge(user);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur.", e);
        }
    }

    public boolean delete(int id) {

        EntityTransaction transaction = em.getTransaction();

        try {

            transaction.begin();

            User user = em.find(User.class, id);

            if (user == null) {
                transaction.rollback();
                return false;
            }

            em.remove(user);

            transaction.commit();

            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur.", e);
        }
    }

    public long count() {

        return em.createQuery(
                "SELECT COUNT(u) FROM User u",
                Long.class
        ).getSingleResult();

    }

}