package entidades;
import estruturas.Registro;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Avaliacao implements Registro {
        private int idAvaliacao;
        private int idUsuario;
        private int idResposta;
        private boolean avaliacao;

        public Avaliacao(){
        this(-1, -1, -1, false);
    }

    public Avaliacao(int idUsuario, int idResposta, boolean avaliacao){
        this(-1, idUsuario, idResposta, avaliacao);
    }

    public Avaliacao(int idAvaliacao, int idUsuario, int idResposta, boolean avaliacao){
        this.idAvaliacao = idAvaliacao;
        this.idUsuario = idUsuario;
        this.idResposta = idResposta;
        this.avaliacao = avaliacao;
    }

     @Override
    public int getId(){
        return this.idAvaliacao;
    }
     @Override
    public void setId(int idAvaliacao){
        this.idAvaliacao = idAvaliacao;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdResposta() { return idResposta; }
    public void setIdResposta(int idResposta) { this.idResposta = idResposta; }

    public boolean isAvaliacao() { return avaliacao; }
    public void setAvaliacao(boolean avaliacao) { this.avaliacao = avaliacao; }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.idAvaliacao);
        dos.writeInt(this.idUsuario);
        dos.writeInt(this.idResposta);
        dos.writeBoolean(this.avaliacao);
       
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idAvaliacao = dis.readInt();
        this.idUsuario = dis.readInt();
        this.idResposta = dis.readInt();
        this.avaliacao = dis.readBoolean();
    }

}

