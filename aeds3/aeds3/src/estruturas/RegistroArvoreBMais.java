package estruturas;

import java.io.IOException;

public interface RegistroArvoreBMais<T> extends Comparable<T> {
    public T clone();
    public short size();
    public byte[] toByteArray() throws IOException;
    public void fromByteArray(byte[] ba) throws IOException;
}