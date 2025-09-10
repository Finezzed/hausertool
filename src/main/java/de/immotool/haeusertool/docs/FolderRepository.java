package de.immotool.haeusertool.docs;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByParentAndCategoryOrderByNameAsc(Folder parent, DocumentCategory category);
    List<Folder> findByParentIsNullAndCategoryOrderByNameAsc(DocumentCategory category);
}

