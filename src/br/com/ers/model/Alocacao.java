package br.com.ers.model;

public class Alocacao {
    private int id;
    private int colaboradorId;
    private int recursoId;
    private String data;
    private String observacao;
    private boolean ativa;

    public Alocacao(int id, int colaboradorId, int recursoId, String data, String observacao, boolean ativa) {
        this.id = id;
        this.colaboradorId = colaboradorId;
        this.recursoId = recursoId;
        this.data = data;
        this.observacao = observacao;
        this.ativa = ativa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getColaboradorId() { return colaboradorId; }
    public int getRecursoId() { return recursoId; }
    public String getData() { return data; }
    public String getObservacao() { return observacao; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    @Override
    public String toString() {
        return "Alocacao{" +
                "id=" + id +
                ", colaboradorId=" + colaboradorId +
                ", recursoId=" + recursoId +
                ", data='" + data + '\'' +
                ", observacao='" + observacao + '\'' +
                ", ativa=" + ativa +
                '}';
    }
}
