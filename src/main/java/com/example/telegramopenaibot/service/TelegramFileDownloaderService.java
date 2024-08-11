package com.example.telegramopenaibot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramFileDownloaderService {

    private final FileService fileService;

    @Value("${telegram.bot.token}")
    String botToken;

    @NotNull
    public Optional<Path> downloadFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return empty();
        }

        try {
            URL fileUrl = buildFileUrl(filePath);
            String fileName = extractFileName(filePath);

            try (InputStream in = fileUrl.openStream()) {
                return Optional.of(fileService.saveFile(in, fileName));
            }
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to download file", e);
            return empty();
        }
    }

    private URL buildFileUrl(String filePath) throws URISyntaxException, IOException {
        String fileUrl = "https://api.telegram.org/file/bot" + this.botToken + "/" + filePath;
        URI uri = new URI(fileUrl);
        return uri.toURL();
    }

    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }
}

