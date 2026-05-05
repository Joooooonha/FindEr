package com.finder.hospital.dto;

import com.finder.hospital.domain.BlockMessage;

import java.util.List;

public record BlockMessageResponse(
        String message,
        String messageType,
        String diseaseTypeName
) {
    public static BlockMessageResponse from(BlockMessage source) {
        return new BlockMessageResponse(
                source.message(),
                source.messageType(),
                source.diseaseTypeName()
        );
    }

    /** null 또는 빈 리스트는 빈 List로 변환한다. HospitalResponse·HospitalDetailResponse 공통 사용. */
    public static List<BlockMessageResponse> fromList(List<BlockMessage> sources) {
        if (sources == null || sources.isEmpty()) return List.of();
        return sources.stream().map(BlockMessageResponse::from).toList();
    }
}
