package entidades;

import estruturas.Registro;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Resposta implements Registro {
    private int idResposta;
    private int idPergunta;
    private int idUsuario;
    private long criacao;
    private short nota;        // saldo das avaliações (ajustado pelo módulo de avaliações)
    private short avaliacoes;  // quantidade de avaliações recebidas
    private String resposta;
    private boolean ativa;

    // construtores
    public Resposta() {
        this(-1, -1, -1, "");
    }

    public Resposta(int idPergunta, int idUsuario, String resposta) {
        this(-1, idPergunta, idUsuario, resposta);
    }

    public Resposta(int idResposta, int idPergunta, int idUsuario, String resposta) {
        this.idResposta = idResposta;
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        this.criacao = System.currentTimeMillis();
        this.nota = 0;
        this.avaliacoes = 0;
        this.resposta = resposta;
        this.ativa = true;
    }

    // interface registro
    @Override
    public int getId() { return this.idResposta; }

    @Override
    public void setId(int id) { this.idResposta = id; }

    // getts e setts
    public int getIdPergunta() { return idPergunta; }
    public void setIdPergunta(int idPergunta) { this.idPergunta = idPergunta; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public long getCriacao() { return criacao; }
    public void setCriacao(long criacao) { this.criacao = criacao; }

    public short getNota() { return nota; }
    public void setNota(short nota) { this.nota = nota; }

    public short getAvaliacoes() { return avaliacoes; }
    public void setAvaliacoes(short avaliacoes) { this.avaliacoes = avaliacoes; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    // Serialização
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.idResposta);
        dos.writeInt(this.idPergunta);
        dos.writeInt(this.idUsuario);
        dos.writeLong(this.criacao);
        dos.writeShort(this.nota);
        dos.writeShort(this.avaliacoes);
        dos.writeUTF(this.resposta);
        dos.writeBoolean(this.ativa);

        return baos.toByteArray();
    }

    // Desserialização
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idResposta = dis.readInt();
        this.idPergunta = dis.readInt();
        this.idUsuario = dis.readInt();
        this.criacao = dis.readLong();
        this.nota = dis.readShort();
        this.avaliacoes = dis.readShort();
        this.resposta = dis.readUTF();
        this.ativa = dis.readBoolean();
    }
}
