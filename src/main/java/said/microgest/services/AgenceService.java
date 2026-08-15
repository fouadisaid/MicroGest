package said.microgest.services;

import said.microgest.entities.Agence;
import said.microgest.repositories.AgenceRepository;

import java.util.List;
import java.util.regex.Pattern;

public class AgenceService {

    private final AgenceRepository agenceRepository =
            new AgenceRepository();

    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    public Agence findById(int id) {

        return agenceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Agence introuvable."));
    }

    public List<Agence> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return agenceRepository.search(keyword);
    }

    public List<Agence> findPaginated(int page, int size) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        return agenceRepository.findPaginated(page, size);
    }

    public long count() {
        return agenceRepository.count();
    }

    public long countSearch(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return count();
        }

        return agenceRepository.countSearch(keyword);
    }

    public Agence create(Agence agence) {

        validate(agence);

        return agenceRepository.save(agence);
    }

    public Agence update(Agence agence) {

        agenceRepository.findById(agence.getId())
                .orElseThrow(() ->
                        new RuntimeException("Agence introuvable."));

        validate(agence);

        return agenceRepository.save(agence);
    }

    public boolean delete(int id) {

        agenceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Agence introuvable."));

        return agenceRepository.delete(id);
    }

    private void validate(Agence agence) {

        if (agence.getNom() == null ||
                agence.getNom().isBlank()) {

            throw new RuntimeException(
                    "Le nom de l'agence est obligatoire."
            );
        }

        if (agence.getAdresse() == null ||
                agence.getAdresse().isBlank()) {

            throw new RuntimeException(
                    "L'adresse est obligatoire."
            );
        }

        if (agence.getTelephone() == null ||
                agence.getTelephone().isBlank()) {

            throw new RuntimeException(
                    "Le téléphone est obligatoire."
            );
        }

        if (!isValidSenegalPhone(agence.getTelephone())) {

            throw new RuntimeException(
                    "Le numéro de téléphone doit être un numéro sénégalais valide."
            );
        }

        if (agence.getEmail() == null ||
                agence.getEmail().isBlank()) {

            throw new RuntimeException(
                    "L'adresse email est obligatoire."
            );
        }

        if (!isValidEmail(agence.getEmail())) {

            throw new RuntimeException(
                    "L'adresse email n'est pas valide."
            );
        }
    }

    private boolean isValidSenegalPhone(String telephone) {

        String value =
                telephone
                        .replaceAll("\\s+", "")
                        .trim();

        return Pattern.matches(
                "^(\\+221|00221)?(70|75|76|77|78)[0-9]{7}$",
                value
        );
    }

    private boolean isValidEmail(String email) {

        return Pattern.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                email.trim()
        );
    }
}