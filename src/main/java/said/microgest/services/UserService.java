package said.microgest.services;

import org.mindrot.jbcrypt.BCrypt;
import said.microgest.entities.User;
import said.microgest.enums.Role;
import said.microgest.repositories.UserRepository;
import said.microgest.utils.SessionContext;

import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    // =========================================================
    // LECTURE
    // =========================================================

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findPaginated(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        return userRepository.findPaginated(page, size);
    }

    public long count() {
        return userRepository.count();
    }

    public List<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return userRepository.search(keyword);
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }

    // =========================================================
    // AUTHENTIFICATION
    // =========================================================

    public User login(String login, String password) {
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new RuntimeException("Nom d'utilisateur ou mot de passe incorrect."));

        if (!user.isActif()) {
            throw new RuntimeException("Ce compte est désactivé.");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Nom d'utilisateur ou mot de passe incorrect.");
        }

        SessionContext.setCurrentUser(user);
        return user;
    }

    public void logout() {
        SessionContext.clear();
    }

    // =========================================================
    // CREATION
    // =========================================================

    public User create(User user) {
        validate(user);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cette adresse email existe déjà.");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Le mot de passe est obligatoire pour un nouvel utilisateur.");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

        if (!user.isActif()) {
            user.setActif(true);
        }

        return userRepository.save(user);
    }

    // =========================================================
    // MODIFICATION
    // =========================================================

    public User update(User user) {
        User oldUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        validate(user);

        userRepository.findByUsername(user.getUsername())
                .ifPresent(u -> {
                    if (u.getId() != user.getId()) {
                        throw new RuntimeException("Ce nom d'utilisateur existe déjà.");
                    }
                });

        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    if (u.getId() != user.getId()) {
                        throw new RuntimeException("Cette adresse email existe déjà.");
                    }
                });

        if (!user.isActif()) {
            verifierDesactivationAutorisee(oldUser);
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(oldUser.getPassword());
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        user.setCreatedAt(oldUser.getCreatedAt());
        user.setCreatedBy(oldUser.getCreatedBy());

        return userRepository.save(user);
    }

    // =========================================================
    // ACTIVATION / DESACTIVATION
    // =========================================================

    public User toggleActif(int id) {
        User user = findById(id);

        if (user.isActif()) {
            verifierDesactivationAutorisee(user);
        }

        user.setActif(!user.isActif());
        return userRepository.save(user);
    }

    private void verifierDesactivationAutorisee(User user) {
        if (SessionContext.getCurrentUser() != null &&
                SessionContext.getCurrentUser().getId() == user.getId()) {
            throw new RuntimeException("Impossible de désactiver votre propre compte.");
        }

        if (user.getRole() == Role.ADMIN) {
            long adminsActifs = userRepository.countActifsByRole(Role.ADMIN);
            if (adminsActifs <= 1) {
                throw new RuntimeException(
                        "Impossible de désactiver le dernier administrateur actif du système."
                );
            }
        }
    }

    // =========================================================
    // SUPPRESSION
    // =========================================================

    public boolean delete(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        if (SessionContext.getCurrentUser() != null &&
                SessionContext.getCurrentUser().getId() == user.getId()) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur connecté.");
        }

        if (user.getRole() == Role.ADMIN) {
            long totalAdmins = userRepository.countByRole(Role.ADMIN);
            if (totalAdmins <= 1) {
                throw new RuntimeException(
                        "Impossible de supprimer le dernier administrateur du système."
                );
            }
        }

        return userRepository.delete(id);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validate(User user) {
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        if (nom.isBlank()) {
            throw new RuntimeException("Le nom est obligatoire.");
        }

        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        if (prenom.isBlank()) {
            throw new RuntimeException("Le prénom est obligatoire.");
        }

        String username = user.getUsername() != null ? user.getUsername().trim() : "";
        if (username.isBlank()) {
            throw new RuntimeException("Le nom d'utilisateur est obligatoire.");
        }
        if (username.length() < 3) {
            throw new RuntimeException("Le nom d'utilisateur doit contenir au moins 3 caractères.");
        }

        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        if (email.isBlank()) {
            throw new RuntimeException("L'adresse email est obligatoire.");
        }
        if (!Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
            throw new RuntimeException("L'adresse email est invalide.");
        }

        String telephone = user.getTelephone() != null
                ? user.getTelephone().replaceAll("[\\s\\-().]", "")
                : "";

        if (telephone.isBlank()) {
            throw new RuntimeException("Le téléphone est obligatoire.");
        }

        if (telephone.startsWith("+221")) {
            telephone = telephone.substring(4);
        }

        if (!telephone.matches("^7[05678]\\d{7}$")) {
            throw new RuntimeException("Le numéro de téléphone sénégalais est invalide.");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Le rôle est obligatoire.");
        }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setUsername(username);
        user.setEmail(email);
        user.setTelephone(telephone);
    }
}