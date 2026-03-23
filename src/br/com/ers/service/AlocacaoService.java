package br.com.ers.service;

import br.com.ers.model.Alocacao;
import br.com.ers.model.Colaborador;
import br.com.ers.model.Recurso;
import br.com.ers.repository.AlocacaoRepository;
import br.com.ers.util.LogService;

import java.time.LocalDate;
import java.util.List;

public class AlocacaoService {
    private final AlocacaoRepository repository;
    private final ColaboradorService colaboradorService;
    private final RecursoService recursoService;
    private final LogService logService;

    public AlocacaoService(AlocacaoRepository repository, ColaboradorService colaboradorService, RecursoService recursoService, LogService logService) {
        this.repository = repository;
        this.colaboradorService = colaboradorService;
        this.recursoService = recursoService;
        this.logService = logService;
    }

    public Alocacao alocar(int colaboradorId, int recursoId, String observacao) {
        return alocar(colaboradorId, recursoId, observacao, false);
    }

    public Alocacao alocar(int colaboradorId, int recursoId, String observacao, boolean autorizadoAltoValor) {
        Colaborador colaborador = colaboradorService.buscarPorId(colaboradorId);
        Recurso recurso = recursoService.buscarPorId(recursoId);

        if (!colaborador.isAtivo()) {
            throw new IllegalStateException("Colaborador inativo não pode receber recursos.");
        }
        if (!recurso.isDisponivel()) {
            throw new IllegalStateException("Recurso já está alocado.");
        }
        if (recurso.exigeAutorizacaoEspecial() && !autorizadoAltoValor) {
            throw new IllegalStateException("Recurso exige autorização especial por alto valor.");
        }

        Alocacao alocacao = new Alocacao(0, colaboradorId, recursoId, LocalDate.now().toString(), observacao, true);
        repository.save(alocacao);
        recurso.setDisponivel(false);

        if (recurso.exigeAutorizacaoEspecial()) {
            recurso.registrarEvento("ALOCADO COM AUTORIZAÇÃO ESPECIAL para colaboradorId=" + colaboradorId);
            colaborador.registrarEvento("Recebeu recurso de alto valor id=" + recursoId + " com autorização especial");
            logService.info("Recurso de alto valor " + recursoId + " alocado com autorização para colaborador " + colaboradorId);
        } else {
            recurso.registrarEvento("ALOCADO para colaboradorId=" + colaboradorId);
            colaborador.registrarEvento("Recebeu recurso id=" + recursoId);
            logService.info("Recurso " + recursoId + " alocado para colaborador " + colaboradorId);
        }

        recursoService.salvar(recurso);
        return alocacao;
    }

    public void devolver(int recursoId, String observacao) {
        Recurso recurso = recursoService.buscarPorId(recursoId);
        Alocacao alocacao = repository.findAtivaByRecursoId(recursoId)
                .orElseThrow(() -> new IllegalStateException("Não existe alocação ativa para este recurso."));

        alocacao.setAtiva(false);
        alocacao.setObservacao(observacao);
        repository.save(alocacao);
        recurso.setDisponivel(true);
        recurso.registrarEvento("DEVOLVIDO | " + observacao);
        recursoService.salvar(recurso);
        logService.info("Recurso " + recursoId + " devolvido.");
    }

    public List<Alocacao> listarTodas() { return repository.findAll(); }
    public List<Alocacao> listarPorColaborador(int colaboradorId) { return repository.findByColaboradorId(colaboradorId); }
}
