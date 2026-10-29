package com.fitness.fitness.service;

import com.fitness.fitness.dto.AiChatDTO;
import com.fitness.fitness.entity.AiChat;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

public interface AiChatService {
    Map<String, Object> chat(AiChatDTO dto, Long userId);
    Page<AiChat> getHistory(Long pageNum, Long pageSize, Long userId);
}