package br.com.ers.model;

import java.util.ArrayList;
import java.util.List;

public class Colaborador {
    private int id;
    private String nome;
    private String cargo;
    private double salario;
    private boolean ativo;
    private String dataDeAdmissao;
    private final List<String> historico;

    public Colaborador(int id, String nome, String cargo, double salario, String dataDeAdmissao) {
        validar(nome, cargo, salario, dataDeAdmissao);
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.salario = salario;
        this.dataDeAdmissao = dataDeAdmissao;
        this.ativo = true;
        this.historico = new ArrayList<>();
        registrarEvento("Colaborador cadastrado.");
    }

    private void validar(String nome, String cargo, double salario, String dataDeAdmissao) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do colaborador é obrigatório.");
        if (cargo == null || cargo.isBlank()) throw new IllegalArgumentException("Cargo do colaborador é obrigatório.");
        if (salario <= 0) throw new IllegalArgumentException("Salário deve ser maior que zero.");
        if (dataDeAdmissao == null || dataDeAdmissao.isBlank()) throw new IllegalArgumentException("Data de admissão é obrigatória.");
    }

    public void promover(String novoCargo, double novoSalario) {
        if (novoCargo == null || novoCargo.isBlank()) {
            throw new IllegalArgumentException("Novo cargo inválido.");
        }
        if (novoSalario <= 0) {
            throw new IllegalArgumentException("Novo salário deve ser maior que zero.");
        }
        this.cargo = novoCargo;
        this.salario = novoSalario;
        registrarEvento("Promoção realizada para cargo " + novoCargo + " com salário " + novoSalario + ".");
    }

    public void registrarEvento(String evento) {
        historico.add(evento);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public String getCargo() { return cargo; }
    public double getSalario() { return salario; }
    public boolean isAtivo() { return ativo; }
    public String getDataDeAdmissao() { return dataDeAdmissao; }
    public List<String> getHistorico() { return historico; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return "Colaborador{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cargo='" + cargo + '\'' +
                ", salario=" + salario +
                ", ativo=" + ativo +
                ", dataDeAdmissao='" + dataDeAdmissao + '\'' +
                '}';
    }
}
