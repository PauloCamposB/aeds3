package entidades;

import estruturas.Registro;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pergunta implements Registro {
    private int idPergunta;
    private int idUsuario;
    private long criacao;
    private long alteracao;
    private short nota;
    private String pergunta;
    private String palavrasChave;
    private boolean ativa;

    // construtores
    public Pergunta() {
        this(-1, -1, "", "");
    }

    public Pergunta(int idUsuario, String pergunta, String palavrasChave) {
        this(-1, idUsuario, pergunta, palavrasChave);
    }

    public Pergunta(int idPergunta, int idUsuario, String pergunta, String palavrasChave) {
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        this.criacao = System.currentTimeMillis();
        this.alteracao = this.criacao;
        this.nota = 0;
        this.pergunta = pergunta;
        this.palavrasChave = palavrasChave;
        this.ativa = true;
    }

    // interface registro
    @Override
    public int getId() { return this.idPergunta; }

    @Override
    public void setId(int id) { this.idPergunta = id; }

    // getts e setts
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public long getCriacao() { return criacao; }
    public void setCriacao(long criacao) { this.criacao = criacao; }

    public long getAlteracao() { return alteracao; }
    public void setAlteracao(long alteracao) { this.alteracao = alteracao; }

    public short getNota() { return nota; }
    public void setNota(short nota) { this.nota = nota; }

    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { 
        this.pergunta = pergunta; 
        this.alteracao = System.currentTimeMillis(); // att a alteracao
    }

    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { 
        this.palavrasChave = palavrasChave; 
        this.alteracao = System.currentTimeMillis(); // att a alteracao
    }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    // Serialização
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.idPergunta);
        dos.writeInt(this.idUsuario);
        dos.writeLong(this.criacao);
        dos.writeLong(this.alteracao);
        dos.writeShort(this.nota);
        dos.writeUTF(this.pergunta);
        dos.writeUTF(this.palavrasChave);
        dos.writeBoolean(this.ativa);

        return baos.toByteArray();
    }

    // Desserialização
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idPergunta = dis.readInt();
        this.idUsuario = dis.readInt();
        this.criacao = dis.readLong();
        this.alteracao = dis.readLong();
        this.nota = dis.readShort();
        this.pergunta = dis.readUTF();
        this.palavrasChave = dis.readUTF();
        this.ativa = dis.readBoolean();
    }
}