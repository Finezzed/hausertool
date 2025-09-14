package de.immotool.haeusertool.service;

import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.repo.PropertyRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository repo;
    private final Path storageRoot;

    public PropertyService(
            PropertyRepository repo,
            @Value("${storage.root:./data/storage}") String rootDir
    ) {
        this.repo = repo;
        this.storageRoot = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    // -------------------- Bestehende Fachmethoden --------------------

    public List<Property> listAll() {
        return repo.findAll();
    }

    @Transactional
    public Property create(@Valid Property p) {
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
        existing.setStreet(update.getStreet());
        existing.setAreaM2(update.getAreaM2());
        // Bildpfad könnte man hier optional auch übernehmen:
        // existing.setImagePath(update.getImagePath());
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    // -------------------- Bildspeicherung --------------------

    /**
     * Verschiebt eine temporär hochgeladene Datei an den endgültigen Ort
     * unterhalb von storage.root und gibt den RELATIVEN Pfad zurück
     * (z. B. "property-images/8df1...b2.jpg").
     */
    public String storeImage(Path tempFile, String originalFilename) {
        if (tempFile == null || !Files.exists(tempFile)) return null;

        try {
            Files.createDirectories(storageRoot);

            String ext = "";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot >= 0) ext = originalFilename.substring(dot);
            }
            String fileName = UUID.randomUUID() + ext;

            Path imagesDir = storageRoot.resolve("property-images");
            Files.createDirectories(imagesDir);

            Path target = imagesDir.resolve(fileName);
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);

            return "property-images/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Bildspeicherung fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative: Bild direkt aus einem InputStream speichern.
     */
    public String storeImage(InputStream in, String originalFilename) {
        if (in == null) return null;
        try {
            Files.createDirectories(storageRoot);

            String ext = "";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot >= 0) ext = originalFilename.substring(dot);
            }
            String fileName = UUID.randomUUID() + ext;

            Path imagesDir = storageRoot.resolve("property-images");
            Files.createDirectories(imagesDir);

            Path target = imagesDir.resolve(fileName);
            try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                in.transferTo(out);
            }
            return "property-images/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Bildspeicherung fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    /**
     * Liefert einen Stream zum Lesen eines gespeicherten Bildes (für StreamResource).
     */
    public InputStream openImage(String relativePath) {
        if (relativePath == null) return null;
        try {
            Path p = storageRoot.resolve(relativePath).normalize();
            return Files.newInputStream(p, StandardOpenOption.READ);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Optional nützlich, falls du mal den absoluten Pfad brauchst.
     */
    public Path resolveImage(String relativePath) {
        return relativePath == null ? null : storageRoot.resolve(relativePath).normalize();
    }
}

