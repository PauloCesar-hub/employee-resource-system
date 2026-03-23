package br.com.ers.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class LogService {
    private final Path logPath;

    public LogService(Path basePath) {
        this.logPath = basePath.resolve("logs.txt");
        try {
            Files.createDirectories(basePath);
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar o arquivo de log.", e);
        }
    }

    public void info(String message) { write("INFO", message); }
    public void error(String message) { write("ERROR", message); }

    private void write(String level, String message) {
        String line = LocalDateTime.now() + " [" + level + "] " + message + System.lineSeparator();
        try {
            Files.writeString(logPath, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Falha ao escrever no log: " + e.getMessage());
        }
    }

    public Path getLogPath() { return logPath; }
}
