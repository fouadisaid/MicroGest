package said.microgest.services;

import org.mindrot.jbcrypt.BCrypt;
import said.microgest.entities.User;
import said.microgest.repositories.UserRepository;
import said.microgest.utils.SessionContext;

import java.util.List;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(int id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable."));
    }

    public User login(String login, String password) {

        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() ->
                        new RuntimeException("Nom d'utilisateur ou mot de passe incorrect."));

        if (!user.isActif()) {
            throw new RuntimeException("Ce compte est désactivé.");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Nom d'utilisateur ou mot de passe incorrect.");
        }

        SessionContext.setCurrentUser(user);

        return user;
    }

    public User create(User user) {

        validate(user);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cette adresse email existe déjà.");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

        return userRepository.save(user);
    }

    public User update(User user) {

        User oldUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable."));

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

        if (user.getPassword() == null || user.getPassword().isBlank()) {

            user.setPassword(oldUser.getPassword());

        } else {

            user.setPassword(
                    BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())
            );
        }

        return userRepository.save(user);
    }

    public boolean delete(int id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable."));

        if (SessionContext.getCurrentUser() != null &&
                SessionContext.getCurrentUser().getId() == user.getId()) {

            throw new RuntimeException("Impossible de supprimer l'utilisateur connecté.");
        }

        return userRepository.delete(id);
    }

    public void logout() {

        SessionContext.clear();

    }

    private void validate(User user) {

        if (user.getNom() == null || user.getNom().isBlank()) {
            throw new RuntimeException("Le nom est obligatoire.");
        }

        if (user.getPrenom() == null || user.getPrenom().isBlank()) {
            throw new RuntimeException("Le prénom est obligatoire.");
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new RuntimeException("Le nom d'utilisateur est obligatoire.");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("L'adresse email est obligatoire.");
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Adresse email invalide.");
        }

        if (user.getTelephone() == null || user.getTelephone().isBlank()) {
            throw new RuntimeException("Le téléphone est obligatoire.");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Le rôle est obligatoire.");
        }

        if (user.getId() == 0 &&
                (user.getPassword() == null || user.getPassword().isBlank())) {

            throw new RuntimeException("Le mot de passe est obligatoire.");
        }

    }

}