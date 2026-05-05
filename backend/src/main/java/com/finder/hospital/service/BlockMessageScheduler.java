package com.finder.hospital.service;

import com.finder.hospital.client.BlockMessageClient;
import com.finder.hospital.client.BlockMessageItem;
import com.finder.hospital.domain.BlockMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 차단메시지 주기적 폴링 스케줄러. */
@Component
@RequiredArgsConstructor
public class BlockMessageScheduler {

    private static final Logger log = LoggerFactory.getLogger(BlockMessageScheduler.class);

    private final BlockMessageClient blockMessageClient;
    private final BlockMessageCache cache;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        refresh();
    }

    /** 5분 주기로 갱신. 차단 메시지는 실시간 병상보다 변경 빈도가 낮음. */
    @Scheduled(fixedDelayString = "${block.message.refresh-interval-ms}")
    public void refresh() {
        List<BlockMessageItem> items = blockMessageClient.getAllMessages();

        Map<String, List<BlockMessage>> grouped = new HashMap<>();
        for (BlockMessageItem item : items) {
            if (item.getHpid() == null) continue;
            grouped.computeIfAbsent(item.getHpid(), k -> new ArrayList<>())
                    .add(BlockMessage.from(item));
        }
        cache.replaceAll(grouped);
        log.info("차단메시지 갱신 완료. 병원 {}개 / 메시지 {}건", grouped.size(), items.size());
    }
}
