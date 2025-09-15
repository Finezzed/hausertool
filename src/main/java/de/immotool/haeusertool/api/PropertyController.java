package de.immotool.haeusertool.api;

import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    private final PropertyService svc;

    public PropertyController(PropertyService svc) {
        this.svc = svc; }

    @GetMapping
    public List<PropertyDTO> list() {
        return svc.listAll().stream().map(this::toDTO).toList();
    }

    @PostMapping
    public ResponseEntity<PropertyDTO> create(@Valid @RequestBody PropertyDTO dto) {
        var saved = svc.create(toEntity(dto));
        return ResponseEntity.created(URI.create("/api/properties/" + saved.getId()))
                .body(toDTO(saved));
    }

    @PutMapping("/{id}")
    public PropertyDTO update(@PathVariable Long id, @Valid @RequestBody PropertyDTO dto) {
        var updated = svc.update(id, toEntity(dto));
        return toDTO(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { svc.delete(id); }

    // --- Mapper ---
    private PropertyDTO toDTO(Property p) {
        return new PropertyDTO(p.getId(), p.getStreet(), p.getHausnr(), p.getPostleitzahl(), p.getOrt(), p.getAreaM2());
    }
    private Property toEntity(PropertyDTO d) {
        var p = new Property();
        p.setStreet(d.street());
        p.setStreet(String.valueOf(d.hausnr()));
        p.setStreet(String.valueOf(d.postleitzahl()));
        p.setStreet(d.ort());
        p.setAreaM2(d.areaM2());
        return p;
    }
}

