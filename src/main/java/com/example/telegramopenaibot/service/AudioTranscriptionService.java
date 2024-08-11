package com.example.telegramopenaibot.service;

import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioTranscriptionService {

    public static final String WHISPER_1 = "whisper-1";

    @Value("${openai.api.key}")
    private String apiKey;

    @NotNull
    public String transcribeAudio(String fileUrl) {
        OpenAiService openAIService = new OpenAiService(apiKey);

        CreateTranscriptionRequest request = new CreateTranscriptionRequest();
        request.setModel(WHISPER_1);
        return openAIService.createTranscription(request, fileUrl).getText();
    }
}
