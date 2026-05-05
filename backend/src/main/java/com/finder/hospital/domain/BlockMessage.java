package com.finder.hospital.domain;

import com.finder.hospital.client.BlockMessageItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 응급실/중증질환 차단 메시지 도메인. 활성 여부 판단은 endedAt 기준. */
public record BlockMessage(
        String message,
        String messageType,
        String diseaseTypeName,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {

    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static BlockMessage from(BlockMessageItem item) {
        return new BlockMessage(
                item.getMessage(),
                item.getMessageType(),
                item.getDiseaseTypeName(),
                parseDate(item.getStartedAt()),
                parseDate(item.getEndedAt())
        );
    }

    /** 차단이 아직 진행 중인지 (endedAt 미래이거나 비어있음). */
    public boolean isActive() {
        if (endedAt == null) return true;
        return endedAt.isAfter(LocalDateTime.now());
    }

    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), API_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
