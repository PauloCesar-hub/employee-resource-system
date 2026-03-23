package br.com.ers.service;

import br.com.ers.model.Colaborador;
import br.com.ers.repository.ColaboradorRepository;
import br.com.ers.util.LogService;

import java.util.List;

public class ColaboradorService {
    private final ColaboradorRepository repository;
    private final LogService logService;

    public ColaboradorService(ColaboradorRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    public Colaborador cadastrar(String nome, String cargo, double salario, String dataAdmissao) {
        Colaborador colaborador = new Colaborador(0, nome, cargo, salario, dataAdmissao);
        repository.save(colaborador);
        logService.info("Colaborador cadastrado: " + colaborador.getNome() + " (id=" + colaborador.getId() + ")");
        return colaborador;
    }

    public Colaborador buscarPorId(int id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado."));
    }

    public Colaborador buscarPorNome(String nome) {
        return repository.findByNome(nome).orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado pelo nome."));
    }

    public List<Colaborador> listarTodos() { return repository.findAll(); }

    public List<Colaborador> listarAtivos() {
        return repository.findAll().stream().filter(Colaborador::isAtivo).toList();
    }

    public void remover(int id) {
        Colaborador colaborador = buscarPorId(id);
        repository.deleteById(id);
        logService.info("Colaborador removido: " + colaborador.getNome() + " (id=" + id + ")");
    }


    public Colaborador ativar(int id) {
        Colaborador colaborador = buscarPorId(id);
        if (!colaborador.isAtivo()) {
            colaborador.setAtivo(true);
            colaborador.registrarEvento("Colaborador ativado.");
            repository.save(colaborador);
            logService.info("Colaborador ativado: " + colaborador.getNome() + " (id=" + id + ")");
        }
        return colaborador;
    }

    public Colaborador desativar(int id) {
        Colaborador colaborador = buscarPorId(id);
        if (colaborador.isAtivo()) {
            colaborador.setAtivo(false);
            colaborador.registrarEvento("Colaborador desativado.");
            repository.save(colaborador);
            logService.info("Colaborador desativado: " + colaborador.getNome() + " (id=" + id + ")");
        }
        return colaborador;
    }

    public Colaborador promover(int id, String novoCargo, double novoSalario) {
        Colaborador colaborador = buscarPorId(id);
        colaborador.promover(novoCargo, novoSalario);
        repository.save(colaborador);
        logService.info("Colaborador promovido: " + colaborador.getNome() + " para " + novoCargo);
        return colaborador;
    }
}
