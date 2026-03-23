package br.com.ers.service;

import br.com.ers.model.CategoriaRecurso;
import br.com.ers.model.Recurso;
import br.com.ers.repository.RecursoRepository;
import br.com.ers.util.LogService;

import java.util.List;

public class RecursoService {
    private final RecursoRepository repository;
    private final LogService logService;

    public RecursoService(RecursoRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    public Recurso cadastrar(String nome, String categoria, double valor) {
        Recurso recurso = new Recurso(0, nome, CategoriaRecurso.fromText(categoria), valor);
        repository.save(recurso);
        logService.info("Recurso cadastrado: " + recurso.getNomeDoRecurso() + " (id=" + recurso.getId() + ")");
        return recurso;
    }

    public Recurso buscarPorId(int id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Recurso não encontrado."));
    }

    public Recurso buscarPorNome(String nome) {
        return repository.findByNome(nome).orElseThrow(() -> new IllegalArgumentException("Recurso não encontrado pelo nome."));
    }

    public List<Recurso> listarTodos() { return repository.findAll(); }
    public List<Recurso> listarDisponiveis() { return repository.findAll().stream().filter(Recurso::isDisponivel).toList(); }

    public void remover(int id) {
        Recurso recurso = buscarPorId(id);
        repository.deleteById(id);
        logService.info("Recurso removido: " + recurso.getNomeDoRecurso() + " (id=" + id + ")");
    }

    public void salvar(Recurso recurso) { repository.save(recurso); }
}
