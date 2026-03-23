package br.com.ers.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvStorage {
    private final Path basePath;

    public CsvStorage(Path basePath) {
        this.basePath = basePath;
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de dados.", e);
        }
    }

    public List<String[]> read(String fileName) {
        Path file = basePath.resolve(fileName);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String[]> rows = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    rows.add(line.split(";", -1));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo " + fileName, e);
        }
    }

    public void write(String fileName, List<String> lines) {
        Path file = basePath.resolve(fileName);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gravar arquivo " + fileName, e);
        }
    }
}
