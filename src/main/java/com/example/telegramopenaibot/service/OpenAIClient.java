package com.example.telegramopenaibot.service;

import com.example.telegramopenaibot.dto.ChatGptResponse;
import com.example.telegramopenaibot.dto.Message;
import com.example.telegramopenaibot.web.request.ChatGPTRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private static final String PROMPT = "Ответь на следующий вопрос";

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    private final RestTemplate restTemplate;

    public String getResponse(String userMessage) {

        if (StringUtils.isBlank(userMessage)) {
            return "Пожалуйста, введите сообщение";
        }

        ChatGPTRequest request = new ChatGPTRequest(model, PROMPT);
        request.getMessages().add(new Message("user", userMessage));

        ChatGptResponse chatGptResponse = restTemplate.postForObject(apiURL, request, ChatGptResponse.class);

        if (chatGptResponse == null || chatGptResponse.getChoices() == null || chatGptResponse.getChoices().isEmpty()) {
            return "Что-то пошло не так";
        }

        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }
}

