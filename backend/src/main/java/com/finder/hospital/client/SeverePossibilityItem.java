package com.finder.hospital.client;

import java.util.Set;

/** 한 병원의 중증질환 시술 가능 시술 코드 집합. (예: ["mkioskty1", "mkioskty22"]) */
public record SeverePossibilityItem(
        String hpid,
        Set<String> availableCodes
) {
    /** 외부에서 전달받은 가변 컬렉션이 record 내부 상태로 새지 않도록 불변 사본을 보관한다. */
    public SeverePossibilityItem {
        availableCodes = availableCodes == null ? Set.of() : Set.copyOf(availableCodes);
    }
}
