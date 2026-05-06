package com.finder.hospital.client;

import java.util.Set;

/** 한 병원의 중증질환 시술 가능 시술 코드 집합. (예: ["mkioskty1", "mkioskty22"]) */
public record SeverePossibilityItem(
        String hpid,
        Set<String> availableCodes
) {}
