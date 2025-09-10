package de.immotool.haeusertool.repo;

import de.immotool.haeusertool.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {}
