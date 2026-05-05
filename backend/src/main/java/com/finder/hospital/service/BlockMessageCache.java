package com.finder.hospital.service;

import com.finder.hospital.domain.BlockMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** 차단 메시지 메모리 캐시. hpid → 메시지 목록 매핑. */
@Component
public class BlockMessageCache {

    private volatile Map<String, List<BlockMessage>> messages = Map.of();

    /** 새 스냅샷으로 통째 교체한다. */
    public void replaceAll(Map<String, List<BlockMessage>> newMessages) {
        this.messages = Map.copyOf(newMessages);
    }

    /** 특정 병원의 활성(차단 진행 중) 메시지만 반환한다. */
    public List<BlockMessage> findActive(String hpid) {
        List<BlockMessage> all = messages.get(hpid);
        if (all == null || all.isEmpty()) return List.of();
        return all.stream().filter(BlockMessage::isActive).toList();
    }

    public int size() {
        return messages.size();
    }
}
