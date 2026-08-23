-- ============================================================
-- MicroGest — Script de création de la base de données
-- SGBD cible : PostgreSQL 14+
-- ============================================================
-- Utilisation :
--   1. Créer la base : CREATE DATABASE microgest;
--   2. Se connecter dessus : \c microgest
--   3. Exécuter ce fichier : \i schema.sql
-- ============================================================

-- Décommentez ces deux lignes si vous exécutez le script en tant
-- que superutilisateur et souhaitez tout faire en une fois :
-- CREATE DATABASE microgest;
-- \c microgest


-- ============================================================
-- NETTOYAGE (ordre inverse des dépendances)
-- ============================================================

DROP TABLE IF EXISTS remboursements CASCADE;
DROP TABLE IF EXISTS operations CASCADE;
DROP TABLE IF EXISTS prets CASCADE;
DROP TABLE IF EXISTS epargnes CASCADE;
DROP TABLE IF EXISTS adherents CASCADE;
DROP TABLE IF EXISTS agences CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- ============================================================
-- TABLE : agences
-- ============================================================

CREATE TABLE agences (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL UNIQUE,
    adresse     VARCHAR(150) NOT NULL,
    telephone   VARCHAR(20)  NOT NULL,
    email       VARCHAR(100) UNIQUE,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_at  TIMESTAMP
);


-- ============================================================
-- TABLE : users (comptes d'accès à l'application)
-- ============================================================

CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    telephone   VARCHAR(20)  NOT NULL,
    password    VARCHAR(255) NOT NULL,             -- hash BCrypt
    role        VARCHAR(20)  NOT NULL,
    actif       BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_at  TIMESTAMP,

    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'AGENT', 'SUPERVISEUR'))
);


-- ============================================================
-- TABLE : adherents
-- ============================================================

CREATE TABLE adherents (
    id                SERIAL PRIMARY KEY,
    numero_adherent   VARCHAR(30)  NOT NULL UNIQUE,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100) NOT NULL,
    sexe              VARCHAR(1)   NOT NULL,
    date_naissance    DATE,
    adresse           VARCHAR(255),
    telephone         VARCHAR(20)  NOT NULL UNIQUE,
    email             VARCHAR(150) UNIQUE,
    date_adhesion     DATE,
    statut            VARCHAR(20)  NOT NULL,
    agence_id         INTEGER      NOT NULL,

    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(100),
    updated_at        TIMESTAMP,

    CONSTRAINT fk_adherent_agence
        FOREIGN KEY (agence_id) REFERENCES agences (id),

    CONSTRAINT chk_adherent_sexe
        CHECK (sexe IN ('M', 'F')),

    CONSTRAINT chk_adherent_statut
        CHECK (statut IN ('ACTIF', 'INACTIF', 'SUSPENDU'))
);

CREATE INDEX idx_adherent_nom_prenom ON adherents (nom, prenom);
CREATE INDEX idx_adherent_agence     ON adherents (agence_id);
CREATE INDEX idx_adherent_statut     ON adherents (statut);


-- ============================================================
-- TABLE : epargnes (compte épargne — 1 par adhérent)
-- ============================================================

CREATE TABLE epargnes (
    id               SERIAL PRIMARY KEY,
    solde            NUMERIC(15, 2) NOT NULL DEFAULT 0,
    date_ouverture   DATE,
    adherent_id      INTEGER NOT NULL UNIQUE,

    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100),
    updated_at       TIMESTAMP,

    CONSTRAINT fk_epargne_adherent
        FOREIGN KEY (adherent_id) REFERENCES adherents (id)
);


-- ============================================================
-- TABLE : operations (dépôts / retraits)
-- ============================================================

CREATE TABLE operations (
    id               SERIAL PRIMARY KEY,
    type             VARCHAR(20)    NOT NULL,
    montant          NUMERIC(15, 2) NOT NULL,
    date_operation   TIMESTAMP,
    observation      VARCHAR(255),
    adherent_id      INTEGER        NOT NULL,

    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100),
    updated_at       TIMESTAMP,

    CONSTRAINT fk_operation_adherent
        FOREIGN KEY (adherent_id) REFERENCES adherents (id),

    CONSTRAINT chk_operation_type
        CHECK (type IN ('DEPOT', 'RETRAIT')),

    CONSTRAINT chk_operation_montant_positif
        CHECK (montant > 0)
);

CREATE INDEX idx_operation_adherent ON operations (adherent_id);
CREATE INDEX idx_operation_date     ON operations (date_operation);


-- ============================================================
-- TABLE : prets
-- ============================================================

CREATE TABLE prets (
    id            SERIAL PRIMARY KEY,
    montant       NUMERIC(15, 2) NOT NULL,
    taux          NUMERIC(5, 2)  NOT NULL,
    duree         INTEGER        NOT NULL,
    date_pret     DATE,
    statut        VARCHAR(20)    NOT NULL,
    adherent_id   INTEGER        NOT NULL,

    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(100),
    updated_at    TIMESTAMP,

    CONSTRAINT fk_pret_adherent
        FOREIGN KEY (adherent_id) REFERENCES adherents (id),

    CONSTRAINT chk_pret_statut
        CHECK (statut IN ('EN_ATTENTE', 'VALIDE', 'REJETE', 'REMBOURSE')),

    CONSTRAINT chk_pret_montant_positif
        CHECK (montant > 0),

    CONSTRAINT chk_pret_taux_valide
        CHECK (taux >= 0 AND taux <= 100),

    CONSTRAINT chk_pret_duree_valide
        CHECK (duree > 0 AND duree <= 360)
);

CREATE INDEX idx_pret_adherent ON prets (adherent_id);
CREATE INDEX idx_pret_statut   ON prets (statut);


-- ============================================================
-- TABLE : remboursements
-- ============================================================

CREATE TABLE remboursements (
    id                SERIAL PRIMARY KEY,
    montant           NUMERIC(15, 2) NOT NULL,
    date_paiement     DATE,
    numero_echeance   INTEGER,
    pret_id           INTEGER        NOT NULL,

    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(100),
    updated_at        TIMESTAMP,

    CONSTRAINT fk_remboursement_pret
        FOREIGN KEY (pret_id) REFERENCES prets (id),

    CONSTRAINT chk_remboursement_montant_positif
        CHECK (montant > 0)
);

CREATE INDEX idx_remboursement_pret ON remboursements (pret_id);


-- ============================================================
-- DONNEES INITIALES (comptes de test + agences de base)
-- Mots de passe hachés avec BCrypt (coût 10), voir README.md
-- pour les identifiants et mots de passe en clair.
-- ============================================================

INSERT INTO agences (nom, adresse, telephone, email) VALUES
    ('Agence Dakar',       'Plateau, Dakar',           '338210001', 'dakar@microgest.com'),
    ('Agence Saint-Louis', 'Centre-ville, Saint-Louis', '338210002', 'saintlouis@microgest.com');

INSERT INTO users (nom, prenom, username, email, telephone, password, role, actif) VALUES
    ('FOUADI',   'Said',       'admin',       'admin@microgest.com',       '771000000',
     '$2a$10$gd1UFvMbtYuPaQbMIYOXXu1sq/tmvnwraHKYq6iYjX7fcVNWVS/mK', 'ADMIN',       TRUE),

    ('ALI',      'Abdou',      'agent1',      'agent1@microgest.com',      '771000001',
     '$2a$10$U2nEiu8Nm08zlJXuu/otLOJB.HTvLNGHJoYaZThvuV59qMXRBuhVe', 'AGENT',       TRUE),

    ('ABOU',     'Anrifi',     'super1',      'super1@microgest.com',      '771000002',
     '$2a$10$/UO6k.3QVFyURW.F0xIXGOlkHtRECBZDCp8nedjrs.pmlO38FLXmW', 'SUPERVISEUR', TRUE);

-- Fin du script
