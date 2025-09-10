package de.immotool.haeusertool.service;

import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.repo.PropertyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyService {
    private final PropertyRepository repo;

    public PropertyService(PropertyRepository repo) { this.repo = repo; }

    public List<Property> listAll() { return repo.findAll(); }

    @Transactional
    public Property create(@Valid Property p) {
        // Beispielregel: Fläche darf nicht negativ sein.
        if (p.getAreaM2() != null && p.getAreaM2() < 0) {
            throw new IllegalArgumentException("areaM2 must be >= 0");
        }
        return repo.save(p);
    }

    @Transactional
    public Property update(Long id, @Valid Property update) {
        var existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property " + id + " not found"));
        existing.setName(update.getName());
        existing.setAddress(update.getAddress());
        existing.setAreaM2(update.getAreaM2());
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}

