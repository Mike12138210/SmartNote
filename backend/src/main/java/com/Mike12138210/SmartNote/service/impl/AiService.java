package com.Mike12138210.SmartNote.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
    @Autowired
    private WebClient webClient;

    @Value("${ai.api.key}")
    private String apiKey;
    @Value("${ai.api.model:deepseek-chat}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void tellCallAi(){
        String prompt = "请简单介绍一下你自己，一句话即可。";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model",model);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role","user");
        userMessage.put("content",prompt);
        messages.add(userMessage);
        requestBody.put("messages",messages);
        requestBody.put("temperature",0.7);

        try{
            String json = new ObjectMapper().writeValueAsString(requestBody);
            System.out.println("请求体：" + json);
        }catch (Exception e){
            e.printStackTrace();
        }

        String response = webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization","Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class) // 以String形式接收完整响应
                .block();
        System.out.println("AI返回：");
        System.out.println(response);
    }

    public Map<String, Object> generateSummaryAndKeyPoints(String content){
        // 构造Prompt
        String prompt = String.format(
                "请为以下笔记生成一段摘要（50字以内）和3个要点（每条20字以内）。\n" +
                        "**重要：只输出纯JSON，不要有任何额外解释、不要用Markdown代码块、不要添加任何前缀或后缀。**\n" +
                        "输出格式必须严格为：{\"summary\":\"摘要内容\", \"keyPoints\":[\"要点1\",\"要点2\",\"要点3\"]}\n\n" +
                        "笔记内容：\n%s",content
        );

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model",model);
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role","user");
        userMessage.put("content",prompt);
        messages.add(userMessage);
        requestBody.put("messages",messages);
        requestBody.put("temperature",0.7);

        // 发送请求
        String responseJson = webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization","Bearer " + apiKey)
                .bodyValue(requestBody) // 把上面构造的 requestBody 作为请求体发送（Spring 会自动转成 JSON）
                .retrieve() // 表示要获取响应
                .bodyToMono(String.class) // 声明要把响应体当作一个字符串接收（Mono是响应式编程中的“单个结果”）
                .block(); // 阻塞当前线程，直到拿到响应结果（因为 AI 调用是同步的，需要等待）

        // 解析响应，提取content
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(responseJson,Map.class); // 将 JSON 字符串 responseJson 解析成一个 Java 的 Map 对象。
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String aiContent = (String) message.get("content");
            return mapper.readValue(aiContent,Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败",e);
        }
    }
}