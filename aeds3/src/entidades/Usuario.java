package entidades;

import estruturas.Registro;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Usuario implements Registro {
    private int idUsuario;
    private String nome;
    private String email;
    private String hashSenha;
    private String perguntaSecreta;
    private String hashRespostaSecreta;

    // Construtores
    public Usuario() {
        this(-1, "", "", "", "", "");
    }

    public Usuario(String nome, String email, String hashSenha, String perguntaSecreta, String hashRespostaSecreta) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
    }

    public Usuario(int idUsuario, String nome, String email, String hashSenha, String perguntaSecreta, String hashRespostaSecreta) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.hashSenha = hashSenha;
        this.perguntaSecreta = perguntaSecreta;
        this.hashRespostaSecreta = hashRespostaSecreta;
    }

    // Getters e Setters
    @Override
    public int getId() { return idUsuario; }

    @Override
    public void setId(int id) { this.idUsuario = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashSenha() { return hashSenha; }
    public void setHashSenha(String hashSenha) { this.hashSenha = hashSenha; }

    public String getPerguntaSecreta() { return perguntaSecreta; }
    public void setPerguntaSecreta(String perguntaSecreta) { this.perguntaSecreta = perguntaSecreta; }

    public String getHashRespostaSecreta() { return hashRespostaSecreta; }
    public void setHashRespostaSecreta(String hashRespostaSecreta) { this.hashRespostaSecreta = hashRespostaSecreta; }

    // Serializacao
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.idUsuario);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.email);
        dos.writeUTF(this.hashSenha);
        dos.writeUTF(this.perguntaSecreta);
        dos.writeUTF(this.hashRespostaSecreta);

        return baos.toByteArray();
    }

    // Desserializacao
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idUsuario = dis.readInt();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();
        this.hashSenha = dis.readUTF();
        this.perguntaSecreta = dis.readUTF();
        this.hashRespostaSecreta = dis.readUTF();
    }
}