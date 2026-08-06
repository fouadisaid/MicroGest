package said.microgest.services;

import said.microgest.entities.Agence;
import said.microgest.repositories.AgenceRepository;

import java.util.List;

public class AgenceService {

    private final AgenceRepository agenceRepository = new AgenceRepository();

    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    public Agence findById(int id) {

        return agenceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Agence introuvable."));
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

        if (agence.getNom() == null || agence.getNom().isBlank()) {
            throw new RuntimeException("Le nom de l'agence est obligatoire.");
        }

        if (agence.getAdresse() == null || agence.getAdresse().isBlank()) {
            throw new RuntimeException("L'adresse est obligatoire.");
        }

        if (agence.getTelephone() == null || agence.getTelephone().isBlank()) {
            throw new RuntimeException("Le téléphone est obligatoire.");
        }

    }

}