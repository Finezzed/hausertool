package de.immotool.haeusertool.docs;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByFolder(Folder folder);
    List<Document> findByFolderIsNullAndCategory(DocumentCategory category);
}
