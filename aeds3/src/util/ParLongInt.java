package util;

import estruturas.RegistroArvoreBMais;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Par (data de criação, idPergunta) usado no índice de perguntas por data.
 * A ordem é DECRESCENTE pela data: as perguntas mais recentes vêm primeiro
 * na árvore B+, então a listagem sai pronta, sem precisar ordenar.
 */
public class ParLongInt implements RegistroArvoreBMais<ParLongInt> {
    private long data;
    private int idPergunta;

    public ParLongInt() {
        this(-1L, -1);
    }

    public ParLongInt(long data, int idPergunta) {
        this.data = data;
        this.idPergunta = idPergunta;
    }

    public long getData() { return data; }
    public int getIdPergunta() { return idPergunta; }

    @Override
    public ParLongInt clone() {
        return new ParLongInt(this.data, this.idPergunta);
    }

    @Override
    public short size() {
        return 12; // 8 bytes (long) + 4 bytes (int)
    }

    @Override
    public int compareTo(ParLongInt o) {
        // Data invertida => ordem decrescente (mais recente primeiro)
        if (this.data != o.data) {
            return Long.compare(o.data, this.data);
        }
        // Desempate pelo ID, para que duas perguntas criadas no mesmo
        // milissegundo não tenham a mesma chave
        return Integer.compare(this.idPergunta, o.idPergunta);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(this.data);
        dos.writeInt(this.idPergunta);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.data = dis.readLong();
        this.idPergunta = dis.readInt();
    }
}