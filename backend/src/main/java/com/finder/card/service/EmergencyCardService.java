package com.finder.card.service;

import com.finder.card.domain.EmergencyCard;
import com.finder.card.dto.CardCreateRequest;
import com.finder.card.dto.CardCreateResponse;
import com.finder.card.dto.CardResponse;
import com.finder.card.dto.CardUpdateRequest;
import com.finder.card.repository.EmergencyCardRepository;
import com.finder.common.exception.NotFoundException;
import com.finder.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/** 응급카드 생성, 조회, 수정, 삭제 유스케이스 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyCardService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmergencyCardRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    /** 응급카드를 생성하고 공유 URL이 포함된 응답을 반환한다. */
    @Transactional
    public CardCreateResponse createCard(CardCreateRequest req) {
        String token = generateUniqueToken();
        String pinHash = passwordEncoder.encode(req.pin());
        repository.save(EmergencyCard.create(token, req, pinHash));
        return new CardCreateResponse(token, baseUrl + "/card/" + token);
    }

    /** 토큰으로 응급카드를 조회한다. */
    public CardResponse getCard(String token) {
        return CardResponse.from(findByTokenOrThrow(token));
    }

    /** PIN을 검증한 후 응급카드를 수정한다. */
    @Transactional
    public CardResponse updateCard(String token, String pin, CardUpdateRequest req) {
        EmergencyCard card = findByTokenOrThrow(token);
        verifyPin(pin, card.getPinHash());
        card.update(req);
        return CardResponse.from(card);
    }

    /** PIN을 검증한 후 응급카드를 삭제한다. */
    @Transactional
    public void deleteCard(String token, String pin) {
        EmergencyCard card = findByTokenOrThrow(token);
        verifyPin(pin, card.getPinHash());
        repository.delete(card);
    }

    private EmergencyCard findByTokenOrThrow(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카드입니다."));
    }

    /** PIN 검증 실패 시 카드 존재 여부 노출 방지를 위해 동일한 예외를 반환한다. */
    private void verifyPin(String pin, String pinHash) {
        if (!passwordEncoder.matches(pin, pinHash)) {
            throw new UnauthorizedException("PIN이 올바르지 않습니다.");
        }
    }

    private String generateUniqueToken() {
        String token;
        do {
            StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
            }
            token = sb.toString();
        } while (repository.existsByToken(token));
        return token;
    }
}
