package de.immotool.haeusertool.docs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final Path root;

    /**
     * @param rootDir Basisverzeichnis für die Dateiablage (z. B. "./data/storage")
     */
    public DocumentService(DocumentRepository repo,
                           @Value("${storage.root:./data/storage}") String rootDir) {
        this.repo = repo;
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    /**
     * Speichert eine Datei im (optional) gewählten Ordner und legt den DB-Datensatz an.
     *
     * @param in              Dateiinhalt
     * @param originalFilename Ursprünglicher Dateiname (für Download/Anzeige)
     * @param contentType     MIME-Type (kann null sein)
     * @param sizeBytes       Größe in Bytes (kann -1 sein, wenn unbekannt)
     * @param title           Optionaler Titel (fällt auf originalFilename zurück)
     * @param category        Optionale Kategorie
     * @param folder          Zielordner (null = Wurzel)
     * @return gespeichertes Document
     */
    @Transactional
    public Document store(InputStream in,
                          String originalFilename,
                          String contentType,
                          long sizeBytes,
                          String title,
                          DocumentCategory category,
                          Folder folder) {
        try {
            // Root-Verzeichnis sicherstellen
            Files.createDirectories(root);

            // Basisordner: Kategorie-Root oder expliziter Folder-Pfad
            Path base = (folder == null)
                    ? root.resolve(category.name())
                    : root.resolve(folder.getPath());
            Files.createDirectories(base);

            // Dateiname: UUID + Original-Endung
            String ext = "";
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0) ext = originalFilename.substring(dot);
            String storedName = java.util.UUID.randomUUID() + ext;
            Path target = base.resolve(storedName);

            // Datei schreiben + SHA-256 berechnen
            var md = java.security.MessageDigest.getInstance("SHA-256");
            try (var out = java.nio.file.Files.newOutputStream(target, java.nio.file.StandardOpenOption.CREATE_NEW);
                 var dos = new java.security.DigestOutputStream(out, md)) {
                in.transferTo(dos);
            }
            String sha256 = java.util.HexFormat.of().formatHex(md.digest());

            // DB-Entity befüllen (jetzt ERST doc erzeugen!)
            Document doc = new Document();
            doc.setOriginalFilename(originalFilename);
            doc.setTitle((title == null || title.isBlank()) ? originalFilename : title);
            doc.setContentType(contentType);
            doc.setSizeBytes(sizeBytes >= 0 ? sizeBytes : null);
            doc.setStoragePath(root.relativize(target).toString().replace('\\', '/'));
            doc.setSha256(sha256);
            doc.setFolder(folder);
            doc.setCategory(folder != null ? folder.getCategory() : category);

            return repo.save(doc);

        } catch (Exception e) {
            throw new RuntimeException("Speichern fehlgeschlagen: " + e.getMessage(), e);
        }
    }


    /**
     * Liefert die gespeicherte Datei als Spring-Resource zurück.
     */
    public Resource loadAsResource(Long id) {
        Document doc = repo.findById(id).orElseThrow();
        Path file = root.resolve(doc.getStoragePath());
        return new FileSystemResource(file);
    }

    /**
     * Löscht Datei (falls vorhanden) und den DB-Eintrag.
     */
    @Transactional
    public void delete(Long id) {
        Document doc = repo.findById(id).orElseThrow();
        try {
            Files.deleteIfExists(root.resolve(doc.getStoragePath()));
        } catch (Exception ignore) {
            // Wenn die Datei schon fehlt, löschen wir trotzdem den DB-Eintrag
        }
        repo.delete(doc);
    }
}
