package de.immotool.haeusertool.api;


public record PropertyDTO(

        Long id,
        String street,
        String hausnr,
        String postleitzahl,
        String ort,
        Double areaM2) {}
