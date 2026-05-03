package com.finder.card.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record CardUpdateRequest(
        @NotBlank String name,
        LocalDate birthDate,
        String bloodType,
        List<String> allergies,
        List<String> medications,
        List<String> conditions,
        List<String> surgeries,
        String guardianName,
        String guardianPhone,
        Boolean isPregnant
) {}
