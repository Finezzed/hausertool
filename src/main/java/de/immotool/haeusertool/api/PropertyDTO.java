package de.immotool.haeusertool.api;

import jakarta.validation.constraints.NotBlank;

public record PropertyDTO(Long id,
                          @NotBlank String name,
                          String address,
                          Double areaM2) {}
