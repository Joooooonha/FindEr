package com.finder.card.dto;

import com.finder.card.domain.EmergencyCard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CardResponse(
        String token,
        String name,
        LocalDate birthDate,
        String bloodType,
        List<String> allergies,
        List<String> medications,
        List<String> conditions,
        List<String> surgeries,
        String guardianName,
        String guardianPhone,
        Boolean isPregnant,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CardResponse from(EmergencyCard card) {
        return new CardResponse(
                card.getToken(),
                card.getName(),
                card.getBirthDate(),
                card.getBloodType(),
                card.getAllergiesList(),
                card.getMedicationsList(),
                card.getConditionsList(),
                card.getSurgeriesList(),
                card.getGuardianName(),
                card.getGuardianPhone(),
                card.getIsPregnant(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
