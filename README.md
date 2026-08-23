# MicroGest

Application de bureau pour la gestion d'une institution de microfinance : adhérents, agences, comptes épargne, opérations (dépôts/retraits), prêts et remboursements, notifications par email et exports PDF.

---

## 1. Qu'est-ce que MicroGest ?

MicroGest est une application JavaFX destinée à centraliser la gestion quotidienne d'une structure de microfinance. Elle couvre l'ensemble du parcours d'un adhérent :

- **Adhésion** : enregistrement d'un nouvel adhérent, rattaché à une agence, avec génération automatique d'un numéro d'adhérent et création d'un compte épargne.
- **Épargne** : suivi du solde et de l'historique des mouvements de chaque adhérent.
- **Opérations** : dépôts et retraits, avec mise à jour immédiate du solde et contrôle qu'un retrait ne dépasse jamais le solde disponible.
- **Prêts** : demande, validation ou rejet par un superviseur, calcul automatique de la mensualité, suivi des remboursements jusqu'au solde complet.
- **Utilisateurs** : gestion des comptes d'accès à l'application, avec des rôles (Administrateur, Agent, Superviseur) et des permissions dédiées.
- **Notifications** : emails automatiques (bienvenue, prêt validé, rappel d'échéance, relevé de compte en pièce jointe).
- **Export** : génération de documents PDF pour les listes de données et les relevés de compte.

---

## 2. Stack technique

| Catégorie                       | Technologie                                 |
| ------------------------------- | ------------------------------------------- |
| Langage                         | Java 17                                     |
| Interface graphique             | JavaFX 17 + FXML                            |
| Persistance                     | Jakarta Persistence API (JPA) / Hibernate 6 |
| Base de données                 | PostgreSQL                                  |
| Sécurité des mots de passe      | jBCrypt                                     |
| Envoi d'emails                  | Jakarta Mail (`com.sun.mail:jakarta.mail`)  |
| Génération de PDF               | OpenPDF                                     |
| Réduction de code répétitif     | Lombok                                      |
| Gestion des dépendances / build | Apache Maven                                |
| IDE recommandé                  | IntelliJ IDEA                               |

---

## 3. Création de la base de données

Le schéma complet (tables, contraintes, index, comptes de test) se trouve dans **`schema.sql`**, à la racine du projet.

```bash
# 1. Créer la base
psql -U postgres -c "CREATE DATABASE microgest;"

# 2. Exécuter le script de création
psql -U postgres -d microgest -f schema.sql
```

Le script crée les tables suivantes, dans l'ordre de leurs dépendances :

```
agences  →  adherents  →  epargnes
                       →  operations
                       →  prets  →  remboursements
users (indépendante)
```

Il insère également deux agences et **trois comptes de test** (voir section 7) prêts à l'emploi.

---

## 4. Prérequis

- **JDK 17** (Java Development Kit)
- **Apache Maven** 3.8+
- **PostgreSQL** 14+ installé et démarré
- Un IDE compatible JavaFX (IntelliJ IDEA recommandé)
- Un compte **Mailtrap** (gratuit) pour tester l'envoi d'emails sans risque — voir [mailtrap.io](https://mailtrap.io)

---

## 5. Installation

### 5.1 Cloner / récupérer le projet

```bash
git clone https://github.com/fouadisaid/MicroGest.git
cd MicroGest
```

### 5.2 Configurer la base de données

Ouvrez `src/main/resources/config.properties` (ou équivalent selon votre configuration Hibernate/`persistence.xml`) et renseignez :

```properties
jakarta.persistence.jdbc.url=jdbc:postgresql://localhost:5432/microgest
jakarta.persistence.jdbc.user=postgres
jakarta.persistence.jdbc.password=VOTRE_MOT_DE_PASSE
```

### 5.3 Configurer l'envoi d'emails

Créez `src/main/resources/email.properties` :

```properties
mail.smtp.host=sandbox.smtp.mailtrap.io
mail.smtp.port=587
mail.smtp.username=VOTRE_USERNAME_MAILTRAP
mail.smtp.password=VOTRE_PASSWORD_MAILTRAP
mail.from=noreply@microgest.com
mail.from.name=MicroGest
```

Ces identifiants sont disponibles dans votre tableau de bord Mailtrap, onglet **SMTP Settings → Java**.

### 5.4 Installer les dépendances et compiler

```bash
mvn clean install
```

### 5.5 Lancer l'application

```bash
mvn javafx:run
```

ou directement depuis l'IDE, en exécutant la classe :

```
said.microgest.MainApplication
```

---

## 6. Architecture

MicroGest suit une **architecture en couches**, appliquée de façon identique à chaque module :

```
┌─────────────────────────────────────────────┐
│  PRÉSENTATION   →  vues FXML + contrôleurs   │
│                    (*-form.fxml, *Controller)│
├─────────────────────────────────────────────┤
│  MÉTIER         →  services                  │
│                    (*Service.java)           │
│                    validations, calculs,      │
│                    règles de gestion          │
├─────────────────────────────────────────────┤
│  ACCÈS DONNÉES  →  repositories               │
│                    (*Repository.java)         │
│                    JPA / Hibernate             │
├─────────────────────────────────────────────┤
│  INFRASTRUCTURE →  PostgreSQL + SMTP (Mailtrap)│
└─────────────────────────────────────────────┘
```

Chaque module (Adhérent, Opération, Prêt, Remboursement, Épargne, Utilisateur) reproduit le même schéma : une entité JPA, un repository, un service, et un contrôleur gérant un écran de liste paginée avec un formulaire en fenêtre modale (popup).

### Arborescence du projet

```
src/main/java/said/microgest/
├── config/           → configuration Hibernate, EmailConfig
├── controllers/       → contrôleurs JavaFX (un par module)
├── entities/          → entités JPA (Adherent, Pret, Operation, ...)
├── enums/              → StatutAdherent, TypeOperation, StatutPret, Role, Permissions
├── repositories/       → accès aux données (JPA)
├── services/           → règles métier
├── utils/              → AlertUtil, EmailUtil, PdfExporter, SessionContext
└── MainApplication.java

src/main/resources/
├── views/              → fichiers *.fxml
├── css/                → application.css
├── email.propertie     → configuration SMTP (à créer, voir §5.3)
└── persistence.xml     → configuration JPA/Hibernate

schema.sql              → script de création de la base de données
```

---

## 7. Comptes de test

Ces comptes sont créés automatiquement par `schema.sql`.

| Rôle           | Identifiant | Mot de passe | Accès                                        |
| -------------- |-------------|------------| -------------------------------------------- |
| Administrateur | `admin`     | `admin123` | Accès complet à tous les modules             |
| Agent          | `agent1`    | `agent123` | Adhérents, opérations, prêts, remboursements |
| Superviseur    | `super1`    | `super123` | Validation des prêts, consultation, export   |
