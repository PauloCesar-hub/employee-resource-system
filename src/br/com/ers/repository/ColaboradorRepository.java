package br.com.ers.repository;

import br.com.ers.model.Colaborador;
import br.com.ers.persistence.CsvStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColaboradorRepository {
    private static final String FILE = "colaboradores.csv";
    private final CsvStorage storage;
    private final List<Colaborador> colaboradores = new ArrayList<>();
    private int nextId = 1;

    public ColaboradorRepository(CsvStorage storage) {
        this.storage = storage;
        load();
    }

    public Colaborador save(Colaborador colaborador) {
        if (colaborador.getId() <= 0) {
            colaborador.setId(nextId++);
        } else if (colaborador.getId() >= nextId) {
            nextId = colaborador.getId() + 1;
        }
        colaboradores.removeIf(c -> c.getId() == colaborador.getId());
        colaboradores.add(colaborador);
        persist();
        return colaborador;
    }

    public List<Colaborador> findAll() { return new ArrayList<>(colaboradores); }
    public Optional<Colaborador> findById(int id) { return colaboradores.stream().filter(c -> c.getId() == id).findFirst(); }
    public Optional<Colaborador> findByNome(String nome) { return colaboradores.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst(); }

    public void deleteById(int id) {
        colaboradores.removeIf(c -> c.getId() == id);
        persist();
    }

    private void load() {
        for (String[] row : storage.read(FILE)) {
            if (row.length >= 6) {
                Colaborador c = new Colaborador(Integer.parseInt(row[0]), row[1], row[2], Double.parseDouble(row[3]), row[5]);
                c.setAtivo(Boolean.parseBoolean(row[4]));
                colaboradores.add(c);
                nextId = Math.max(nextId, c.getId() + 1);
            }
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (Colaborador c : colaboradores) {
            lines.add(c.getId() + ";" + sanitize(c.getNome()) + ";" + sanitize(c.getCargo()) + ";" + c.getSalario() + ";" + c.isAtivo() + ";" + sanitize(c.getDataDeAdmissao()));
        }
        storage.write(FILE, lines);
    }

    private String sanitize(String value) { return value.replace(";", ","); }
}
