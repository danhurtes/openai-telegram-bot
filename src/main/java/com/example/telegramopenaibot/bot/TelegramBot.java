package com.example.telegramopenaibot.bot;

import com.example.telegramopenaibot.service.AudioTranscriptionService;
import com.example.telegramopenaibot.service.OpenAIClient;
import com.example.telegramopenaibot.service.TelegramFileDownloaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    public static final String START_MESSAGE =
            "Привет! Я бот, который поможет тебе получить ответ на любой вопрос. Просто напиши мне или отправь голосовое сообщение(файл).";
    public static final String UNPARSED_DUMMY_AUDIO = "you";

    private final OpenAIClient openAIClient;
    private final TelegramFileDownloaderService fileDownloaderService;
    private final AudioTranscriptionService audioTranscriptionService;

    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message telegaMessage = update.getMessage();
            if (telegaMessage.hasText() && telegaMessage.getText().startsWith("/start")) {
                handleStartMessage(telegaMessage.getChatId());
            } else if (telegaMessage.hasText()) {
                handleTextMessage(telegaMessage.getText(), telegaMessage.getChatId());
            } else if (telegaMessage.hasVoice() || telegaMessage.hasAudio()) {
                handleVoiceMessage(telegaMessage.hasVoice()
                        ? telegaMessage.getVoice().getFileId()
                        : telegaMessage.getAudio().getFileId(), telegaMessage.getChatId());
            }
        }
    }

    private void handleStartMessage(Long chatId) {
        try {
            execute(getMessageToSend(chatId, START_MESSAGE));
        } catch (TelegramApiException e) {
            log.error("Error while sending message", e);
        }
    }

    private void handleTextMessage(String text, Long chatId) {
        String response = openAIClient.getResponse(text);

        try {
            execute(getMessageToSend(chatId, response));
        } catch (TelegramApiException e) {
            log.error("Error while sending message", e);
        }
    }

    private void handleVoiceMessage(String fileId, Long chatId) {
        try {
            File file = execute(getGetFile(fileId));
            Optional<Path> localFilePathOptional = fileDownloaderService.downloadFile(file.getFilePath());

            if (localFilePathOptional.isPresent()) {
                String transcription = audioTranscriptionService.transcribeAudio(localFilePathOptional.get().toString());
                log.info("Transcription: {}", transcription);

                if (transcription.isEmpty() || UNPARSED_DUMMY_AUDIO.equalsIgnoreCase(transcription)) {
                    execute(getMessageToSend(chatId, "Повторите ваш вопрос, пожалуйста"));
                    return;
                }
                String response = this.openAIClient.getResponse(transcription);
                execute(getMessageToSend(chatId, combineTranscriptionWithResponse(transcription, response)));
            } else {
                log.error("Failed to download file from Telegram");
            }
        } catch (TelegramApiException e) {
            log.error("Error while handling voice message", e);
        }
    }

    private static String combineTranscriptionWithResponse(String transcription, String response) {
        return String.format("Вопрос: %s%n%nОтвет: %s%n%n", transcription, response);
    }

    private static GetFile getGetFile(String fileId) {
        final GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        return getFileMethod;
    }

    private static SendMessage getMessageToSend(Long chatId, String response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(response);
        return message;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
