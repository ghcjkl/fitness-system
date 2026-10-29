package com.fitness.fitness.serviceimpl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.dto.AiChatDTO;
import com.fitness.fitness.entity.AiChat;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.AiChatMapper;
import com.fitness.fitness.service.AiChatService;
import com.fitness.fitness.service.HealthRecordService;
import com.fitness.fitness.util.AiHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiChatServiceImpl implements AiChatService {
    @Autowired
    private AiChatMapper aiChatMapper;
    @Autowired
    private AiHttpUtil aiHttpUtil;
    @Autowired
    private HealthRecordService healthRecordService;

    @Override
    public Map<String, Object> chat(AiChatDTO dto, Long userId) {
        // 组装参数：用户提问 + 用户健康档案
        Map<String, Object> param = new HashMap<>();
        param.put("question", dto.getQuestion());
        HealthRecord record = healthRecordService.getByUserId(userId);
        param.put("health", record);

        // 调用Python AI接口
        Map<String, Object> aiResult = aiHttpUtil.chatAi(param);
        if (aiResult == null || aiResult.get("answer") == null) {
            throw new BusinessException("AI服务返回结果异常");
        }
        String answer = aiResult.get("answer").toString();

        // 保存对话记录（含RAG溯源引用）
        AiChat chat = new AiChat();
        chat.setUserId(userId);
        chat.setQuestion(dto.getQuestion());
        chat.setAnswer(answer);
        Object sources = aiResult.get("sources");
        if (sources != null) {
            chat.setSources(JSONUtil.toJsonStr(sources));
        }
        chat.setCreateTime(LocalDateTime.now());
        aiChatMapper.insert(chat);

        return aiResult;
    }

    @Override
    public Page<AiChat> getHistory(Long pageNum, Long pageSize, Long userId) {
        Page<AiChat> page = new Page<>(pageNum, pageSize);
        return aiChatMapper.selectPage(page, Wrappers.<AiChat>lambdaQuery()
                .eq(AiChat::getUserId, userId)
                .orderByDesc(AiChat::getCreateTime));
    }
}