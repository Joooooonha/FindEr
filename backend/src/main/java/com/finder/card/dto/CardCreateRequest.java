package com.finder.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CardCreateRequest(
        @NotBlank String name,
        LocalDate birthDate,
        String bloodType,
        List<String> allergies,
        List<String> medications,
        List<String> conditions,
        List<String> surgeries,
        String guardianName,
        String guardianPhone,
        Boolean isPregnant,
        @NotBlank @Size(min = 4, max = 8) String pin
) {}
