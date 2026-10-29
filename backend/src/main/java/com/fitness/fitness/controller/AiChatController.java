package com.fitness.fitness.controller;

import com.fitness.fitness.dto.AiChatDTO;
import com.fitness.fitness.entity.AiChat;
import com.fitness.fitness.service.AiChatService;
import com.fitness.fitness.vo.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 发起AI问答，调用Python后端生成饮食运动方案
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@Valid @RequestBody AiChatDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        Map<String, Object> res = aiChatService.chat(dto, userId);
        return Result.success(res, "AI方案生成完成");
    }

    /**
     * 查询历史对话记录
     */
    @GetMapping("/history")
    public Result<Page<AiChat>> history(@RequestParam(defaultValue = "1") Long pageNum,
                                        @RequestParam(defaultValue = "10") Long pageSize,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        Page<AiChat> page = aiChatService.getHistory(pageNum, pageSize, userId);
        return Result.success(page);
    }
}