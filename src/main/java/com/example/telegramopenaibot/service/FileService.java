package com.example.telegramopenaibot.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileService {

    private static final String DOWNLOADS_DIR = "downloads";

    public FileService() {
        createDirectoryIfNotExists();
    }

    /**
     * Creates the downloads directory if it does not already exist.
     */
    private void createDirectoryIfNotExists() {
        Path downloadsDir = Paths.get(DOWNLOADS_DIR);
        if (Files.notExists(downloadsDir)) {
            try {
                Files.createDirectories(downloadsDir);
            } catch (IOException e) {
                log.error("Failed to create downloads directory", e);
            }
        }
    }

    /**
     * Saves the input stream to a local file.
     *
     * @param inputStream the input stream of the file to save
     * @param fileName    the name of the file to save
     * @return the path of the saved file
     */
    @NotNull
    public Path saveFile(InputStream inputStream, String fileName) throws IOException {
        Path filePath = Paths.get(DOWNLOADS_DIR, fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    /**
     * Retrieves the file from the local directory.
     *
     * @param fileName the name of the file to retrieve
     * @return the path of the file
     */
    @NotNull
    public Path getFile(String fileName) {
        return Paths.get(DOWNLOADS_DIR, fileName);
    }
}
