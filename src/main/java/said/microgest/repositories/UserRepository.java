package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.User;
import said.microgest.enums.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<User> findAll() {
        try {
            em.clear();
            return em.createQuery(
                    "SELECT u FROM User u ORDER BY u.nom, u.prenom",
                    User.class
            ).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<User> findPaginated(int page, int size) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT u FROM User u ORDER BY u.nom, u.prenom",
                            User.class
                    )
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
            return em.createQuery(
                    "SELECT COUNT(u) FROM User u",
                    Long.class
            ).getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public long countByRole(Role role) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.role = :role",
                            Long.class
                    ).setParameter("role", role)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public long countActifsByRole(Role role) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.actif = true",
                            Long.class
                    ).setParameter("role", role)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<User> findByRole(Role role) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.role = :role ORDER BY u.nom, u.prenom",
                            User.class
                    ).setParameter("role", role)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<User> search(String keyword) {
        try {
            em.clear();
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
        } catch (Exception e) {
            return new ArrayList<>();
        }
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
}