package com.finder.card.domain;

import com.finder.card.dto.CardCreateRequest;
import com.finder.card.dto.CardUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 응급카드 Aggregate Root. 환자 의료 정보를 보관하고 QR로 공유된다. */
@Entity
@Table(name = "emergency_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EmergencyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String token;

    @Column(length = 50)
    private String name;

    private LocalDate birthDate;

    @Column(length = 5)
    private String bloodType;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(columnDefinition = "TEXT")
    private String surgeries;

    @Column(length = 50)
    private String guardianName;

    @Column(length = 20)
    private String guardianPhone;

    private Boolean isPregnant;

    @Column(length = 255)
    private String pinHash;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /** 새 응급카드를 생성한다. PIN은 이미 해시된 값을 받는다. */
    public static EmergencyCard create(String token, CardCreateRequest req, String pinHash) {
        EmergencyCard card = new EmergencyCard();
        card.token = token;
        card.name = req.name();
        card.birthDate = req.birthDate();
        card.bloodType = req.bloodType();
        card.allergies = toCommaSeparated(req.allergies());
        card.medications = toCommaSeparated(req.medications());
        card.conditions = toCommaSeparated(req.conditions());
        card.surgeries = toCommaSeparated(req.surgeries());
        card.guardianName = req.guardianName();
        card.guardianPhone = req.guardianPhone();
        card.isPregnant = req.isPregnant();
        card.pinHash = pinHash;
        return card;
    }

    /** 응급카드 정보를 수정한다. */
    public void update(CardUpdateRequest req) {
        this.name = req.name();
        this.birthDate = req.birthDate();
        this.bloodType = req.bloodType();
        this.allergies = toCommaSeparated(req.allergies());
        this.medications = toCommaSeparated(req.medications());
        this.conditions = toCommaSeparated(req.conditions());
        this.surgeries = toCommaSeparated(req.surgeries());
        this.guardianName = req.guardianName();
        this.guardianPhone = req.guardianPhone();
        this.isPregnant = req.isPregnant();
    }

    public List<String> getAllergiesList() { return toList(allergies); }
    public List<String> getMedicationsList() { return toList(medications); }
    public List<String> getConditionsList() { return toList(conditions); }
    public List<String> getSurgeriesList() { return toList(surgeries); }

    private static String toCommaSeparated(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private static List<String> toList(String str) {
        if (str == null || str.isBlank()) return List.of();
        return List.of(str.split(","));
    }
}
