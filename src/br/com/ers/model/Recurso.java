package br.com.ers.model;

import java.util.ArrayList;
import java.util.List;

public class Recurso {
    private int id;
    private String nomeDoRecurso;
    private CategoriaRecurso categoria;
    private boolean disponivel;
    private double valorEstimado;
    private final List<String> historico;

    public Recurso(int id, String nomeDoRecurso, CategoriaRecurso categoria, double valorEstimado) {
        if (nomeDoRecurso == null || nomeDoRecurso.isBlank()) {
            throw new IllegalArgumentException("Nome do recurso é obrigatório.");
        }
        if (valorEstimado <= 0) {
            throw new IllegalArgumentException("Valor estimado deve ser maior que zero.");
        }
        this.id = id;
        this.nomeDoRecurso = nomeDoRecurso;
        this.categoria = categoria == null ? CategoriaRecurso.OUTRO : categoria;
        this.valorEstimado = valorEstimado;
        this.disponivel = true;
        this.historico = new ArrayList<>();
        registrarEvento("Recurso cadastrado e marcado como disponível.");
    }

    public boolean podeSerAlocadoSemAutorizacao() {
        return disponivel && valorEstimado <= 5000;
    }

    public boolean exigeAutorizacaoEspecial() {
        return valorEstimado > 5000;
    }

    public void registrarEvento(String evento) {
        historico.add(evento);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeDoRecurso() { return nomeDoRecurso; }
    public CategoriaRecurso getCategoria() { return categoria; }
    public boolean isDisponivel() { return disponivel; }
    public double getValorEstimado() { return valorEstimado; }
    public List<String> getHistorico() { return historico; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    @Override
    public String toString() {
        return "Recurso{" +
                "id=" + id +
                ", nomeDoRecurso='" + nomeDoRecurso + '\'' +
                ", categoria=" + categoria +
                ", disponivel=" + disponivel +
                ", valorEstimado=" + valorEstimado +
                '}';
    }
}
