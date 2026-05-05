package com.finder.hospital.dto;

import com.finder.hospital.domain.BlockMessage;

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
}
