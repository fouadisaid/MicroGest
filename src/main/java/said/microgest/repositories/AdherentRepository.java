package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Adherent;
import said.microgest.entities.Agence;
import said.microgest.enums.StatutAdherent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdherentRepository {

    private final EntityManager em = HibernateUtil.getEntityManager();



    public List<Adherent> findAll() {
        return em.createQuery("""
                SELECT a
                FROM Adherent a
                LEFT JOIN FETCH a.agence
                ORDER BY a.nom, a.prenom
                """, Adherent.class)
                .getResultList();
    }

    public Optional<Adherent> findById(int id) {
        return Optional.ofNullable(em.find(Adherent.class, id));
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

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Erreur lors de l'enregistrement de l'adhérent.", e);
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

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Erreur lors de la suppression de l'adhérent.", e);
        }
    }

    // Recherche

    public List<Adherent> search(String keyword) {

        String pattern = "%" + keyword.toLowerCase() + "%";

        return em.createQuery("""
                SELECT a
                FROM Adherent a
                LEFT JOIN FETCH a.agence
                WHERE LOWER(a.numeroAdherent) LIKE :q
                   OR LOWER(a.nom) LIKE :q
                   OR LOWER(a.prenom) LIKE :q
                   OR LOWER(a.email) LIKE :q
                   OR LOWER(a.telephone) LIKE :q
                ORDER BY a.nom, a.prenom
                """, Adherent.class)
                .setParameter("q", pattern)
                .getResultList();
    }

    // Pagination
    public List<Adherent> findPaginated(int page, int size) {

        return em.createQuery("""
                SELECT a
                FROM Adherent a
                LEFT JOIN FETCH a.agence
                ORDER BY a.nom, a.prenom
                """, Adherent.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long count() {

        return em.createQuery(
                "SELECT COUNT(a) FROM Adherent a",
                Long.class
        ).getSingleResult();

    }

    // Filtres

    public List<Adherent> filter(
            StatutAdherent statut,
            Agence agence,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {

        StringBuilder jpql = new StringBuilder("""
                SELECT a
                FROM Adherent a
                WHERE 1=1
                """);

        if (statut != null) {
            jpql.append(" AND a.statut = :statut");
        }

        if (agence != null) {
            jpql.append(" AND a.agence = :agence");
        }

        if (dateDebut != null) {
            jpql.append(" AND a.dateAdhesion >= :dateDebut");
        }

        if (dateFin != null) {
            jpql.append(" AND a.dateAdhesion <= :dateFin");
        }

        jpql.append(" ORDER BY a.nom, a.prenom");

        TypedQuery<Adherent> query = em.createQuery(jpql.toString(), Adherent.class);

        if (statut != null) {
            query.setParameter("statut", statut);
        }

        if (agence != null) {
            query.setParameter("agence", agence);
        }

        if (dateDebut != null) {
            query.setParameter("dateDebut", dateDebut);
        }

        if (dateFin != null) {
            query.setParameter("dateFin", dateFin);
        }

        return query.getResultList();
    }

    // Vérifications

    public Optional<Adherent> findByNumeroAdherent(String numero) {

        try {

            return Optional.of(
                    em.createQuery("""
                            SELECT a
                            FROM Adherent a
                            WHERE a.numeroAdherent = :numero
                            """, Adherent.class)
                            .setParameter("numero", numero)
                            .getSingleResult()
            );

        } catch (NoResultException e) {

            return Optional.empty();

        }
    }

    public Optional<Adherent> findByTelephone(String telephone) {

        try {

            return Optional.of(
                    em.createQuery("""
                            SELECT a
                            FROM Adherent a
                            WHERE a.telephone = :telephone
                            """, Adherent.class)
                            .setParameter("telephone", telephone)
                            .getSingleResult()
            );

        } catch (NoResultException e) {

            return Optional.empty();

        }
    }

    public Optional<Adherent> findByEmail(String email) {

        try {

            return Optional.of(
                    em.createQuery("""
                            SELECT a
                            FROM Adherent a
                            WHERE a.email = :email
                            """, Adherent.class)
                            .setParameter("email", email)
                            .getSingleResult()
            );

        } catch (NoResultException e) {

            return Optional.empty();

        }
    }

    public long countActifs() {
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM Adherent a WHERE a.statut = :statut",
                            Long.class
                    ).setParameter("statut", StatutAdherent.ACTIF)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public long countByStatut(String statut) {
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM Adherent a WHERE a.statut = :statut",
                            Long.class
                    ).setParameter("statut", StatutAdherent.valueOf(statut))
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, Long> countByAgence() {
        try {
            List<Object[]> results = em.createQuery(
                    "SELECT a.agence.nom, COUNT(a) FROM Adherent a GROUP BY a.agence.nom",
                    Object[].class
            ).getResultList();

            Map<String, Long> stats = new HashMap<>();
            for (Object[] row : results) {
                stats.put((String) row[0], (Long) row[1]);
            }
            return stats;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }


}