package de.immotool.haeusertool.docs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.List;

@Service
public class FolderService {
    private final FolderRepository repo;
    private final Path root;

    public FolderService(FolderRepository repo, @Value("${storage.root:./data/storage}") String rootDir) {
        this.repo = repo;
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    public List<Folder> roots(DocumentCategory cat) {
        return repo.findByParentIsNullAndCategoryOrderByNameAsc(cat);
    }
    public List<Folder> children(Folder parent, DocumentCategory cat) {
        return parent == null
                ? repo.findByParentIsNullAndCategoryOrderByNameAsc(cat)
                : repo.findByParentAndCategoryOrderByNameAsc(parent, parent.getCategory());
    }

    @Transactional
    public Folder create(String name, Long parentId, DocumentCategory category) {
        var parent = parentId == null ? null : repo.findById(parentId).orElseThrow();
        var effectiveCat = (parent != null) ? parent.getCategory() : category;
        if (effectiveCat == null) throw new IllegalArgumentException("Kategorie fehlt");

        var safeName = safe(name);
        var basePath = parent == null ? effectiveCat.name() : parent.getPath(); // z. B. "STEUER" als Rootordner
        var path = basePath + "/" + safeName;

        var folder = new Folder();
        folder.setName(name.trim());
        folder.setParent(parent);
        folder.setCategory(effectiveCat);
        folder.setPath(path);
        var saved = repo.save(folder);

        try { Files.createDirectories(root.resolve(path)); }
        catch (Exception e) { throw new RuntimeException("Ordner anlegen fehlgeschlagen: " + e.getMessage(), e); }

        return saved;
    }

    private static String safe(String name) {
        var n = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","");
        return n.replaceAll("[^a-zA-Z0-9._ -]", "_").replaceAll("[ ]+"," ").trim();
    }
}


