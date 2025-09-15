package de.immotool.haeusertool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "property")
public class Property {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(name = "image_path")
    private String imagePath;

    private String street;

    private String postleitzahl;

    private String hausnr;

    private String ort;

    private Double areaM2;

    @Version                       // Optimistic Locking (gleichzeitiges Bearbeiten absichern)
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    // --- Getter/Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getPostleitzahl() { return postleitzahl; }
    public void setPostleitzahl(String postleitzahl) { this.postleitzahl = postleitzahl; }
    public String getHausnr() { return hausnr; }
    public void setHausnr(String hausnr) { this.hausnr = hausnr; }
    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }
    public Double getAreaM2() { return areaM2; }
    public void setAreaM2(Double areaM2) { this.areaM2 = areaM2; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public LocalDate getPurchaseDate() { return purchaseDate;}
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

}
