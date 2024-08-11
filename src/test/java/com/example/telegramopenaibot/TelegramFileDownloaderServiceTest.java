package com.example.telegramopenaibot;

import com.example.telegramopenaibot.service.FileService;
import com.example.telegramopenaibot.service.TelegramFileDownloaderService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TelegramFileDownloaderService.class)
class TelegramFileDownloaderServiceTest {

    public static final String PATH_TO_FILE_TXT = "path/to/file.txt";
    @Autowired
    private TelegramFileDownloaderService telegramFileDownloaderService;

    @MockBean
    private FileService fileService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Test
    void testDownloadFileSuccessful() throws IOException, URISyntaxException {
        String fileName = "file.txt";
        var expectedPath = Paths.get("downloads", fileName);
        var inputStream = new ByteArrayInputStream("content".getBytes());

        var fileUri = new URI("https://api.telegram.org/file/bot" + this.botToken + "/" + PATH_TO_FILE_TXT);

        when(fileService.saveFile(any(InputStream.class), eq(fileName))).thenReturn(expectedPath);
        when(mock(URL.class).openStream()).thenReturn(inputStream);

        try (MockedConstruction<URL> mockedConstructionUrl = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openStream()).thenReturn(inputStream);
            when(mock.toURI()).thenReturn(fileUri);
        })) {
            Optional<Path> result = telegramFileDownloaderService.downloadFile(PATH_TO_FILE_TXT);

            assertTrue(result.isPresent());
            assertEquals(expectedPath, result.get());
        }
    }

    @Test
    void testDownloadFileFilePathNull() {
        Optional<Path> result = telegramFileDownloaderService.downloadFile(null);

        assertFalse(result.isPresent());
    }

    @Test
    void testDownloadFileFilePathEmpty() {
        Optional<Path> result = telegramFileDownloaderService.downloadFile(" ");

        assertFalse(result.isPresent());
    }

    @Test
    void testDownloadFileIOException() throws IOException, URISyntaxException {
        var fileUri = new URI("https://api.telegram.org/file/bot" + this.botToken + "/" + PATH_TO_FILE_TXT);

        when(fileService.saveFile(any(InputStream.class), anyString())).thenThrow(new IOException("Download error"));
        when(mock(URL.class).openStream()).thenThrow(new IOException("Download error"));

        try (MockedConstruction<URL> mockedConstructionUrl = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openStream()).thenThrow(new IOException("Download error"));
            when(mock.toURI()).thenReturn(fileUri);
        })) {
            Optional<Path> result = telegramFileDownloaderService.downloadFile(PATH_TO_FILE_TXT);

            assertFalse(result.isPresent());
        }
    }
}
