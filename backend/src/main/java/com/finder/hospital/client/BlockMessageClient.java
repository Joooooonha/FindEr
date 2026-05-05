package com.finder.hospital.client;

import java.util.List;

/** E-Gen 응급실/중증질환 메시지 API 클라이언트 */
public interface BlockMessageClient {

    /** 전국 응급실/중증질환 차단 메시지를 조회한다. */
    List<BlockMessageItem> getAllMessages();
}
