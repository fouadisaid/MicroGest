package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.User;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();

    public List<User> findAll() {
        return em.createQuery(
                "SELECT u FROM User u ORDER BY u.id",
                User.class
        ).getResultList();
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.of(user);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class)
                    .setParameter("email", email)
                    .getSingleResult();

            return Optional.of(user);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public User save(User user) {

        em.getTransaction().begin();

        User result;

        if (user.getId() == 0) {
            em.persist(user);
            result = user;
        } else {
            result = em.merge(user);
        }

        em.getTransaction().commit();

        return result;
    }

    public boolean delete(int id) {

        em.getTransaction().begin();

        User user = em.find(User.class, id);

        if (user == null) {
            em.getTransaction().rollback();
            return false;
        }

        em.remove(user);

        em.getTransaction().commit();

        return true;
    }
}