package de.immotool.haeusertool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "property")
public class Property {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank                      // Bean Validation: darf nicht leer sein
    @Column(nullable = false)
    private String name;

    private String address;

    private Double areaM2;

    @Version                       // Optimistic Locking (gleichzeitiges Bearbeiten absichern)
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // --- Getter/Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getAreaM2() { return areaM2; }
    public void setAreaM2(Double areaM2) { this.areaM2 = areaM2; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
