package com.finder.card.controller;

import com.finder.card.dto.CardCreateRequest;
import com.finder.card.dto.CardCreateResponse;
import com.finder.card.dto.CardResponse;
import com.finder.card.dto.CardUpdateRequest;
import com.finder.card.service.EmergencyCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 응급카드 CRUD API */
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class EmergencyCardController {

    private final EmergencyCardService service;

    /** 응급카드를 생성한다. */
    @PostMapping
    public ResponseEntity<CardCreateResponse> createCard(@Valid @RequestBody CardCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCard(req));
    }

    /** 토큰으로 응급카드를 공개 조회한다. */
    @GetMapping("/{token}")
    public ResponseEntity<CardResponse> getCard(@PathVariable String token) {
        return ResponseEntity.ok(service.getCard(token));
    }

    /** PIN을 검증한 후 응급카드를 수정한다. */
    @PutMapping("/{token}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable String token,
            @RequestHeader("X-Card-Pin") String pin,
            @Valid @RequestBody CardUpdateRequest req) {
        return ResponseEntity.ok(service.updateCard(token, pin, req));
    }

    /** PIN을 검증한 후 응급카드를 삭제한다. */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable String token,
            @RequestHeader("X-Card-Pin") String pin) {
        service.deleteCard(token, pin);
        return ResponseEntity.noContent().build();
    }
}
