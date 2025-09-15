package de.immotool.haeusertool.api;


public record PropertyDTO(Long id,

                          String street,
                          Double hausnr,
                          Double postleitzahl,
                          String ort,
                          Double areaM2) {}
