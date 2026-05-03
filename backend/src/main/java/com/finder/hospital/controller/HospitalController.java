package com.finder.hospital.controller;

import com.finder.hospital.dto.HospitalDetailResponse;
import com.finder.hospital.dto.HospitalListResponse;
import com.finder.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 응급실 조회 API */
@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /** 위치 기반 근처 응급실 목록을 조회한다. */
    @GetMapping
    public ResponseEntity<HospitalListResponse> getHospitals(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(hospitalService.getHospitals(lat, lng, radius));
    }

    /** 응급실 상세 정보를 조회한다. */
    @GetMapping("/{hospitalId}")
    public ResponseEntity<HospitalDetailResponse> getHospitalDetail(
            @PathVariable String hospitalId) {
        return ResponseEntity.ok(hospitalService.getHospitalDetail(hospitalId));
    }
}
