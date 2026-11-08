package util;

import entidades.Pergunta;

/**
 * Um resultado da busca por palavras-chave: a pergunta (pelo id) e o seu
 * valor TFxIDF somado entre os termos da consulta.
 * A ordem natural é por score DECRESCENTE (maior relevância primeiro).
 */
public class ResultadoBusca implements Comparable<ResultadoBusca> {

    private final int id;
    private final float score;
    private Pergunta pergunta; // preenchida pela BuscaPerguntas (pode ser null em testes)

    public ResultadoBusca(int id, float score) {
        this.id = id;
        this.score = score;
    }

    public ResultadoBusca(int id, float score, Pergunta pergunta) {
        this(id, score);
        this.pergunta = pergunta;
    }

    public int getId() { return id; }
    public float getScore() { return score; }
    public Pergunta getPergunta() { return pergunta; }
    public void setPergunta(Pergunta pergunta) { this.pergunta = pergunta; }

    // score maior vem primeiro; em empate, o menor id vem primeiro (ordem estável)
    @Override
    public int compareTo(ResultadoBusca outro) {
        int c = Float.compare(outro.score, this.score);
        return c != 0 ? c : Integer.compare(this.id, outro.id);
    }

    @Override
    public String toString() {
        return "(" + id + "; " + String.format("%.3f", score) + ")";
    }
}
