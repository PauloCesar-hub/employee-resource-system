package br.com.ers.repository;

import br.com.ers.model.CategoriaRecurso;
import br.com.ers.model.Recurso;
import br.com.ers.persistence.CsvStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecursoRepository {
    private static final String FILE = "recursos.csv";
    private final CsvStorage storage;
    private final List<Recurso> recursos = new ArrayList<>();
    private int nextId = 1;

    public RecursoRepository(CsvStorage storage) {
        this.storage = storage;
        load();
    }

    public Recurso save(Recurso recurso) {
        if (recurso.getId() <= 0) {
            recurso.setId(nextId++);
        } else if (recurso.getId() >= nextId) {
            nextId = recurso.getId() + 1;
        }
        recursos.removeIf(r -> r.getId() == recurso.getId());
        recursos.add(recurso);
        persist();
        return recurso;
    }

    public List<Recurso> findAll() { return new ArrayList<>(recursos); }
    public Optional<Recurso> findById(int id) { return recursos.stream().filter(r -> r.getId() == id).findFirst(); }
    public Optional<Recurso> findByNome(String nome) { return recursos.stream().filter(r -> r.getNomeDoRecurso().equalsIgnoreCase(nome)).findFirst(); }

    public void deleteById(int id) {
        recursos.removeIf(r -> r.getId() == id);
        persist();
    }

    private void load() {
        for (String[] row : storage.read(FILE)) {
            if (row.length >= 5) {
                Recurso r = new Recurso(Integer.parseInt(row[0]), row[1], CategoriaRecurso.fromText(row[2]), Double.parseDouble(row[4]));
                r.setDisponivel(Boolean.parseBoolean(row[3]));
                recursos.add(r);
                nextId = Math.max(nextId, r.getId() + 1);
            }
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (Recurso r : recursos) {
            lines.add(r.getId() + ";" + sanitize(r.getNomeDoRecurso()) + ";" + r.getCategoria().name() + ";" + r.isDisponivel() + ";" + r.getValorEstimado());
        }
        storage.write(FILE, lines);
    }

    private String sanitize(String value) { return value.replace(";", ","); }
}
