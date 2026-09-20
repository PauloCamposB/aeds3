package util;

import estruturas.RegistroArvoreBMais;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParIntInt implements RegistroArvoreBMais<ParIntInt> {
    private int idUsuario;
    private int idPergunta;

    public ParIntInt() {
        this(-1, -1);
    }

    public ParIntInt(int idUsuario, int idPergunta) {
        this.idUsuario = idUsuario;
        this.idPergunta = idPergunta;
    }

    public int getIdUsuario() { return idUsuario; }
    public int getIdPergunta() { return idPergunta; }

    @Override
    public ParIntInt clone() {
        return new ParIntInt(this.idUsuario, this.idPergunta);
    }

    @Override
    public short size() {
        return 8; // 4 bytes (int) + 4 bytes (int)
    }

    @Override
    public int compareTo(ParIntInt o) {
        // Ordena primeiramente pelo ID do Usuário (agrupa as perguntas do mesmo usuário)
        if (this.idUsuario != o.idUsuario) {
            return Integer.compare(this.idUsuario, o.idUsuario);
        }
        // idPergunta == -1 funciona como coringa: permite buscar todas as
        // perguntas de um usuário com new ParIntInt(idUsuario, -1)
        if (this.idPergunta == -1 || o.idPergunta == -1) {
            return 0;
        }
        // Desempata pelo ID da Pergunta
        return Integer.compare(this.idPergunta, o.idPergunta);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idUsuario);
        dos.writeInt(this.idPergunta);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.idUsuario = dis.readInt();
        this.idPergunta = dis.readInt();
    }
}