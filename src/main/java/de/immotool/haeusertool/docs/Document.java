package de.immotool.haeusertool.docs;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String originalFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    private String title;                 // frei wählbarer Anzeigename
    private String contentType;           // z.B. application/pdf
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    private DocumentCategory category;

    @Column(nullable=false)
    private String storagePath;           // wo auf der Platte gespeichert

    private String sha256;                // optional: Integrität

    @Column(nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    @Version private Long version;

    // getters/setters
    public Long getId() { return id; } public void setId(Long id){this.id=id;}
    public String getOriginalFilename(){return originalFilename;}
    public void setOriginalFilename(String f){this.originalFilename=f;}
    public String getTitle(){return title;} public void setTitle(String t){this.title=t;}
    public String getContentType(){return contentType;} public void setContentType(String c){this.contentType=c;}
    public Long getSizeBytes(){return sizeBytes;} public void setSizeBytes(Long s){this.sizeBytes=s;}
    public DocumentCategory getCategory(){return category;} public void setCategory(DocumentCategory c){this.category=c;}
    public String getStoragePath(){return storagePath;} public void setStoragePath(String p){this.storagePath=p;}
    public String getSha256(){return sha256;} public void setSha256(String s){this.sha256=s;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant t){this.createdAt=t;}
    public Long getVersion(){return version;} public void setVersion(Long v){this.version=v;}
    public Folder getFolder(){return folder;} public void setFolder(Folder f){this.folder=f;}
}

