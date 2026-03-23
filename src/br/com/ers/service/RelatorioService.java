package br.com.ers.service;

import br.com.ers.model.Alocacao;
import br.com.ers.model.Colaborador;
import br.com.ers.model.Recurso;

import java.util.ArrayList;
import java.util.List;

public class RelatorioService {
    private final ColaboradorService colaboradorService;
    private final RecursoService recursoService;
    private final AlocacaoService alocacaoService;

    public RelatorioService(ColaboradorService colaboradorService, RecursoService recursoService, AlocacaoService alocacaoService) {
        this.colaboradorService = colaboradorService;
        this.recursoService = recursoService;
        this.alocacaoService = alocacaoService;
    }

    public double custoTotalPorColaborador(int colaboradorId) {
        return alocacaoService.listarPorColaborador(colaboradorId).stream()
                .map(Alocacao::getRecursoId)
                .map(recursoService::buscarPorId)
                .mapToDouble(Recurso::getValorEstimado)
                .sum();
    }

    public List<Recurso> recursosMaisCaros(double valorMinimo) {
        return recursoService.listarTodos().stream()
                .filter(r -> r.getValorEstimado() >= valorMinimo)
                .toList();
    }

    public List<Colaborador> colaboradoresSemRecursos() {
        List<Colaborador> resultado = new ArrayList<>();
        for (Colaborador colaborador : colaboradorService.listarTodos()) {
            if (alocacaoService.listarPorColaborador(colaborador.getId()).isEmpty()) {
                resultado.add(colaborador);
            }
        }
        return resultado;
    }
}
