package util;

import estruturas.RegistroHashExtensivel;
import java.nio.ByteBuffer;

public class ParIDEndereco implements RegistroHashExtensivel<ParIDEndereco> {
    private int id;
    private long endereco;
    private static final int TAMANHO = 12; // 4 bytes (int) + 8 bytes (long)

    public ParIDEndereco() {
        this(-1, -1);
    }

    public ParIDEndereco(int id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public int getId() {
        return id;
    }

    public long getEndereco() {
        return endereco;
    }

    @Override
    public int hashCode() {
        return id;
    }

  @Override 
    public short size() {
        return TAMANHO;
    }

    
    public byte[] toByteArray() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(TAMANHO);
        buffer.putInt(id);
        buffer.putLong(endereco);
        return buffer.array();
    }

  
    public void fromByteArray(byte[] ba) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(ba);
        this.id = buffer.getInt();
        this.endereco = buffer.getLong();
    }

    @Override
    public ParIDEndereco clone() {
        return new ParIDEndereco(this.id, this.endereco);
    }
}