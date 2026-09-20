package estruturas;


public interface RegistroHashExtensivel<T> {
    public int hashCode();
    public short size();
    public byte[] toByteArray() throws Exception;
    public void fromByteArray(byte[] ba) throws Exception;
    public T clone();
}