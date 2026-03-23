package br.com.ers.repository;

import br.com.ers.model.Alocacao;
import br.com.ers.persistence.CsvStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlocacaoRepository {
    private static final String FILE = "alocacoes.csv";
    private final CsvStorage storage;
    private final List<Alocacao> alocacoes = new ArrayList<>();
    private int nextId = 1;

    public AlocacaoRepository(CsvStorage storage) {
        this.storage = storage;
        load();
    }

    public Alocacao save(Alocacao alocacao) {
        if (alocacao.getId() <= 0) {
            alocacao.setId(nextId++);
        } else if (alocacao.getId() >= nextId) {
            nextId = alocacao.getId() + 1;
        }
        alocacoes.removeIf(a -> a.getId() == alocacao.getId());
        alocacoes.add(alocacao);
        persist();
        return alocacao;
    }

    public List<Alocacao> findAll() { return new ArrayList<>(alocacoes); }
    public List<Alocacao> findByColaboradorId(int colaboradorId) { return alocacoes.stream().filter(a -> a.getColaboradorId() == colaboradorId).collect(Collectors.toList()); }
    public Optional<Alocacao> findAtivaByRecursoId(int recursoId) { return alocacoes.stream().filter(a -> a.getRecursoId() == recursoId && a.isAtiva()).findFirst(); }

    private void load() {
        for (String[] row : storage.read(FILE)) {
            if (row.length >= 6) {
                Alocacao a = new Alocacao(Integer.parseInt(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]), row[3], row[4], Boolean.parseBoolean(row[5]));
                alocacoes.add(a);
                nextId = Math.max(nextId, a.getId() + 1);
            }
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (Alocacao a : alocacoes) {
            lines.add(a.getId() + ";" + a.getColaboradorId() + ";" + a.getRecursoId() + ";" + sanitize(a.getData()) + ";" + sanitize(a.getObservacao()) + ";" + a.isAtiva());
        }
        storage.write(FILE, lines);
    }

    private String sanitize(String value) { return value == null ? "" : value.replace(";", ","); }
}
