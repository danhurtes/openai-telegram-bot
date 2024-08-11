package com.example.telegramopenaibot;

import com.example.telegramopenaibot.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FileService.class)
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Test
    void testCreateDirectoryIfNotExists() throws IOException {
        var testDirectory = Paths.get("downloads");

        if (Files.exists(testDirectory)) {
            try (Stream<Path> paths = Files.walk(testDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        fileService = new FileService();
        assertTrue(Files.exists(testDirectory), "The downloads directory should be created.");
    }

    @Test
    void testSaveFile() throws IOException {
        var inputStream = new ByteArrayInputStream("test data".getBytes());
        var fileName = "testfile.txt";

        var savedFilePath = fileService.saveFile(inputStream, fileName);

        var expectedPath = Paths.get("downloads", fileName);
        assertTrue(Files.exists(expectedPath));
        assertEquals(expectedPath, savedFilePath);

        Files.delete(expectedPath);
    }

    @Test
    void testGetFile() {
        String fileName = "testfile.txt";
        var filePath = fileService.getFile(fileName);

        var expectedPath = Paths.get("downloads", fileName);
        assertEquals(expectedPath, filePath);
    }
}

