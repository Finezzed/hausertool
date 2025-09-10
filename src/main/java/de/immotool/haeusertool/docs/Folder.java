package de.immotool.haeusertool.docs;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity @Table(name="folder",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id","name", "category"}))
public class Folder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable=false)
    private String name;                // Anzeigename

    @ManyToOne(fetch = FetchType.LAZY)  // null = Wurzelordner
    @JoinColumn(name="parent_id")
    private Folder parent;

    @Column(nullable=false)
    private String path;                // z.B. "Steuer/2025" relativ zu storage-root

    @Column(nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentCategory category;

    @Version private Long version;

    // getters/setters
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public Folder getParent(){return parent;} public void setParent(Folder parent){this.parent=parent;}
    public String getPath(){return path;} public void setPath(String path){this.path=path;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant t){this.createdAt=t;}
    public Long getVersion(){return version;} public void setVersion(Long v){this.version=v;}

    public DocumentCategory getCategory() {
        return category;
    }

    public void setCategory(DocumentCategory category) {
        this.category = category;
    }
}
