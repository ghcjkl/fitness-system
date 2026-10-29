package com.fitness.fitness.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AiHttpUtil {

    private final WebClient aiWebClient;

    @Autowired
    public AiHttpUtil(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    /**
     * 同步调用Python AI问答接口
     * @param param 请求参数（用户档案+提问）
     * @return AI返回方案JSON
     */
    public Map<String, Object> chatAi(Map<String, Object> param) {
        return postAi("/rag/chat", param);
    }

    /**
     * 同步调用Python服务的通用POST接口
     * @param uri 相对路径，如 /rag/plan/diet
     * @param param 请求参数
     * @return AI返回JSON
     */
    public Map<String, Object> postAi(String uri, Map<String, Object> param) {
        Mono<Map> mono = aiWebClient.post()
                .uri(uri)
                .bodyValue(param)
                .retrieve()
                .bodyToMono(Map.class);
        // 阻塞同步获取结果
        return mono.block();
    }
}