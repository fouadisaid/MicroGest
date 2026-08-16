package said.microgest.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import said.microgest.config.HibernateUtil;
import said.microgest.entities.Operation;
import said.microgest.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OperationRepository {

    private final EntityManager em =
            HibernateUtil.getEntityManager();


    // =========================================================
    // TOUTES LES OPERATIONS
    // =========================================================

    public List<Operation> findAll() {

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .getResultList();
    }


    // =========================================================
    // PAGINATION
    // =========================================================

    public List<Operation> findPaginated(
            int page,
            int size
    ) {

        int firstResult =
                (page - 1) * size;

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setFirstResult(firstResult)
                .setMaxResults(size)
                .getResultList();
    }


    // =========================================================
    // RECHERCHE PAR TEXTE
    // =========================================================

    public List<Operation> search(
            String keyword
    ) {

        String value =
                "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent a
                WHERE
                    LOWER(o.observation) LIKE :keyword
                    OR LOWER(a.nom) LIKE :keyword
                    OR LOWER(a.prenom) LIKE :keyword
                    OR LOWER(a.numeroAdherent) LIKE :keyword
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("keyword", value)
                .getResultList();
    }


    // =========================================================
    // PAGINATION + RECHERCHE TEXTE
    // =========================================================

    public List<Operation> findPaginatedSearch(
            String keyword,
            int page,
            int size
    ) {

        int firstResult =
                (page - 1) * size;

        String value =
                "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent a
                WHERE
                    LOWER(o.observation) LIKE :keyword
                    OR LOWER(a.nom) LIKE :keyword
                    OR LOWER(a.prenom) LIKE :keyword
                    OR LOWER(a.numeroAdherent) LIKE :keyword
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("keyword", value)
                .setFirstResult(firstResult)
                .setMaxResults(size)
                .getResultList();
    }


    // =========================================================
    // NOUVEAU :
    // PAGINATION + RECHERCHE TEXTE + TYPE
    // =========================================================

    public List<Operation> findPaginatedSearch(
            String keyword,
            TypeOperation type,
            int page,
            int size
    ) {

        int firstResult =
                (page - 1) * size;

        String value =
                "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent a
                WHERE
                    o.type = :type
                    AND (
                        LOWER(o.observation) LIKE :keyword
                        OR LOWER(a.nom) LIKE :keyword
                        OR LOWER(a.prenom) LIKE :keyword
                        OR LOWER(a.numeroAdherent) LIKE :keyword
                    )
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("type", type)
                .setParameter("keyword", value)
                .setFirstResult(firstResult)
                .setMaxResults(size)
                .getResultList();
    }


    // =========================================================
    // PAGINATION UNIQUEMENT PAR TYPE
    // =========================================================

    public List<Operation> findPaginatedByType(
            TypeOperation type,
            int page,
            int size
    ) {

        int firstResult =
                (page - 1) * size;

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                WHERE o.type = :type
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("type", type)
                .setFirstResult(firstResult)
                .setMaxResults(size)
                .getResultList();
    }


    // =========================================================
    // RECHERCHE PAR TYPE
    // =========================================================

    public List<Operation> findByType(
            TypeOperation type
    ) {

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                WHERE o.type = :type
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("type", type)
                .getResultList();
    }


    // =========================================================
    // COMPTER TOUTES LES OPERATIONS
    // =========================================================

    public long count() {

        return em.createQuery(
                "SELECT COUNT(o) FROM Operation o",
                Long.class
        ).getSingleResult();
    }


    // =========================================================
    // COMPTER AVEC RECHERCHE TEXTE
    // =========================================================

    public long countSearch(
            String keyword
    ) {

        String value =
                "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery("""
                SELECT COUNT(o) FROM Operation o
                LEFT JOIN o.adherent a
                WHERE
                    LOWER(o.observation) LIKE :keyword
                    OR LOWER(a.nom) LIKE :keyword
                    OR LOWER(a.prenom) LIKE :keyword
                    OR LOWER(a.numeroAdherent) LIKE :keyword
                """, Long.class)
                .setParameter("keyword", value)
                .getSingleResult();
    }


    // =========================================================
    // NOUVEAU :
    // COMPTER AVEC RECHERCHE + TYPE
    // =========================================================

    public long countSearch(
            String keyword,
            TypeOperation type
    ) {

        String value =
                "%" + keyword.toLowerCase().trim() + "%";

        return em.createQuery("""
                SELECT COUNT(o) FROM Operation o
                LEFT JOIN o.adherent a
                WHERE
                    o.type = :type
                    AND (
                        LOWER(o.observation) LIKE :keyword
                        OR LOWER(a.nom) LIKE :keyword
                        OR LOWER(a.prenom) LIKE :keyword
                        OR LOWER(a.numeroAdherent) LIKE :keyword
                    )
                """, Long.class)
                .setParameter("type", type)
                .setParameter("keyword", value)
                .getSingleResult();
    }


    // =========================================================
    // COMPTER PAR TYPE
    // =========================================================

    public long countByType(
            TypeOperation type
    ) {

        return em.createQuery(
                        "SELECT COUNT(o) FROM Operation o WHERE o.type = :type",
                        Long.class
                )
                .setParameter("type", type)
                .getSingleResult();
    }


    // =========================================================
    // RECHERCHE PAR ADHERENT
    // =========================================================

    public List<Operation> findByAdherent(
            int adherentId
    ) {

        return em.createQuery("""
                SELECT o FROM Operation o
                LEFT JOIN FETCH o.adherent
                WHERE o.adherent.id = :id
                ORDER BY o.dateOperation DESC
                """, Operation.class)
                .setParameter("id", adherentId)
                .getResultList();
    }


    // =========================================================
    // RECHERCHE PAR ID
    // =========================================================

    public Optional<Operation> findById(
            int id
    ) {

        return Optional.ofNullable(
                em.find(
                        Operation.class,
                        id
                )
        );
    }


    // =========================================================
    // ENREGISTREMENT
    // =========================================================

    public Operation save(
            Operation operation
    ) {

        EntityTransaction transaction =
                em.getTransaction();

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

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Erreur lors de l'opération.",
                    e
            );
        }
    }


    // =========================================================
    // SUPPRESSION
    // =========================================================

    public boolean delete(
            int id
    ) {

        EntityTransaction transaction =
                em.getTransaction();

        try {

            transaction.begin();

            Operation operation =
                    em.find(
                            Operation.class,
                            id
                    );

            if (operation == null) {

                transaction.rollback();

                return false;
            }

            em.remove(operation);

            transaction.commit();

            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Erreur lors de la suppression.",
                    e
            );
        }
    }


    // =========================================================
    // STATISTIQUES
    // =========================================================

    public long countByDateRange(
            LocalDateTime debut,
            LocalDateTime fin
    ) {

        try {

            return em.createQuery(
                            """
                            SELECT COUNT(o)
                            FROM Operation o
                            WHERE o.dateOperation
                            BETWEEN :debut AND :fin
                            """,
                            Long.class
                    )
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getSingleResult();

        } catch (Exception e) {

            return 0;
        }
    }


    public BigDecimal sumByTypeAndDateRange(
            TypeOperation type,
            LocalDateTime debut,
            LocalDateTime fin
    ) {

        try {

            BigDecimal sum =
                    em.createQuery(
                                    """
                                    SELECT SUM(o.montant)
                                    FROM Operation o
                                    WHERE o.type = :type
                                    AND o.dateOperation
                                    BETWEEN :debut AND :fin
                                    """,
                                    BigDecimal.class
                            )
                            .setParameter("type", type)
                            .setParameter("debut", debut)
                            .setParameter("fin", fin)
                            .getSingleResult();

            return sum != null
                    ? sum
                    : BigDecimal.ZERO;

        } catch (Exception e) {

            return BigDecimal.ZERO;
        }
    }
}